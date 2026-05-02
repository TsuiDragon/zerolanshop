package cn.zerolan.zerolanshop.domain.dto;

import cn.zerolan.zerolanshop.domain.entity.OrderTemplate;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderTemplateResponse {
    private Long id;
    private String name;
    private String description;
    private Integer sort;
    private Integer status;
    private List<OrderTemplateFieldResponse> fields;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static OrderTemplateResponse from(OrderTemplate template, List<OrderTemplateFieldResponse> fields) {
        OrderTemplateResponse response = new OrderTemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setDescription(template.getDescription());
        response.setSort(template.getSort());
        response.setStatus(template.getStatus());
        response.setFields(fields);
        response.setCreateTime(template.getCreateTime());
        response.setUpdateTime(template.getUpdateTime());
        return response;
    }
}
