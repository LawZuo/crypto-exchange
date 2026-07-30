create table if not exists `user_information` (
    `id` bigint auto_increment primary key comment '主键ID',
    `uid` varchar(32) not null comment '用户UID',
    `username` varchar(64) not null comment '账号',
    `name` varchar(64) comment '姓名',
    `email` varchar(128) not null comment '邮箱',
    `password` varchar(100) not null comment '登录密码',
    `trade_password` varchar(100) comment '交易密码',
    `status` int not null default 1 comment '用户状态：0-禁用，1-启用',
    `kyc_status` int not null default 0 comment 'KYC状态',
    `last_login_time` datetime comment '最近登录时间',
    `last_login_ip` varchar(100) comment '最近登录IP',
    `register_ip` varchar(100) comment '注册IP',
    `create_time` datetime not null default current_timestamp comment '创建时间',
    `update_time` datetime not null default current_timestamp on update current_timestamp comment '更新时间',
    `is_deleted` int not null default 0 comment '是否删除：0-未删除，1-已删除',
    unique key `uk_user_information_uid` (`uid`),
    unique key `uk_user_information_username` (`username`),
    unique key `uk_user_information_email` (`email`),
    key `idx_user_information_status` (`status`)
) comment '用户信息表';

create table if not exists `user_kyc_application` (
    `id` bigint auto_increment primary key comment '主键ID',
    `user_id` bigint not null comment '用户ID',
    `name` varchar(64) not null comment '姓名',
    `id_card` varchar(32) not null comment '身份证号',
    `id_card_front` varchar(255) not null comment '身份证正面',
    `id_card_back` varchar(255) not null comment '身份证反面',
    `status` int not null default 0 comment 'KYC状态',
    `create_time` datetime not null default current_timestamp comment '创建时间',
    `update_time` datetime not null default current_timestamp on update current_timestamp comment '更新时间',
    `is_deleted` int not null default 0 comment '是否删除：0-未删除，1-已删除',
    key `idx_user_kyc_application_user_id` (`user_id`),
    constraint `fk_kyc_user`
        foreign key (`user_id`)
        references `user_information` (`id`)
        on delete cascade
) comment '用户KYC申请表';

create table if not exists `user_login_log` (
    `id` bigint auto_increment primary key comment '主键ID',
    `user_id` bigint not null comment '用户ID',
    `ip_address` varchar(100) comment 'ip地址',
    `device_source` varchar(10) comment '设备来源: WEB,H5,Android,IOS,API',
    `device_info` varchar(10) comment '设备信息',
    `status` int comment '登录状态',
    `fail_reason` varchar(225) comment '失败原因',
    `create_time` datetime not null default current_timestamp comment '创建时间',
    `update_time` datetime not null default current_timestamp on update current_timestamp comment '更新时间',
    `is_deleted` int not null default 0 comment '是否删除：0-未删除，1-已删除',
    key `idx_user_login_log_user_id` (`user_id`),
    key `idx_user_login_log_create_time` (`create_time`),
    constraint `fk_logs_user`
        foreign key (`user_id`)
        references `user_information` (`id`)
        on delete cascade
) comment '用户登录日志';
