package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.ProductCreateRequest;
import cn.zerolan.zerolanshop.domain.entity.OrderTemplate;
import cn.zerolan.zerolanshop.domain.entity.PricingTemplate;
import cn.zerolan.zerolanshop.domain.entity.Product;
import cn.zerolan.zerolanshop.domain.entity.ProductCategory;
import cn.zerolan.zerolanshop.mapper.PricingTemplateMapper;
import cn.zerolan.zerolanshop.mapper.ProductCategoryMapper;
import cn.zerolan.zerolanshop.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTests {

    private final ProductMapper productMapper = mock(ProductMapper.class);
    private final ProductCategoryMapper productCategoryMapper = mock(ProductCategoryMapper.class);
    private final PricingTemplateMapper pricingTemplateMapper = mock(PricingTemplateMapper.class);
    private final OrderTemplateService orderTemplateService = mock(OrderTemplateService.class);
    private final PricingTemplateService pricingTemplateService = new PricingTemplateService(null);
    private final ProductService productService = new ProductService(
            productMapper,
            productCategoryMapper,
            pricingTemplateMapper,
            orderTemplateService,
            pricingTemplateService
    );

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
}
