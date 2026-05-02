package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderTemplateCreateRequest {
    private String name;
    private String description;
    private Integer sort;
    private Integer status;
    private List<OrderTemplateFieldRequest> fields;
}
