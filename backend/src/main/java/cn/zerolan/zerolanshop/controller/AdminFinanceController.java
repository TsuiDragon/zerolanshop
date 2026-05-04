package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.UserBalanceRecordResponse;
import cn.zerolan.zerolanshop.service.UserBalanceRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/finance")
public class AdminFinanceController {

    private final UserBalanceRecordService userBalanceRecordService;

    public AdminFinanceController(UserBalanceRecordService userBalanceRecordService) {
        this.userBalanceRecordService = userBalanceRecordService;
    }

    @GetMapping("/balance-records")
    public Result<List<UserBalanceRecordResponse>> listBalanceRecords(
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String userKeyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        return Result.success(userBalanceRecordService.listForAdmin(recordType, orderNo, userKeyword, startTime, endTime));
    }
}
