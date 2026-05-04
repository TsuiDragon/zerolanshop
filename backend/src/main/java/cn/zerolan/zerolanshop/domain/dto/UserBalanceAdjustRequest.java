package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserBalanceAdjustRequest {
    private BigDecimal amount;
    private String remark;
}
