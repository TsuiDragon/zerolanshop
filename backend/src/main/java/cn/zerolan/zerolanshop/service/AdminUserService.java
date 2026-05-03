package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.AdminUserResponse;
import cn.zerolan.zerolanshop.domain.dto.UserBalanceAdjustRequest;
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
public class AdminUserService {

    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_ENABLED = 1;

    private final UserMapper userMapper;

    public AdminUserService(UserMapper userMapper) {
        this.userMapper = userMapper;
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
            throw new RuntimeException("User status request is required");
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
            throw new RuntimeException("Balance amount is required");
        }
        BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("Balance amount cannot be zero");
        }
        User user = getExistingUser(id);
        BigDecimal currentBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        BigDecimal nextBalance = currentBalance.add(amount).setScale(2, RoundingMode.HALF_UP);
        if (nextBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Balance cannot be negative");
        }
        user.setBalance(nextBalance);
        userMapper.updateById(user);
        return AdminUserResponse.from(user);
    }

    private User getExistingUser(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("User ID is required");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("User does not exist");
        }
        return user;
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != STATUS_DISABLED && status != STATUS_ENABLED)) {
            throw new RuntimeException("Status must be 0 or 1");
        }
    }
}
