package cn.zerolan.zerolanshop.user.service.impl;

import cn.zerolan.zerolanshop.auth.service.SmsScene;
import cn.zerolan.zerolanshop.auth.service.SmsVerificationService;
import cn.zerolan.zerolanshop.domain.entity.User;
import cn.zerolan.zerolanshop.mapper.UserMapper;
import cn.zerolan.zerolanshop.user.dto.PhoneUpdateRequest;
import cn.zerolan.zerolanshop.user.dto.UserProfileResponse;
import cn.zerolan.zerolanshop.user.service.UserProfileService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserMapper userMapper;
    private final SmsVerificationService smsVerificationService;

    public UserProfileServiceImpl(UserMapper userMapper, SmsVerificationService smsVerificationService) {
        this.userMapper = userMapper;
        this.smsVerificationService = smsVerificationService;
    }

    @Override
    public UserProfileResponse currentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return UserProfileResponse.from(user);
    }

    @Override
    public void sendPhoneCode(Long userId, PhoneUpdateRequest request) {
        if (request == null) {
            throw new RuntimeException("手机号修改请求不能为空");
        }
        User user = getExistingUser(userId);
        String phone = smsVerificationService.normalizePhone(request.getPhone());
        assertPhoneChanged(user, phone);
        assertPhoneAvailable(phone, userId);
        smsVerificationService.sendCode(phone, SmsScene.BIND_PHONE);
    }

    @Override
    public UserProfileResponse updatePhone(Long userId, PhoneUpdateRequest request) {
        if (request == null) {
            throw new RuntimeException("手机号修改请求不能为空");
        }
        User user = getExistingUser(userId);
        String phone = smsVerificationService.normalizePhone(request.getPhone());
        assertPhoneChanged(user, phone);
        assertPhoneAvailable(phone, userId);
        smsVerificationService.verifyCode(phone, SmsScene.BIND_PHONE, request.getCode());
        user.setPhone(phone);
        userMapper.updateById(user);
        return UserProfileResponse.from(user);
    }

    private User getExistingUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new RuntimeException("用户 ID 不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    private void assertPhoneChanged(User user, String phone) {
        if (user.getPhone() != null && user.getPhone().equals(phone)) {
            throw new RuntimeException("新手机号不能与当前手机号相同");
        }
    }

    private void assertPhoneAvailable(String phone, Long currentUserId) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        User existing = userMapper.selectOne(wrapper);
        if (existing != null && !existing.getId().equals(currentUserId)) {
            throw new RuntimeException("手机号已存在");
        }
    }
}
