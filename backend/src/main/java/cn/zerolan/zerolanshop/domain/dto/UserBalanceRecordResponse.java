package cn.zerolan.zerolanshop.domain.dto;

import cn.zerolan.zerolanshop.domain.entity.UserBalanceRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserBalanceRecordResponse {
    private Long id;
    private Long userId;
    private String username;
    private String recordType;
    private BigDecimal beforeBalance;
    private BigDecimal changeAmount;
    private BigDecimal afterBalance;
    private String orderNo;
    private String remark;
    private LocalDateTime createTime;

    public static UserBalanceRecordResponse from(UserBalanceRecord record) {
        UserBalanceRecordResponse response = new UserBalanceRecordResponse();
        response.setId(record.getId());
        response.setUserId(record.getUserId());
        response.setUsername(record.getUsername());
        response.setRecordType(record.getRecordType());
        response.setBeforeBalance(record.getBeforeBalance());
        response.setChangeAmount(record.getChangeAmount());
        response.setAfterBalance(record.getAfterBalance());
        response.setOrderNo(record.getOrderNo());
        response.setRemark(record.getRemark());
        response.setCreateTime(record.getCreateTime());
        return response;
    }
}
