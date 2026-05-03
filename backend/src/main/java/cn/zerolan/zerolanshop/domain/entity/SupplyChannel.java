package cn.zerolan.zerolanshop.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("supply_channel")
public class SupplyChannel {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("channel_type")
    private String channelType;

    @TableField("name")
    private String name;

    @TableField("api_url")
    private String apiUrl;

    @TableField("user_id")
    private String userId;

    @TableField("secret_key")
    private String secretKey;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
