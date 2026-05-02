package cn.zerolan.zerolanshop.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_template_field")
public class OrderTemplateField {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("template_id")
    private Long templateId;

    @TableField("field_type")
    private String fieldType;

    @TableField("field_name")
    private String fieldName;

    @TableField("placeholder")
    private String placeholder;

    @TableField("required")
    private Integer required;

    @TableField("sort")
    private Integer sort;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
