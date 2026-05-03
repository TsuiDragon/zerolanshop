package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductUpdateRequest {
    private String productType;
    private Long categoryId;
    private String name;
    private BigDecimal costPrice;
    private BigDecimal terminalLimitPrice;
    private String supplyCostStrategy;
    private Long pricingTemplateId;
    private String image;
    private BigDecimal faceValue;
    private Long orderTemplateId;
    private Integer minPurchaseQuantity;
    private Integer maxPurchaseQuantity;
    private Integer sort;
    private Integer status;
    private List<ProductSupplyBindingRequest> supplyBindings;
}
