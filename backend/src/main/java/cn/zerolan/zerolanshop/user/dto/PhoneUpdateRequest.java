package cn.zerolan.zerolanshop.user.dto;

import lombok.Data;

@Data
public class PhoneUpdateRequest {
    private String phone;
    private String code;
}
