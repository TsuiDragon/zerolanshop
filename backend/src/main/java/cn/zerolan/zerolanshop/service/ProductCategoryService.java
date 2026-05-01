package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.CategoryCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.CategoryResponse;
import cn.zerolan.zerolanshop.domain.dto.CategoryStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.CategoryTreeResponse;
import cn.zerolan.zerolanshop.domain.dto.CategoryUpdateRequest;
import cn.zerolan.zerolanshop.domain.entity.ProductCategory;
import cn.zerolan.zerolanshop.mapper.ProductCategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductCategoryService {

    private static final long ROOT_PARENT_ID = 0L;
    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_ENABLED = 1;

    @Autowired
    private ProductCategoryMapper productCategoryMapper;

    public List<CategoryResponse> list(Long parentId, Integer status, String name) {
        QueryWrapper<ProductCategory> wrapper = new QueryWrapper<>();
        if (parentId != null) {
            wrapper.eq("parent_id", normalizeParentId(parentId));
        }
        if (status != null) {
            validateStatus(status);
            wrapper.eq("status", status);
        }
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name.trim());
        }
        wrapper.orderByAsc("parent_id", "sort", "id");
        return productCategoryMapper.selectList(wrapper)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<CategoryTreeResponse> tree() {
        QueryWrapper<ProductCategory> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("parent_id", "sort", "id");
        List<ProductCategory> categories = productCategoryMapper.selectList(wrapper);

        Map<Long, List<ProductCategory>> childrenByParentId = categories.stream()
                .filter(category -> !isRoot(category.getParentId()))
                .collect(Collectors.groupingBy(ProductCategory::getParentId));

        List<CategoryTreeResponse> tree = new ArrayList<>();
        for (ProductCategory category : categories) {
            if (!isRoot(category.getParentId())) {
                continue;
            }
            CategoryTreeResponse node = CategoryTreeResponse.from(category);
            List<CategoryResponse> children = childrenByParentId
                    .getOrDefault(category.getId(), List.of())
                    .stream()
                    .map(CategoryResponse::from)
                    .toList();
            node.setChildren(children);
            tree.add(node);
        }
        return tree;
    }

    public CategoryResponse detail(Long id) {
        return CategoryResponse.from(getExistingCategory(id));
    }

    public CategoryResponse create(CategoryCreateRequest request) {
        if (request == null) {
            throw new RuntimeException("Category request is required");
        }
        String name = normalizeName(request.getName());
        Long parentId = normalizeParentId(request.getParentId());
        Integer status = request.getStatus() == null ? STATUS_ENABLED : request.getStatus();
        validateStatus(status);
        validateParent(parentId);
        validateSameParentName(null, parentId, name);

        ProductCategory category = new ProductCategory();
        category.setParentId(parentId);
        category.setName(name);
        category.setIcon(normalizeText(request.getIcon()));
        category.setSort(nextSort(parentId));
        category.setStatus(status);
        category.setDeleted(0);
        productCategoryMapper.insert(category);
        return CategoryResponse.from(category);
    }

    public CategoryResponse update(Long id, CategoryUpdateRequest request) {
        if (request == null) {
            throw new RuntimeException("Category request is required");
        }
        ProductCategory category = getExistingCategory(id);
        String name = normalizeName(request.getName());
        Long parentId = normalizeParentId(request.getParentId());
        Integer status = request.getStatus() == null ? category.getStatus() : request.getStatus();
        validateStatus(status);
        if (parentId.equals(id)) {
            throw new RuntimeException("Category cannot use itself as parent");
        }
        if (!isRoot(parentId) && hasChildren(id)) {
            throw new RuntimeException("Category with child categories cannot become a child category");
        }
        validateParent(parentId);
        validateSameParentName(id, parentId, name);

        category.setParentId(parentId);
        category.setName(name);
        category.setIcon(normalizeText(request.getIcon()));
        category.setStatus(status);
        if (request.getSort() != null) {
            if (request.getSort() < 0) {
                throw new RuntimeException("Sort must be greater than or equal to 0");
            }
            category.setSort(request.getSort());
        }
        productCategoryMapper.updateById(category);
        return CategoryResponse.from(category);
    }

    public CategoryResponse updateStatus(Long id, CategoryStatusRequest request) {
        if (request == null) {
            throw new RuntimeException("Category status request is required");
        }
        ProductCategory category = getExistingCategory(id);
        validateStatus(request.getStatus());
        category.setStatus(request.getStatus());
        productCategoryMapper.updateById(category);
        return CategoryResponse.from(category);
    }

    public void delete(Long id) {
        ProductCategory category = getExistingCategory(id);
        if (isRoot(category.getParentId()) && hasChildren(id)) {
            throw new RuntimeException("Cannot delete a parent category with child categories");
        }
        productCategoryMapper.deleteById(id);
    }

    private ProductCategory getExistingCategory(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("Category ID is required");
        }
        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("Category does not exist");
        }
        return category;
    }

    private void validateParent(Long parentId) {
        if (isRoot(parentId)) {
            return;
        }
        ProductCategory parent = productCategoryMapper.selectById(parentId);
        if (parent == null) {
            throw new RuntimeException("Parent category does not exist");
        }
        if (!isRoot(parent.getParentId())) {
            throw new RuntimeException("Only two category levels are supported");
        }
    }

    private void validateSameParentName(Long currentId, Long parentId, String name) {
        QueryWrapper<ProductCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId)
                .eq("name", name);
        if (currentId != null) {
            wrapper.ne("id", currentId);
        }
        if (productCategoryMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("Category name already exists under the same parent");
        }
    }

    private boolean hasChildren(Long id) {
        QueryWrapper<ProductCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        return productCategoryMapper.selectCount(wrapper) > 0;
    }

    private Integer nextSort(Long parentId) {
        QueryWrapper<ProductCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId)
                .select("sort")
                .orderByDesc("sort")
                .orderByDesc("id")
                .last("LIMIT 1");
        ProductCategory lastCategory = productCategoryMapper.selectOne(wrapper);
        if (lastCategory == null || lastCategory.getSort() == null) {
            return 1;
        }
        return lastCategory.getSort() + 1;
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? ROOT_PARENT_ID : parentId;
    }

    private String normalizeName(String name) {
        String normalized = normalizeText(name);
        if (!StringUtils.hasText(normalized)) {
            throw new RuntimeException("Category name is required");
        }
        if (normalized.length() > 50) {
            throw new RuntimeException("Category name must be 50 characters or fewer");
        }
        return normalized;
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != STATUS_DISABLED && status != STATUS_ENABLED)) {
            throw new RuntimeException("Status must be 0 or 1");
        }
    }

    private boolean isRoot(Long parentId) {
        return parentId == null || parentId == ROOT_PARENT_ID;
    }
}
