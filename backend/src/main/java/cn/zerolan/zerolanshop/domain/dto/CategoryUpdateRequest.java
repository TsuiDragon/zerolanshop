package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

@Data
public class CategoryUpdateRequest {
    private Long parentId;
    private String name;
    private String icon;
    private Integer sort;
    private Integer status;
}
