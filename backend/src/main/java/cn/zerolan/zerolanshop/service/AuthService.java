package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.LoginRequest;
import cn.zerolan.zerolanshop.domain.dto.LoginResponse;
import cn.zerolan.zerolanshop.domain.dto.RegisterRequest;
import cn.zerolan.zerolanshop.domain.entity.User;
import cn.zerolan.zerolanshop.mapper.UserMapper;
import cn.zerolan.zerolanshop.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 前台采购端用户登录，支持用户名、邮箱或手机号。
     */
    public LoginResponse login(LoginRequest request) {
        String account = normalize(request.getUsername());
        if (!StringUtils.hasText(account) || !StringUtils.hasText(request.getPassword())) {
            throw new RuntimeException("Username and password are required");
        }

        User user = findByAccount(account);
        if (user == null) {
            throw new RuntimeException("User does not exist");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        return buildLoginResponse(user);
    }

    /**
     * 注册前台采购端用户，并返回与登录一致的会话信息。
     */
    public LoginResponse register(RegisterRequest request, String registerIp) {
        validateRegisterRequest(request);

        String username = normalize(request.getUsername());
        String email = normalize(request.getEmail());
        String phone = normalize(request.getPhone());

        assertUnique("username", username, "Username already exists");
        if (StringUtils.hasText(email)) {
            assertUnique("email", email, "Email already exists");
        }
        if (StringUtils.hasText(phone)) {
            assertUnique("phone", phone, "Phone already exists");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(email);
        user.setPhone(phone);
        user.setNickname(username);
        user.setBalance(BigDecimal.ZERO);
        user.setStatus(1);
        user.setRegisterIp(registerIp);
        user.setRegisterTime(now);
        user.setLastLoginTime(now);
        user.setUpdateTime(now);
        user.setDeleted(0);

        userMapper.insert(user);
        return buildLoginResponse(user);
    }

    public void logout(Long userId) {
        // JWT is stateless. The client clears its local token on logout.
    }

    private void validateRegisterRequest(RegisterRequest request) {
        String username = normalize(request.getUsername());
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        if (!StringUtils.hasText(username)) {
            throw new RuntimeException("Username is required");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new RuntimeException("Username must be 3 to 50 characters");
        }
        if (!StringUtils.hasText(password)) {
            throw new RuntimeException("Password is required");
        }
        if (password.length() < 6 || password.length() > 32) {
            throw new RuntimeException("Password must be 6 to 32 characters");
        }
        if (!password.equals(confirmPassword)) {
            throw new RuntimeException("Passwords do not match");
        }
    }

    private User findByAccount(String account) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", account)
                .or()
                .eq("email", account)
                .or()
                .eq("phone", account);
        return userMapper.selectOne(wrapper);
    }

    private void assertUnique(String column, String value, String message) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq(column, value);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException(message);
        }
    }

    private LoginResponse buildLoginResponse(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername());
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
