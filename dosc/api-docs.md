# 接口文档配置说明

## 目标

为各服务生成 OpenAPI 文档，并通过 gateway 提供统一 Swagger UI 入口。

## 技术选型

使用 `springdoc-openapi`：

| 服务类型 | 依赖 |
|---|---|
| Spring MVC 服务 | `springdoc-openapi-starter-webmvc-ui` |
| Spring Cloud Gateway | `springdoc-openapi-starter-webflux-ui` |

版本在 `exchange-parent/pom.xml` 中统一管理：

```xml
<springdoc.version>2.6.0</springdoc.version>
```

## 访问地址

### Gateway 聚合入口

```text
http://localhost/swagger-ui.html
```

页面内包含：

```text
auth -> /v3/api-docs/auth
user -> /v3/api-docs/user
```

### 服务直连

认证服务：

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

用户服务：

```text
http://localhost:8081/swagger-ui.html
http://localhost:8081/v3/api-docs
```

## Gateway 转发

gateway 新增两个文档路由：

```text
/v3/api-docs/auth -> http://localhost:8080/v3/api-docs
/v3/api-docs/user -> http://localhost:8081/v3/api-docs
```

Swagger UI 使用这两个地址聚合下游服务文档。

## 鉴权放行

gateway 鉴权白名单已放开：

```text
/swagger-ui.html
/swagger-ui/**
/webjars/**
/v3/api-docs/**
```

`AuthFilter` 使用 Ant 风格路径匹配，所以 `/**` 可以匹配多级路径。

## OpenAPI 配置类

认证服务：

```text
exchange-auth/src/main/java/coin/exchange/auth/config/OpenApiConfig.java
```

用户服务：

```text
exchange-business/exchange-business-user/src/main/java/coin/exchange/module/user/config/OpenApiConfig.java
```

两者都配置了 Bearer JWT 安全方案，Swagger UI 页面可通过 `Authorize` 输入 token 后调试需要登录的接口。
