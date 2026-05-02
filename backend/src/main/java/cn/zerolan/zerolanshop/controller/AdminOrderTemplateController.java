package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.OrderTemplateCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.OrderTemplateResponse;
import cn.zerolan.zerolanshop.domain.dto.OrderTemplateStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.OrderTemplateUpdateRequest;
import cn.zerolan.zerolanshop.service.OrderTemplateService;
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
@RequestMapping("/api/admin/order-templates")
public class AdminOrderTemplateController {

    private final OrderTemplateService orderTemplateService;

    public AdminOrderTemplateController(OrderTemplateService orderTemplateService) {
        this.orderTemplateService = orderTemplateService;
    }

    @GetMapping
    public Result<List<OrderTemplateResponse>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(orderTemplateService.list(name, status));
    }

    @GetMapping("/{id}")
    public Result<OrderTemplateResponse> detail(@PathVariable Long id) {
        return Result.success(orderTemplateService.detail(id));
    }

    @PostMapping
    public Result<OrderTemplateResponse> create(@RequestBody OrderTemplateCreateRequest request) {
        return Result.success(orderTemplateService.create(request));
    }

    @PutMapping("/{id}")
    public Result<OrderTemplateResponse> update(@PathVariable Long id, @RequestBody OrderTemplateUpdateRequest request) {
        return Result.success(orderTemplateService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public Result<OrderTemplateResponse> updateStatus(@PathVariable Long id, @RequestBody OrderTemplateStatusRequest request) {
        return Result.success(orderTemplateService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderTemplateService.delete(id);
        return Result.success();
    }
}
