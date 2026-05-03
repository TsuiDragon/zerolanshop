package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.UserProfileResponse;
import cn.zerolan.zerolanshop.domain.dto.VirtualOrderCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.VirtualOrderResponse;
import cn.zerolan.zerolanshop.service.UserProfileService;
import cn.zerolan.zerolanshop.service.VirtualOrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class VirtualOrderController {

    private final VirtualOrderService virtualOrderService;
    private final UserProfileService userProfileService;

    public VirtualOrderController(VirtualOrderService virtualOrderService, UserProfileService userProfileService) {
        this.virtualOrderService = virtualOrderService;
        this.userProfileService = userProfileService;
    }

    @PostMapping("/orders")
    public Result<List<VirtualOrderResponse>> create(@RequestBody VirtualOrderCreateRequest request, HttpServletRequest servletRequest) {
        return Result.success(virtualOrderService.create(currentUserId(), request, getClientIp(servletRequest)));
    }

    @GetMapping("/orders")
    public Result<List<VirtualOrderResponse>> list(
            @RequestParam(required = false) String rechargeAccount,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String channelOrderNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        return Result.success(virtualOrderService.listForUser(
                currentUserId(),
                rechargeAccount,
                productName,
                productId,
                orderNo,
                channelOrderNo,
                status,
                startTime,
                endTime
        ));
    }

    @GetMapping("/users/me")
    public Result<UserProfileResponse> currentUser() {
        return Result.success(userProfileService.currentUser(currentUserId()));
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
