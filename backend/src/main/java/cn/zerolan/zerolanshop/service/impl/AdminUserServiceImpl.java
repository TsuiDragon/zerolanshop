package cn.zerolan.zerolanshop.service.impl;

import cn.zerolan.zerolanshop.service.*;

import cn.zerolan.zerolanshop.domain.dto.AdminUserResponse;
import cn.zerolan.zerolanshop.domain.dto.UserBalanceAdjustRequest;
import cn.zerolan.zerolanshop.domain.dto.UserCreditUpdateRequest;
import cn.zerolan.zerolanshop.domain.dto.UserStatusRequest;
import cn.zerolan.zerolanshop.domain.entity.User;
import cn.zerolan.zerolanshop.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_ENABLED = 1;

    private final UserMapper userMapper;
    private final UserBalanceRecordService userBalanceRecordService;

    public AdminUserServiceImpl(UserMapper userMapper, UserBalanceRecordService userBalanceRecordService) {
        this.userMapper = userMapper;
        this.userBalanceRecordService = userBalanceRecordService;
    }

    public List<AdminUserResponse> list(String keyword, Integer status) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String normalizedKeyword = keyword.trim();
            wrapper.and(query -> query
                    .like("username", normalizedKeyword)
                    .or()
                    .like("nickname", normalizedKeyword)
                    .or()
                    .like("email", normalizedKeyword)
                    .or()
                    .like("phone", normalizedKeyword)
            );
        }
        if (status != null) {
            validateStatus(status);
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("register_time").orderByDesc("id");
        return userMapper.selectList(wrapper).stream().map(AdminUserResponse::from).toList();
    }

    public AdminUserResponse detail(Long id) {
        return AdminUserResponse.from(getExistingUser(id));
    }

    public AdminUserResponse updateStatus(Long id, UserStatusRequest request) {
        if (request == null) {
            throw new RuntimeException("用户状态请求不能为空");
        }
        User user = getExistingUser(id);
        validateStatus(request.getStatus());
        user.setStatus(request.getStatus());
        userMapper.updateById(user);
        return AdminUserResponse.from(user);
    }

    @Transactional
    public AdminUserResponse adjustBalance(Long id, UserBalanceAdjustRequest request) {
        if (request == null || request.getAmount() == null) {
            throw new RuntimeException("余额变动金额不能为空");
        }
        if (!StringUtils.hasText(request.getRemark())) {
            throw new RuntimeException("余额变动备注不能为空");
        }
        BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("余额变动金额不能为 0");
        }
        User user = getExistingUserForUpdate(id);
        BigDecimal currentBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        BigDecimal nextBalance = currentBalance.add(amount).setScale(2, RoundingMode.HALF_UP);
        if (nextBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("余额不能为负数");
        }
        user.setBalance(nextBalance);
        userMapper.updateById(user);
        userBalanceRecordService.record(
                user,
                amount.compareTo(BigDecimal.ZERO) > 0 ? UserBalanceRecordService.TYPE_ADMIN_ADD : UserBalanceRecordService.TYPE_ADMIN_DEDUCT,
                currentBalance,
                amount,
                nextBalance,
                null,
                request.getRemark()
        );
        return AdminUserResponse.from(user);
    }

    public AdminUserResponse updateCredit(Long id, UserCreditUpdateRequest request) {
        if (request == null) {
            throw new RuntimeException("授信信息不能为空");
        }
        BigDecimal creditLimit = normalizeNonNegativeAmount(request.getCreditLimit(), "授信额度不能为空");
        BigDecimal securityDeposit = normalizeNonNegativeAmount(request.getSecurityDeposit(), "保证金不能为空");
        if (!StringUtils.hasText(request.getCreditRating())) {
            throw new RuntimeException("授信评级不能为空");
        }
        String creditRating = request.getCreditRating().trim();
        if (creditRating.length() > 20) {
            throw new RuntimeException("授信评级不能超过 20 个字符");
        }
        User user = getExistingUser(id);
        user.setCreditLimit(creditLimit);
        user.setSecurityDeposit(securityDeposit);
        user.setCreditRating(creditRating);
        userMapper.updateById(user);
        return AdminUserResponse.from(user);
    }

    private User getExistingUser(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("用户 ID 不能为空");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    private User getExistingUserForUpdate(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("用户 ID 不能为空");
        }
        User user = userMapper.selectByIdForUpdate(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    private BigDecimal normalizeNonNegativeAmount(BigDecimal amount, String emptyMessage) {
        if (amount == null) {
            throw new RuntimeException(emptyMessage);
        }
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("金额不能为负数");
        }
        return normalized;
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != STATUS_DISABLED && status != STATUS_ENABLED)) {
            throw new RuntimeException("状态只能为 0 或 1");
        }
    }
}
