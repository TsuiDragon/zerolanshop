package cn.zerolan.zerolanshop.service;

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
public class ProductService {

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

    public ProductService(
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
            throw new RuntimeException("Product request is required");
        }
        Product product = new Product();
        BigDecimal costPrice = resolveCostPrice(request.getCostPrice(), request.getSupplyCostStrategy(), request.getSupplyBindings());
        applyEditableFields(product, request.getProductType(), request.getCategoryId(), request.getName(),
                costPrice, request.getTerminalLimitPrice(), request.getSupplyCostStrategy(), request.getPricingTemplateId(), request.getImage(), request.getFaceValue(),
                request.getOrderTemplateId(), request.getMinPurchaseQuantity(), request.getMaxPurchaseQuantity(),
                request.getSort(), request.getStatus() == null ? STATUS_ENABLED : request.getStatus());
        productMapper.insert(product);
        replaceSupplyBindings(product.getId(), request.getSupplyBindings());
        return toResponse(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        if (request == null) {
            throw new RuntimeException("Product request is required");
        }
        Product product = getExistingProduct(id);
        BigDecimal costPrice = resolveCostPrice(request.getCostPrice(), request.getSupplyCostStrategy(), request.getSupplyBindings());
        applyEditableFields(product, request.getProductType(), request.getCategoryId(), request.getName(),
                costPrice, request.getTerminalLimitPrice(), request.getSupplyCostStrategy(), request.getPricingTemplateId(), request.getImage(), request.getFaceValue(),
                request.getOrderTemplateId(), request.getMinPurchaseQuantity(), request.getMaxPurchaseQuantity(),
                request.getSort(), request.getStatus() == null ? product.getStatus() : request.getStatus());
        productMapper.updateById(product);
        replaceSupplyBindings(id, request.getSupplyBindings());
        return toResponse(product);
    }

    public ProductResponse updateStatus(Long id, ProductStatusRequest request) {
        if (request == null) {
            throw new RuntimeException("Product status request is required");
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
            throw new RuntimeException("Product ID is required");
        }
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("Product does not exist");
        }
        return product;
    }

    private ProductCategory getExistingCategory(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("Product category ID is required");
        }
        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("Product category does not exist");
        }
        return category;
    }

    private PricingTemplate getExistingPricingTemplate(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("Pricing template ID is required");
        }
        PricingTemplate template = pricingTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("Pricing template does not exist");
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

    private void replaceSupplyBindings(Long productId, List<ProductSupplyBindingRequest> bindings) {
        deleteSupplyBindings(productId);
        if (bindings == null || bindings.isEmpty()) {
            return;
        }
        List<ProductSupplyBindingRequest> sortedBindings = bindings.stream()
                .sorted(Comparator.comparing(binding -> binding.getSort() == null ? 0 : binding.getSort()))
                .toList();
        long activeCount = sortedBindings.stream().filter(binding -> Boolean.TRUE.equals(binding.getActive())).count();
        if (activeCount > 1) {
            throw new RuntimeException("Only one supply channel binding can be active");
        }
        int index = 1;
        for (ProductSupplyBindingRequest request : sortedBindings) {
            ProductSupplyBinding binding = new ProductSupplyBinding();
            binding.setProductId(productId);
            binding.setChannelId(getExistingSupplyChannel(request.getChannelId()).getId());
            binding.setChannelProductId(normalizeRequiredText(request.getChannelProductId(), "Channel product ID is required", 100));
            binding.setChannelProductName(normalizeRequiredText(request.getChannelProductName(), "Channel product name is required", 100));
            binding.setChannelCostPrice(normalizeMoney(request.getChannelCostPrice(), "Channel cost price"));
            binding.setActive(Boolean.TRUE.equals(request.getActive()));
            Integer status = request.getStatus() == null ? STATUS_ENABLED : request.getStatus();
            validateStatus(status);
            validateSort(request.getSort());
            binding.setSort(request.getSort() == null ? index : request.getSort());
            binding.setStatus(status);
            productSupplyBindingMapper.insert(binding);
            index++;
        }
    }

    private void deleteSupplyBindings(Long productId) {
        QueryWrapper<ProductSupplyBinding> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        productSupplyBindingMapper.delete(wrapper);
    }

    private BigDecimal resolveCostPrice(
            BigDecimal requestCostPrice,
            String supplyCostStrategy,
            List<ProductSupplyBindingRequest> bindings
    ) {
        String normalizedStrategy = normalizeSupplyCostStrategy(supplyCostStrategy);
        if (bindings == null || bindings.isEmpty()) {
            return requestCostPrice;
        }
        List<BigDecimal> enabledCosts = bindings.stream()
                .filter(binding -> binding.getStatus() == null || binding.getStatus() == STATUS_ENABLED)
                .map(ProductSupplyBindingRequest::getChannelCostPrice)
                .filter(cost -> cost != null && cost.compareTo(BigDecimal.ZERO) >= 0)
                .toList();
        if (enabledCosts.isEmpty()) {
            return requestCostPrice;
        }
        return SUPPLY_COST_HIGHEST.equals(normalizedStrategy)
                ? enabledCosts.stream().max(BigDecimal::compareTo).orElse(requestCostPrice)
                : enabledCosts.stream().min(BigDecimal::compareTo).orElse(requestCostPrice);
    }

    private SupplyChannel getExistingSupplyChannel(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("Supply channel ID is required");
        }
        SupplyChannel channel = supplyChannelMapper.selectById(id);
        if (channel == null) {
            throw new RuntimeException("Supply channel does not exist");
        }
        return channel;
    }

    private String normalizeProductType(String productType) {
        String normalized = normalizeText(productType);
        normalized = normalized == null ? "" : normalized.toUpperCase(Locale.ROOT);
        if (!List.of(TYPE_VIRTUAL, TYPE_CARD, TYPE_NORMAL).contains(normalized)) {
            throw new RuntimeException("Product type must be VIRTUAL, CARD, or NORMAL");
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
            throw new RuntimeException("Supply cost strategy must be LOWEST or HIGHEST");
        }
        return normalized;
    }

    private String normalizeName(String name) {
        String normalized = normalizeText(name);
        if (!StringUtils.hasText(normalized)) {
            throw new RuntimeException("Product name is required");
        }
        if (normalized.length() > 100) {
            throw new RuntimeException("Product name must be 100 characters or fewer");
        }
        return normalized;
    }

    private String normalizeRequiredText(String value, String message, int maxLength) {
        String normalized = normalizeText(value);
        if (!StringUtils.hasText(normalized)) {
            throw new RuntimeException(message);
        }
        if (normalized.length() > maxLength) {
            throw new RuntimeException(message.replace(" is required", "") + " must be " + maxLength + " characters or fewer");
        }
        return normalized;
    }

    private String normalizeImage(String image) {
        String normalized = normalizeText(image);
        if (normalized != null && normalized.length() > 500) {
            throw new RuntimeException("Product image must be 500 characters or fewer");
        }
        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new RuntimeException(fieldName + " is required");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(fieldName + " must be greater than or equal to 0");
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
            throw new RuntimeException("Minimum purchase quantity must be greater than or equal to 1");
        }
        if (maxPurchaseQuantity != null && maxPurchaseQuantity < minQuantity) {
            throw new RuntimeException("Maximum purchase quantity must be greater than or equal to minimum purchase quantity");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != STATUS_DISABLED && status != STATUS_ENABLED)) {
            throw new RuntimeException("Status must be 0 or 1");
        }
    }

    private void validateSort(Integer sort) {
        if (sort != null && sort < 0) {
            throw new RuntimeException("Sort must be greater than or equal to 0");
        }
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }
}
