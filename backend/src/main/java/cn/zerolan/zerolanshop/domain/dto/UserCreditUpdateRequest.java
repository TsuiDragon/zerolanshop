package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserCreditUpdateRequest {
    private BigDecimal creditLimit;
    private BigDecimal securityDeposit;
    private String creditRating;
}
