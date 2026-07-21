# exchange-auth 登录 500 排查记录

- 时间：2026-07-17
- 模块：`exchange-auth`（Spring Boot 3.2.9 + spring-cloud-starter-openfeign + spring-cloud-starter-loadbalancer + spring-boot-starter-webflux）
- 现象：调用 `POST /auth/login` 一直返回 500，错误日志逐步变化

---

## 一、问题演进与根因

整个排查过程**串了三个不同的错误**，每个都是上一修完暴露出来的下一个问题：

| # | 错误日志（关键） | 根因 | 修复 |
|---|---|---|---|
| 1 | `IllegalStateException: block()/blockFirst()/blockLast() are blocking, which is not supported in thread reactor-http-nio-2` | 在 webflux 容器里同步阻塞调用 Feign 远程接口 | `LoginService.login` 返回 `Mono<UserVo>`，远端调用包成 `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())` |
| 2 | `feign.FeignException$ServiceUnavailable: [503] ... Load balancer does not contain an instance for the service user-route` | 没有启用服务注册中心，LoadBalancer 拿不到 `user-route` 的实例列表 | `@FeignClient` 加 `url = "${bt.upstream.base-url:http://localhost:8081}"` 直接走绝对地址 |
| 3 | `feign.codec.DecodeException: No qualifying bean of type 'HttpMessageConverters'` | webflux 容器里 `HttpMessageConverters` Bean 不存在（默认由 spring-boot-starter-web 的自动配置提供），Feign 反序列化响应时找不到 | 在 `exchange-api-user` 下新增 `FeignConfig`，显式注册 `HttpMessageConverters` 和 `feignDecoder` |

> 三个错误是一条链：先解决"不能 block"（webflux 异步化）→ 再解决"找不到服务"（写死 URL）→ 再解决"解码器缺失"（feign 配置）。每修一个都暴露下一个。

---

## 二、最终改动清单

### 1. `exchange-auth/src/main/java/coin/exchange/auth/service/LoginService.java`

**改动**：把同步方法改成返回 `Mono<UserVo>`，远端 Feign 调用包到 `Mono.fromCallable + boundedElastic` 线程池里，避免在 reactor 事件循环线程 block。

```java
public Mono<UserVo> login(LoginDto loginDto, ServerHttpRequest request) {
    // ... IP 解析保持不变 ...

    return Mono.fromCallable(() -> remoteUserService.getUserInfo(loginDto.getUsername()))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(result -> {
                UserVo user = result == null ? null : result.getData();
                if (user == null) {
                    return Mono.error(new RuntimeException("用户不存在"));
                }
                if (user.getStatus() != null && user.getStatus() == 0) {
                    return Mono.error(new RuntimeException("用户被禁用"));
                }
                return Mono.just(user);
            });
}
```

### 2. `exchange-auth/src/main/java/coin/exchange/auth/controller/AuthController.java`

**改动**：三个接口全部返回 `Mono<R<...>>`，与 webflux 栈一致。

```java
@PostMapping("/login")
public Mono<R<UserVo>> login(@RequestBody LoginDto loginDto, ServerHttpRequest request) {
    return loginService.login(loginDto, request).map(R::success);
}
```

### 3. `exchange-api-user/src/main/java/coin/exchange/api/user/service/RemoteUserService.java`

**改动**：
- `@FeignClient` 的 `value` 从 `"user-route"` 改为 `name = "exchange-business-user"`
- 加 `url = "${bt.upstream.base-url:http://localhost:8081}"` 走绝对地址（**当前环境没启用服务发现**）
- 加 `configuration = FeignConfig.class` 显式指定 Feign 配置

```java
@FeignClient(
        name = "exchange-business-user",
        url = "${bt.upstream.base-url:http://localhost:8081}",
        configuration = FeignConfig.class,
        fallbackFactory = RemoteUserFallbackFactory.class
)
public interface RemoteUserService {
    @GetMapping("/user/info/{username}")
    R<UserVo> getUserInfo(@PathVariable("username") String username);
}
```

### 4. `exchange-api-user/src/main/java/coin/exchange/api/user/config/FeignConfig.java`（新增）

**目的**：webflux 容器里 `HttpMessageConverters` Bean 默认不存在（由 spring-boot-starter-web 自动配置提供），必须手动注册，否则 Feign 反序列化响应会抛 `No qualifying bean of type 'HttpMessageConverters'`。

```java
@Slf4j
@Configuration
public class FeignConfig {

    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @Bean
    public HttpMessageConverters feignHttpMessageConverters() {
        ObjectMapper objectMapper;
        try {
            objectMapper = applicationContext.getBean(ObjectMapper.class);
        } catch (Exception e) {
            log.warn("未找到 Spring 托管的 ObjectMapper，回退到新建实例");
            objectMapper = new ObjectMapper();
        }
        HttpMessageConverter<?> jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        return new HttpMessageConverters(jacksonConverter);
    }

    @Bean
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringDecoder(messageConverters);
    }
}
```

### 5. `exchange-business-user/src/main/resources/application.yml`

**改动**：`spring.application.name` 由 `service-auth` 改为 `user-route`，跟 RemoteUserService 的服务名对齐。

```yaml
spring:
  application:
    name: user-route
```

> 注：当前环境不走注册中心，这项修改只在未来启用 Nacos 时生效。但保留一致的命名能避免后续误用。

---

## 三、配套配置

### `exchange-auth/src/main/resources/application.yml`（无需改动，已就绪）

```yaml
bt:
  upstream:
    base-url: ${BT_UPSTREAM_BASE_URL:http://localhost:8081}
```

`bt.upstream.base-url` 已经被 `RemoteUserService` 的 `url` 引用，部署时可通过环境变量 `BT_UPSTREAM_BASE_URL` 覆盖。

---

## 四、重启 / 验证流程

1. 重新打包公共 jar（`exchange-api-user` 改了 FeignConfig，必须重新 install）：
   ```bash
   mvn clean install -pl exchange-api/exchange-api-user -am -DskipTests
   ```

2. 先启动 `exchange-business-user`（8081），确认 `/user/info/{username}` 直接 curl 能通：
   ```bash
   curl http://localhost:8081/user/info/test
   ```

3. 再启动 `exchange-auth`（8080）。

4. 调用登录接口：
   ```bash
   curl -X POST http://localhost:8080/auth/login \
     -H 'Content-Type: application/json' \
     -d '{"username":"test","password":"xxx"}'
   ```

5. 预期看到 `UserController.getUserInfo` 的命中日志，HTTP 状态码 200。

---

## 五、已发现 / 待处理事项

### 当前已知隐患
- **OpenFeign 在 webflux 下是「伪响应式」**：底层还是同步 HTTP 调用，靠 `subscribeOn(boundedElastic)` 切线程。功能 OK，性能不是最优（线程占用、无法走 reactive 连接池）。
  - 后续优化方向：换 `spring-cloud-starter-openfeign` 的 reactive 客户端，或者直接换 `WebClient` 调绝对地址。

### 关联检查项
- **exchange-gateway**：进程已在跑（PID 64674），其路由配置里如果也用服务名转发，可能同样存在 503 问题。建议扫一遍 `application.yml` 里的 spring.cloud.gateway.routes。
- **Nacos / 服务发现**：当前工程 `application-feign.yml` 把 `nacos.discovery.enabled` 设为 `false`，等于完全没接服务发现。如果未来要扩展多实例部署，需要：
  1. 启动 Nacos（本地/容器/远程均可）
  2. 把两个服务的 `nacos.discovery.enabled` 改回 `true`
  3. 在 `RemoteUserService` 里去掉 `url` 字段，恢复服务名

---

## 六、排查方法论

这次问题的特点是 **"日志里的报错和根因不一一对应"**：

- 第一个报错是「线程模型不匹配」（reactor vs 同步）
- 第二个报错是「服务发现缺失」
- 第三个报错是「webflux 缺 web 自动配置」

每一个都跟 webflux 这个相对小众的栈有关，**核心思路**：
1. 报「block 不支持」→ 异步化
2. 报「503 找不到服务」→ 确认注册中心是否启用 / 改写死 URL
3. 报「No qualifying bean of type HttpMessageConverters」→ webflux 缺 web 自动配置，手动补 Bean

**经验**：webflux 项目里用 OpenFeign 是一个长期「维护负担」组合，**短期能跑，长期不推荐**。建议路线：先维持现状（业务能通），流量起来后切 `WebClient`。
