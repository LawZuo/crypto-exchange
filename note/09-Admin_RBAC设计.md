# Admin 端登录与 RBAC 权限设计

## Summary

由 `exchange-admin` 独立负责管理员认证、会话和权限管理，使用独立的 `exchange_admin` 数据库，不复用普通用户账号。管理员接口只能通过 Gateway 访问，并要求 `ADMIN` 类型 Token。第一版实现 RBAC 权限和可选 TOTP 二次验证。

## Key Changes

### 数据与账号体系

- 为 `exchange-admin` 启用 MySQL、Flyway 和 Redis，移除当前数据库自动配置排除。
- 创建以下表：
    - `admin_user`：账号、BCrypt 密码、邮箱、状态、TOTP 密钥及启用状态、失败次数、锁定时间、最后登录信息。
    - `admin_role`：角色编码、名称、状态。
    - `admin_permission`：权限编码、名称、接口或菜单类型。
    - `admin_user_role`：管理员与角色关系。
    - `admin_role_permission`：角色与权限关系。
    - `admin_login_log`：登录结果、IP、设备、失败原因和时间。
    - `admin_operation_log`：后台关键操作审计。
- 预置 `SUPER_ADMIN` 角色；超级管理员拥有全部权限。
- 首个管理员通过部署环境变量 `ADMIN_INITIAL_USERNAME`、`ADMIN_INITIAL_PASSWORD` 初始化，仅在管理员表为空时执行，密码立即 BCrypt 加密，不写入 SQL 或日志。

### 登录与会话

新增 Gateway 对外接口：

- `POST /api/crypto-exchange/admin/auth/login`
- `POST /api/crypto-exchange/admin/auth/refresh`
- `POST /api/crypto-exchange/admin/auth/logout`
- `GET /api/crypto-exchange/admin/auth/me`
- `POST /api/crypto-exchange/admin/auth/change-password`

登录请求：

```json
{
  "username": "admin",
  "password": "******",
  "totpCode": "123456"
}
```

登录成功返回：

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 1800,
  "admin": {
    "id": 1,
    "username": "admin",
    "roles": ["SUPER_ADMIN"],
    "permissions": ["*"],
    "totpEnabled": false
  }
}
```

- Access Token 有效期 30 分钟，Refresh Token 有效期 7 天。
- JWT 使用独立的管理员密钥，并包含 `tokenType=ADMIN`、`adminId`、`username`、`sessionId`、`issuer` 和 `audience`。
- Redis 保存管理员会话及 Refresh Token，key 使用哈希后的 Token/Session ID：
    - `exchange:{env}:admin:session:{sessionId}`
    - `exchange:{env}:admin:refresh:{tokenHash}`
- 登出、禁用管理员、修改密码时立即删除全部相关会话。
- Refresh Token 每次刷新后轮换，旧 Refresh Token 立即失效。
- TOTP 按管理员配置启用；启用后登录必须提交正确验证码，并提供绑定、确认和重置流程。

### Gateway 与权限控制

- Gateway 白名单只增加 Admin 登录和刷新接口。
- `/api/crypto-exchange/admin/**` 必须使用 `ADMIN` Token；普通用户 Token 请求后台接口返回 403。
- `/api/crypto-exchange/web/**` 继续只接受普通用户 Token，管理员 Token 不混用。
- Gateway 验证 JWT、Redis 会话、管理员状态，并向 `exchange-admin` 透传管理员 ID、账号和 Session ID。
- `exchange-admin` 增加权限注解，例如：

```java
@RequirePermission("admin:user:read")
```

- RBAC 权限从数据库加载并缓存到 Redis；角色或权限变更后主动清除缓存。
- 现有 `/management/**` 接口分别配置用户查询、钱包查询、行情查询权限，超级管理员使用 `*` 放行。
- 管理员认证信息不信任客户端传入的身份请求头，Gateway 必须覆盖这些请求头。

### 安全与风控

- JWT 密钥、初始管理员密码和 TOTP 加密密钥全部从环境变量或 Nacos Secret 配置读取。
- 禁止记录密码、完整 Token、TOTP 密钥及完整请求头。
- 同一账号连续失败 5 次，在 15 分钟窗口内锁定 30 分钟。
- 对账号和 IP 分别进行登录限流；错误响应统一为“账号或密码错误”，详细原因只写安全日志。
- 停用账号、账号锁定、密码错误、TOTP 错误和登录成功均写入登录日志。
- 修改密码、角色授权、禁用管理员等操作写入操作审计日志。
- 密码至少 10 位，并要求字母、数字和特殊字符组合。

## Test Plan

- 验证首个超级管理员只初始化一次，数据库已有管理员时不会覆盖。
- 验证正确密码、错误密码、禁用账号、锁定账号及可选 TOTP 登录流程。
- 验证普通用户 Token 无法访问 Admin 接口，Admin Token 无法冒充普通用户。
- 验证 Access Token 过期、Refresh Token 轮换、登出和修改密码后的会话失效。
- 验证不同角色只能访问被授权接口，权限调整后缓存及时失效。
- 验证登录失败次数、账号锁定、IP 限流及并发刷新安全性。
- 验证所有 Admin 接口只能经过 Gateway 访问，伪造身份请求头无效。
- 验证日志中不出现密码、完整 Token、TOTP 密钥等敏感数据。

## Assumptions

- 管理员仅支持独立用户名登录；邮箱用于通知和找回，不作为第一版登录标识。
- `exchange-admin` 自己管理 `exchange_admin` 数据库，不新增业务模块。
- 第一版采用 RBAC 接口权限控制，菜单权限复用同一权限表。
- TOTP 能力第一版实现但按账号选择性启用，不强制所有管理员绑定。
- 普通用户现有登录接口和用户数据库保持不变。
