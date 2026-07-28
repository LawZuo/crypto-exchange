# 项目完善记录 - 2026-07-28

## 背景

本次按前一次体检清单中的 3、5、6、7、9、10、11、12、15 依次完善项目，目标是让工程更适合持续开发：

- 清理 Git 噪音
- 统一错误响应语义
- 让 gateway 鉴权配置化
- 保护服务间内部接口
- 对齐用户表字段和查询逻辑
- 修复注册返回 ID
- 统一 Maven 依赖版本
- 减少启动类手写扫描包
- 更新文档记录

## 3. Git 工程卫生

新增根目录 `.gitignore`，忽略以下内容：

- `.DS_Store`
- `.idea/`
- `*.iml`
- `out/`
- `target/`
- `*.log`

目的：仓库只保留源码、配置和文档，不提交 IDE 私有配置和 Maven 构建产物。

## 5. 统一错误码

`R` 新增两个基于 `StatusCode` 的失败响应方法：

```java
R.fail(StatusCode statusCode)
R.fail(StatusCode statusCode, String message)
```

gateway 鉴权失败时，HTTP 状态码和 body code 现在都使用 401 语义：

```json
{"code":401,"message":"未提供认证token","data":null}
```

## 6. Gateway 鉴权白名单配置化

`AuthFilter` 不再硬编码白名单数组，新增：

```java
coin.exchange.gateway.config.GatewaySecurityProperties
```

配置位置：

```yaml
security:
  ignore-paths:
    - /api/crypto-exchange/auth/login
    - /api/crypto-exchange/auth/register
    - /api/crypto-exchange/auth/logout
```

后续新增公开接口时，只需要改配置。

## 7. 内部接口保护

`/user/login-record` 是 auth 调 business-user 的内部接口，本次增加 `from-source` 头校验。

Feign 调用侧：

```java
remoteUserService.recordLogin(SecurityConstants.INNER, dto)
```

业务服务接收侧：

```java
@RequestHeader(value = SecurityConstants.FROM_SOURCE, required = false) String source
```

如果不是 `from-source: inner`，返回 `FORBIDDEN`。

## 9. 数据库字段和查询对齐

修复和确认以下内容：

- `UserDo.registerIp` 对齐 `register_ip`
- `lastLoginIp` 对齐 `last_login_ip`
- `lastLoginTime` 对齐 `last_login_time`
- `getUserByUid` 改为走 `userMapper.getUserByUid`
- `getUserByEmail` 改为走 `userMapper.getUserByEmail`
- `mybatis-plus.type-aliases-package` 从旧包名改为 `coin.exchange.module.user.domain`

## 10. 注册返回真实用户 ID

原逻辑：

```java
Long userId = (long) userMapper.insert(userDo);
```

这个返回的是影响行数，不是新用户 ID。

新逻辑：

```java
userMapper.insert(userDo);
return userDo.getId();
```

依赖 `UserDo.id` 的 `@TableId(type = IdType.AUTO)` 回填主键。

## 11. 统一 Maven 依赖版本

`exchange-business/pom.xml` 中：

- `dynamic-datasource-spring-boot-starter` 改为 `dynamic-datasource-spring-boot3-starter`
- 移除业务 POM 内手写版本号
- `mybatis-plus-spring-boot3-starter` 移除手写版本号

版本统一交给 `exchange-parent/pom.xml` 的 `dependencyManagement` 管理。

## 12. 减少启动类扫描包

新增 Spring Boot 3 自动配置，让公共模块随依赖自动加载：

- `exchange-api-user`
  - `ExchangeApiUserAutoConfiguration`
  - 自动注册 `RemoteUserFallbackFactory`

- `exchange-common-redis`
  - `ExchangeRedisAutoConfiguration`
  - 自动导入 `RedisConfig`
  - 自动注册 `RedisService`

- `exchange-common-security`
  - 通过 `AutoConfiguration.imports` 自动加载 `WebMvcConfig`

启动类恢复为普通形式：

```java
@SpringBootApplication
```

不再依赖手写 `scanBasePackages` 扫公共模块。

## 15. 文档更新

当前关键链路：

```text
auth 登录成功
  -> Redis 缓存 user:login:{token}
  -> Feign 调 business-user /user/login-record，带 from-source: inner

gateway 收到业务请求
  -> AuthFilter 校验 JWT + Redis
  -> 透传 user_id / username / Authorization

business-user
  -> common-security 读取请求头
  -> 写入 SecurityContextHolder
```

## 验证

已执行：

```bash
mvn -q -DskipTests compile
mvn -q -DskipTests package
```

均通过。

