package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.PurchaseDashboardResponse;

public interface DashboardService {

    PurchaseDashboardResponse purchaseOverview(Long userId);
}
