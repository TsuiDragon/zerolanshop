-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `balance` DECIMAL(10,2) DEFAULT '0.00' COMMENT '余额',
    `wx_openid` VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID',
    `wx_unionid` VARCHAR(100) DEFAULT NULL COMMENT '微信UnionID',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT DEFAULT '1' COMMENT '状态：0-封禁，1-正常',
    `register_ip` VARCHAR(50) DEFAULT NULL COMMENT '注册IP',
    `api_key` VARCHAR(64) DEFAULT NULL COMMENT 'API密钥',
    `api_secret` VARCHAR(128) DEFAULT NULL COMMENT 'API密钥',
    `register_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_wx_openid` (`wx_openid`),
    KEY `idx_api_key` (`api_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- Admin table
CREATE TABLE IF NOT EXISTS `admin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Admin ID',
    `username` VARCHAR(50) NOT NULL COMMENT 'Login username',
    `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt password',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT 'Display name',
    `email` VARCHAR(100) DEFAULT NULL COMMENT 'Email',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT 'Phone',
    `role` VARCHAR(50) DEFAULT 'SUPER_ADMIN' COMMENT 'Admin role',
    `status` TINYINT DEFAULT '1' COMMENT '0-disabled, 1-enabled',
    `last_login_time` DATETIME DEFAULT NULL COMMENT 'Last login time',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `deleted` TINYINT DEFAULT '0' COMMENT '0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_username` (`username`),
    UNIQUE KEY `uk_admin_email` (`email`),
    UNIQUE KEY `uk_admin_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Admin table';

-- Product category table
CREATE TABLE IF NOT EXISTS `product_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Category ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Parent category ID, 0 means top level',
    `name` VARCHAR(50) NOT NULL COMMENT 'Category name',
    `icon` VARCHAR(500) DEFAULT NULL COMMENT 'Category icon URL',
    `sort` INT NOT NULL DEFAULT 0 COMMENT 'Sort value',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0-disabled, 1-enabled',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    KEY `idx_category_parent_name` (`parent_id`, `name`),
    KEY `idx_category_parent_sort` (`parent_id`, `sort`, `id`),
    KEY `idx_category_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product category table';
