package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.AdminLoginRequest;
import cn.zerolan.zerolanshop.domain.dto.AdminLoginResponse;
import cn.zerolan.zerolanshop.service.AdminAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * POST /api/admin/login
     * 兼容旧登录路径。新代码优先使用 POST /api/admin/sessions。
     */
    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        return createSession(request);
    }

    /**
     * POST /api/admin/sessions
     * 创建后台运营端登录会话，成功后返回 JWT。
     */
    @PostMapping("/sessions")
    public Result<AdminLoginResponse> createSession(@RequestBody AdminLoginRequest request) {
        return Result.success(adminAuthService.login(request));
    }
}
