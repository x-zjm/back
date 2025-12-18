-- 创建数据库
CREATE DATABASE IF NOT EXISTS nianji CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE nianji;

-- 用户表
CREATE TABLE `users` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
 `username` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
 `public_id` char(36) COLLATE utf8mb4_general_ci NOT NULL COMMENT '对外公开ID',
 `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
 `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '邮箱',
 `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号',
 `nickname` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '昵称',
 `avatar` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像',
 `status` tinyint(4) DEFAULT '1' COMMENT '状态: 0-禁用，1-正常，2-锁定',
 `last_login_time` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
 `last_login_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后登录的ip',
 `login_count` int(11) DEFAULT '0' COMMENT '登录次数',
 `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
 PRIMARY KEY (`id`),
 UNIQUE KEY `username` (`username`),
 KEY `idx_username` (`username`),
 KEY `idx_email` (`email`),
 KEY `idx_phone` (`phone`),
 KEY `idx_status` (`status`),
 KEY `idx_publicId` (`public_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 初始化管理员用户
INSERT INTO `users` (`username`, `public_id`, `password`, `email`, `phone`, `nickname`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `login_count`, `deleted`)
VALUES ('admin', '2e56b23e00345fdsf45hgm5446de4g5', '$2a$10$/VdY3x2HKt1iiCioHa9.C.BWJnyo1OKmDBbmhASA5M1385SSwT4oW', 'admin@nianji.com', NULL, '系统管理员', NULL, 1, NULL, NULL, 0, 0);

-- 登录日志表
CREATE TABLE `login_logs` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
 `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
 `username` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
 `login_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录IP',
 `login_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '登录地点',
 `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户代理',
 `login_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
 `login_status` tinyint(4) DEFAULT '1' COMMENT '登录状态: 0-失败, 1-成功',
 `fail_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '失败原因',
 `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 `deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
 PRIMARY KEY (`id`),
 KEY `idx_user_id` (`user_id`),
 KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='登录日志表';

