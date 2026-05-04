package cn.zerolan.zerolanshop.auth.dto;

import lombok.Data;

@Data
public class SmsLoginRequest {
    private String phone;
    private String code;
}
