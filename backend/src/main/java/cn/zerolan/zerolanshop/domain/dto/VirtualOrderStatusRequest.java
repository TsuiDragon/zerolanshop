package cn.zerolan.zerolanshop.domain.dto;

import lombok.Data;

@Data
public class VirtualOrderStatusRequest {
    private String status;
    private String exceptionMessage;
}
