package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.AdminLoginRequest;
import cn.zerolan.zerolanshop.domain.dto.AdminLoginResponse;
import cn.zerolan.zerolanshop.domain.entity.Admin;
import cn.zerolan.zerolanshop.mapper.AdminMapper;
import cn.zerolan.zerolanshop.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AdminAuthService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AdminAuthService(AdminMapper adminMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 后台运营端管理员登录。
     */
    public AdminLoginResponse login(AdminLoginRequest request) {
        String username = normalize(request.getUsername());
        if (!StringUtils.hasText(username) || !StringUtils.hasText(request.getPassword())) {
            throw new RuntimeException("Username and password are required");
        }

        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        Admin admin = adminMapper.selectOne(wrapper);
        if (admin == null) {
            throw new RuntimeException("Admin does not exist");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new RuntimeException("Admin account is disabled");
        }
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        admin.setLastLoginTime(LocalDateTime.now());
        adminMapper.updateById(admin);

        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername());
        return new AdminLoginResponse(
                token,
                admin.getId(),
                admin.getUsername(),
                admin.getNickname(),
                admin.getRole()
        );
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
