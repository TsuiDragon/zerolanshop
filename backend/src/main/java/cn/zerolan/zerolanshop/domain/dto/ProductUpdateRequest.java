package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {
    private String productType;
    private Long categoryId;
    private String name;
    private BigDecimal costPrice;
    private Long pricingTemplateId;
    private String image;
    private BigDecimal faceValue;
    private Long orderTemplateId;
    private Integer minPurchaseQuantity;
    private Integer maxPurchaseQuantity;
    private Integer sort;
    private Integer status;
}
