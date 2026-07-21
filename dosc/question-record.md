# 问题记录

## 2026-07-18

### 问题

把登录调用用户信息的 Feign 使用的 WebFlux 全部换成 Servlet MVC。

### 处理状态

已处理：
- `exchange-auth` 从 `spring-boot-starter-webflux` 切换为 `spring-boot-starter-web`
- 登录控制器返回值从 `Mono<R<...>>` 改为同步 `R<...>`
- 登录服务从 `ServerHttpRequest` 改为 `HttpServletRequest`
- Feign 用户信息调用移除 WebFlux 专用配置
- Maven 编译验证通过

### 处理步骤

1. 搜索 WebFlux、Reactor、Feign、登录相关代码，确认 WebFlux 主要集中在 `exchange-auth` 登录链路。
2. 查看 `exchange-auth/pom.xml`，确认 auth 模块依赖的是 `spring-boot-starter-webflux`。
3. 查看 `AuthController`，确认 `/auth/login`、`/auth/logout`、`/auth/register` 返回 `Mono<R<...>>`，并使用 `ServerHttpRequest`。
4. 查看 `LoginService`，确认登录服务通过 `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())` 包装同步 Feign 调用。
5. 查看 `RemoteUserService` 和 `FeignConfig`，确认 Feign 客户端指定了 WebFlux 环境下补充 `HttpMessageConverters` 的专用配置。
6. 修改 `exchange-auth/pom.xml`，把 `spring-boot-starter-webflux` 替换为 `spring-boot-starter-web`。
7. 修改 `AuthController`，把接口返回值改成同步 `R<...>`，把请求对象改成 `HttpServletRequest`。
8. 修改 `LoginService`，删除 Reactor 相关代码，直接同步调用 `remoteUserService.getUserInfo(...)`，并通过 Servlet request header 获取客户端 IP。
9. 修改 `RemoteUserService`，移除 `configuration = FeignConfig.class`。
10. 删除 `exchange-api-user/src/main/java/coin/exchange/api/user/config/FeignConfig.java`。
11. 搜索 `exchange-auth`、`exchange-api-user`、`exchange-business-user`，确认无 `webflux`、`Mono`、`ServerHttpRequest`、`Schedulers`、`FeignConfig` 等残留。
12. 执行 `mvn -q -f exchange-parent/pom.xml -pl ../exchange-auth -am compile`，确认编译通过。
13. 执行 `mvn -q -f exchange-parent/pom.xml -pl ../exchange-api/exchange-api-user,../exchange-auth -am test -DskipTests`，确认测试生命周期通过。
14. 执行 `mvn -q -f exchange-parent/pom.xml -pl ../exchange-auth -am clean compile`，清理旧 target 产物并重新编译通过。
