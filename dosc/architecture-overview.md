# Crypto Exchange 项目架构说明

## 项目定位

`crypto-exchange` 是一个基于 Spring Boot / Spring Cloud 的加密货币交易所后端雏形项目。当前代码重点覆盖了网关、认证、用户业务、公共基础能力和跨模块 API 契约。

项目采用 Maven 多模块结构，服务间通过 HTTP / OpenFeign 通信。当前本地开发环境没有启用 Nacos 服务发现，Feign 和 Gateway 路由主要使用固定地址。

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
```

公共模块提供复用能力：

```text
exchange-api
  对外/服务间 API 契约、DTO、VO、Feign Client

exchange-common
  通用响应、状态码、JWT、IP、Redis、安全上下文、文件日志、幂等提交等

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
    exchange-common-security/     MVC 服务端请求头上下文拦截器、幂等提交
    exchange-common-doc/          OpenAPI 接口文档公共配置
  exchange-api/                   API 契约聚合模块
    exchange-api-user/            用户 DTO / VO / Feign Client
  exchange-gateway/               Spring Cloud Gateway 网关服务
  exchange-auth/                  认证服务
  exchange-business/              业务聚合模块
    exchange-business-user/       用户业务服务
  exchange-module/                预留模块，目前未承载具体服务
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

## 端口和路由

| 服务 | 默认端口 | 说明 |
|---|---:|---|
| exchange-gateway | 80 | 统一入口 |
| exchange-auth | 8080 | 认证服务 |
| exchange-business-user | 8081 | 用户业务服务 |

Gateway 路由：

```text
/api/crypto-exchange/auth/** -> http://localhost:8080
/api/crypto-exchange/user/** -> http://localhost:8081
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
```

已知需要进一步外置的敏感项：

- Redis host / port / password
- MySQL url / username / password
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

后续新增业务时建议继续保持：

```text
API 契约放 exchange-api
公共能力放 exchange-common
业务实现放 exchange-business
入口控制放 exchange-gateway
认证登录放 exchange-auth
```
