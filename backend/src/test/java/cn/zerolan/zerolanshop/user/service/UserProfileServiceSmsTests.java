package cn.zerolan.zerolanshop.user.service;

import cn.zerolan.zerolanshop.auth.service.SmsAuthenticationClient;
import cn.zerolan.zerolanshop.auth.service.SmsScene;
import cn.zerolan.zerolanshop.auth.service.SmsVerificationService;
import cn.zerolan.zerolanshop.auth.service.impl.SmsVerificationServiceImpl;
import cn.zerolan.zerolanshop.domain.entity.User;
import cn.zerolan.zerolanshop.mapper.UserMapper;
import cn.zerolan.zerolanshop.user.dto.PhoneUpdateRequest;
import cn.zerolan.zerolanshop.user.service.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileServiceSmsTests {

    private final UserMapper userMapper = mock(UserMapper.class);
    private final SmsAuthenticationClient smsAuthenticationClient = mock(SmsAuthenticationClient.class);
    private final SmsVerificationService smsVerificationService = new SmsVerificationServiceImpl(smsAuthenticationClient);
    private final UserProfileService userProfileService = new UserProfileServiceImpl(userMapper, smsVerificationService);

    @Test
    void updatePhoneRejectsDuplicatePhone() {
        when(userMapper.selectById(5L)).thenReturn(user(5L, "buyer01", "13800138000"));
        when(userMapper.selectOne(any())).thenReturn(user(6L, "buyer02", "13900139000"));
        PhoneUpdateRequest request = new PhoneUpdateRequest();
        request.setPhone("13900139000");
        request.setCode("123456");

        assertThatThrownBy(() -> userProfileService.updatePhone(5L, request))
                .hasMessage("Phone already exists");
    }

    @Test
    void updatePhoneVerifiesCodeAndPersistsPhone() {
        User user = user(5L, "buyer01", "13800138000");
        when(userMapper.selectById(5L)).thenReturn(user);
        when(userMapper.selectOne(any())).thenReturn(null);
        when(smsAuthenticationClient.verifyCode("13900139000", SmsScene.BIND_PHONE, "123456")).thenReturn(true);
        PhoneUpdateRequest request = new PhoneUpdateRequest();
        request.setPhone("13900139000");
        request.setCode("123456");

        userProfileService.updatePhone(5L, request);

        assertThat(user.getPhone()).isEqualTo("13900139000");
        verify(userMapper).updateById(user);
    }

    private User user(Long id, String username, String phone) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPhone(phone);
        user.setStatus(1);
        return user;
    }
}
