package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelBalanceResponse;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelProductResponse;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelResponse;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.SupplyChannelUpdateRequest;
import cn.zerolan.zerolanshop.service.SupplyChannelService;
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
@RequestMapping("/api/admin/supply-channels")
public class AdminSupplyChannelController {

    private final SupplyChannelService supplyChannelService;

    public AdminSupplyChannelController(SupplyChannelService supplyChannelService) {
        this.supplyChannelService = supplyChannelService;
    }

    @GetMapping
    public Result<List<SupplyChannelResponse>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String channelType,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(supplyChannelService.list(name, channelType, status));
    }

    @GetMapping("/{id}")
    public Result<SupplyChannelResponse> detail(@PathVariable Long id) {
        return Result.success(supplyChannelService.detail(id));
    }

    @GetMapping("/{id}/balance")
    public Result<SupplyChannelBalanceResponse> balance(@PathVariable Long id) {
        return Result.success(supplyChannelService.balance(id));
    }

    @GetMapping("/{id}/products/{channelProductId}")
    public Result<SupplyChannelProductResponse> channelProduct(@PathVariable Long id, @PathVariable String channelProductId) {
        return Result.success(supplyChannelService.channelProduct(id, channelProductId));
    }

    @PostMapping
    public Result<SupplyChannelResponse> create(@RequestBody SupplyChannelCreateRequest request) {
        return Result.success(supplyChannelService.create(request));
    }

    @PutMapping("/{id}")
    public Result<SupplyChannelResponse> update(@PathVariable Long id, @RequestBody SupplyChannelUpdateRequest request) {
        return Result.success(supplyChannelService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public Result<SupplyChannelResponse> updateStatus(@PathVariable Long id, @RequestBody SupplyChannelStatusRequest request) {
        return Result.success(supplyChannelService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        supplyChannelService.delete(id);
        return Result.success();
    }
}
