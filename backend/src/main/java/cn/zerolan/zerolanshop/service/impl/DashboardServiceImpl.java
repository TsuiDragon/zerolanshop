package cn.zerolan.zerolanshop.service.impl;

import cn.zerolan.zerolanshop.domain.dto.PurchaseDashboardResponse;
import cn.zerolan.zerolanshop.domain.dto.PurchaseDashboardResponse.PurchaseTrendPoint;
import cn.zerolan.zerolanshop.domain.entity.VirtualOrder;
import cn.zerolan.zerolanshop.mapper.VirtualOrderMapper;
import cn.zerolan.zerolanshop.service.DashboardService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final VirtualOrderMapper virtualOrderMapper;

    public DashboardServiceImpl(VirtualOrderMapper virtualOrderMapper) {
        this.virtualOrderMapper = virtualOrderMapper;
    }

    @Override
    public PurchaseDashboardResponse purchaseOverview(Long userId) {
        if (userId == null || userId <= 0) {
            throw new RuntimeException("用户 ID 不能为空");
        }

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate trendStart = today.minusDays(6);
        LocalDate queryStart = monthStart.isBefore(trendStart) ? monthStart : trendStart;
        LocalDateTime queryStartTime = queryStart.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        List<VirtualOrder> orders = virtualOrderMapper.selectList(new QueryWrapper<VirtualOrder>()
                .select("order_amount", "created_at")
                .eq("user_id", userId)
                .ge("created_at", queryStartTime)
                .lt("created_at", tomorrowStart));

        Map<LocalDate, DailyPurchaseStats> dailyStats = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            dailyStats.put(today.minusDays(i), new DailyPurchaseStats());
        }

        BigDecimal todayAmount = BigDecimal.ZERO;
        long todayOrderCount = 0;
        BigDecimal monthAmount = BigDecimal.ZERO;
        long monthOrderCount = 0;

        for (VirtualOrder order : orders) {
            if (order.getCreatedAt() == null) {
                continue;
            }
            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            BigDecimal amount = money(order.getOrderAmount());

            if (orderDate.equals(today)) {
                todayAmount = todayAmount.add(amount);
                todayOrderCount++;
            }
            if (!orderDate.isBefore(monthStart) && !orderDate.isAfter(today)) {
                monthAmount = monthAmount.add(amount);
                monthOrderCount++;
            }

            DailyPurchaseStats stats = dailyStats.get(orderDate);
            if (stats != null) {
                stats.add(amount);
            }
        }

        List<PurchaseTrendPoint> trend = dailyStats.entrySet().stream()
                .map(entry -> new PurchaseTrendPoint(
                        entry.getKey().format(DATE_FORMATTER),
                        entry.getValue().amount(),
                        entry.getValue().orderCount()
                ))
                .toList();

        return new PurchaseDashboardResponse(
                money(todayAmount),
                todayOrderCount,
                money(monthAmount),
                monthOrderCount,
                trend
        );
    }

    private BigDecimal money(BigDecimal amount) {
        return (amount == null ? BigDecimal.ZERO : amount).setScale(2, RoundingMode.HALF_UP);
    }

    private static class DailyPurchaseStats {

        private BigDecimal amount = BigDecimal.ZERO;
        private long orderCount;

        void add(BigDecimal value) {
            amount = amount.add(value);
            orderCount++;
        }

        BigDecimal amount() {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }

        long orderCount() {
            return orderCount;
        }
    }
}
