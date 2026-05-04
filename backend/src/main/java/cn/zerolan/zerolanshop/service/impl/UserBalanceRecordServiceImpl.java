package cn.zerolan.zerolanshop.service.impl;

import cn.zerolan.zerolanshop.service.*;

import cn.zerolan.zerolanshop.domain.dto.UserBalanceRecordResponse;
import cn.zerolan.zerolanshop.domain.entity.User;
import cn.zerolan.zerolanshop.domain.entity.UserBalanceRecord;
import cn.zerolan.zerolanshop.mapper.UserBalanceRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class UserBalanceRecordServiceImpl implements UserBalanceRecordService {

    public static final String TYPE_PURCHASE = "PURCHASE";
    public static final String TYPE_ORDER_REFUND = "ORDER_REFUND";
    public static final String TYPE_ADMIN_ADD = "ADMIN_ADD";
    public static final String TYPE_ADMIN_DEDUCT = "ADMIN_DEDUCT";
    public static final String TYPE_SELF_RECHARGE = "SELF_RECHARGE";

    private static final List<String> SUPPORTED_TYPES = List.of(
            TYPE_PURCHASE,
            TYPE_ORDER_REFUND,
            TYPE_ADMIN_ADD,
            TYPE_ADMIN_DEDUCT,
            TYPE_SELF_RECHARGE
    );

    private final UserBalanceRecordMapper userBalanceRecordMapper;

    public UserBalanceRecordServiceImpl(UserBalanceRecordMapper userBalanceRecordMapper) {
        this.userBalanceRecordMapper = userBalanceRecordMapper;
    }

    public void record(User user, String type, BigDecimal beforeBalance, BigDecimal changeAmount, BigDecimal afterBalance, String orderNo, String remark) {
        if (user == null || user.getId() == null) {
            throw new RuntimeException("余额记录用户不能为空");
        }
        UserBalanceRecord record = new UserBalanceRecord();
        record.setUserId(user.getId());
        record.setUsername(StringUtils.hasText(user.getUsername()) ? user.getUsername().trim() : "");
        record.setRecordType(normalizeType(type));
        record.setBeforeBalance(scale(beforeBalance));
        record.setChangeAmount(scale(changeAmount));
        record.setAfterBalance(scale(afterBalance));
        record.setOrderNo(normalizeNullable(orderNo, 40));
        record.setRemark(normalizeNullable(remark, 255));
        record.setCreateTime(LocalDateTime.now());
        userBalanceRecordMapper.insert(record);
    }

    public List<UserBalanceRecordResponse> listForUser(Long userId, String recordType, String orderNo, LocalDateTime startTime, LocalDateTime endTime) {
        if (userId == null || userId <= 0) {
            throw new RuntimeException("用户 ID 不能为空");
        }
        QueryWrapper<UserBalanceRecord> wrapper = buildListWrapper(recordType, orderNo, null, startTime, endTime);
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time", "id");
        return userBalanceRecordMapper.selectList(wrapper).stream().map(UserBalanceRecordResponse::from).toList();
    }

    public List<UserBalanceRecordResponse> listForAdmin(
            String recordType,
            String orderNo,
            String userKeyword,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        QueryWrapper<UserBalanceRecord> wrapper = buildListWrapper(recordType, orderNo, userKeyword, startTime, endTime);
        wrapper.orderByDesc("create_time", "id");
        return userBalanceRecordMapper.selectList(wrapper).stream().map(UserBalanceRecordResponse::from).toList();
    }

    private QueryWrapper<UserBalanceRecord> buildListWrapper(
            String recordType,
            String orderNo,
            String userKeyword,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        QueryWrapper<UserBalanceRecord> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(recordType)) {
            wrapper.eq("record_type", normalizeType(recordType));
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like("order_no", orderNo.trim());
        }
        if (StringUtils.hasText(userKeyword)) {
            String keyword = userKeyword.trim();
            wrapper.and(query -> query.like("username", keyword).or().eq(isLong(keyword), "user_id", parseLong(keyword)));
        }
        if (startTime != null) {
            wrapper.ge("create_time", startTime);
        }
        if (endTime != null) {
            wrapper.le("create_time", endTime);
        }
        return wrapper;
    }

    private String normalizeType(String type) {
        String normalized = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_TYPES.contains(normalized)) {
            throw new RuntimeException("不支持的余额记录类型");
        }
        return normalized;
    }

    private BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeNullable(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        return normalized.length() > maxLength ? normalized.substring(0, maxLength) : normalized;
    }

    private boolean isLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private Long parseLong(String value) {
        return isLong(value) ? Long.parseLong(value) : null;
    }
}
