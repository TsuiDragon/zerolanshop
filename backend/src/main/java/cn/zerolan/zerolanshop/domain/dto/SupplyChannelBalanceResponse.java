package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SupplyChannelBalanceResponse {
    private Long channelId;
    private String channelType;
    private BigDecimal balance;
    private String message;
}
