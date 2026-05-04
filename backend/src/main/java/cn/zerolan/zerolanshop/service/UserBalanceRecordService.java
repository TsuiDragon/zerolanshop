package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.UserBalanceRecordResponse;
import cn.zerolan.zerolanshop.domain.entity.User;
import cn.zerolan.zerolanshop.domain.entity.UserBalanceRecord;
import cn.zerolan.zerolanshop.mapper.UserBalanceRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public interface UserBalanceRecordService {

    String TYPE_PURCHASE = "PURCHASE";
    String TYPE_ORDER_REFUND = "ORDER_REFUND";
    String TYPE_ADMIN_ADD = "ADMIN_ADD";
    String TYPE_ADMIN_DEDUCT = "ADMIN_DEDUCT";
    String TYPE_SELF_RECHARGE = "SELF_RECHARGE";

    void record(User user, String type, BigDecimal beforeBalance, BigDecimal changeAmount, BigDecimal afterBalance, String orderNo, String remark);

    List<UserBalanceRecordResponse> listForUser(Long userId, String recordType, String orderNo, LocalDateTime startTime, LocalDateTime endTime);

    List<UserBalanceRecordResponse> listForAdmin( String recordType, String orderNo, String userKeyword, LocalDateTime startTime, LocalDateTime endTime );

}
