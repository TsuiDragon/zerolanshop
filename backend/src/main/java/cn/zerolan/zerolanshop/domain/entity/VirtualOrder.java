package cn.zerolan.zerolanshop.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("virtual_order")
public class VirtualOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("product_id")
    private Long productId;

    @TableField("product_name")
    private String productName;

    @TableField("product_snapshot")
    private String productSnapshot;

    @TableField("quantity")
    private Integer quantity;

    @TableField("recharge_account")
    private String rechargeAccount;

    @TableField("order_amount")
    private BigDecimal orderAmount;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("source_ip")
    private String sourceIp;

    @TableField("status")
    private String status;

    @TableField("channel_id")
    private Long channelId;

    @TableField("channel_name")
    private String channelName;

    @TableField("channel_type")
    private String channelType;

    @TableField("channel_order_no")
    private String channelOrderNo;

    @TableField("exception_message")
    private String exceptionMessage;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField("processed_at")
    private LocalDateTime processedAt;

    @TableField("processing_duration_seconds")
    private Long processingDurationSeconds;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
