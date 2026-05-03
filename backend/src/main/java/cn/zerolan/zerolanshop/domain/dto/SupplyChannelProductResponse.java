package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SupplyChannelProductResponse {
    private Long channelId;
    private String channelType;
    private String channelProductId;
    private String channelProductName;
    private BigDecimal channelCostPrice;
    private String message;
}
