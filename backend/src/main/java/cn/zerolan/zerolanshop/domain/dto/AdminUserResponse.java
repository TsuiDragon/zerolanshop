package cn.zerolan.zerolanshop.domain.dto;

import cn.zerolan.zerolanshop.domain.entity.User;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminUserResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private String avatar;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private BigDecimal securityDeposit;
    private String creditRating;
    private Integer status;
    private String registerIp;
    private LocalDateTime registerTime;
    private LocalDateTime lastLoginTime;
    private LocalDateTime updateTime;

    public static AdminUserResponse from(User user) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setBalance(user.getBalance());
        response.setCreditLimit(user.getCreditLimit());
        response.setSecurityDeposit(user.getSecurityDeposit());
        response.setCreditRating(user.getCreditRating());
        response.setStatus(user.getStatus());
        response.setRegisterIp(user.getRegisterIp());
        response.setRegisterTime(user.getRegisterTime());
        response.setLastLoginTime(user.getLastLoginTime());
        response.setUpdateTime(user.getUpdateTime());
        return response;
    }
}
