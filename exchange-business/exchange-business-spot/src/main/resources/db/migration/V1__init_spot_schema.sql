CREATE TABLE IF NOT EXISTS `spot_symbol` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `symbol` VARCHAR(32) NOT NULL COMMENT '交易对，例如 BTCUSDT',
    `base_asset` VARCHAR(20) NOT NULL COMMENT '基础币种，例如 BTC',
    `quote_asset` VARCHAR(20) NOT NULL COMMENT '计价币种，例如 USDT',
    `price_precision` INT NOT NULL DEFAULT 8 COMMENT '价格精度',
    `quantity_precision` INT NOT NULL DEFAULT 8 COMMENT '数量精度',
    `min_order_quantity` DECIMAL(36,18) NOT NULL DEFAULT 0 COMMENT '最小委托数量',
    `min_order_amount` DECIMAL(36,18) NOT NULL DEFAULT 0 COMMENT '最小委托金额',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-否，1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spot_symbol` (`symbol`),
    KEY `idx_spot_symbol_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='现货交易对表';

CREATE TABLE IF NOT EXISTS `spot_order` (
    `order_id` VARCHAR(32) NOT NULL COMMENT '订单ID，格式 SP_O_XXXXXXXX',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `symbol` VARCHAR(32) NOT NULL COMMENT '交易对',
    `base_asset` VARCHAR(32) NOT NULL COMMENT '基本单位',
    `quote_asset` VARCHAR(32) NOT NULL COMMENT '计价单位',
    `side` TINYINT NOT NULL COMMENT '买卖方向：1-买入，2-卖出',
    `order_type` TINYINT NOT NULL COMMENT '订单类型：1-限价单，2-市价单',
    `order_price` DECIMAL(36,18) DEFAULT NULL COMMENT '委托价格，市价单为0或NULL',
    `order_quantity` DECIMAL(36,18) NOT NULL COMMENT '委托数量',
    `filled_quantity` DECIMAL(36,18) NOT NULL DEFAULT 0 COMMENT '成交数量',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待成交，2-部分成交，3-完全成交，4-已取消，5-异常失败',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-否，1-是',
    PRIMARY KEY (`order_id`),
    KEY `idx_spot_order_user_time` (`user_id`, `create_time`),
    KEY `idx_spot_order_symbol_status` (`symbol`, `status`),
    KEY `idx_spot_order_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='现货订单表';

CREATE TABLE IF NOT EXISTS `spot_trade` (
    `trade_id` VARCHAR(32) NOT NULL COMMENT '成交记录ID，格式 SP_T_XXXXXXXX',
    `order_id` VARCHAR(32) NOT NULL COMMENT '关联订单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `symbol` VARCHAR(32) NOT NULL COMMENT '交易对',
    `base_asset` VARCHAR(32) NOT NULL COMMENT '基本单位',
    `quote_asset` VARCHAR(32) NOT NULL COMMENT '计价单位',
    `side` TINYINT NOT NULL COMMENT '买卖方向：1-买入，2-卖出',
    `trade_price` DECIMAL(36,18) NOT NULL COMMENT '实际成交价格',
    `trade_quantity` DECIMAL(36,18) NOT NULL COMMENT '成交数量',
    `trade_amount` DECIMAL(36,18) NOT NULL COMMENT '成交额，成交价格乘以成交数量',
    `fee` DECIMAL(36,18) NOT NULL DEFAULT 0 COMMENT '手续费',
    `fee_asset` VARCHAR(20) DEFAULT NULL COMMENT '手续费币种',
    `fee_usdt_value` DECIMAL(36,18) NOT NULL DEFAULT 0 COMMENT '手续费折合USDT价值',
    `market_price` DECIMAL(36,18) DEFAULT NULL COMMENT '成交时市场价格',
    `trade_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成交时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`trade_id`),
    KEY `idx_spot_trade_order` (`order_id`),
    KEY `idx_spot_trade_user_time` (`user_id`, `trade_time`),
    KEY `idx_spot_trade_symbol_time` (`symbol`, `trade_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='现货成交记录表';

CREATE TABLE IF NOT EXISTS `spot_account_flow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `asset` VARCHAR(20) NOT NULL COMMENT '币种',
    `change_amount` DECIMAL(36,18) NOT NULL COMMENT '变动金额，正数增加，负数减少',
    `balance_before` DECIMAL(36,18) NOT NULL COMMENT '变动前余额',
    `balance_after` DECIMAL(36,18) NOT NULL COMMENT '变动后余额',
    `biz_type` TINYINT NOT NULL COMMENT '业务类型：1-下单冻结，2-成交扣款，3-成交回款，4-手续费扣除，5-订单解冻',
    `biz_id` VARCHAR(32) NOT NULL COMMENT '关联业务ID，例如订单ID或成交ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_spot_flow_user_asset_time` (`user_id`, `asset`, `create_time`),
    KEY `idx_spot_flow_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='现货账户流水表';

INSERT INTO `spot_symbol`
    (`symbol`, `base_asset`, `quote_asset`, `price_precision`, `quantity_precision`, `status`, `sort`)
VALUES
    ('BTCUSDT', 'BTC', 'USDT', 8, 8, 1, 1),
    ('ETHUSDT', 'ETH', 'USDT', 8, 8, 1, 2),
    ('DOGEUSDT', 'DOGE', 'USDT', 8, 8, 1, 3)
ON DUPLICATE KEY UPDATE `symbol` = VALUES(`symbol`);
