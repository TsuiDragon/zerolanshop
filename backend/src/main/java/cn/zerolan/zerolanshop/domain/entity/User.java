package cn.zerolan.zerolanshop.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("balance")
    private BigDecimal balance;

    @TableField("wx_openid")
    private String wxOpenid;

    @TableField("wx_unionid")
    private String wxUnionid;

    @TableField("nickname")
    private String nickname;

    @TableField("avatar")
    private String avatar;

    @TableField("status")
    private Integer status;

    @TableField("register_ip")
    private String registerIp;

    @TableField("api_key")
    private String apiKey;

    @TableField("api_secret")
    private String apiSecret;

    @TableField(value = "register_time", fill = FieldFill.INSERT)
    private LocalDateTime registerTime;

    @TableField(value = "last_login_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastLoginTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
