# Admin 端登录与权限设计方案

> 文档状态：待实施设计。本文档仅记录方案，不代表相关功能已开发或上线。

## 1. 设计目标

Admin 端用于平台运营、客服、财务和风控人员访问后台管理功能。管理员账号属于高权限账号，不应与交易用户共用账号、Token 或权限体系。

方案目标：

- 管理员与普通用户的身份、数据和会话完全隔离。
- 所有 Admin 接口统一经过 Gateway 访问。
- 支持 RBAC 角色权限控制。
- 支持短效 Access Token 和可撤销的 Refresh Token。
- 预留并支持按账号开启 TOTP 二次验证。
- 所有登录和关键操作可追溯、可审计。

## 2. 系统边界

### 2.1 服务归属

`exchange-admin` 独立负责：

- 管理员账号、角色和权限管理。
- 管理员登录、Token 刷新和登出。
- Admin 接口权限判断。
- 登录日志与后台操作审计。

`exchange-admin` 使用独立的 `exchange_admin` 数据库，不读写普通用户的认证数据。

Gateway 负责：

- 对 Admin 请求校验管理员 Token。
- 校验 Redis 会话是否有效。
- 防止普通用户 Token 访问 Admin 接口。
- 覆盖客户端伪造的身份请求头，向下游传递可信的管理员身份。

### 2.2 账号隔离

- Admin 账号不存放在 `user_information` 表中。
- Admin Token 不得访问需要普通用户身份的 Web 业务接口。
- 普通用户 Token 访问 Admin 接口时返回 `403 Forbidden`。
- 两类 Token 使用不同的 Redis key 前缀和 JWT 签名密钥。

## 3. 数据库设计

### 3.1 admin_user

管理员账号表，建议包含：

| 字段 | 说明 |
| --- | --- |
| `id` | 管理员主键 |
| `username` | 登录账号，唯一 |
| `password` | BCrypt 密码摘要 |
| `display_name` | 显示名称 |
| `email` | 通知及找回邮箱 |
| `status` | `0-禁用，1-正常` |
| `totp_enabled` | 是否启用 TOTP |
| `totp_secret` | 加密存储的 TOTP 密钥 |
| `failed_login_count` | 连续登录失败次数 |
| `locked_until` | 账号锁定截止时间 |
| `last_login_ip` | 最后登录 IP |
| `last_login_time` | 最后登录时间 |
| `password_changed_at` | 最近密码修改时间 |
| `create_time` / `update_time` | 创建和更新时间 |
| `is_deleted` | 逻辑删除标识 |

### 3.2 RBAC 表

- `admin_role`：角色编码、角色名称、状态和备注。
- `admin_permission`：权限编码、名称、类型及资源信息。
- `admin_user_role`：管理员与角色的多对多关系。
- `admin_role_permission`：角色与权限的多对多关系。

系统预置 `SUPER_ADMIN` 角色，使用 `*` 表示全部权限。

权限编码建议格式：

```text
admin:{resource}:{action}
```

示例：

```text
admin:user:read
admin:user:update-status
admin:account:read
admin:market:read
admin:role:grant
```

### 3.3 日志表

- `admin_login_log`：记录账号、IP、设备、登录结果、失败原因和时间。
- `admin_operation_log`：记录管理员、权限、接口、操作类型、资源标识、结果、IP 和时间。

操作日志不得保存密码、Token、TOTP 密钥、身份证信息等敏感内容。

## 4. 公共接口设计

### 4.1 管理员登录

```http
POST /api/crypto-exchange/admin/auth/login
Content-Type: application/json
```

请求：

```json
{
  "username": "admin",
  "password": "Admin@123456",
  "totpCode": "123456"
}
```

`totpCode` 仅在该管理员启用 TOTP 后必填。

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "admin": {
      "id": 1,
      "username": "admin",
      "displayName": "超级管理员",
      "roles": ["SUPER_ADMIN"],
      "permissions": ["*"],
      "totpEnabled": false
    }
  }
}
```

登录失败对外统一返回“账号或密码错误”，不暴露账号是否存在。禁用、锁定和 TOTP 错误的详细原因只写入安全日志。

### 4.2 刷新 Token

```http
POST /api/crypto-exchange/admin/auth/refresh
```

```json
{
  "refreshToken": "..."
}
```

刷新成功后同时返回新的 Access Token 和 Refresh Token，原 Refresh Token 立即失效，防止重放。

### 4.3 登出

```http
POST /api/crypto-exchange/admin/auth/logout
Authorization: Bearer <accessToken>
```

删除当前管理员会话和 Refresh Token。

### 4.4 当前管理员

```http
GET /api/crypto-exchange/admin/auth/me
Authorization: Bearer <accessToken>
```

返回当前管理员的基本信息、角色和权限集合。

### 4.5 修改密码

```http
POST /api/crypto-exchange/admin/auth/change-password
Authorization: Bearer <accessToken>
```

```json
{
  "oldPassword": "OldPassword@123",
  "newPassword": "NewPassword@456"
}
```

密码修改成功后，撤销该管理员的所有现有会话，必须重新登录。

## 5. Token 和 Redis 会话设计

### 5.1 Access Token

- 有效期：30 分钟。
- JWT 必须包含 `tokenType=ADMIN`、`adminId`、`username`、`sessionId`、`issuer` 和 `audience`。
- JWT 使用 Admin 专用签名密钥，密钥从环境变量或 Nacos 安全配置读取。
- 权限不全量写入 JWT，避免权限修改后旧 Token 仍持有旧权限。

### 5.2 Refresh Token

- 有效期：7 天。
- 使用高强度随机值，Redis 中只保存 Token 哈希。
- 每次刷新时进行 Token Rotation，旧 Token 使用后立即失效。
- 发现旧 Refresh Token 被重复使用时，撤销对应会话并记录安全日志。

### 5.3 Redis Key

```text
exchange:{env}:admin:session:{sessionId}
exchange:{env}:admin:refresh:{tokenHash}
exchange:{env}:admin:permission:{adminId}
exchange:{env}:admin:login-failure:{username}
exchange:{env}:admin:login-ip:{ip}
```

禁用管理员、修改密码、修改角色或登出时，必须立即删除相关会话或权限缓存。

## 6. Gateway 认证流程

1. 前端只通过 Gateway 调用 Admin 接口。
2. 登录和刷新接口加入白名单，其他 Admin 接口均要求 Token。
3. Gateway 根据 Admin 路由使用 Admin 密钥验证 JWT 签名、过期时间、issuer 和 audience。
4. Gateway 校验 `tokenType` 必须为 `ADMIN`。
5. Gateway 检查 Redis Session 是否存在且状态正常。
6. Gateway 删除客户端伪造的管理员身份请求头，再写入可信的管理员 ID、账号和 Session ID。
7. `exchange-admin` 根据权限注解进行接口级授权。

Token 无效或会话过期返回 `401 Unauthorized`；Token 有效但权限不足返回 `403 Forbidden`。

## 7. RBAC 权限校验

Admin Controller 使用权限注解：

```java
@RequirePermission("admin:user:read")
```

权限校验流程：

1. 从 Gateway 传入的可信请求头获取管理员 ID。
2. 优先从 Redis 获取权限集合。
3. Redis 未命中时，查询管理员角色和权限并写入缓存。
4. 存在 `*` 或指定权限时放行，否则返回 403。

现有 Admin 接口建议权限：

| 接口 | 权限 |
| --- | --- |
| 查询用户 | `admin:user:read` |
| 查询用户钱包 | `admin:account:read` |
| 查询行情交易对 | `admin:market:read` |
| 管理角色与授权 | `admin:role:grant` |

## 8. TOTP 二次验证

TOTP 第一版按管理员账号选择性启用，不强制所有管理员立即绑定。

- 生成 TOTP 密钥后，密钥必须加密存储。
- 管理员需使用验证器生成的验证码确认绑定。
- 启用 TOTP 后，登录时缺少或输入错误验证码不得签发 Token。
- 重置 TOTP 必须由超级管理员执行，并写入审计日志。
- 可为每个管理员生成一组一次性恢复码，数据库仅保存其哈希。

## 9. 安全策略

### 9.1 密码

- 使用 BCrypt 存储密码摘要。
- 密码至少 10 位，包含字母、数字和特殊字符。
- 禁止在配置、SQL、日志和接口响应中保存或输出明文密码。
- 修改密码后撤销所有现有会话。

### 9.2 失败锁定和限流

- 同一账号在 15 分钟内连续失败 5 次，锁定 30 分钟。
- 对账号和 IP 分别进行登录频率限制。
- 成功登录后清空该账号的失败计数。
- 高频失败、多 IP 异常登录和 Refresh Token 重放应产生安全告警。

### 9.3 日志脱敏

禁止记录：

- 密码和 TOTP 验证码。
- 完整 JWT 和 Refresh Token。
- TOTP 密钥和恢复码。
- 包含认证信息的完整请求头。

## 10. 初始超级管理员

首个超级管理员通过部署环境变量初始化：

```text
ADMIN_INITIAL_USERNAME
ADMIN_INITIAL_PASSWORD
ADMIN_INITIAL_EMAIL
```

初始化规则：

1. 仅在 `admin_user` 表为空时执行。
2. 启动时对密码进行 BCrypt 加密后写入数据库。
3. 同时创建 `SUPER_ADMIN` 角色并关联该账号。
4. 不在启动日志中输出初始密码。
5. 首次登录后建议强制修改初始密码。
6. 数据库已存在管理员时，不得覆盖任何账号或密码。

## 11. 测试与验收

### 11.1 功能测试

- 正确账号密码可以登录。
- 账号不存在或密码错误返回统一提示。
- 禁用、删除和锁定账号无法登录。
- 启用 TOTP 的账号必须提交正确验证码。
- Access Token 过期后可通过有效 Refresh Token 刷新。
- Refresh Token 刷新后旧 Token 立即失效。
- 登出、禁用账号或修改密码后，现有会话立即失效。

### 11.2 权限测试

- 普通用户 Token 无法访问 Admin 接口。
- Admin Token 无法冒充普通用户执行交易业务。
- 无对应权限的管理员访问接口时返回 403。
- `SUPER_ADMIN` 可以访问全部 Admin 接口。
- 角色或权限修改后，Redis 权限缓存及时失效。

### 11.3 安全测试

- 伪造管理员身份请求头不能绕过 Gateway 认证。
- 连续登录失败可正确触发锁定和 IP 限流。
- 重放已轮换的 Refresh Token 会撤销会话。
- 日志中不出现密码、完整 Token 或 TOTP 密钥。
- 初始管理员只创建一次，重启不会覆盖已有数据。

## 12. 建议实施顺序

1. 为 `exchange-admin` 接入独立 MySQL、Flyway 和 Redis。
2. 实现管理员、角色、权限及日志表。
3. 实现初始超级管理员安全初始化。
4. 实现登录、Token 刷新、登出和密码修改。
5. 改造 Gateway，实现 Admin 与 Web Token 隔离。
6. 实现 RBAC 权限注解、缓存和现有 Admin 接口授权。
7. 实现 TOTP 绑定、校验和重置。
8. 完成安全、权限、并发会话及审计测试后再上线。

