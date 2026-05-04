package cn.zerolan.zerolanshop.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private String phone;
    private String phoneCode;
}
