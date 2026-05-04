package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.ProductCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.ProductSupplyBindingRequest;
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
import cn.zerolan.zerolanshop.service.impl.PricingTemplateServiceImpl;
import cn.zerolan.zerolanshop.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTests {

    private final ProductMapper productMapper = mock(ProductMapper.class);
    private final ProductCategoryMapper productCategoryMapper = mock(ProductCategoryMapper.class);
    private final PricingTemplateMapper pricingTemplateMapper = mock(PricingTemplateMapper.class);
    private final ProductSupplyBindingMapper productSupplyBindingMapper = mock(ProductSupplyBindingMapper.class);
    private final SupplyChannelMapper supplyChannelMapper = mock(SupplyChannelMapper.class);
    private final OrderTemplateService orderTemplateService = mock(OrderTemplateService.class);
    private final PricingTemplateService pricingTemplateService = new PricingTemplateServiceImpl(null);
    private final ProductService productService = new ProductServiceImpl(
            productMapper,
            productCategoryMapper,
            pricingTemplateMapper,
            productSupplyBindingMapper,
            supplyChannelMapper,
            orderTemplateService,
            pricingTemplateService
    );

    @BeforeEach
    void setUp() {
        when(productSupplyBindingMapper.selectList(any())).thenReturn(List.of());
    }

    @Test
    void createCalculatesAndPersistsSalePrice() {
        ProductCategory category = new ProductCategory();
        category.setId(7L);
        category.setName("视频会员");
        PricingTemplate pricingTemplate = new PricingTemplate();
        pricingTemplate.setId(3L);
        pricingTemplate.setName("加价10%");
        pricingTemplate.setPricingType(PricingTemplateService.TYPE_PERCENTAGE);
        pricingTemplate.setPricingValue(new BigDecimal("10"));
        OrderTemplate orderTemplate = new OrderTemplate();
        orderTemplate.setId(2L);
        orderTemplate.setName("手机号");
        when(productCategoryMapper.selectById(7L)).thenReturn(category);
        when(pricingTemplateMapper.selectById(3L)).thenReturn(pricingTemplate);
        when(orderTemplateService.getExistingTemplate(2L)).thenReturn(orderTemplate);

        ProductCreateRequest request = new ProductCreateRequest();
        request.setProductType(ProductService.TYPE_VIRTUAL);
        request.setCategoryId(7L);
        request.setName("爱奇艺月卡");
        request.setCostPrice(new BigDecimal("20.00"));
        request.setPricingTemplateId(3L);
        request.setOrderTemplateId(2L);

        productService.create(request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).insert(productCaptor.capture());
        assertThat(productCaptor.getValue().getSalePrice()).isEqualByComparingTo(new BigDecimal("22.00"));
        assertThat(productCaptor.getValue().getMinPurchaseQuantity()).isEqualTo(1);
    }

    @Test
    void createRejectsMaxPurchaseQuantityLowerThanMinimum() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setProductType(ProductService.TYPE_VIRTUAL);
        request.setCategoryId(7L);
        request.setName("爱奇艺月卡");
        request.setCostPrice(new BigDecimal("20.00"));
        request.setPricingTemplateId(3L);
        request.setOrderTemplateId(2L);
        request.setMinPurchaseQuantity(3);
        request.setMaxPurchaseQuantity(2);

        ProductCategory category = new ProductCategory();
        category.setId(7L);
        PricingTemplate pricingTemplate = new PricingTemplate();
        pricingTemplate.setId(3L);
        OrderTemplate orderTemplate = new OrderTemplate();
        orderTemplate.setId(2L);
        when(productCategoryMapper.selectById(7L)).thenReturn(category);
        when(pricingTemplateMapper.selectById(3L)).thenReturn(pricingTemplate);
        when(orderTemplateService.getExistingTemplate(2L)).thenReturn(orderTemplate);

        assertThatThrownBy(() -> productService.create(request))
                .hasMessage("Maximum purchase quantity must be greater than or equal to minimum purchase quantity");
    }

    @Test
    void createUsesLowestEnabledSupplyBindingCostAndRecalculatesSalePrice() {
        ProductCategory category = new ProductCategory();
        category.setId(7L);
        PricingTemplate pricingTemplate = new PricingTemplate();
        pricingTemplate.setId(3L);
        pricingTemplate.setPricingType(PricingTemplateService.TYPE_PERCENTAGE);
        pricingTemplate.setPricingValue(new BigDecimal("10"));
        OrderTemplate orderTemplate = new OrderTemplate();
        orderTemplate.setId(2L);
        SupplyChannel channel = new SupplyChannel();
        channel.setId(9L);
        when(productCategoryMapper.selectById(7L)).thenReturn(category);
        when(pricingTemplateMapper.selectById(3L)).thenReturn(pricingTemplate);
        when(orderTemplateService.getExistingTemplate(2L)).thenReturn(orderTemplate);
        when(supplyChannelMapper.selectById(9L)).thenReturn(channel);

        ProductSupplyBindingRequest highCost = bindingRequest("1000", new BigDecimal("12.00"), 1);
        ProductSupplyBindingRequest lowCost = bindingRequest("1001", new BigDecimal("8.00"), 1);
        ProductCreateRequest request = new ProductCreateRequest();
        request.setProductType(ProductService.TYPE_VIRTUAL);
        request.setCategoryId(7L);
        request.setName("Video card");
        request.setCostPrice(new BigDecimal("20.00"));
        request.setSupplyCostStrategy(ProductService.SUPPLY_COST_LOWEST);
        request.setPricingTemplateId(3L);
        request.setOrderTemplateId(2L);
        request.setSupplyBindings(List.of(highCost, lowCost));

        productService.create(request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).insert(productCaptor.capture());
        assertThat(productCaptor.getValue().getCostPrice()).isEqualByComparingTo("8.00");
        assertThat(productCaptor.getValue().getSalePrice()).isEqualByComparingTo("8.80");
        assertThat(productCaptor.getValue().getSupplyCostStrategy()).isEqualTo(ProductService.SUPPLY_COST_LOWEST);

        ArgumentCaptor<ProductSupplyBinding> bindingCaptor = ArgumentCaptor.forClass(ProductSupplyBinding.class);
        verify(productSupplyBindingMapper, times(2)).insert(bindingCaptor.capture());
        assertThat(bindingCaptor.getAllValues())
                .filteredOn(ProductSupplyBinding::getActive)
                .singleElement()
                .extracting(ProductSupplyBinding::getChannelProductId)
                .isEqualTo("1001");
    }

    @Test
    void createUsesHighestEnabledSupplyBindingAsActiveCost() {
        ProductCategory category = new ProductCategory();
        category.setId(7L);
        PricingTemplate pricingTemplate = new PricingTemplate();
        pricingTemplate.setId(3L);
        pricingTemplate.setPricingType(PricingTemplateService.TYPE_PERCENTAGE);
        pricingTemplate.setPricingValue(new BigDecimal("10"));
        OrderTemplate orderTemplate = new OrderTemplate();
        orderTemplate.setId(2L);
        SupplyChannel channel = new SupplyChannel();
        channel.setId(9L);
        when(productCategoryMapper.selectById(7L)).thenReturn(category);
        when(pricingTemplateMapper.selectById(3L)).thenReturn(pricingTemplate);
        when(orderTemplateService.getExistingTemplate(2L)).thenReturn(orderTemplate);
        when(supplyChannelMapper.selectById(9L)).thenReturn(channel);

        ProductCreateRequest request = new ProductCreateRequest();
        request.setProductType(ProductService.TYPE_VIRTUAL);
        request.setCategoryId(7L);
        request.setName("Video card");
        request.setCostPrice(new BigDecimal("20.00"));
        request.setSupplyCostStrategy(ProductService.SUPPLY_COST_HIGHEST);
        request.setPricingTemplateId(3L);
        request.setOrderTemplateId(2L);
        request.setSupplyBindings(List.of(
                bindingRequest("1000", new BigDecimal("12.00"), 1),
                bindingRequest("1001", new BigDecimal("8.00"), 2)
        ));

        productService.create(request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).insert(productCaptor.capture());
        assertThat(productCaptor.getValue().getCostPrice()).isEqualByComparingTo("12.00");
        assertThat(productCaptor.getValue().getSalePrice()).isEqualByComparingTo("13.20");

        ArgumentCaptor<ProductSupplyBinding> bindingCaptor = ArgumentCaptor.forClass(ProductSupplyBinding.class);
        verify(productSupplyBindingMapper, times(2)).insert(bindingCaptor.capture());
        assertThat(bindingCaptor.getAllValues())
                .filteredOn(ProductSupplyBinding::getActive)
                .singleElement()
                .extracting(ProductSupplyBinding::getChannelProductId)
                .isEqualTo("1000");
    }

    @Test
    void createRejectsMoreThanOneManuallyActiveBinding() {
        ProductCategory category = new ProductCategory();
        category.setId(7L);
        PricingTemplate pricingTemplate = new PricingTemplate();
        pricingTemplate.setId(3L);
        OrderTemplate orderTemplate = new OrderTemplate();
        orderTemplate.setId(2L);
        SupplyChannel channel = new SupplyChannel();
        channel.setId(9L);
        when(productCategoryMapper.selectById(7L)).thenReturn(category);
        when(pricingTemplateMapper.selectById(3L)).thenReturn(pricingTemplate);
        when(orderTemplateService.getExistingTemplate(2L)).thenReturn(orderTemplate);
        when(supplyChannelMapper.selectById(9L)).thenReturn(channel);

        ProductSupplyBindingRequest first = bindingRequest("1000", new BigDecimal("12.00"), 1);
        first.setActive(true);
        ProductSupplyBindingRequest second = bindingRequest("1001", new BigDecimal("8.00"), 2);
        second.setActive(true);
        ProductCreateRequest request = new ProductCreateRequest();
        request.setProductType(ProductService.TYPE_VIRTUAL);
        request.setCategoryId(7L);
        request.setName("Video card");
        request.setCostPrice(new BigDecimal("20.00"));
        request.setPricingTemplateId(3L);
        request.setOrderTemplateId(2L);
        request.setSupplyBindings(List.of(first, second));

        assertThatThrownBy(() -> productService.create(request))
                .hasMessage("Only one supply channel binding can be active");
    }

    private ProductSupplyBindingRequest bindingRequest(String productId, BigDecimal costPrice, Integer sort) {
        ProductSupplyBindingRequest request = new ProductSupplyBindingRequest();
        request.setChannelId(9L);
        request.setChannelProductId(productId);
        request.setChannelProductName("Channel product " + productId);
        request.setChannelCostPrice(costPrice);
        request.setSort(sort);
        request.setStatus(1);
        return request;
    }
}
