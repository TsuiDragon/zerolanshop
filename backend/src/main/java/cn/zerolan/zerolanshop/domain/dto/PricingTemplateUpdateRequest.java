package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PricingTemplateUpdateRequest {
    private String name;
    private String pricingType;
    private BigDecimal pricingValue;
    private String description;
    private Integer sort;
    private Integer status;
}
