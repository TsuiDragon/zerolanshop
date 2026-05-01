package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

@Data
public class CategoryCreateRequest {
    private Long parentId;
    private String name;
    private String icon;
    private Integer status;
}
