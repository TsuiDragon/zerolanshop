package cn.zerolan.zerolanshop.auth.service.impl;

import cn.zerolan.zerolanshop.auth.service.SmsAuthenticationClient;
import cn.zerolan.zerolanshop.auth.service.SmsScene;
import cn.zerolan.zerolanshop.auth.service.SmsVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Service
public class SmsVerificationServiceImpl implements SmsVerificationService {

    private static final Pattern MAINLAND_PHONE = Pattern.compile("^1[3-9]\\d{9}$");

    private final SmsAuthenticationClient smsAuthenticationClient;

    public SmsVerificationServiceImpl(SmsAuthenticationClient smsAuthenticationClient) {
        this.smsAuthenticationClient = smsAuthenticationClient;
    }

    @Override
    public void sendCode(String phone, SmsScene scene) {
        smsAuthenticationClient.sendCode(normalizePhone(phone), scene);
    }

    @Override
    public void verifyCode(String phone, SmsScene scene, String code) {
        String normalizedPhone = normalizePhone(phone);
        String normalizedCode = normalizeCode(code);
        if (!smsAuthenticationClient.verifyCode(normalizedPhone, scene, normalizedCode)) {
            throw new RuntimeException("短信验证码无效");
        }
    }

    @Override
    public String normalizePhone(String phone) {
        String normalized = phone == null ? "" : phone.trim();
        if (!MAINLAND_PHONE.matcher(normalized).matches()) {
            throw new RuntimeException("手机号格式不正确");
        }
        return normalized;
    }

    private String normalizeCode(String code) {
        String normalized = code == null ? "" : code.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new RuntimeException("短信验证码不能为空");
        }
        return normalized;
    }
}
