package cn.zerolan.zerolanshop.auth.service;

public interface SmsVerificationService {
    void sendCode(String phone, SmsScene scene);

    void verifyCode(String phone, SmsScene scene, String code);

    String normalizePhone(String phone);
}
