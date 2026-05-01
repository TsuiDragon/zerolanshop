package cn.zerolan.zerolanshop.domain.dto;

import cn.zerolan.zerolanshop.domain.entity.ProductCategory;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryTreeResponse {
    private Long id;
    private Long parentId;
    private String name;
    private String icon;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<CategoryResponse> children = new ArrayList<>();

    public static CategoryTreeResponse from(ProductCategory category) {
        CategoryTreeResponse response = new CategoryTreeResponse();
        response.setId(category.getId());
        response.setParentId(category.getParentId());
        response.setName(category.getName());
        response.setIcon(category.getIcon());
        response.setSort(category.getSort());
        response.setStatus(category.getStatus());
        response.setCreateTime(category.getCreateTime());
        response.setUpdateTime(category.getUpdateTime());
        return response;
    }
}
