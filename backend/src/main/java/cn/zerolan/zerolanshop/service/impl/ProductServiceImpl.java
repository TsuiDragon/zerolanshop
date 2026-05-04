package cn.zerolan.zerolanshop.service.impl;

import cn.zerolan.zerolanshop.service.*;

import cn.zerolan.zerolanshop.domain.dto.ProductCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.ProductResponse;
import cn.zerolan.zerolanshop.domain.dto.ProductStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.ProductSupplyBindingRequest;
import cn.zerolan.zerolanshop.domain.dto.ProductSupplyBindingResponse;
import cn.zerolan.zerolanshop.domain.dto.ProductUpdateRequest;
import cn.zerolan.zerolanshop.domain.entity.OrderTemplate;
import cn.zerolan.zerolanshop.domain.entity.PricingTemplate;
import cn.zerolan.zerolanshop.domain.entity.Product;
import cn.zerolan.zerolanshop.domain.entity.ProductCategory;
import cn.zerolan.zerolanshop.domain.entity.ProductSupplyBinding;
import cn.zerolan.zerolanshop.domain.entity.SupplyChannel;
import cn.zerolan.zerolanshop.mapper.PricingTemplateMapper;
import cn.zerolan.zerolanshop.mapper.ProductCategoryMapper;
import cn.zerolan.zerolanshop.mapper.ProductMapper;
import cn.zerolan.zerolanshop.mapper.ProductSupplyBindingMapper;
import cn.zerolan.zerolanshop.mapper.SupplyChannelMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ProductServiceImpl implements ProductService {

    public static final String TYPE_VIRTUAL = "VIRTUAL";
    public static final String TYPE_CARD = "CARD";
    public static final String TYPE_NORMAL = "NORMAL";
    public static final String SUPPLY_COST_LOWEST = "LOWEST";
    public static final String SUPPLY_COST_HIGHEST = "HIGHEST";

    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_ENABLED = 1;

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final PricingTemplateMapper pricingTemplateMapper;
    private final ProductSupplyBindingMapper productSupplyBindingMapper;
    private final SupplyChannelMapper supplyChannelMapper;
    private final OrderTemplateService orderTemplateService;
    private final PricingTemplateService pricingTemplateService;

    public ProductServiceImpl(
            ProductMapper productMapper,
            ProductCategoryMapper productCategoryMapper,
            PricingTemplateMapper pricingTemplateMapper,
            ProductSupplyBindingMapper productSupplyBindingMapper,
            SupplyChannelMapper supplyChannelMapper,
            OrderTemplateService orderTemplateService,
            PricingTemplateService pricingTemplateService
    ) {
        this.productMapper = productMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.pricingTemplateMapper = pricingTemplateMapper;
        this.productSupplyBindingMapper = productSupplyBindingMapper;
        this.supplyChannelMapper = supplyChannelMapper;
        this.orderTemplateService = orderTemplateService;
        this.pricingTemplateService = pricingTemplateService;
    }

    public List<ProductResponse> list(String name, String productType, Long categoryId, Integer status) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name.trim());
        }
        if (StringUtils.hasText(productType)) {
            wrapper.eq("product_type", normalizeProductType(productType));
        }
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        if (status != null) {
            validateStatus(status);
            wrapper.eq("status", status);
        }
        wrapper.orderByAsc("sort", "id");
        return productMapper.selectList(wrapper).stream().map(this::toResponse).toList();
    }

    public ProductResponse detail(Long id) {
        return toResponse(getExistingProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        if (request == null) {
            throw new RuntimeException("商品请求不能为空");
        }
        validateSupplyBindingActiveCount(request.getSupplyBindings());
        Product product = new Product();
        BigDecimal costPrice = resolveEffectiveCostPrice(request.getCostPrice(), request.getSupplyCostStrategy(), request.getSupplyBindings());
        applyEditableFields(product, request.getProductType(), request.getCategoryId(), request.getName(),
                costPrice, request.getTerminalLimitPrice(), request.getSupplyCostStrategy(), request.getPricingTemplateId(), request.getImage(), request.getFaceValue(),
                request.getOrderTemplateId(), request.getMinPurchaseQuantity(), request.getMaxPurchaseQuantity(),
                request.getSort(), request.getStatus() == null ? STATUS_ENABLED : request.getStatus());
        productMapper.insert(product);
        replaceSupplyBindings(product.getId(), request.getSupplyBindings(), product.getSupplyCostStrategy());
        return toResponse(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        if (request == null) {
            throw new RuntimeException("商品请求不能为空");
        }
        validateSupplyBindingActiveCount(request.getSupplyBindings());
        Product product = getExistingProduct(id);
        BigDecimal costPrice = resolveEffectiveCostPrice(request.getCostPrice(), request.getSupplyCostStrategy(), request.getSupplyBindings());
        applyEditableFields(product, request.getProductType(), request.getCategoryId(), request.getName(),
                costPrice, request.getTerminalLimitPrice(), request.getSupplyCostStrategy(), request.getPricingTemplateId(), request.getImage(), request.getFaceValue(),
                request.getOrderTemplateId(), request.getMinPurchaseQuantity(), request.getMaxPurchaseQuantity(),
                request.getSort(), request.getStatus() == null ? product.getStatus() : request.getStatus());
        productMapper.updateById(product);
        replaceSupplyBindings(id, request.getSupplyBindings(), product.getSupplyCostStrategy());
        return toResponse(product);
    }

    public ProductResponse updateStatus(Long id, ProductStatusRequest request) {
        if (request == null) {
            throw new RuntimeException("商品状态请求不能为空");
        }
        Product product = getExistingProduct(id);
        validateStatus(request.getStatus());
        product.setStatus(request.getStatus());
        productMapper.updateById(product);
        return toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        getExistingProduct(id);
        deleteSupplyBindings(id);
        productMapper.deleteById(id);
    }

    private void applyEditableFields(
            Product product,
            String productType,
            Long categoryId,
            String name,
            BigDecimal costPrice,
            BigDecimal terminalLimitPrice,
            String supplyCostStrategy,
            Long pricingTemplateId,
            String image,
            BigDecimal faceValue,
            Long orderTemplateId,
            Integer minPurchaseQuantity,
            Integer maxPurchaseQuantity,
            Integer sort,
            Integer status
    ) {
        String normalizedProductType = normalizeProductType(productType);
        ProductCategory category = getExistingCategory(categoryId);
        PricingTemplate pricingTemplate = getExistingPricingTemplate(pricingTemplateId);
        OrderTemplate orderTemplate = orderTemplateService.getExistingTemplate(orderTemplateId);
        validateStatus(status);
        validateSort(sort);
        String normalizedSupplyCostStrategy = normalizeSupplyCostStrategy(supplyCostStrategy);
        BigDecimal normalizedCostPrice = normalizeMoney(costPrice, "Cost price");
        BigDecimal normalizedTerminalLimitPrice = normalizeOptionalMoney(terminalLimitPrice, "Terminal limit price");
        BigDecimal normalizedFaceValue = normalizeOptionalMoney(faceValue, "Face value");
        validatePurchaseQuantity(minPurchaseQuantity, maxPurchaseQuantity);

        product.setProductType(normalizedProductType);
        product.setCategoryId(category.getId());
        product.setName(normalizeName(name));
        product.setCostPrice(normalizedCostPrice);
        product.setSalePrice(pricingTemplateService.calculateSalePrice(normalizedCostPrice, pricingTemplate));
        product.setTerminalLimitPrice(normalizedTerminalLimitPrice);
        product.setSupplyCostStrategy(normalizedSupplyCostStrategy);
        product.setPricingTemplateId(pricingTemplate.getId());
        product.setImage(normalizeImage(image));
        product.setFaceValue(normalizedFaceValue);
        product.setOrderTemplateId(orderTemplate.getId());
        product.setMinPurchaseQuantity(minPurchaseQuantity == null ? 1 : minPurchaseQuantity);
        product.setMaxPurchaseQuantity(maxPurchaseQuantity);
        product.setSort(sort == null ? 0 : sort);
        product.setStatus(status);
    }

    private ProductResponse toResponse(Product product) {
        ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
        PricingTemplate pricingTemplate = pricingTemplateMapper.selectById(product.getPricingTemplateId());
        OrderTemplate orderTemplate = orderTemplateService.getExistingTemplate(product.getOrderTemplateId());
        return ProductResponse.from(
                product,
                category == null ? null : category.getName(),
                pricingTemplate == null ? null : pricingTemplate.getName(),
                orderTemplate == null ? null : orderTemplate.getName(),
                listSupplyBindings(product.getId())
        );
    }

    private Product getExistingProduct(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("商品 ID 不能为空");
        }
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return product;
    }

    private ProductCategory getExistingCategory(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("商品分类 ID 不能为空");
        }
        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("商品分类不存在");
        }
        return category;
    }

    private PricingTemplate getExistingPricingTemplate(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("定价模板 ID 不能为空");
        }
        PricingTemplate template = pricingTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("定价模板不存在");
        }
        return template;
    }

    private List<ProductSupplyBindingResponse> listSupplyBindings(Long productId) {
        QueryWrapper<ProductSupplyBinding> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId).orderByAsc("sort", "id");
        return productSupplyBindingMapper.selectList(wrapper)
                .stream()
                .map(binding -> {
                    SupplyChannel channel = supplyChannelMapper.selectById(binding.getChannelId());
                    return ProductSupplyBindingResponse.from(
                            binding,
                            channel == null ? null : channel.getName(),
                            channel == null ? null : channel.getChannelType()
                    );
                })
                .toList();
    }

    private void replaceSupplyBindings(Long productId, List<ProductSupplyBindingRequest> bindings, String supplyCostStrategy) {
        deleteSupplyBindings(productId);
        if (bindings == null || bindings.isEmpty()) {
            return;
        }
        List<ProductSupplyBindingRequest> sortedBindings = bindings.stream()
                .sorted(Comparator.comparing(binding -> binding.getSort() == null ? 0 : binding.getSort()))
                .toList();
        long activeCount = sortedBindings.stream().filter(binding -> Boolean.TRUE.equals(binding.getActive())).count();
        if (activeCount > 1) {
            throw new RuntimeException("只能启用一个供货渠道绑定");
        }
        ProductSupplyBindingRequest effectiveBinding = selectEffectiveBindingRequest(sortedBindings, supplyCostStrategy);
        int index = 1;
        for (ProductSupplyBindingRequest request : sortedBindings) {
            ProductSupplyBinding binding = new ProductSupplyBinding();
            binding.setProductId(productId);
            binding.setChannelId(getExistingSupplyChannel(request.getChannelId()).getId());
            binding.setChannelProductId(normalizeRequiredText(request.getChannelProductId(), "渠道商品 ID", 100));
            binding.setChannelProductName(normalizeRequiredText(request.getChannelProductName(), "渠道商品名称", 100));
            binding.setChannelCostPrice(normalizeMoney(request.getChannelCostPrice(), "渠道成本价"));
            binding.setActive(effectiveBinding == null ? Boolean.TRUE.equals(request.getActive()) : request == effectiveBinding);
            Integer status = request.getStatus() == null ? STATUS_ENABLED : request.getStatus();
            validateStatus(status);
            validateSort(request.getSort());
            binding.setSort(request.getSort() == null ? index : request.getSort());
            binding.setStatus(status);
            productSupplyBindingMapper.insert(binding);
            index++;
        }
    }

    private void validateSupplyBindingActiveCount(List<ProductSupplyBindingRequest> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return;
        }
        long activeCount = bindings.stream().filter(binding -> Boolean.TRUE.equals(binding.getActive())).count();
        if (activeCount > 1) {
            throw new RuntimeException("只能启用一个供货渠道绑定");
        }
    }

    private void deleteSupplyBindings(Long productId) {
        QueryWrapper<ProductSupplyBinding> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        productSupplyBindingMapper.delete(wrapper);
    }

    private BigDecimal resolveEffectiveCostPrice(
            BigDecimal requestCostPrice,
            String supplyCostStrategy,
            List<ProductSupplyBindingRequest> bindings
    ) {
        ProductSupplyBindingRequest effectiveBinding = selectEffectiveBindingRequest(bindings, supplyCostStrategy);
        return effectiveBinding == null ? requestCostPrice : effectiveBinding.getChannelCostPrice();
    }

    private ProductSupplyBindingRequest selectEffectiveBindingRequest(
            List<ProductSupplyBindingRequest> bindings,
            String supplyCostStrategy
    ) {
        if (bindings == null || bindings.isEmpty()) {
            return null;
        }
        String normalizedStrategy = normalizeSupplyCostStrategy(supplyCostStrategy);
        List<ProductSupplyBindingRequest> enabledBindings = bindings.stream()
                .sorted(Comparator.comparing(binding -> binding.getSort() == null ? 0 : binding.getSort()))
                .filter(binding -> binding.getStatus() == null || binding.getStatus() == STATUS_ENABLED)
                .filter(binding -> binding.getChannelCostPrice() != null && binding.getChannelCostPrice().compareTo(BigDecimal.ZERO) >= 0)
                .toList();
        if (enabledBindings.isEmpty()) {
            return null;
        }
        Comparator<ProductSupplyBindingRequest> comparator = Comparator.comparing(ProductSupplyBindingRequest::getChannelCostPrice);
        return SUPPLY_COST_HIGHEST.equals(normalizedStrategy)
                ? enabledBindings.stream().max(comparator).orElse(null)
                : enabledBindings.stream().min(comparator).orElse(null);
    }

    private SupplyChannel getExistingSupplyChannel(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("供货渠道 ID 不能为空");
        }
        SupplyChannel channel = supplyChannelMapper.selectById(id);
        if (channel == null) {
            throw new RuntimeException("供货渠道不存在");
        }
        return channel;
    }

    private String normalizeProductType(String productType) {
        String normalized = normalizeText(productType);
        normalized = normalized == null ? "" : normalized.toUpperCase(Locale.ROOT);
        if (!List.of(TYPE_VIRTUAL, TYPE_CARD, TYPE_NORMAL).contains(normalized)) {
            throw new RuntimeException("商品类型只能为虚拟商品、卡密或普通商品");
        }
        return normalized;
    }

    private String normalizeSupplyCostStrategy(String supplyCostStrategy) {
        String normalized = normalizeText(supplyCostStrategy);
        if (!StringUtils.hasText(normalized)) {
            return SUPPLY_COST_LOWEST;
        }
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!List.of(SUPPLY_COST_LOWEST, SUPPLY_COST_HIGHEST).contains(normalized)) {
            throw new RuntimeException("供货成本策略只能为最低价或最高价");
        }
        return normalized;
    }

    private String normalizeName(String name) {
        String normalized = normalizeText(name);
        if (!StringUtils.hasText(normalized)) {
            throw new RuntimeException("商品名称不能为空");
        }
        if (normalized.length() > 100) {
            throw new RuntimeException("商品名称不能超过 100 个字符");
        }
        return normalized;
    }

    private String normalizeRequiredText(String value, String fieldName, int maxLength) {
        String normalized = normalizeText(value);
        if (!StringUtils.hasText(normalized)) {
            throw new RuntimeException(fieldName + "不能为空");
        }
        if (normalized.length() > maxLength) {
            throw new RuntimeException(fieldName + "不能超过 " + maxLength + " 个字符");
        }
        return normalized;
    }

    private String normalizeImage(String image) {
        String normalized = normalizeText(image);
        if (normalized != null && normalized.length() > 500) {
            throw new RuntimeException("商品图片地址不能超过 500 个字符");
        }
        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new RuntimeException(fieldName + "不能为空");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(fieldName + "不能小于 0");
        }
        return value;
    }

    private BigDecimal normalizeOptionalMoney(BigDecimal value, String fieldName) {
        if (value == null) {
            return null;
        }
        return normalizeMoney(value, fieldName);
    }

    private void validatePurchaseQuantity(Integer minPurchaseQuantity, Integer maxPurchaseQuantity) {
        int minQuantity = minPurchaseQuantity == null ? 1 : minPurchaseQuantity;
        if (minQuantity < 1) {
            throw new RuntimeException("最小购买数量不能小于 1");
        }
        if (maxPurchaseQuantity != null && maxPurchaseQuantity < minQuantity) {
            throw new RuntimeException("最大购买数量不能小于最小购买数量");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != STATUS_DISABLED && status != STATUS_ENABLED)) {
            throw new RuntimeException("状态只能为 0 或 1");
        }
    }

    private void validateSort(Integer sort) {
        if (sort != null && sort < 0) {
            throw new RuntimeException("排序值不能小于 0");
        }
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }
}
