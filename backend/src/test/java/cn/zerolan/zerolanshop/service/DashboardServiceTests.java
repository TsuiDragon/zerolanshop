package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.PurchaseDashboardResponse;
import cn.zerolan.zerolanshop.domain.entity.VirtualOrder;
import cn.zerolan.zerolanshop.mapper.VirtualOrderMapper;
import cn.zerolan.zerolanshop.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTests {

    private final VirtualOrderMapper virtualOrderMapper = mock(VirtualOrderMapper.class);
    private final DashboardService dashboardService = new DashboardServiceImpl(virtualOrderMapper);

    @Test
    void purchaseOverviewAggregatesTodayMonthAndSevenDayTrend() {
        LocalDate today = LocalDate.now();
        when(virtualOrderMapper.selectList(any())).thenReturn(List.of(
                order("10.00", today),
                order("15.50", today),
                order("8.00", today.minusDays(1)),
                order("6.25", today.minusDays(6))
        ));

        PurchaseDashboardResponse response = dashboardService.purchaseOverview(12L);

        assertThat(response.getTodayAmount()).isEqualByComparingTo(new BigDecimal("25.50"));
        assertThat(response.getTodayOrderCount()).isEqualTo(2);
        assertThat(response.getMonthAmount()).isGreaterThanOrEqualTo(new BigDecimal("25.50"));
        assertThat(response.getTrend()).hasSize(7);
        assertThat(response.getTrend().get(6).getDate()).isEqualTo(today.toString());
        assertThat(response.getTrend().get(6).getAmount()).isEqualByComparingTo(new BigDecimal("25.50"));
        assertThat(response.getTrend().get(6).getOrderCount()).isEqualTo(2);
    }

    private VirtualOrder order(String amount, LocalDate date) {
        VirtualOrder order = new VirtualOrder();
        order.setOrderAmount(new BigDecimal(amount));
        order.setCreatedAt(date.atTime(10, 30));
        return order;
    }
}
