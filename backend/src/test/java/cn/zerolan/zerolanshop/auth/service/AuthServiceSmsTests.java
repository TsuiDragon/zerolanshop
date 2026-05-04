package cn.zerolan.zerolanshop.auth.service;

import cn.zerolan.zerolanshop.auth.dto.PasswordResetRequest;
import cn.zerolan.zerolanshop.auth.dto.RegisterRequest;
import cn.zerolan.zerolanshop.auth.dto.SmsCodeRequest;
import cn.zerolan.zerolanshop.auth.dto.SmsLoginRequest;
import cn.zerolan.zerolanshop.auth.service.impl.AuthServiceImpl;
import cn.zerolan.zerolanshop.auth.service.impl.SmsVerificationServiceImpl;
import cn.zerolan.zerolanshop.domain.entity.User;
import cn.zerolan.zerolanshop.mapper.UserMapper;
import cn.zerolan.zerolanshop.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceSmsTests {

    private final UserMapper userMapper = mock(UserMapper.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SmsAuthenticationClient smsAuthenticationClient = mock(SmsAuthenticationClient.class);
    private final SmsVerificationService smsVerificationService = new SmsVerificationServiceImpl(smsAuthenticationClient);
    private final AuthService authService = new AuthServiceImpl(userMapper, passwordEncoder, jwtUtil, smsVerificationService);

    @Test
    void sendSmsCodeDelegatesToAliyunClient() {
        SmsCodeRequest request = new SmsCodeRequest();
        request.setPhone("13800138000");
        request.setScene("REGISTER");
        when(userMapper.selectCount(any())).thenReturn(0L);

        authService.sendSmsCode(request);

        verify(smsAuthenticationClient).sendCode("13800138000", SmsScene.REGISTER);
    }

    @Test
    void registerVerifiesPhoneCodeAndCreatesUser() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(smsAuthenticationClient.verifyCode("13800138000", SmsScene.REGISTER, "123456")).thenReturn(true);
        when(jwtUtil.generateToken(any(), any())).thenReturn("token");

        RegisterRequest request = new RegisterRequest();
        request.setUsername("buyer01");
        request.setPassword("123456");
        request.setConfirmPassword("123456");
        request.setEmail("buyer@example.com");
        request.setPhone("13800138000");
        request.setPhoneCode("123456");

        authService.register(request, "127.0.0.1");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertThat(userCaptor.getValue().getPhone()).isEqualTo("13800138000");
        assertThat(userCaptor.getValue().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void registerRejectsInvalidPhoneCode() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(smsAuthenticationClient.verifyCode("13800138000", SmsScene.REGISTER, "000000")).thenReturn(false);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("buyer01");
        request.setPassword("123456");
        request.setConfirmPassword("123456");
        request.setPhone("13800138000");
        request.setPhoneCode("000000");

        assertThatThrownBy(() -> authService.register(request, "127.0.0.1"))
                .hasMessage("SMS verification code is invalid");
        verify(userMapper, never()).insert(any());
    }

    @Test
    void smsLoginReturnsTokenForExistingEnabledUser() {
        User user = user("buyer01", "13800138000");
        when(userMapper.selectOne(any())).thenReturn(user);
        when(smsAuthenticationClient.verifyCode("13800138000", SmsScene.LOGIN, "123456")).thenReturn(true);
        when(jwtUtil.generateToken(5L, "buyer01")).thenReturn("token");

        SmsLoginRequest request = new SmsLoginRequest();
        request.setPhone("13800138000");
        request.setCode("123456");

        assertThat(authService.smsLogin(request).getToken()).isEqualTo("token");
        verify(userMapper).updateById(user);
    }

    @Test
    void smsLoginRejectsMissingUser() {
        when(userMapper.selectOne(any())).thenReturn(null);
        SmsLoginRequest request = new SmsLoginRequest();
        request.setPhone("13800138000");
        request.setCode("123456");

        assertThatThrownBy(() -> authService.smsLogin(request))
                .hasMessage("User does not exist");
    }

    @Test
    void resetPasswordUpdatesEncodedPassword() {
        User user = user("buyer01", "13800138000");
        user.setPassword(passwordEncoder.encode("oldpass"));
        when(userMapper.selectOne(any())).thenReturn(user);
        when(smsAuthenticationClient.verifyCode("13800138000", SmsScene.PASSWORD_RESET, "123456")).thenReturn(true);

        PasswordResetRequest request = new PasswordResetRequest();
        request.setPhone("13800138000");
        request.setCode("123456");
        request.setPassword("newpass");
        request.setConfirmPassword("newpass");

        authService.resetPassword(request);

        assertThat(passwordEncoder.matches("newpass", user.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("oldpass", user.getPassword())).isFalse();
        verify(userMapper).updateById(user);
    }

    private User user(String username, String phone) {
        User user = new User();
        user.setId(5L);
        user.setUsername(username);
        user.setPhone(phone);
        user.setStatus(1);
        return user;
    }
}
