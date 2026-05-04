package cn.zerolan.zerolanshop.service;

import cn.zerolan.zerolanshop.domain.dto.AdminUserResponse;
import cn.zerolan.zerolanshop.domain.dto.UserBalanceAdjustRequest;
import cn.zerolan.zerolanshop.domain.dto.UserCreditUpdateRequest;
import cn.zerolan.zerolanshop.domain.dto.UserStatusRequest;

import java.util.List;

public interface AdminUserService {

    List<AdminUserResponse> list(String keyword, Integer status);

    AdminUserResponse detail(Long id);

    AdminUserResponse updateStatus(Long id, UserStatusRequest request);

    AdminUserResponse adjustBalance(Long id, UserBalanceAdjustRequest request);

    AdminUserResponse updateCredit(Long id, UserCreditUpdateRequest request);

}
