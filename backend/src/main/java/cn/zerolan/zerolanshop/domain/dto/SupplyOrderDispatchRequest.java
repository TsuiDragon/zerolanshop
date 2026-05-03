package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SupplyOrderDispatchRequest {
    private Long productId;
    private Long bindingId;
    private String externalOrderNo;
    private Integer quantity;
    private Map<String, Object> orderParams;
}
