# 模块职责与工具库说明

## 根工程

### `pom.xml`

根聚合工程，主要用于从项目根目录直接执行 Maven 命令。

当前只聚合：

```xml
<module>exchange-parent</module>
```

实际模块聚合由 `exchange-parent` 继续完成。

## `exchange-parent`

### 职责

Maven 父工程，统一管理：

- Java 版本
- Spring Boot / Spring Cloud 版本
- 第三方依赖版本
- Maven 编译插件
- Spring Boot 打包插件版本

### 关键版本

| 依赖 | 版本 |
|---|---|
| Spring Boot | 3.2.9 |
| Spring Cloud | 2023.0.3 |
| Spring Cloud Alibaba | 2023.0.3.2 |
| Lombok | 1.18.46 |
| MySQL Connector | 8.3.0 |
| MyBatis-Plus | 3.5.7 |
| dynamic-datasource | 4.3.0 |
| Druid | 1.2.22 |
| Hutool | 5.8.20 |

### 聚合模块

```text
exchange-common
exchange-api
exchange-gateway
exchange-auth
exchange-module
exchange-business
exchange-resource
```

## `exchange-common`

公共能力聚合模块。

### `exchange-common-core`

#### 职责

基础公共能力，不依赖具体业务服务：

- 统一响应 `R`
- 状态码枚举 `StatusCode`
- JWT 工具 `JwtUtil`
- IP 获取工具 `IpUtil`
- Servlet 工具 `ServletUtils`
- UUID 工具
- MQ 常量 `MqConstants`
- Redis key 常量 `RedisKeyConstants`
- 安全上下文 `SecurityContextHolder`
- 安全常量 `SecurityConstants`
- 公共 Logback 文件日志配置

#### 使用库

| 工具库 | 用途 |
|---|---|
| Hutool | 类型转换、Bean 拷贝等工具能力 |
| Jackson | JSON 处理 |
| JJWT | JWT 生成和解析 |
| TransmittableThreadLocal | 跨线程上下文传递 |
| Jakarta Servlet API | Servlet 请求/响应类型 |
| Lombok | 简化 Java Bean 和日志代码 |
| Logback | 控制台日志、文件日志、错误日志、滚动归档 |

#### 典型使用

```java
R.success(data);
R.fail(StatusCode.UNAUTHORIZED);
JwtUtil.generate(userId, username, now);
IpUtil.getClientIp(request);
SecurityContextHolder.getUserId();
```

### `exchange-common-redis`

#### 职责

封装 Redis 基础能力：

- RedisTemplate 序列化配置
- RedisService 基础读写
- `setIfAbsent` 原子占位，支持幂等提交等场景
- Spring Boot 自动配置

#### 使用库

| 工具库 | 用途 |
|---|---|
| Spring Data Redis | RedisTemplate |
| Lettuce | Spring Data Redis 默认客户端 |
| commons-pool2 | Lettuce 连接池 |
| Jackson | Redis value JSON 序列化 |

#### 自动配置

模块提供：

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

依赖该模块的 Spring Boot 服务会自动加载：

- `RedisConfig`
- `RedisService`

不需要在启动类里手写扫描 `coin.exchange.common.redis`。

### `exchange-common-rabbitmq`

#### 职责

封装 RabbitMQ 基础能力：

- RabbitMQ 自动配置
- JSON 消息转换器
- `RabbitTemplate` confirm / return 回调
- 按配置动态声明交换机、队列、绑定和死信队列
- 标准消息体 `MqMessage<T>`
- 统一发送入口 `MqMessageService`

#### 核心类

| 类 | 职责 |
|---|---|
| `ExchangeRabbitMqAutoConfiguration` | RabbitMQ 自动配置、消息转换器、RabbitTemplate、动态队列声明 |
| `RabbitMqProperties` | 读取 `exchange.rabbitmq.queues` 配置 |
| `MqMessage` | 标准消息包装，提供 messageId、bizType、payload、timestamp |
| `MqMessageService` | 统一包装并发送 MQ 消息 |
| `RabbitMqService` | RabbitTemplate / AmqpTemplate 轻量发送工具 |

#### 使用库

| 工具库 | 用途 |
|---|---|
| Spring AMQP | RabbitTemplate、队列、交换机、绑定、监听注解 |
| RabbitMQ Java Client | Consumer 手动 ACK / NACK |
| Jackson | MQ 消息 JSON 序列化 |
| exchange-common-redis | 消息消费幂等去重 |
| Lombok | 消息模型和服务样板代码 |

#### 自动配置

模块提供：

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

依赖该模块的 Spring Boot 服务会自动加载 `ExchangeRabbitMqAutoConfiguration`。

业务服务可以通过配置声明队列：

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

队列名、交换机和 routing key 必须与生产者发送参数、消费者 `@RabbitListener` 监听队列一致。

标准发送方式：

```java
mqMessageService.send(exchange, routingKey, bizType, payload);
```

消费端建议使用 `messageId` 做幂等 key，资金、订单、充值提现等强一致场景仍需要业务流水号或数据库唯一约束兜底。

### `exchange-common-web`

#### 职责

封装 Web 服务公共能力：

- 全局异常处理
- Jackson `LocalDateTime` 序列化和反序列化格式
- Spring Boot 自动配置

#### 核心类

| 类 | 职责 |
|---|---|
| `ExchangeWebAutoConfiguration` | 自动注册全局异常处理和 Jackson 时间格式配置 |
| `GlobalExceptionHandler` | 统一处理业务异常、参数异常和系统异常 |

#### 使用库

| 工具库 | 用途 |
|---|---|
| Spring WebMVC | 全局异常处理 |
| Spring Boot Validation | 请求参数校验 |
| Jackson JSR310 | Java Time 类型处理 |

### `exchange-common-security`

#### 职责

为 Spring MVC 业务服务提供安全上下文读取能力和接口级防重复提交能力。

它不负责校验 JWT，也不负责查 Redis。当前设计是：

```text
gateway 校验 token
business 服务只读取 gateway 透传的 user_id / username / Authorization
```

#### 核心类

| 类 | 职责 |
|---|---|
| `WebMvcConfig` | 注册 HeaderInterceptor |
| `HeaderInterceptor` | 读取请求头并写入 SecurityContextHolder |
| `SecurityUtils` | 业务代码获取当前用户上下文 |
| `Idempotent` | 防重复提交注解 |
| `IdempotentAspect` | 基于 Redis 的幂等切面 |
| `IdempotentAutoConfiguration` | 自动注册幂等切面 |

#### 使用库

| 工具库 | 用途 |
|---|---|
| Spring WebMVC | HandlerInterceptor |
| Spring Boot AOP | 注解切面 |
| Spring Security Crypto | BCrypt 密码工具 |
| exchange-common-redis | Redis 原子占位 |
| Lombok | 日志和样板代码 |

#### 典型使用

业务服务引入 `exchange-common-security` 后，可以使用：

```java
SecurityUtils.getUserId();
SecurityUtils.getUsername();
SecurityUtils.getLoginUser();
```

写接口可以使用：

```java
@Idempotent(prefix = "order:create", expire = 10)
@PostMapping("/orders")
public R<Long> createOrder(@RequestBody CreateOrderDto dto) {
    return orderService.createOrder(dto);
}
```

### `exchange-common-doc`

#### 职责

封装接口文档公共配置：

- OpenAPI 基础信息
- Bearer JWT 安全方案
- Spring Boot 自动配置
- `exchange.doc` 配置项

#### 核心类

| 类 | 职责 |
|---|---|
| `ExchangeDocAutoConfiguration` | 自动注册 OpenAPI Bean |
| `ExchangeDocProperties` | 读取 `exchange.doc` 配置 |

#### 使用库

| 工具库 | 用途 |
|---|---|
| springdoc-openapi-starter-common | OpenAPI 模型和公共配置 |
| Spring Boot Autoconfigure | 自动配置 |

服务侧仍需要按 Web 类型引入 UI starter：

```text
Spring MVC -> springdoc-openapi-starter-webmvc-ui
Gateway WebFlux -> springdoc-openapi-starter-webflux-ui
```

## `exchange-api`

API 契约聚合模块。

### `exchange-api-user`

#### 职责

定义用户域跨服务契约：

- DTO
- VO
- OpenFeign Client
- Feign fallback

#### 核心内容

| 类型 | 说明 |
|---|---|
| `RegisterUserDto` | 注册入参 |
| `LoginRecordDto` | 登录记录入参 |
| `KycApplicationDto` | KYC 入参 |
| `UserVo` | 用户信息出参 |
| `LoginVo` | 登录返回对象 |
| `RemoteUserService` | auth 调用 business-user 的 Feign Client |
| `RemoteUserFallbackFactory` | Feign 调用失败降级 |

#### 使用库

| 工具库 | 用途 |
|---|---|
| Spring Cloud OpenFeign | 服务间 HTTP 调用 |
| Spring Cloud LoadBalancer | Feign 负载均衡支持 |
| Lombok | DTO/VO 简化 |

#### 自动配置

模块自动注册 `RemoteUserFallbackFactory`，避免消费方额外扫描 API 包下的 fallback。

## `exchange-gateway`

### 职责

系统统一入口。

当前负责：

- 路由转发
- CORS
- 真实 IP 获取和透传
- JWT 鉴权
- Redis token 校验
- 用户上下文请求头透传

### 核心类

| 类 | 职责 |
|---|---|
| `GatewayApplication` | 网关启动类 |
| `RealIpFilter` | 获取真实客户端 IP，写入 `X-Real-Client-IP` |
| `AuthFilter` | JWT + Redis 鉴权，透传用户上下文 |
| `GatewaySecurityProperties` | 鉴权白名单配置 |

### 使用库

| 工具库 | 用途 |
|---|---|
| Spring Cloud Gateway | WebFlux 网关 |
| Spring Cloud LoadBalancer | 路由/服务调用支持 |
| Spring Cloud Alibaba Nacos Discovery | 服务发现预留，目前关闭 |
| exchange-common-core | JWT、状态码、安全常量 |
| exchange-common-redis | Redis token 校验 |
| Jackson | 鉴权失败响应 JSON 序列化 |

### 关键配置

```yaml
security:
  ignore-paths:
    - /api/crypto-exchange/auth/login
    - /api/crypto-exchange/auth/register
    - /api/crypto-exchange/auth/logout
```

## `exchange-auth`

### 职责

认证服务。

当前负责：

- 用户登录
- 用户注册转发
- JWT 生成
- 登录 token 写入 Redis
- 登录 IP 获取
- 登录记录通知用户服务
- 注册接口防重复提交

### 核心类

| 类 | 职责 |
|---|---|
| `ExchangeAuthApplication` | auth 启动类 |
| `AuthController` | 登录、退出、注册接口 |
| `LoginService` | 登录业务、远程用户服务调用、登录记录 |
| `LoginDto` | 登录入参 |

### 使用库

| 工具库 | 用途 |
|---|---|
| Spring Boot Starter Web | Servlet Web 服务 |
| OpenFeign | 调用用户服务 |
| exchange-common-redis | 缓存 token |
| exchange-common-security | 注册接口幂等提交 |
| exchange-common-core | JWT、IP、响应对象 |
| exchange-api-user | 用户服务契约 |

### 登录态存储

登录成功后写入 Redis：

```text
user:login:{token} -> userId
```

gateway 后续通过该 key 判断 token 是否仍有效。

## `exchange-business`

业务聚合模块，当前只聚合用户业务服务。

### 父模块职责

统一为业务服务提供：

- `exchange-common-core`
- dynamic-datasource boot3 starter
- MyBatis-Plus boot3 starter

### 使用库

| 工具库 | 用途 |
|---|---|
| MyBatis-Plus | ORM / Mapper |
| dynamic-datasource | 动态数据源 |
| Flyway | 数据库版本迁移 |
| Druid | 数据库连接池 |
| MySQL Connector | MySQL 驱动 |

### `exchange-business-user`

#### 职责

用户业务服务。

当前负责：

- 用户信息查询
- 用户注册
- 注册 IP 记录
- 最近登录 IP / 时间记录
- KYC 申请创建和修改
- 读取 gateway 透传用户上下文
- 用户注册和 KYC 写接口防重复提交

#### 核心类

| 类 | 职责 |
|---|---|
| `ExchangeBusinessUserApplication` | 用户服务启动类 |
| `UserController` | 用户查询、注册、登录记录接口 |
| `UserService` / `UserServiceImpl` | 用户业务逻辑 |
| `UserMapper` | 用户表 SQL |
| `UserDo` | users 表实体 |
| `KycApplicationController` | KYC 接口 |
| `KycApplicationDo` | KYC 实体 |
| `ValidationUtil` | 注册/KYC 入参校验 |

#### 使用库

| 工具库 | 用途 |
|---|---|
| Spring Boot Starter Web | Servlet Web 服务 |
| MyBatis-Plus | Mapper 和实体映射 |
| Hutool BeanUtil | DO / VO 属性拷贝 |
| exchange-api-user | DTO / VO |
| exchange-common-security | 读取当前用户上下文、防重复提交 |

#### 内部接口

`/user/login-record` 是内部接口，只允许 auth 通过 Feign 调用。

调用时必须带：

```text
from-source: inner
```

## `exchange-resource`

通用业务能力聚合模块，适合承载可独立部署、可被多个业务域复用的能力。

当前聚合：

```text
exchange-resource-mail
```

### `exchange-resource-mail`

#### 职责

邮件服务。

当前负责：

- 邮件验证码接口
- 生成验证码和邮件内容
- 发送邮件任务到 RabbitMQ
- 消费 RabbitMQ 邮件任务
- 使用 Redis 对 MQ 消息做幂等去重
- 通过 JavaMailSender 发送文本邮件
- 预留 HTML 模板邮件能力

#### 核心类

| 类 | 职责 |
|---|---|
| `ExchangeModuleMailApplication` | 邮件服务启动类 |
| `VerificationCodeEmailController` | 邮件验证码接口 |
| `EmailService` / `EmailServiceImpl` | 邮件发送、MQ 投递和 MQ 消费 |
| `EmailCodeVo` | 邮件验证码接口入参 |
| `templates/email/code.html` | HTML 邮件模板预留 |

#### 使用库

| 工具库 | 用途 |
|---|---|
| exchange-common-rabbitmq | 发送和消费邮件 MQ 任务 |
| exchange-common-redis | MQ 消费幂等去重 |
| Spring Boot Starter Mail | 邮件发送 |
| Thymeleaf | HTML 邮件模板 |
| Spring Boot Starter Web | 对外提供邮件接口 |

#### 消息处理

生产侧：

```text
EmailServiceImpl.sendTextEmail
  -> MqMessageService.send
  -> RabbitTemplate.convertAndSend
```

当前发送参数来自 `MqConstants`：

| 常量 | 当前值 | 用途 |
|---|---|---|
| `EMAIL_SEND_TYPE` | `EMAIL_SEND` | 消息业务类型 |
| `EMAIL_SEND_NAME` | `exchange.email.send` | 当前发送 exchange，同时也是监听队列名 |
| `EMAIL_SEND_KEY` | `queue.email.send` | routing key |

消费侧：

```text
@RabbitListener(queues = MqConstants.EMAIL_SEND_NAME)
  -> Redis SETNX mq:dedup:{messageId}
  -> JavaMailSender.send
  -> channel.basicAck
```

异常时执行：

```text
channel.basicNack(tag, false, false)
```

消息不会重新入队，按队列配置进入死信队列。

## `exchange-resource`

资源服务聚合模块。

当前聚合：

```text
exchange-resource-upload
```

### `exchange-resource-upload`

#### 职责

图片上传服务。

当前负责：

- KYC 图片上传
- 用户头像上传
- 图片类型校验
- 按业务路径和日期分目录存储
- 生成访问 URL

#### 核心类

| 类 | 职责 |
|---|---|
| `ExchangeResourceUploadApplication` | 上传服务启动类 |
| `UploadResourceConfig` | 静态资源访问配置 |
| `ImageUploadController` | 图片上传接口 |
| `ImageUploadService` / `ImageUploadServiceImpl` | 图片校验、存储和 URL 生成 |
| `UploadVo` | 上传结果返回对象 |

#### 使用库

| 工具库 | 用途 |
|---|---|
| exchange-common-web | 统一 Web 异常处理和 Jackson 配置 |
| exchange-common-doc | OpenAPI 文档配置 |
| Spring Boot Starter Web | 文件上传接口 |
| springdoc-openapi-starter-webmvc-ui | Swagger UI |
| Commons IO | 文件处理工具 |
| Lombok | DTO/VO 简化 |

#### 存储规则

上传文件会写入：

```text
{file.upload-path}/image/{bizPath}/{yyyy/MM/dd}/{uuid}.{ext}
```

返回访问路径：

```text
{file.access-path}/image/{bizPath}/{yyyy/MM/dd}/{uuid}.{ext}
```

当前支持接口：

| 接口 | 说明 |
|---|---|
| `POST /upload/image/user/kyc` | 上传用户 KYC 图片 |
| `POST /upload/image/user/avatar` | 上传用户头像 |

## 模块依赖关系

```text
exchange-gateway
  -> exchange-common-core
  -> exchange-common-redis
  -> exchange-common-doc

exchange-auth
  -> exchange-common-redis
  -> exchange-common-security
  -> exchange-common-doc
  -> exchange-api-user
  -> spring-boot-starter-web

exchange-business-user
  -> exchange-api-user
  -> exchange-common-security
  -> exchange-common-doc
  -> spring-boot-starter-web

exchange-common-security
  -> exchange-common-core
  -> exchange-common-redis
  -> exchange-api-user
  -> spring-boot-starter-aop
  -> spring-webmvc
  -> spring-security-crypto

exchange-common-redis
  -> exchange-common-core
  -> spring-boot-starter-data-redis

exchange-common-rabbitmq
  -> exchange-common-redis
  -> spring-boot-starter-amqp
  -> spring-boot-autoconfigure

exchange-common-doc
  -> springdoc-openapi-starter-common
  -> spring-boot-autoconfigure

exchange-common-web
  -> exchange-common-core
  -> spring-webmvc
  -> spring-boot-starter-validation
  -> spring-boot-autoconfigure

exchange-api-user
  -> exchange-common-core
  -> spring-cloud-starter-openfeign
  -> spring-cloud-starter-loadbalancer

exchange-resource-mail
  -> exchange-common-rabbitmq
  -> spring-boot-starter-mail
  -> spring-boot-starter-thymeleaf
  -> spring-boot-starter-web

exchange-resource-upload
  -> exchange-common-web
  -> spring-boot-starter-web
  -> springdoc-openapi-starter-webmvc-ui
  -> commons-io
```

## 使用建议

新增能力时优先遵循以下边界：

- DTO / VO / Feign：放 `exchange-api`
- 通用工具和基础能力：放 `exchange-common`
- 网关入口控制：放 `exchange-gateway`
- 登录认证：放 `exchange-auth`
- 具体业务：放 `exchange-business`
- 通用业务能力：放 `exchange-module`
- 文件、图片等资源能力：放 `exchange-resource`

如果某个类既想放 common，又依赖具体业务表或业务流程，通常说明边界放错了。这个项目现在最需要守住的就是模块边界，别让公共模块慢慢长成“万能厨房”。
