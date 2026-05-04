package cn.zerolan.zerolanshop.auth.dto;

import lombok.Data;

@Data
public class SmsCodeRequest {
    private String phone;
    private String scene;
}
