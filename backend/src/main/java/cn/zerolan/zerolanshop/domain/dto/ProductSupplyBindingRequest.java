package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSupplyBindingRequest {
    private Long id;
    private Long channelId;
    private String channelProductId;
    private String channelProductName;
    private BigDecimal channelCostPrice;
    private Boolean active;
    private Integer sort;
    private Integer status;
}
