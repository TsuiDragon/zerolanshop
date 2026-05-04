package cn.zerolan.zerolanshop.user.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.user.dto.PhoneUpdateRequest;
import cn.zerolan.zerolanshop.user.dto.UserProfileResponse;
import cn.zerolan.zerolanshop.user.service.UserProfileService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping("/phone/sms-codes")
    public Result<Void> sendPhoneCode(@RequestBody PhoneUpdateRequest request) {
        userProfileService.sendPhoneCode(currentUserId(), request);
        return Result.success();
    }

    @PatchMapping("/phone")
    public Result<UserProfileResponse> updatePhone(@RequestBody PhoneUpdateRequest request) {
        return Result.success(userProfileService.updatePhone(currentUserId(), request));
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
