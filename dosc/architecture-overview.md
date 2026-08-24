# Crypto Exchange 项目架构说明

## 项目定位

`crypto-exchange` 是一个基于 Spring Boot / Spring Cloud 的加密货币交易所后端雏形项目。当前代码重点覆盖了网关、认证、用户业务、公共基础能力和跨模块 API 契约。

项目采用 Maven 多模块结构，服务间通过 HTTP / OpenFeign 通信。当前本地开发环境没有启用 Nacos 服务发现，Feign 和 Gateway 路由主要使用固定地址。

异步任务通过 RabbitMQ 承载，公共封装位于 `exchange-common-rabbitmq`。当前邮件验证码发送已经接入消息队列，生产者将邮件任务投递到 RabbitMQ，消费者完成邮件发送并使用 Redis 做消息幂等去重。

## 整体架构

```text
Client
  |
  v
exchange-gateway
  - 路由转发
  - CORS
  - 真实 IP 透传
  - JWT + Redis 登录态校验
  - 向下游透传 user_id / username / Authorization
  |
  +--> exchange-auth
  |      - 登录
  |      - 注册转发
  |      - JWT 生成
  |      - Redis 缓存 token
  |      - 记录登录 IP
  |
  +--> exchange-business-user
         - 用户查询
         - 用户注册
         - KYC 业务
         - 登录记录落库
  |
  +--> exchange-resource-upload
  |      - KYC 图片上传
  |      - 用户头像上传
  |
  +--> exchange-resource-mail
         - 邮件验证码
         - RabbitMQ 异步邮件任务消费
```

公共模块提供复用能力：

```text
exchange-api
  对外/服务间 API 契约、DTO、VO、Feign Client

exchange-common
  通用响应、状态码、JWT、IP、Redis、RabbitMQ、安全上下文、文件日志、幂等提交等

exchange-parent
  Maven 父工程，统一版本和插件
```

## 目录结构

```text
crypto-exchange/
  pom.xml                         根聚合 POM
  dosc/                           项目文档和排障记录
  exchange-parent/                Maven 父工程，统一依赖版本
  exchange-common/                公共能力聚合模块
    exchange-common-core/         通用核心工具、响应、状态码、JWT、上下文、公共日志配置
    exchange-common-redis/        Redis 配置和 RedisService
    exchange-common-rabbitmq/     RabbitMQ 自动配置、消息包装、队列/交换机声明
    exchange-common-security/     MVC 服务端请求头上下文拦截器、幂等提交
    exchange-common-doc/          OpenAPI 接口文档公共配置
    exchange-common-web/          Web 公共异常处理、Jackson 时间格式配置
  exchange-api/                   API 契约聚合模块
    exchange-api-user/            用户 DTO / VO / Feign Client
  exchange-gateway/               Spring Cloud Gateway 网关服务
  exchange-auth/                  认证服务
  exchange-business/              业务聚合模块
    exchange-business-user/       用户业务服务
  exchange-module/                可独立部署的通用业务能力聚合模块
  exchange-resource/              资源服务聚合模块
    exchange-resource-mail/       邮件发送资源模块，消费 RabbitMQ 邮件任务
    exchange-resource-upload/     本地图片上传服务
```

## 技术栈

| 分类 | 技术 |
|---|---|
| JDK | Java 17 |
| 框架 | Spring Boot 3.2.9 |
| 微服务 | Spring Cloud 2023.0.3 |
| Alibaba Cloud | Spring Cloud Alibaba 2023.0.3.2 |
| 网关 | Spring Cloud Gateway |
| 服务调用 | Spring Cloud OpenFeign |
| 负载均衡 | Spring Cloud LoadBalancer |
| 数据访问 | MyBatis-Plus 3.5.7 |
| 动态数据源 | dynamic-datasource-spring-boot3-starter 4.3.0 |
| 数据库迁移 | Flyway |
| 数据库连接池 | Druid 1.2.22 |
| 数据库 | MySQL |
| 缓存 | Spring Data Redis |
| Redis 客户端 | Spring Data Redis / Lettuce |
| 消息队列 | RabbitMQ / Spring AMQP |
| 邮件 | Spring Boot Starter Mail |
| 模板引擎 | Thymeleaf |
| 文件处理 | Commons IO |
| JWT | jjwt 0.12.6 |
| 工具库 | Hutool 5.8.20 |
| 线程上下文 | TransmittableThreadLocal |
| 代码简化 | Lombok |
| 构建 | Maven |

## 工程化公共能力

### 文件日志

各可启动服务通过 `logback-spring.xml` 引入 common-core 的公共日志配置。

默认本地日志目录：

```text
./log
```

日志文件按服务名区分：

```text
log/{spring.application.name}.log
log/{spring.application.name}-error.log
```

可通过环境变量覆盖：

```bash
LOG_PATH=/data/logs/crypto-exchange LOG_LEVEL=INFO java -jar exchange-auth-1.0.jar
```

### 幂等提交

业务写接口可使用 `@Idempotent` 防止短时间重复提交。底层通过 Redis `setIfAbsent` 原子占位实现。

当前已接入：

```text
POST /auth/register
POST /user/register
POST /user-kyc
PUT /user-kyc/{id}
DELETE /user-kyc/{id}
PUT /user-kyc/{id}/{status}
```

详细说明见：

```text
dosc/logging-and-idempotent.md
```

### 接口文档

项目使用 `springdoc-openapi` 生成接口文档，公共 OpenAPI 配置位于 `exchange-common-doc`，gateway 聚合 auth 和 user 服务文档。

本地统一入口：

```text
http://localhost/swagger-ui.html
```

详细说明见：

```text
dosc/api-docs.md
```

### RabbitMQ 消息封装

`exchange-common-rabbitmq` 提供 Spring Boot 自动配置：

- JSON 消息转换器，支持 Java Time 类型
- `RabbitTemplate` 发送确认和 return 回调
- 根据 `exchange.rabbitmq.queues` 动态声明交换机、队列、绑定和死信队列
- `MqMessage<T>` 标准消息体，包含 `messageId`、`bizType`、`payload`、`timestamp`
- `MqMessageService` 统一包装消息并携带 `CorrelationData`

当前邮件服务使用 RabbitMQ 异步发送邮件，消费者用 Redis key 做幂等去重：

```text
mq:dedup:{messageId}
```

## 请求链路

### 登录链路

```text
POST /api/crypto-exchange/auth/login
  -> gateway auth-route
  -> exchange-auth /auth/login
  -> auth 通过 Feign 查询 exchange-business-user /user/{username}
  -> auth 生成 JWT
  -> auth 缓存 Redis key: user:login:{token}
  -> auth 调用 /user/login-record 记录 last_login_ip / last_login_time
```

### 业务鉴权链路

```text
Client 带 Authorization
  -> exchange-gateway AuthFilter
     - 解析 JWT
     - 校验 Redis token 是否存在
     - 写入 user_id / username / Authorization 请求头
  -> exchange-business-user
  -> exchange-common-security HeaderInterceptor
     - 读取 gateway 透传请求头
     - 写入 SecurityContextHolder
```

业务代码可以通过：

```java
SecurityUtils.getUserId();
SecurityUtils.getUsername();
SecurityUtils.getLoginUser();
```

获取当前用户上下文。

### 邮件验证码链路

```text
POST /email/verification/code
  -> exchange-resource-mail 生成验证码和邮件内容
  -> MqMessageService 包装 MqMessage
  -> RabbitTemplate 按 MqConstants 投递邮件消息
  -> @RabbitListener 消费邮件任务
  -> Redis SETNX: mq:dedup:{messageId}
  -> JavaMailSender 发送邮件
  -> 手动 ACK；异常时 NACK 并进入死信队列
```

当前邮件 MQ 常量：

```text
MqConstants.EMAIL_SEND_TYPE = EMAIL_SEND
MqConstants.EMAIL_SEND_NAME = exchange.email.send
MqConstants.EMAIL_SEND_KEY  = queue.email.send
```

RabbitMQ 队列声明通过业务服务配置。配置中的队列名、交换机和 routing key 需要与生产者发送参数、`@RabbitListener` 监听队列保持一致：

```yaml
exchange:
  rabbitmq:
    queues:
      - name: ${email.queue.name}
        exchange: ${email.exchange.name}
        routing-key: ${email.routing-key}
        dlx-exchange: exchange.notification.dlx
        dlx-routing-key: email.send.dlx
        dlx-queue: queue.email.send.dlx
```

## 端口和路由

| 服务 | 默认端口 | 说明 |
|---|---:|---|
| exchange-gateway | 80 | 统一入口 |
| exchange-auth | 8080 | 认证服务 |
| exchange-business-user | 8081 | 用户业务服务 |
| exchange-resource-mail | 8078 | 邮件服务 |
| exchange-resource-upload | 8079 | 图片上传服务 |

Gateway 路由：

```text
/api/crypto-exchange/auth/** -> http://localhost:8080
/api/crypto-exchange/user/** -> http://localhost:8081
/api/crypto-exchange/upload/** -> http://localhost:8079
/api/crypto-exchange/files/** -> http://localhost:8079
```

当前 `application-route.yml` 使用固定 URL。后续如果启用 Nacos，可切换为服务名路由。

## 配置说明

当前配置按 profile 拆分：

```text
application.yml
application-route.yml
application-redis.yml
application-db.yml
application-feign.yml
application-mail.yml
application-mq.yml
```

已知需要进一步外置的敏感项：

- Redis host / port / password
- RabbitMQ host / port / username / password / virtual-host
- MySQL url / username / password
- Mail host / username / password
- JWT secret
- Druid 监控密码

## 当前边界

目前项目更接近“可运行的微服务骨架”，核心交易域能力还没有展开，例如：

- 账户资产
- 充值提现
- 订单撮合
- 行情
- 风控
- 管理后台
- 权限模型
- 消息可靠投递表和失败重试补偿
- 对象存储或 CDN 文件资源服务

后续新增业务时建议继续保持：

```text
API 契约放 exchange-api
公共能力放 exchange-common
业务实现放 exchange-business
入口控制放 exchange-gateway
认证登录放 exchange-auth
```
