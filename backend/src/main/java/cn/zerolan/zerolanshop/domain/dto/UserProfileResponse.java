package cn.zerolan.zerolanshop.domain.dto;

import cn.zerolan.zerolanshop.domain.entity.User;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserProfileResponse {
    private Long userId;
    private String username;
    private BigDecimal balance;

    public static UserProfileResponse from(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setBalance(user.getBalance());
        return response;
    }
}
