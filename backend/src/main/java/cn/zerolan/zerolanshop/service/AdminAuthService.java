package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.AdminLoginRequest;
import cn.zerolan.zerolanshop.domain.dto.AdminLoginResponse;
import cn.zerolan.zerolanshop.domain.entity.Admin;
import cn.zerolan.zerolanshop.mapper.AdminMapper;
import cn.zerolan.zerolanshop.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;

public interface AdminAuthService {

    AdminLoginResponse login(AdminLoginRequest request);

}
