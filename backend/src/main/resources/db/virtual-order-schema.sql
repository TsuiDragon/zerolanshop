-- Virtual order extension schema.
SET @product_supply_binding_active_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product_supply_binding'
      AND COLUMN_NAME = 'active'
);
SET @product_supply_binding_active_sql := IF(
    @product_supply_binding_active_exists = 0,
    'ALTER TABLE `product_supply_binding` ADD COLUMN `active` TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否生效：0-否，1-是'' AFTER `channel_cost_price`',
    'SELECT 1'
);
PREPARE product_supply_binding_active_stmt FROM @product_supply_binding_active_sql;
EXECUTE product_supply_binding_active_stmt;
DEALLOCATE PREPARE product_supply_binding_active_stmt;

CREATE TABLE IF NOT EXISTS `virtual_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(40) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '下单用户名',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称快照',
    `product_snapshot` JSON DEFAULT NULL COMMENT '商品冗余信息快照',
    `quantity` INT NOT NULL COMMENT '下单数量',
    `recharge_account` TEXT NOT NULL COMMENT '充值账号',
    `order_amount` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
    `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式',
    `source_ip` VARCHAR(50) DEFAULT NULL COMMENT '来源IP',
    `status` VARCHAR(20) NOT NULL COMMENT '订单状态',
    `channel_id` BIGINT DEFAULT NULL COMMENT '供货渠道ID',
    `channel_name` VARCHAR(50) DEFAULT NULL COMMENT '供货渠道名称',
    `channel_type` VARCHAR(20) DEFAULT NULL COMMENT '供货渠道类型',
    `channel_order_no` VARCHAR(100) DEFAULT NULL COMMENT '外部/渠道订单号',
    `exception_message` VARCHAR(500) DEFAULT NULL COMMENT '异常信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `processed_at` DATETIME DEFAULT NULL COMMENT '处理时间',
    `processing_duration_seconds` BIGINT DEFAULT NULL COMMENT '处理耗时秒数',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_virtual_order_no` (`order_no`),
    KEY `idx_virtual_order_user` (`user_id`, `created_at`),
    KEY `idx_virtual_order_product` (`product_id`),
    KEY `idx_virtual_order_status` (`status`),
    KEY `idx_virtual_order_channel_order_no` (`channel_order_no`),
    KEY `idx_virtual_order_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='虚拟商品订单表';
