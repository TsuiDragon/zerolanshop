package cn.zerolan.zerolanshop.auth.controller;

import cn.zerolan.zerolanshop.auth.dto.LoginRequest;
import cn.zerolan.zerolanshop.auth.dto.LoginResponse;
import cn.zerolan.zerolanshop.auth.dto.PasswordResetRequest;
import cn.zerolan.zerolanshop.auth.dto.RegisterRequest;
import cn.zerolan.zerolanshop.auth.dto.SmsCodeRequest;
import cn.zerolan.zerolanshop.auth.dto.SmsLoginRequest;
import cn.zerolan.zerolanshop.auth.service.AuthService;
import cn.zerolan.zerolanshop.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     * 兼容旧登录路径。新代码优先使用 POST /api/auth/sessions。
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return createSession(request);
    }

    /**
     * POST /api/auth/sessions
     * 创建前台采购端登录会话，成功后返回 JWT。
     */
    @PostMapping("/sessions")
    public Result<LoginResponse> createSession(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/sms-codes")
    public Result<Void> sendSmsCode(@RequestBody SmsCodeRequest request) {
        authService.sendSmsCode(request);
        return Result.success();
    }

    @PostMapping("/sms-sessions")
    public Result<LoginResponse> createSmsSession(@RequestBody SmsLoginRequest request) {
        return Result.success(authService.smsLogin(request));
    }

    @PostMapping("/password-reset")
    public Result<Void> resetPassword(@RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return Result.success();
    }

    /**
     * POST /api/auth/register
     * 注册前台采购端用户，成功后返回 JWT。
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        LoginResponse response = authService.register(request, getClientIp(servletRequest));
        return Result.success(response);
    }

    /**
     * DELETE /api/auth/sessions/current
     * 删除当前登录会话。当前 JWT 是无状态方案，主要由前端清理本地 token。
     */
    @DeleteMapping("/sessions/current")
    public Result<Void> logout() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        authService.logout(userId);
        return Result.success();
    }

    /**
     * POST /api/auth/logout
     * 兼容旧退出路径。
     */
    @PostMapping("/logout")
    public Result<Void> legacyLogout() {
        return logout();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
