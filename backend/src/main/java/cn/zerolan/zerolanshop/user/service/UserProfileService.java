package cn.zerolan.zerolanshop.user.service;

import cn.zerolan.zerolanshop.user.dto.PhoneUpdateRequest;
import cn.zerolan.zerolanshop.user.dto.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse currentUser(Long userId);

    void sendPhoneCode(Long userId, PhoneUpdateRequest request);

    UserProfileResponse updatePhone(Long userId, PhoneUpdateRequest request);
}
