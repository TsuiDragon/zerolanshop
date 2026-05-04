package cn.zerolan.zerolanshop.auth.dto;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String phone;
    private String code;
    private String password;
    private String confirmPassword;
}
