package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.CategoryCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.CategoryResponse;
import cn.zerolan.zerolanshop.domain.dto.CategoryStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.CategoryTreeResponse;
import cn.zerolan.zerolanshop.domain.dto.CategoryUpdateRequest;
import cn.zerolan.zerolanshop.domain.entity.ProductCategory;
import cn.zerolan.zerolanshop.mapper.ProductCategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ProductCategoryService {

    List<CategoryResponse> list(Long parentId, Integer status, String name);

    List<CategoryTreeResponse> tree();

    CategoryResponse detail(Long id);

    CategoryResponse create(CategoryCreateRequest request);

    CategoryResponse update(Long id, CategoryUpdateRequest request);

    CategoryResponse updateStatus(Long id, CategoryStatusRequest request);

    void delete(Long id);

}
