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
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("product_type")
    private String productType;

    @TableField("category_id")
    private Long categoryId;

    @TableField("name")
    private String name;

    @TableField("cost_price")
    private BigDecimal costPrice;

    @TableField("sale_price")
    private BigDecimal salePrice;

    @TableField("supply_cost_strategy")
    private String supplyCostStrategy;

    @TableField("pricing_template_id")
    private Long pricingTemplateId;

    @TableField("image")
    private String image;

    @TableField("face_value")
    private BigDecimal faceValue;

    @TableField("order_template_id")
    private Long orderTemplateId;

    @TableField("min_purchase_quantity")
    private Integer minPurchaseQuantity;

    @TableField("max_purchase_quantity")
    private Integer maxPurchaseQuantity;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
