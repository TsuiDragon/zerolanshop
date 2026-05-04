package cn.zerolan.zerolanshop.auth.service;

import cn.zerolan.zerolanshop.auth.dto.LoginRequest;
import cn.zerolan.zerolanshop.auth.dto.LoginResponse;
import cn.zerolan.zerolanshop.auth.dto.PasswordResetRequest;
import cn.zerolan.zerolanshop.auth.dto.RegisterRequest;
import cn.zerolan.zerolanshop.auth.dto.SmsCodeRequest;
import cn.zerolan.zerolanshop.auth.dto.SmsLoginRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request, String registerIp);

    void sendSmsCode(SmsCodeRequest request);

    LoginResponse smsLogin(SmsLoginRequest request);

    void resetPassword(PasswordResetRequest request);

    void logout(Long userId);
}
