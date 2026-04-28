CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码摘要',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `sys_login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '登录账号',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
    `login_type` VARCHAR(32) DEFAULT NULL COMMENT '登录方式',
    `login_ip` VARCHAR(64) DEFAULT NULL COMMENT '登录IP',
    `login_location` VARCHAR(128) DEFAULT NULL COMMENT '登录地点',
    `user_agent` VARCHAR(255) DEFAULT NULL COMMENT 'User-Agent',
    `browser` VARCHAR(64) DEFAULT NULL COMMENT '浏览器',
    `os` VARCHAR(64) DEFAULT NULL COMMENT '操作系统',
    `login_status` TINYINT NOT NULL DEFAULT 1 COMMENT '登录结果：1成功 0失败',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `login_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

INSERT INTO `sys_user` (`id`, `username`, `password_hash`, `real_name`, `status`, `deleted`)
VALUES (1, 'admin', '{noop}admin123', '系统管理员', 1, 0)
ON DUPLICATE KEY UPDATE
    `password_hash` = VALUES(`password_hash`),
    `real_name` = VALUES(`real_name`),
    `status` = VALUES(`status`),
    `deleted` = VALUES(`deleted`);
