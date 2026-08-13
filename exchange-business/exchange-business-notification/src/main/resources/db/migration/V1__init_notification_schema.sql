CREATE TABLE IF NOT EXISTS `notification_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `event_id` VARCHAR(128) NOT NULL COMMENT '来源事件ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `event_type` VARCHAR(64) NOT NULL COMMENT '事件类型',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` VARCHAR(1000) DEFAULT NULL COMMENT '内容',
    `payload` JSON DEFAULT NULL COMMENT '扩展载荷',
    `read_status` INT NOT NULL DEFAULT 0 COMMENT '已读状态：0-未读，1-已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '已读时间',
    `occurred_at` DATETIME DEFAULT NULL COMMENT '业务发生时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_notification_message_event` (`event_id`) USING BTREE,
    KEY `idx_notification_message_user_read` (`user_id`, `read_status`, `create_time`) USING BTREE,
    KEY `idx_notification_message_user_time` (`user_id`, `create_time`) USING BTREE
) COMMENT='用户私有通知表';

CREATE TABLE IF NOT EXISTS `notification_announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `event_id` VARCHAR(128) NOT NULL COMMENT '来源事件ID',
    `event_type` VARCHAR(64) NOT NULL COMMENT '事件类型',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` VARCHAR(1000) DEFAULT NULL COMMENT '内容',
    `payload` JSON DEFAULT NULL COMMENT '扩展载荷',
    `occurred_at` DATETIME DEFAULT NULL COMMENT '业务发生时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_notification_announcement_event` (`event_id`) USING BTREE,
    KEY `idx_notification_announcement_time` (`create_time`) USING BTREE
) COMMENT='系统公告表';

CREATE TABLE IF NOT EXISTS `notification_read` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '已读记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_type` VARCHAR(16) NOT NULL COMMENT '通知目标类型：USER/ALL',
    `target_id` BIGINT NOT NULL COMMENT '通知或公告ID',
    `read_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '已读时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_notification_read_user_target` (`user_id`, `target_type`, `target_id`) USING BTREE,
    KEY `idx_notification_read_user` (`user_id`) USING BTREE
) COMMENT='通知已读记录表';
