package cn.zerolan.zerolanshop.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminLoginResponse {
    private String token;
    private Long adminId;
    private String username;
    private String nickname;
    private String role;
}
