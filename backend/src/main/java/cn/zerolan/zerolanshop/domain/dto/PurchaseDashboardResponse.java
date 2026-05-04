package cn.zerolan.zerolanshop.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public class PurchaseDashboardResponse {

    private BigDecimal todayAmount;
    private long todayOrderCount;
    private BigDecimal monthAmount;
    private long monthOrderCount;
    private List<PurchaseTrendPoint> trend;

    public PurchaseDashboardResponse() {
    }

    public PurchaseDashboardResponse(
            BigDecimal todayAmount,
            long todayOrderCount,
            BigDecimal monthAmount,
            long monthOrderCount,
            List<PurchaseTrendPoint> trend
    ) {
        this.todayAmount = todayAmount;
        this.todayOrderCount = todayOrderCount;
        this.monthAmount = monthAmount;
        this.monthOrderCount = monthOrderCount;
        this.trend = trend;
    }

    public BigDecimal getTodayAmount() {
        return todayAmount;
    }

    public void setTodayAmount(BigDecimal todayAmount) {
        this.todayAmount = todayAmount;
    }

    public long getTodayOrderCount() {
        return todayOrderCount;
    }

    public void setTodayOrderCount(long todayOrderCount) {
        this.todayOrderCount = todayOrderCount;
    }

    public BigDecimal getMonthAmount() {
        return monthAmount;
    }

    public void setMonthAmount(BigDecimal monthAmount) {
        this.monthAmount = monthAmount;
    }

    public long getMonthOrderCount() {
        return monthOrderCount;
    }

    public void setMonthOrderCount(long monthOrderCount) {
        this.monthOrderCount = monthOrderCount;
    }

    public List<PurchaseTrendPoint> getTrend() {
        return trend;
    }

    public void setTrend(List<PurchaseTrendPoint> trend) {
        this.trend = trend;
    }

    public static class PurchaseTrendPoint {

        private String date;
        private BigDecimal amount;
        private long orderCount;

        public PurchaseTrendPoint() {
        }

        public PurchaseTrendPoint(String date, BigDecimal amount, long orderCount) {
            this.date = date;
            this.amount = amount;
            this.orderCount = orderCount;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public long getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(long orderCount) {
            this.orderCount = orderCount;
        }
    }
}
