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
@TableName("user_balance_record")
public class UserBalanceRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("record_type")
    private String recordType;

    @TableField("before_balance")
    private BigDecimal beforeBalance;

    @TableField("change_amount")
    private BigDecimal changeAmount;

    @TableField("after_balance")
    private BigDecimal afterBalance;

    @TableField("order_no")
    private String orderNo;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
