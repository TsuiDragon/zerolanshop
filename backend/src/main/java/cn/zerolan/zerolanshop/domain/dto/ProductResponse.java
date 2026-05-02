package cn.zerolan.zerolanshop.domain.dto;

import cn.zerolan.zerolanshop.domain.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String productType;
    private Long categoryId;
    private String categoryName;
    private String name;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
    private Long pricingTemplateId;
    private String pricingTemplateName;
    private String image;
    private BigDecimal faceValue;
    private Long orderTemplateId;
    private String orderTemplateName;
    private Integer minPurchaseQuantity;
    private Integer maxPurchaseQuantity;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static ProductResponse from(
            Product product,
            String categoryName,
            String pricingTemplateName,
            String orderTemplateName
    ) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setProductType(product.getProductType());
        response.setCategoryId(product.getCategoryId());
        response.setCategoryName(categoryName);
        response.setName(product.getName());
        response.setCostPrice(product.getCostPrice());
        response.setSalePrice(product.getSalePrice());
        response.setPricingTemplateId(product.getPricingTemplateId());
        response.setPricingTemplateName(pricingTemplateName);
        response.setImage(product.getImage());
        response.setFaceValue(product.getFaceValue());
        response.setOrderTemplateId(product.getOrderTemplateId());
        response.setOrderTemplateName(orderTemplateName);
        response.setMinPurchaseQuantity(product.getMinPurchaseQuantity());
        response.setMaxPurchaseQuantity(product.getMaxPurchaseQuantity());
        response.setSort(product.getSort());
        response.setStatus(product.getStatus());
        response.setCreateTime(product.getCreateTime());
        response.setUpdateTime(product.getUpdateTime());
        return response;
    }
}
