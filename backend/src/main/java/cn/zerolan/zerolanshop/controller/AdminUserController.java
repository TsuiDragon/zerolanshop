package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.AdminUserResponse;
import cn.zerolan.zerolanshop.domain.dto.UserBalanceAdjustRequest;
import cn.zerolan.zerolanshop.domain.dto.UserCreditUpdateRequest;
import cn.zerolan.zerolanshop.domain.dto.UserStatusRequest;
import cn.zerolan.zerolanshop.service.AdminUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public Result<List<AdminUserResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(adminUserService.list(keyword, status));
    }

    @GetMapping("/{id}")
    public Result<AdminUserResponse> detail(@PathVariable Long id) {
        return Result.success(adminUserService.detail(id));
    }

    @PatchMapping("/{id}/status")
    public Result<AdminUserResponse> updateStatus(@PathVariable Long id, @RequestBody UserStatusRequest request) {
        return Result.success(adminUserService.updateStatus(id, request));
    }

    @PatchMapping("/{id}/balance")
    public Result<AdminUserResponse> adjustBalance(@PathVariable Long id, @RequestBody UserBalanceAdjustRequest request) {
        return Result.success(adminUserService.adjustBalance(id, request));
    }

    @PatchMapping("/{id}/credit")
    public Result<AdminUserResponse> updateCredit(@PathVariable Long id, @RequestBody UserCreditUpdateRequest request) {
        return Result.success(adminUserService.updateCredit(id, request));
    }
}
