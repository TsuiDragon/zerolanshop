package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

@Data
public class OrderTemplateFieldRequest {
    private Long id;
    private String fieldType;
    private String fieldName;
    private String placeholder;
    private Boolean required;
    private Integer sort;
}
