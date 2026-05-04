package cn.zerolan.zerolanshop.auth.service.impl;

import cn.zerolan.zerolanshop.auth.dto.LoginRequest;
import cn.zerolan.zerolanshop.auth.dto.LoginResponse;
import cn.zerolan.zerolanshop.auth.dto.PasswordResetRequest;
import cn.zerolan.zerolanshop.auth.dto.RegisterRequest;
import cn.zerolan.zerolanshop.auth.dto.SmsCodeRequest;
import cn.zerolan.zerolanshop.auth.dto.SmsLoginRequest;
import cn.zerolan.zerolanshop.auth.service.AuthService;
import cn.zerolan.zerolanshop.auth.service.SmsScene;
import cn.zerolan.zerolanshop.auth.service.SmsVerificationService;
import cn.zerolan.zerolanshop.domain.entity.User;
import cn.zerolan.zerolanshop.mapper.UserMapper;
import cn.zerolan.zerolanshop.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SmsVerificationService smsVerificationService;

    public AuthServiceImpl(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            SmsVerificationService smsVerificationService
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.smsVerificationService = smsVerificationService;
    }

    /**
     * 前台采购端用户登录，支持用户名、邮箱或手机号。
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        String account = normalize(request.getUsername());
        if (!StringUtils.hasText(account) || !StringUtils.hasText(request.getPassword())) {
            throw new RuntimeException("请输入账号和密码");
        }

        User user = findByAccount(account);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        return buildLoginResponse(user);
    }

    /**
     * 注册前台采购端用户，并返回与登录一致的会话信息。
     */
    @Override
    public LoginResponse register(RegisterRequest request, String registerIp) {
        validateRegisterRequest(request);

        String username = normalize(request.getUsername());
        String email = normalize(request.getEmail());
        String phone = smsVerificationService.normalizePhone(request.getPhone());

        assertUnique("username", username, "用户名已存在");
        if (StringUtils.hasText(email)) {
            assertUnique("email", email, "邮箱已存在");
        }
        assertUnique("phone", phone, "手机号已存在");
        smsVerificationService.verifyCode(phone, SmsScene.REGISTER, request.getPhoneCode());

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
        userMapper.insert(user);
        return buildLoginResponse(user);
    }

    @Override
    public void sendSmsCode(SmsCodeRequest request) {
        if (request == null) {
            throw new RuntimeException("短信验证码请求不能为空");
        }
        SmsScene scene = SmsScene.from(request.getScene());
        if (!List.of(SmsScene.REGISTER, SmsScene.LOGIN, SmsScene.PASSWORD_RESET).contains(scene)) {
            throw new RuntimeException("不支持的短信验证场景");
        }
        String phone = smsVerificationService.normalizePhone(request.getPhone());
        if (scene == SmsScene.REGISTER) {
            assertNotExists("phone", phone, "手机号已存在");
        } else {
            User user = findByPhone(phone);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new RuntimeException("账号已被禁用");
            }
        }
        smsVerificationService.sendCode(phone, scene);
    }

    @Override
    public LoginResponse smsLogin(SmsLoginRequest request) {
        if (request == null) {
            throw new RuntimeException("短信登录请求不能为空");
        }
        String phone = smsVerificationService.normalizePhone(request.getPhone());
        User user = findByPhone(phone);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        smsVerificationService.verifyCode(phone, SmsScene.LOGIN, request.getCode());
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        return buildLoginResponse(user);
    }

    @Override
    public void resetPassword(PasswordResetRequest request) {
        if (request == null) {
            throw new RuntimeException("重置密码请求不能为空");
        }
        String phone = smsVerificationService.normalizePhone(request.getPhone());
        User user = findByPhone(phone);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        validatePassword(request.getPassword(), request.getConfirmPassword());
        smsVerificationService.verifyCode(phone, SmsScene.PASSWORD_RESET, request.getCode());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void logout(Long userId) {
        // JWT is stateless. The client clears its local token on logout.
    }

    private void validateRegisterRequest(RegisterRequest request) {
        String username = normalize(request.getUsername());
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        if (!StringUtils.hasText(username)) {
            throw new RuntimeException("用户名不能为空");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new RuntimeException("用户名长度需为 3 到 50 个字符");
        }
        validatePassword(password, confirmPassword);
        smsVerificationService.normalizePhone(request.getPhone());
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

    private User findByPhone(String phone) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        return userMapper.selectOne(wrapper);
    }

    private void assertUnique(String column, String value, String message) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq(column, value);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException(message);
        }
    }

    private void assertNotExists(String column, String value, String message) {
        assertUnique(column, value, message);
    }

    private void validatePassword(String password, String confirmPassword) {
        if (!StringUtils.hasText(password)) {
            throw new RuntimeException("密码不能为空");
        }
        if (password.length() < 6 || password.length() > 32) {
            throw new RuntimeException("密码长度需为 6 到 32 个字符");
        }
        if (!password.equals(confirmPassword)) {
            throw new RuntimeException("两次输入的密码不一致");
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
