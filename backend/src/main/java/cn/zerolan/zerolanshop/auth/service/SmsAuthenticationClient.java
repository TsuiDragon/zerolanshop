package cn.zerolan.zerolanshop.auth.service;

public interface SmsAuthenticationClient {
    void sendCode(String phone, SmsScene scene);

    boolean verifyCode(String phone, SmsScene scene, String code);
}
