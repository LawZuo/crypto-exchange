# 虚拟货币交易所项目说明与重命名规划

本文档用于记录当前仓库现状、目标模块划分、命名规范，以及从 Maven 原型骨架演进到交易所微服务项目时需要完成的重命名和补齐工作。

## 1. 项目定位

项目名：虚拟货币交易所

当前仓库是一个基于 Java 17、Spring Boot、Spring Cloud 的多模块 Maven 工程骨架，目标是逐步拆分为网关、用户中心、账户、订单、风控、撮合、行情、清算、通知等服务。

目前已落地的是基础 Maven 多模块结构，业务代码尚未实现。

## 2. 当前技术栈

以 `exchange-parent/pom.xml` 为准：

- JDK：17
- Spring Boot：3.3.4
- Spring Cloud：2023.0.3
- Spring Cloud Alibaba：2023.0.3.2
- 构建工具：Maven
- 当前服务发现：Nacos Discovery（已在 `exchange-gateway` 依赖中引入）
- 当前网关：Spring Cloud Gateway（已在 `exchange-gateway` 依赖中引入）

规划中但当前 POM 尚未引入：

- MyBatis-Plus
- OpenFeign
- Kafka / RocketMQ
- Redis
- Disruptor
- Netty
- Sentinel
- Docker / Kubernetes 部署脚本

注意：旧文档中的 `Spring Cloud 2020.0.3` 与 Spring Boot 3.x 不匹配，当前 POM 已使用 `2023.0.3`。

## 3. 当前仓库结构

当前真实目录如下：

```text
crypto-exchange-cloud/
├── RENAME.md
├── exchange-parent/
│   └── pom.xml                    # 父 POM，统一版本和模块聚合
├── exchange-common/
│   ├── pom.xml                    # 公共模块，当前仅引入 Lombok
│   └── src/
├── exchange-api/
│   ├── pom.xml                    # API 契约模块，当前仍是 Maven 原型默认依赖
│   └── src/
├── exchange-gateway/
│   ├── pom.xml                    # 网关模块，已依赖 common、Gateway、Nacos、LoadBalancer
│   └── src/
└── exchange-uc/
    ├── pom.xml                    # 用户中心模块，已依赖 common
    └── src/
```

当前各模块源码仍是 Maven quickstart 原型生成的占位类：

```text
src/main/java/coin/exchange/App.java
src/test/java/coin/exchange/AppTest.java
```

这些 `App` / `AppTest` 仅用于占位，后续应替换为各模块自己的启动类、配置类、业务包和测试。

## 4. 命名规范

### 4.1 Maven 坐标

当前 Maven 坐标：

```text
groupId    = coin.exchange
version    = 1.0
artifactId = exchange-parent
artifactId = exchange-common
artifactId = exchange-api
artifactId = exchange-gateway
artifactId = exchange-uc
```

建议继续统一使用 `coin.exchange`，不要再混用旧文档中的 `com.exchange`。

### 4.2 Java 包名

当前占位包名：

```text
coin.exchange
```

建议按模块细化为：

```text
coin.exchange.common
coin.exchange.api
coin.exchange.gateway
coin.exchange.uc
coin.exchange.account
coin.exchange.order
coin.exchange.risk
coin.exchange.matching
coin.exchange.marketdata
coin.exchange.clearing
coin.exchange.notify
```

服务启动类建议命名：

```text
coin.exchange.gateway.ExchangeGatewayApplication
coin.exchange.uc.ExchangeUcApplication
coin.exchange.account.ExchangeAccountApplication
coin.exchange.order.ExchangeOrderApplication
coin.exchange.risk.ExchangeRiskApplication
coin.exchange.matching.ExchangeMatchingApplication
coin.exchange.marketdata.ExchangeMarketDataApplication
coin.exchange.clearing.ExchangeClearingApplication
coin.exchange.notify.ExchangeNotifyApplication
```

### 4.3 服务名

建议 Spring 应用名和注册中心服务名保持一致：

```text
exchange-gateway
exchange-uc
exchange-account
exchange-order
exchange-risk
exchange-matching
exchange-marketdata
exchange-clearing
exchange-notify
```

如果对外接口希望更清晰，也可以在 API 路由层暴露短名称，例如 `/uc/**`、`/account/**`、`/order/**`、`/market/**`。

## 5. 模块职责规划

### 5.1 已创建模块

```text
exchange-parent
```

父 POM，负责：

- 统一 Java 版本、编码、Spring Boot 版本
- 管理 Spring Cloud / Spring Cloud Alibaba BOM
- 聚合子模块
- 后续可继续统一 Maven 插件、测试插件、镜像构建插件

```text
exchange-common
```

公共模块，建议承载：

- 通用常量
- 枚举：订单方向、订单类型、订单状态、账户流水类型等
- 统一响应：`Result<T>`、分页响应
- 统一异常：业务异常、错误码
- 工具类：时间、金额精度、ID、签名等
- 基础注解：幂等、日志、鉴权标记等

不建议放入具体业务流程，避免 `common` 变成跨服务耦合点。

```text
exchange-api
```

API 契约模块，建议承载：

- DTO / VO / Request / Response
- OpenFeign 客户端接口
- 领域事件定义：`OrderEvent`、`TradeEvent`、`AccountEvent`
- 跨服务错误码和契约常量

不建议依赖具体服务实现，也不建议放 Controller。

```text
exchange-gateway
```

API 网关模块，建议承载：

- 路由配置
- 鉴权过滤器
- 限流和黑名单过滤器
- 请求签名校验
- 统一跨域、追踪 ID、访问日志

当前已引入 Spring Cloud Gateway、Nacos Discovery、LoadBalancer。

```text
exchange-uc
```

用户中心模块，建议承载：

- 用户注册、登录、登出
- 账户安全：密码、MFA、设备、登录记录
- KYC / 实名认证状态
- API Key 管理
- 用户权限和风控标签

### 5.2 规划中模块

```text
exchange-account
```

账户服务，负责余额、冻结、解冻、持仓、流水和资产快照。

```text
exchange-order
```

订单服务，负责下单、撤单、订单状态机、订单历史、订单事件投递。

```text
exchange-risk
```

风控服务，负责下单前校验、频率控制、账户风险、交易限制、强平预检查。

```text
exchange-matching
```

撮合引擎，核心模块，负责订单簿、撮合循环、成交生成、快照、WAL 和恢复。

```text
exchange-marketdata
```

行情服务，负责成交订阅、K 线聚合、深度行情、Ticker、WebSocket 推送。

```text
exchange-clearing
```

清算/结算服务，负责资金费率、交割、强平、盈亏结算和清算事件处理。

```text
exchange-notify
```

通知服务，负责站内信、邮件、短信、Webhook、风控告警。

```text
exchange-bootstrap
```

启动与部署模块，负责本地 Docker Compose、Kubernetes YAML、初始化脚本和运维脚本。

## 6. 目标目录结构

建议后续演进为：

```text
crypto-exchange-cloud/
├── exchange-parent/
├── exchange-common/
│   └── src/main/java/coin/exchange/common/
│       ├── annotation/
│       ├── constant/
│       ├── enums/
│       ├── exception/
│       ├── response/
│       └── util/
├── exchange-api/
│   └── src/main/java/coin/exchange/api/
│       ├── dto/
│       ├── event/
│       └── feign/
├── exchange-gateway/
│   └── src/main/java/coin/exchange/gateway/
│       ├── config/
│       ├── filter/
│       └── ExchangeGatewayApplication.java
├── exchange-uc/
│   └── src/main/java/coin/exchange/uc/
│       ├── controller/
│       ├── service/
│       ├── mapper/
│       ├── entity/
│       └── ExchangeUcApplication.java
├── exchange-account/
├── exchange-order/
├── exchange-risk/
├── exchange-matching/
├── exchange-marketdata/
├── exchange-clearing/
├── exchange-notify/
└── exchange-bootstrap/
    ├── docker-compose.yml
    ├── k8s/
    └── scripts/
```

## 7. 重命名与清理清单

### 7.1 必须处理

- 将旧文档和未来代码中的 `com.exchange` 统一为 `coin.exchange`。
- 删除或替换各模块中的 Maven 原型占位类 `App.java`。
- 删除或替换各模块中的 JUnit 3 占位测试 `AppTest.java`。
- 为可启动服务创建明确的 Spring Boot 启动类。
- 为每个服务增加 `application.yml` 或 `bootstrap.yml`，至少包含 `spring.application.name`。
- 给根目录补充顶层 `pom.xml`，或明确要求所有 Maven 命令从 `exchange-parent/pom.xml` 执行。
- 修正所有子模块 `<parent>` 的 `<relativePath>`，当前默认指向 `../pom.xml`，但仓库根目录没有 `pom.xml`。
- 统一子模块版本引用，优先使用父 POM 的 `${project.version}`，减少硬编码 `1.0`。

### 7.2 建议处理

- `exchange-api` 当前仍依赖 JUnit 3.8.1，应改为 Spring Boot 默认测试体系或 JUnit Jupiter。
- `exchange-common` 中 Lombok 依赖建议使用 `optional` 或通过 `maven-compiler-plugin` 配置 annotation processor，避免使用非标准 Maven scope。
- 父 POM 可增加 `maven.compiler.release=17`，比 source/target 更明确。
- 父 POM 可统一管理 MyBatis-Plus、OpenFeign、Redis、Kafka、测试依赖版本。
- 增加 `README.md` 作为面向使用者的项目入口，`RENAME.md` 保留为命名和迁移规划文档。
- `.idea` 目录已进入暂存区，若不是团队统一 IDE 配置，建议只保留必要文件或从版本库移除。

## 8. 推荐开发顺序

1. 修正 Maven 聚合方式和子模块 parent `relativePath`，确认 `mvn -f exchange-parent/pom.xml validate` 可通过。
2. 清理原型占位类和 JUnit 3 测试。
3. 建立 `common` 的错误码、响应体、异常体系。
4. 建立 `api` 的 DTO、事件和 Feign 契约。
5. 完成 `gateway` 启动类、路由和鉴权过滤器骨架。
6. 完成 `uc` 启动类、用户模型和基础登录注册接口。
7. 再按账户、订单、风控、撮合、行情、清算的顺序扩展服务。

## 9. 构建状态与命令

当前仓库根目录没有顶层 `pom.xml`。同时，子模块 `exchange-common`、`exchange-api`、`exchange-gateway`、`exchange-uc` 的 `<parent>` 未显式配置 `<relativePath>`，Maven 默认会查找 `../pom.xml`，因此当前执行：

```bash
mvn -f exchange-parent/pom.xml validate
```

会失败，典型错误为：

```text
Non-resolvable parent POM ... 'parent.relativePath' points at wrong local POM
```

推荐修复方式是在每个子模块 POM 的 `<parent>` 中补充：

```xml
<relativePath>../exchange-parent/pom.xml</relativePath>
```

修复后应从父 POM 执行 Maven 命令：

```bash
mvn -f exchange-parent/pom.xml validate
mvn -f exchange-parent/pom.xml test
mvn -f exchange-parent/pom.xml package
```

如果后续新增根目录聚合 POM，并将 `exchange-parent` 调整为常规父模块，则可以改为：

```bash
mvn validate
mvn test
mvn package
```

## 10. 当前状态总结

当前项目已经具备多模块 Maven 骨架和基础技术版本管理，但业务结构还没有真正落地。下一阶段的重点不是继续增加业务模块目录，而是先完成命名统一、原型代码清理、父 POM 规范化、服务启动类和配置文件补齐，确保后续模块扩展有稳定基础。
