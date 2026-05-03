package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

@Data
public class VirtualOrderCreateRequest {
    private Long productId;
    private Integer quantity;
    private String rechargeAccount;
    private String paymentMethod;
}
