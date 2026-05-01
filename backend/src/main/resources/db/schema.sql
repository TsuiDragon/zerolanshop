-- 用户表：前台采购端用户账号
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt密码',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `balance` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
    `wx_openid` VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID',
    `wx_unionid` VARCHAR(100) DEFAULT NULL COMMENT '微信UnionID',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `register_ip` VARCHAR(50) DEFAULT NULL COMMENT '注册IP',
    `api_key` VARCHAR(64) DEFAULT NULL COMMENT 'API密钥',
    `api_secret` VARCHAR(128) DEFAULT NULL COMMENT 'API密钥',
    `register_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    UNIQUE KEY `uk_user_email` (`email`),
    UNIQUE KEY `uk_user_phone` (`phone`),
    UNIQUE KEY `uk_user_wx_openid` (`wx_openid`),
    KEY `idx_user_api_key` (`api_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 管理员表：后台运营端账号
CREATE TABLE IF NOT EXISTS `admin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt密码',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '显示名称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `role` VARCHAR(50) DEFAULT 'SUPER_ADMIN' COMMENT '管理员角色',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_username` (`username`),
    UNIQUE KEY `uk_admin_email` (`email`),
    UNIQUE KEY `uk_admin_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 商品分类表：目前约束为最多两级分类
CREATE TABLE IF NOT EXISTS `product_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(500) DEFAULT NULL COMMENT '分类图标或图片URL',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号，值越小越靠前',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_parent_name` (`parent_id`, `name`),
    KEY `idx_category_parent_sort` (`parent_id`, `sort`, `id`),
    KEY `idx_category_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 定价模板表：用于商品根据渠道成本价计算销售价
CREATE TABLE IF NOT EXISTS `pricing_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '定价模板ID',
    `name` VARCHAR(50) NOT NULL COMMENT '模板名称',
    `pricing_type` VARCHAR(20) NOT NULL COMMENT '定价方式：PERCENTAGE-百分比加价，FIXED_AMOUNT-固定金额加价',
    `pricing_value` DECIMAL(10,4) NOT NULL DEFAULT '0.0000' COMMENT '定价值：百分比或固定加价金额',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号，值越小越靠前',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pricing_template_name` (`name`),
    KEY `idx_pricing_template_type` (`pricing_type`),
    KEY `idx_pricing_template_status` (`status`),
    KEY `idx_pricing_template_sort` (`sort`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定价模板表';

-- 图片素材表：记录本地上传图片，便于复用和清理
CREATE TABLE IF NOT EXISTS `media_asset` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '素材ID',
    `scene` VARCHAR(20) NOT NULL COMMENT '使用场景：category-分类图标，product-商品图片',
    `url` VARCHAR(500) NOT NULL COMMENT '公开访问路径',
    `filename` VARCHAR(120) NOT NULL COMMENT '存储文件名',
    `original_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    `content_type` VARCHAR(100) NOT NULL COMMENT '文件MIME类型',
    `extension` VARCHAR(20) NOT NULL COMMENT '文件扩展名',
    `size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
    `width` INT DEFAULT NULL COMMENT '图片宽度',
    `height` INT DEFAULT NULL COMMENT '图片高度',
    `storage_path` VARCHAR(500) NOT NULL COMMENT '磁盘存储路径',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_media_asset_url` (`url`),
    KEY `idx_media_asset_scene` (`scene`),
    KEY `idx_media_asset_status` (`status`),
    KEY `idx_media_asset_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片素材表';
