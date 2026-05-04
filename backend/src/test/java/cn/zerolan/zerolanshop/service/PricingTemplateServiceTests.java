package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.entity.PricingTemplate;
import cn.zerolan.zerolanshop.service.impl.PricingTemplateServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PricingTemplateServiceTests {

    private final PricingTemplateService pricingTemplateService = new PricingTemplateServiceImpl(null);

    @Test
    void percentageTemplateCalculatesSalePrice() {
        PricingTemplate template = new PricingTemplate();
        template.setPricingType(PricingTemplateService.TYPE_PERCENTAGE);
        template.setPricingValue(new BigDecimal("10"));

        BigDecimal salePrice = pricingTemplateService.calculateSalePrice(new BigDecimal("100.00"), template);

        assertThat(salePrice).isEqualByComparingTo(new BigDecimal("110.00"));
    }

    @Test
    void fixedAmountTemplateCalculatesSalePrice() {
        PricingTemplate template = new PricingTemplate();
        template.setPricingType(PricingTemplateService.TYPE_FIXED_AMOUNT);
        template.setPricingValue(new BigDecimal("5.5"));

        BigDecimal salePrice = pricingTemplateService.calculateSalePrice(new BigDecimal("100.00"), template);

        assertThat(salePrice).isEqualByComparingTo(new BigDecimal("105.50"));
    }
}
