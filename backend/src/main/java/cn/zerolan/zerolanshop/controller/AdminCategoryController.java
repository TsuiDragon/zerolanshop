package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.CategoryCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.CategoryResponse;
import cn.zerolan.zerolanshop.domain.dto.CategoryStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.CategoryTreeResponse;
import cn.zerolan.zerolanshop.domain.dto.CategoryUpdateRequest;
import cn.zerolan.zerolanshop.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ProductCategoryService productCategoryService;

    @GetMapping
    public Result<List<CategoryResponse>> list(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String name
    ) {
        try {
            return Result.success(productCategoryService.list(parentId, status, name));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/tree")
    public Result<List<CategoryTreeResponse>> tree() {
        try {
            return Result.success(productCategoryService.tree());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<CategoryResponse> detail(@PathVariable Long id) {
        try {
            return Result.success(productCategoryService.detail(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<CategoryResponse> create(@RequestBody CategoryCreateRequest request) {
        try {
            return Result.success(productCategoryService.create(request));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<CategoryResponse> update(@PathVariable Long id, @RequestBody CategoryUpdateRequest request) {
        try {
            return Result.success(productCategoryService.update(id, request));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public Result<CategoryResponse> updateStatus(@PathVariable Long id, @RequestBody CategoryStatusRequest request) {
        try {
            return Result.success(productCategoryService.updateStatus(id, request));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            productCategoryService.delete(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
