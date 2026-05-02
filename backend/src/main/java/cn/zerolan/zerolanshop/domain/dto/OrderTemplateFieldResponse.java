package cn.zerolan.zerolanshop.domain.dto;

import cn.zerolan.zerolanshop.domain.entity.OrderTemplateField;
import lombok.Data;

@Data
public class OrderTemplateFieldResponse {
    private Long id;
    private Long templateId;
    private String fieldType;
    private String fieldName;
    private String placeholder;
    private Boolean required;
    private Integer sort;

    public static OrderTemplateFieldResponse from(OrderTemplateField field) {
        OrderTemplateFieldResponse response = new OrderTemplateFieldResponse();
        response.setId(field.getId());
        response.setTemplateId(field.getTemplateId());
        response.setFieldType(field.getFieldType());
        response.setFieldName(field.getFieldName());
        response.setPlaceholder(field.getPlaceholder());
        response.setRequired(field.getRequired() != null && field.getRequired() == 1);
        response.setSort(field.getSort());
        return response;
    }
}
