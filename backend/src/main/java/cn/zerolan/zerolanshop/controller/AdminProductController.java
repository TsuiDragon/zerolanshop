package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.ProductCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.ProductResponse;
import cn.zerolan.zerolanshop.domain.dto.ProductStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.ProductUpdateRequest;
import cn.zerolan.zerolanshop.service.ProductService;
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
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Result<List<ProductResponse>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(productService.list(name, productType, categoryId, status));
    }

    @GetMapping("/{id}")
    public Result<ProductResponse> detail(@PathVariable Long id) {
        return Result.success(productService.detail(id));
    }

    @PostMapping
    public Result<ProductResponse> create(@RequestBody ProductCreateRequest request) {
        return Result.success(productService.create(request));
    }

    @PutMapping("/{id}")
    public Result<ProductResponse> update(@PathVariable Long id, @RequestBody ProductUpdateRequest request) {
        return Result.success(productService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public Result<ProductResponse> updateStatus(@PathVariable Long id, @RequestBody ProductStatusRequest request) {
        return Result.success(productService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }
}
