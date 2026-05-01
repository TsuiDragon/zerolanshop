package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.CategoryCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.CategoryResponse;
import cn.zerolan.zerolanshop.domain.dto.CategoryStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.CategoryTreeResponse;
import cn.zerolan.zerolanshop.domain.dto.CategoryUpdateRequest;
import cn.zerolan.zerolanshop.service.ProductCategoryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final ProductCategoryService productCategoryService;

    public AdminCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    /**
     * GET /api/admin/categories
     * 查询分类平铺列表，可按父级、状态、名称过滤。
     */
    @GetMapping
    public Result<List<CategoryResponse>> list(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String name
    ) {
        return Result.success(productCategoryService.list(parentId, status, name));
    }

    /**
     * GET /api/admin/categories/tree
     * 查询两级分类树，供后台管理和前台展示使用。
     */
    @GetMapping("/tree")
    public Result<List<CategoryTreeResponse>> tree() {
        return Result.success(productCategoryService.tree());
    }

    /**
     * GET /api/admin/categories/{id}
     * 查询单个分类资源。
     */
    @GetMapping("/{id}")
    public Result<CategoryResponse> detail(@PathVariable Long id) {
        return Result.success(productCategoryService.detail(id));
    }

    /**
     * POST /api/admin/categories
     * 创建一个分类资源。
     */
    @PostMapping
    public Result<CategoryResponse> create(@RequestBody CategoryCreateRequest request) {
        return Result.success(productCategoryService.create(request));
    }

    /**
     * PUT /api/admin/categories/{id}
     * 更新一个分类资源的可编辑字段。
     */
    @PutMapping("/{id}")
    public Result<CategoryResponse> update(@PathVariable Long id, @RequestBody CategoryUpdateRequest request) {
        return Result.success(productCategoryService.update(id, request));
    }

    /**
     * PATCH /api/admin/categories/{id}/status
     * 只更新分类状态，不影响其他字段。
     */
    @PatchMapping("/{id}/status")
    public Result<CategoryResponse> updateStatus(@PathVariable Long id, @RequestBody CategoryStatusRequest request) {
        return Result.success(productCategoryService.updateStatus(id, request));
    }

    /**
     * DELETE /api/admin/categories/{id}
     * 按业务规则删除一个分类资源。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productCategoryService.delete(id);
        return Result.success();
    }
}
