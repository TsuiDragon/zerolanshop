package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.SupplyChannelProductResponse;
import cn.zerolan.zerolanshop.domain.entity.PricingTemplate;
import cn.zerolan.zerolanshop.domain.entity.Product;
import cn.zerolan.zerolanshop.domain.entity.ProductSupplyBinding;
import cn.zerolan.zerolanshop.domain.entity.SupplyChannel;
import cn.zerolan.zerolanshop.mapper.PricingTemplateMapper;
import cn.zerolan.zerolanshop.mapper.ProductMapper;
import cn.zerolan.zerolanshop.mapper.ProductSupplyBindingMapper;
import cn.zerolan.zerolanshop.mapper.SupplyChannelMapper;
import cn.zerolan.zerolanshop.service.impl.PricingTemplateServiceImpl;
import cn.zerolan.zerolanshop.service.impl.SupplyChannelMonitorServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupplyChannelMonitorServiceTests {

    private final ProductMapper productMapper = mock(ProductMapper.class);
    private final ProductSupplyBindingMapper productSupplyBindingMapper = mock(ProductSupplyBindingMapper.class);
    private final SupplyChannelMapper supplyChannelMapper = mock(SupplyChannelMapper.class);
    private final PricingTemplateMapper pricingTemplateMapper = mock(PricingTemplateMapper.class);
    private final PricingTemplateService pricingTemplateService = new PricingTemplateServiceImpl(null);
    private final YoukayunClient youkayunClient = mock(YoukayunClient.class);
    private final SupplyChannelMonitorService monitorService = new SupplyChannelMonitorServiceImpl(
            productMapper,
            productSupplyBindingMapper,
            supplyChannelMapper,
            pricingTemplateMapper,
            pricingTemplateService,
            youkayunClient
    );

    @Test
    void monitorRefreshesCostsSwitchesLowestBindingAndUpdatesProductPrice() {
        Product product = product(ProductService.SUPPLY_COST_LOWEST, new BigDecimal("8.00"), new BigDecimal("8.80"));
        PricingTemplate template = percentageTemplate();
        ProductSupplyBinding high = binding(11L, 21L, "1000", new BigDecimal("10.00"), true, 1);
        ProductSupplyBinding low = binding(12L, 22L, "1001", new BigDecimal("9.00"), false, 2);
        SupplyChannel highChannel = channel(21L);
        SupplyChannel lowChannel = channel(22L);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(productSupplyBindingMapper.selectList(any())).thenReturn(List.of(high, low), List.of(high, low), List.of(high, low));
        when(supplyChannelMapper.selectById(21L)).thenReturn(highChannel);
        when(supplyChannelMapper.selectById(22L)).thenReturn(lowChannel);
        when(youkayunClient.queryProduct(highChannel, "1000")).thenReturn(channelProduct("1000", "High", new BigDecimal("12.00")));
        when(youkayunClient.queryProduct(lowChannel, "1001")).thenReturn(channelProduct("1001", "Low", new BigDecimal("7.00")));
        when(pricingTemplateMapper.selectById(3L)).thenReturn(template);

        monitorService.monitorProduct(1L);

        assertThat(high.getChannelCostPrice()).isEqualByComparingTo("12.00");
        assertThat(low.getChannelCostPrice()).isEqualByComparingTo("7.00");
        assertThat(high.getActive()).isFalse();
        assertThat(low.getActive()).isTrue();
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).updateById(productCaptor.capture());
        assertThat(productCaptor.getValue().getCostPrice()).isEqualByComparingTo("7.00");
        assertThat(productCaptor.getValue().getSalePrice()).isEqualByComparingTo("7.70");
    }

    @Test
    void monitorUsesHighestStrategyWhenSelectingActiveBinding() {
        Product product = product(ProductService.SUPPLY_COST_HIGHEST, new BigDecimal("8.00"), new BigDecimal("8.80"));
        ProductSupplyBinding high = binding(11L, 21L, "1000", new BigDecimal("10.00"), false, 1);
        ProductSupplyBinding low = binding(12L, 22L, "1001", new BigDecimal("9.00"), true, 2);
        SupplyChannel highChannel = channel(21L);
        SupplyChannel lowChannel = channel(22L);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(productSupplyBindingMapper.selectList(any())).thenReturn(List.of(high, low), List.of(high, low), List.of(high, low));
        when(supplyChannelMapper.selectById(21L)).thenReturn(highChannel);
        when(supplyChannelMapper.selectById(22L)).thenReturn(lowChannel);
        when(youkayunClient.queryProduct(highChannel, "1000")).thenReturn(channelProduct("1000", "High", new BigDecimal("12.00")));
        when(youkayunClient.queryProduct(lowChannel, "1001")).thenReturn(channelProduct("1001", "Low", new BigDecimal("7.00")));
        when(pricingTemplateMapper.selectById(3L)).thenReturn(percentageTemplate());

        monitorService.monitorProduct(1L);

        assertThat(high.getActive()).isTrue();
        assertThat(low.getActive()).isFalse();
        assertThat(product.getCostPrice()).isEqualByComparingTo("12.00");
        assertThat(product.getSalePrice()).isEqualByComparingTo("13.20");
    }

    @Test
    void monitorDisablesFailedBindingWithoutDisablingWholeChannel() {
        Product product = product(ProductService.SUPPLY_COST_LOWEST, new BigDecimal("8.00"), new BigDecimal("8.80"));
        ProductSupplyBinding failed = binding(11L, 21L, "1000", new BigDecimal("10.00"), true, 1);
        ProductSupplyBinding valid = binding(12L, 22L, "1001", new BigDecimal("9.00"), false, 2);
        SupplyChannel failedChannel = channel(21L);
        SupplyChannel validChannel = channel(22L);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(productSupplyBindingMapper.selectList(any())).thenReturn(List.of(failed, valid), List.of(valid), List.of(failed, valid));
        when(supplyChannelMapper.selectById(21L)).thenReturn(failedChannel);
        when(supplyChannelMapper.selectById(22L)).thenReturn(validChannel);
        when(youkayunClient.queryProduct(failedChannel, "1000")).thenThrow(new RuntimeException("failed"));
        when(youkayunClient.queryProduct(validChannel, "1001")).thenReturn(channelProduct("1001", "Valid", new BigDecimal("7.00")));
        when(pricingTemplateMapper.selectById(3L)).thenReturn(percentageTemplate());

        monitorService.monitorProduct(1L);

        assertThat(failed.getStatus()).isZero();
        assertThat(failed.getActive()).isFalse();
        assertThat(valid.getActive()).isTrue();
        verify(productSupplyBindingMapper, atLeastOnce()).updateById(failed);
        verify(supplyChannelMapper, never()).updateById(any());
    }

    @Test
    void monitorKeepsProductPriceWhenAllBindingsBecomeUnavailable() {
        Product product = product(ProductService.SUPPLY_COST_LOWEST, new BigDecimal("8.00"), new BigDecimal("8.80"));
        ProductSupplyBinding failed = binding(11L, 21L, "1000", new BigDecimal("10.00"), true, 1);
        SupplyChannel failedChannel = channel(21L);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(productSupplyBindingMapper.selectList(any())).thenReturn(List.of(failed), List.of());
        when(supplyChannelMapper.selectById(21L)).thenReturn(failedChannel);
        when(youkayunClient.queryProduct(failedChannel, "1000")).thenReturn(channelProduct("1000", "Failed", null));

        monitorService.monitorProduct(1L);

        assertThat(failed.getStatus()).isZero();
        assertThat(product.getCostPrice()).isEqualByComparingTo("8.00");
        assertThat(product.getSalePrice()).isEqualByComparingTo("8.80");
        verify(productMapper, never()).updateById(any());
    }

    private Product product(String strategy, BigDecimal costPrice, BigDecimal salePrice) {
        Product product = new Product();
        product.setId(1L);
        product.setPricingTemplateId(3L);
        product.setSupplyCostStrategy(strategy);
        product.setCostPrice(costPrice);
        product.setSalePrice(salePrice);
        product.setStatus(1);
        return product;
    }

    private ProductSupplyBinding binding(Long id, Long channelId, String channelProductId, BigDecimal costPrice, boolean active, int sort) {
        ProductSupplyBinding binding = new ProductSupplyBinding();
        binding.setId(id);
        binding.setProductId(1L);
        binding.setChannelId(channelId);
        binding.setChannelProductId(channelProductId);
        binding.setChannelProductName("Old " + channelProductId);
        binding.setChannelCostPrice(costPrice);
        binding.setActive(active);
        binding.setSort(sort);
        binding.setStatus(1);
        return binding;
    }

    private SupplyChannel channel(Long id) {
        SupplyChannel channel = new SupplyChannel();
        channel.setId(id);
        channel.setChannelType(SupplyChannelService.TYPE_YOUKAYUN);
        channel.setStatus(1);
        return channel;
    }

    private PricingTemplate percentageTemplate() {
        PricingTemplate template = new PricingTemplate();
        template.setId(3L);
        template.setPricingType(PricingTemplateService.TYPE_PERCENTAGE);
        template.setPricingValue(new BigDecimal("10"));
        return template;
    }

    private SupplyChannelProductResponse channelProduct(String channelProductId, String name, BigDecimal costPrice) {
        SupplyChannelProductResponse response = new SupplyChannelProductResponse();
        response.setChannelProductId(channelProductId);
        response.setChannelProductName(name);
        response.setChannelCostPrice(costPrice);
        return response;
    }
}
