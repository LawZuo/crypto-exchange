# 文件日志与幂等提交说明

## 1. 文件日志

### 目标

本地运行时，所有服务日志默认输出到项目根目录 `log/` 文件夹，避免日志散落在各模块目录里。

### 实现位置

公共配置：

```text
exchange-common/exchange-common-core/src/main/resources/logback/base-logback-spring.xml
```

服务入口配置：

```text
exchange-auth/src/main/resources/logback-spring.xml
exchange-gateway/src/main/resources/logback-spring.xml
exchange-business/exchange-business-user/src/main/resources/logback-spring.xml
```

每个服务入口配置只负责引入公共配置：

```xml
<include resource="logback/base-logback-spring.xml"/>
```

### 日志目录

默认目录：

```text
./log
```

日志文件：

```text
log/{spring.application.name}.log
log/{spring.application.name}-error.log
```

例如：

```text
log/service-auth.log
log/service-auth-error.log
log/gateway.log
log/gateway-error.log
```

### 滚动策略

| 项 | 配置 |
|---|---|
| 单文件大小 | 100MB |
| 保留天数 | 30 天 |
| 普通日志总容量 | 5GB |
| 错误日志总容量 | 2GB |
| 历史文件 | gzip 压缩 |

### 环境变量

可以通过环境变量覆盖日志目录和日志级别：

```bash
LOG_PATH=/data/logs/crypto-exchange LOG_LEVEL=INFO java -jar exchange-auth-1.0.jar
```

本地默认不需要配置，直接启动即可写入根目录 `log/`。

### Git 忽略

`.gitignore` 已忽略：

```text
log/
logs/
*.log
```

日志文件不进入仓库。

## 2. 幂等提交

### 目标

防止用户重复点击、浏览器重试、网关/Feign 重试导致同一个写操作被短时间执行多次。

典型场景：

```text
注册用户
提交 KYC
修改 KYC
删除 KYC
更新 KYC 状态
```

### 实现方式

公共注解：

```text
exchange-common/exchange-common-security/src/main/java/coin/exchange/common/security/annotation/Idempotent.java
```

核心切面：

```text
exchange-common/exchange-common-security/src/main/java/coin/exchange/common/security/aspect/IdempotentAspect.java
```

Redis 原子方法：

```text
exchange-common/exchange-common-redis/src/main/java/coin/exchange/common/redis/service/RedisService.java
```

底层使用：

```java
redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit)
```

只有第一次请求能写入成功；重复请求在 key 过期前会被拦截。

### 默认 key 规则

幂等 key 由以下信息组成后做 SHA-256：

```text
prefix
用户身份 user_id / Authorization / IP
Idempotency-Key 请求头
或请求方法 + URI + query + 方法入参
```

优先级：

1. 注解 `key` 指定的 SpEL 表达式
2. 请求头 `Idempotency-Key`
3. 请求指纹

### 注解参数

```java
@Idempotent(
        prefix = "kyc:create",
        key = "#p0.userId",
        expire = 10,
        message = "KYC申请正在处理，请勿重复提交"
)
```

| 参数 | 说明 |
|---|---|
| `prefix` | Redis key 前缀，建议按业务域命名 |
| `key` | 自定义业务 key，支持 SpEL |
| `header` | 客户端幂等请求头，默认 `Idempotency-Key` |
| `expire` | 幂等 key 过期时间 |
| `timeUnit` | 过期时间单位，默认秒 |
| `message` | 重复提交时返回的提示 |
| `releaseOnFailure` | 业务异常时是否释放 key，默认 `true` |

### 当前接入点

| 模块 | 接口 | 幂等策略 |
|---|---|---|
| exchange-auth | `POST /auth/register` | `auth:register`，按用户名 |
| exchange-business-user | `POST /user/register` | `user:register`，按用户名 |
| exchange-business-user | `POST /user-kyc` | `kyc:create`，按 userId |
| exchange-business-user | `PUT /user-kyc/{id}` | `kyc:update`，按请求指纹 |
| exchange-business-user | `DELETE /user-kyc/{id}` | `kyc:delete`，按请求指纹 |
| exchange-business-user | `PUT /user-kyc/{id}/{status}` | `kyc:status`，按请求指纹 |

### 后续使用建议

新增有副作用的接口时，优先加在 Controller 方法上：

```java
@Idempotent(prefix = "withdraw:create", key = "#p0.requestNo", expire = 30)
@PostMapping("/withdraws")
public R<Long> createWithdraw(@RequestBody WithdrawCreateDto dto) {
    return withdrawService.createWithdraw(dto);
}
```

对于资金、订单、充值提现等强一致场景，幂等提交只能防止短时间重复请求，仍然需要数据库唯一索引或业务流水号兜底。

## 3. 验证记录

已执行：

```bash
mvn -q -DskipTests package
```

构建通过。
