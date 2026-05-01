package cn.zerolan.zerolanshop.domain.dto;

import cn.zerolan.zerolanshop.domain.entity.PricingTemplate;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PricingTemplateResponse {
    private Long id;
    private String name;
    private String pricingType;
    private BigDecimal pricingValue;
    private String description;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static PricingTemplateResponse from(PricingTemplate template) {
        PricingTemplateResponse response = new PricingTemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setPricingType(template.getPricingType());
        response.setPricingValue(template.getPricingValue());
        response.setDescription(template.getDescription());
        response.setSort(template.getSort());
        response.setStatus(template.getStatus());
        response.setCreateTime(template.getCreateTime());
        response.setUpdateTime(template.getUpdateTime());
        return response;
    }
}
