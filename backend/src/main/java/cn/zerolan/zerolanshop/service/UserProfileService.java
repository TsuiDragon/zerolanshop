package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.UserProfileResponse;
import cn.zerolan.zerolanshop.domain.entity.User;
import cn.zerolan.zerolanshop.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final UserMapper userMapper;

    public UserProfileService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserProfileResponse currentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User does not exist");
        }
        return UserProfileResponse.from(user);
    }
}
