package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.CategoryTreeResponse;
import cn.zerolan.zerolanshop.domain.dto.ProductResponse;
import cn.zerolan.zerolanshop.service.ProductCategoryService;
import cn.zerolan.zerolanshop.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
public class PublicStoreController {

    private static final int STATUS_ENABLED = 1;

    private final ProductCategoryService productCategoryService;
    private final ProductService productService;

    public PublicStoreController(ProductCategoryService productCategoryService, ProductService productService) {
        this.productCategoryService = productCategoryService;
        this.productService = productService;
    }

    @GetMapping("/categories/tree")
    public Result<List<CategoryTreeResponse>> categories() {
        return Result.success(enabledCategoryTree());
    }

    @GetMapping("/products")
    public Result<List<ProductResponse>> products(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) Long categoryId
    ) {
        Set<Long> enabledCategoryIds = enabledCategoryTree().stream()
                .flatMap(category -> {
                    List<Long> childIds = category.getChildren().stream()
                            .map(child -> child.getId())
                            .toList();
                    return java.util.stream.Stream.concat(java.util.stream.Stream.of(category.getId()), childIds.stream());
                })
                .collect(Collectors.toSet());
        return Result.success(productService.list(name, productType, categoryId, STATUS_ENABLED).stream()
                .filter(product -> enabledCategoryIds.contains(product.getCategoryId()))
                .toList());
    }

    private List<CategoryTreeResponse> enabledCategoryTree() {
        return productCategoryService.tree().stream()
                .filter(category -> Integer.valueOf(STATUS_ENABLED).equals(category.getStatus()))
                .map(category -> {
                    category.setChildren(category.getChildren().stream()
                            .filter(child -> Integer.valueOf(STATUS_ENABLED).equals(child.getStatus()))
                            .toList());
                    return category;
                })
                .toList();
    }
}
