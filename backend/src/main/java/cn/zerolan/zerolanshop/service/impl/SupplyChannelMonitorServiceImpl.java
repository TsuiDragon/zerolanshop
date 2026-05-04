package cn.zerolan.zerolanshop.service.impl;

import cn.zerolan.zerolanshop.service.*;

import cn.zerolan.zerolanshop.domain.dto.SupplyChannelProductResponse;
import cn.zerolan.zerolanshop.domain.entity.PricingTemplate;
import cn.zerolan.zerolanshop.domain.entity.Product;
import cn.zerolan.zerolanshop.domain.entity.ProductSupplyBinding;
import cn.zerolan.zerolanshop.domain.entity.SupplyChannel;
import cn.zerolan.zerolanshop.mapper.PricingTemplateMapper;
import cn.zerolan.zerolanshop.mapper.ProductMapper;
import cn.zerolan.zerolanshop.mapper.ProductSupplyBindingMapper;
import cn.zerolan.zerolanshop.mapper.SupplyChannelMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class SupplyChannelMonitorServiceImpl implements SupplyChannelMonitorService {

    private static final Logger log = LoggerFactory.getLogger(SupplyChannelMonitorService.class);
    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_ENABLED = 1;

    private final ProductMapper productMapper;
    private final ProductSupplyBindingMapper productSupplyBindingMapper;
    private final SupplyChannelMapper supplyChannelMapper;
    private final PricingTemplateMapper pricingTemplateMapper;
    private final PricingTemplateService pricingTemplateService;
    private final YoukayunClient youkayunClient;

    public SupplyChannelMonitorServiceImpl(
            ProductMapper productMapper,
            ProductSupplyBindingMapper productSupplyBindingMapper,
            SupplyChannelMapper supplyChannelMapper,
            PricingTemplateMapper pricingTemplateMapper,
            PricingTemplateService pricingTemplateService,
            YoukayunClient youkayunClient
    ) {
        this.productMapper = productMapper;
        this.productSupplyBindingMapper = productSupplyBindingMapper;
        this.supplyChannelMapper = supplyChannelMapper;
        this.pricingTemplateMapper = pricingTemplateMapper;
        this.pricingTemplateService = pricingTemplateService;
        this.youkayunClient = youkayunClient;
    }

    @Scheduled(
            initialDelayString = "${zerolanshop.supply-monitor.initial-delay-ms:300000}",
            fixedDelayString = "${zerolanshop.supply-monitor.fixed-delay-ms:300000}"
    )
    public void monitorEnabledProducts() {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", STATUS_ENABLED).orderByAsc("id");
        List<Product> products = productMapper.selectList(wrapper);
        for (Product product : products) {
            try {
                monitorProduct(product.getId());
            } catch (RuntimeException exception) {
                log.warn("Supply channel monitor failed for product {}", product.getId(), exception);
            }
        }
    }

    @Transactional
    public void monitorProduct(Long productId) {
        if (productId == null || productId <= 0) {
            throw new RuntimeException("商品 ID 不能为空");
        }
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        List<ProductSupplyBinding> bindings = listEnabledBindings(productId);
        if (bindings.isEmpty()) {
            return;
        }

        for (ProductSupplyBinding binding : bindings) {
            refreshBinding(binding);
        }

        List<ProductSupplyBinding> validBindings = listEnabledBindings(productId).stream()
                .filter(binding -> binding.getChannelCostPrice() != null && binding.getChannelCostPrice().compareTo(BigDecimal.ZERO) >= 0)
                .toList();
        ProductSupplyBinding effectiveBinding = selectEffectiveBinding(product, validBindings);
        if (effectiveBinding == null) {
            return;
        }
        updateActiveBindings(productId, effectiveBinding.getId());
        updateProductPrice(product, effectiveBinding.getChannelCostPrice());
    }

    private List<ProductSupplyBinding> listEnabledBindings(Long productId) {
        QueryWrapper<ProductSupplyBinding> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId)
                .eq("status", STATUS_ENABLED)
                .orderByAsc("sort", "id");
        return productSupplyBindingMapper.selectList(wrapper);
    }

    private void refreshBinding(ProductSupplyBinding binding) {
        SupplyChannel channel = supplyChannelMapper.selectById(binding.getChannelId());
        if (channel == null || channel.getStatus() == null || channel.getStatus() != STATUS_ENABLED) {
            disableBinding(binding);
            return;
        }
        if (!SupplyChannelService.TYPE_YOUKAYUN.equals(channel.getChannelType())) {
            disableBinding(binding);
            return;
        }
        try {
            SupplyChannelProductResponse response = youkayunClient.queryProduct(channel, binding.getChannelProductId());
            BigDecimal costPrice = response == null ? null : response.getChannelCostPrice();
            if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) < 0) {
                disableBinding(binding);
                return;
            }
            boolean changed = false;
            if (!sameMoney(binding.getChannelCostPrice(), costPrice)) {
                binding.setChannelCostPrice(costPrice);
                changed = true;
            }
            if (response != null && StringUtils.hasText(response.getChannelProductName())
                    && !Objects.equals(binding.getChannelProductName(), response.getChannelProductName().trim())) {
                binding.setChannelProductName(response.getChannelProductName().trim());
                changed = true;
            }
            if (changed) {
                productSupplyBindingMapper.updateById(binding);
            }
        } catch (RuntimeException exception) {
            disableBinding(binding);
        }
    }

    private void disableBinding(ProductSupplyBinding binding) {
        binding.setStatus(STATUS_DISABLED);
        binding.setActive(false);
        productSupplyBindingMapper.updateById(binding);
    }

    private ProductSupplyBinding selectEffectiveBinding(Product product, List<ProductSupplyBinding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return null;
        }
        Comparator<ProductSupplyBinding> comparator = Comparator.comparing(ProductSupplyBinding::getChannelCostPrice);
        return ProductService.SUPPLY_COST_HIGHEST.equals(product.getSupplyCostStrategy())
                ? bindings.stream().max(comparator).orElse(null)
                : bindings.stream().min(comparator).orElse(null);
    }

    private void updateActiveBindings(Long productId, Long effectiveBindingId) {
        QueryWrapper<ProductSupplyBinding> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        List<ProductSupplyBinding> bindings = productSupplyBindingMapper.selectList(wrapper);
        for (ProductSupplyBinding binding : bindings) {
            boolean active = effectiveBindingId.equals(binding.getId());
            if (!Objects.equals(binding.getActive(), active)) {
                binding.setActive(active);
                productSupplyBindingMapper.updateById(binding);
            }
        }
    }

    private void updateProductPrice(Product product, BigDecimal costPrice) {
        PricingTemplate template = pricingTemplateMapper.selectById(product.getPricingTemplateId());
        if (template == null) {
            throw new RuntimeException("定价模板不存在");
        }
        BigDecimal salePrice = pricingTemplateService.calculateSalePrice(costPrice, template);
        if (sameMoney(product.getCostPrice(), costPrice) && sameMoney(product.getSalePrice(), salePrice)) {
            return;
        }
        product.setCostPrice(costPrice);
        product.setSalePrice(salePrice);
        productMapper.updateById(product);
    }

    private boolean sameMoney(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.compareTo(right) == 0;
    }
}
