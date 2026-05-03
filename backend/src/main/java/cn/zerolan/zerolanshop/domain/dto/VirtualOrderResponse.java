package cn.zerolan.zerolanshop.domain.dto;

import cn.zerolan.zerolanshop.domain.entity.VirtualOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VirtualOrderResponse {
    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private Long productId;
    private String productName;
    private String productSnapshot;
    private Integer quantity;
    private String rechargeAccount;
    private BigDecimal orderAmount;
    private String paymentMethod;
    private String sourceIp;
    private String status;
    private Long channelId;
    private String channelName;
    private String channelType;
    private String channelOrderNo;
    private String exceptionMessage;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private Long processingDurationSeconds;
    private LocalDateTime updateTime;

    public static VirtualOrderResponse from(VirtualOrder order) {
        VirtualOrderResponse response = new VirtualOrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setUserId(order.getUserId());
        response.setUsername(order.getUsername());
        response.setProductId(order.getProductId());
        response.setProductName(order.getProductName());
        response.setProductSnapshot(order.getProductSnapshot());
        response.setQuantity(order.getQuantity());
        response.setRechargeAccount(order.getRechargeAccount());
        response.setOrderAmount(order.getOrderAmount());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setSourceIp(order.getSourceIp());
        response.setStatus(order.getStatus());
        response.setChannelId(order.getChannelId());
        response.setChannelName(order.getChannelName());
        response.setChannelType(order.getChannelType());
        response.setChannelOrderNo(order.getChannelOrderNo());
        response.setExceptionMessage(order.getExceptionMessage());
        response.setCreatedAt(order.getCreatedAt());
        response.setProcessedAt(order.getProcessedAt());
        response.setProcessingDurationSeconds(order.getProcessingDurationSeconds());
        response.setUpdateTime(order.getUpdateTime());
        return response;
    }
}
