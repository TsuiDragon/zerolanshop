package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.VirtualOrderResponse;
import cn.zerolan.zerolanshop.domain.dto.VirtualOrderStatusRequest;
import cn.zerolan.zerolanshop.service.VirtualOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminVirtualOrderController {

    private final VirtualOrderService virtualOrderService;

    public AdminVirtualOrderController(VirtualOrderService virtualOrderService) {
        this.virtualOrderService = virtualOrderService;
    }

    @GetMapping
    public Result<List<VirtualOrderResponse>> list(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String rechargeAccount,
            @RequestParam(required = false) String paymentMethod
    ) {
        return Result.success(virtualOrderService.listForAdmin(orderNo, status, productName, rechargeAccount, paymentMethod));
    }

    @PatchMapping("/{id}/status")
    public Result<VirtualOrderResponse> updateStatus(@PathVariable Long id, @RequestBody VirtualOrderStatusRequest request) {
        return Result.success(virtualOrderService.updateStatus(id, request));
    }
}
