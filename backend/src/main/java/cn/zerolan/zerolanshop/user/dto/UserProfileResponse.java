package cn.zerolan.zerolanshop.user.dto;

import cn.zerolan.zerolanshop.domain.entity.User;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private BigDecimal securityDeposit;
    private String creditRating;
    private Integer status;

    public static UserProfileResponse from(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setBalance(user.getBalance());
        response.setCreditLimit(user.getCreditLimit());
        response.setSecurityDeposit(user.getSecurityDeposit());
        response.setCreditRating(user.getCreditRating());
        response.setStatus(user.getStatus());
        return response;
    }
}
