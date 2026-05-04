package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.UserBalanceRecordResponse;
import cn.zerolan.zerolanshop.service.UserBalanceRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users/balance-records")
public class UserBalanceRecordController {

    private final UserBalanceRecordService userBalanceRecordService;

    public UserBalanceRecordController(UserBalanceRecordService userBalanceRecordService) {
        this.userBalanceRecordService = userBalanceRecordService;
    }

    @GetMapping
    public Result<List<UserBalanceRecordResponse>> list(
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        return Result.success(userBalanceRecordService.listForUser(currentUserId(), recordType, orderNo, startTime, endTime));
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
