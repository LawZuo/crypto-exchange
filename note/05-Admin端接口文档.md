# Admin 端接口文档

## 1. 基本信息

| 项目 | 值 |
|---|---|
| 网关 Base URL | `http://<host>:18080/api/crypto-exchange/admin` |
| Admin 服务直连地址 | `http://<host>:18301`，仅限本地调试 |
| Content-Type | `application/json` |
| 鉴权方式 | `Authorization: Bearer <token>` |
| Swagger | `http://<host>:18080/swagger-ui.html`，选择 `admin` |

## 2. 当前状态

Admin 聚合服务当前只实现三个只读查询接口：查询用户、查询用户钱包和查询交易对。

当前没有实现：

- 管理员登录接口。
- 管理员用户表和后台会话。
- 角色、菜单和权限点。
- 管理员操作审计。
- 用户冻结、KYC 审核、资产调整等管理写接口。

网关会要求 Admin 路径携带有效 Token，但目前没有验证该 Token 是否属于管理员。因此这些接口只适合开发联调，不应直接作为正式生产后台权限边界。

## 3. 统一响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

未携带 Token：

```json
{
  "code": 401,
  "message": "未提供认证token",
  "data": null
}
```

所有请求头：

```http
Authorization: Bearer <token>
```

## 4. 接口总览

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/management/users/{username}` | 按用户名查询用户 |
| GET | `/management/accounts/{userId}/wallet` | 按用户 ID 查询钱包 |
| GET | `/management/market/symbols` | 查询交易对列表 |

## 5. 用户查询

```http
GET /management/users/{username}
Authorization: Bearer <token>
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 用户登录名 |

请求示例：

```bash
curl "http://localhost:18080/api/crypto-exchange/admin/management/users/alice" \
  -H 'Authorization: Bearer <token>'
```

响应数据：

```json
{
  "id": 1,
  "uid": "U10001",
  "username": "alice",
  "name": "Alice",
  "email": "alice@example.com",
  "status": 1,
  "kycStatus": 1,
  "lastLoginTime": "2026-08-28T09:00:00",
  "lastLoginIp": "127.0.0.1",
  "registerIp": "127.0.0.1",
  "createdTime": "2026-08-01T10:00:00",
  "updateTime": "2026-08-28T09:00:00"
}
```

## 6. 钱包查询

```http
GET /management/accounts/{userId}/wallet
Authorization: Bearer <token>
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| userId | long | 是 | 用户数据库 ID |

响应数据：

```json
{
  "id": 10,
  "userId": 1,
  "currency": "USDT",
  "walletType": 1,
  "balance": "1000.00000000",
  "freezeBalance": "100.00000000",
  "availableBalance": "900.00000000",
  "totalBalance": "1000.00000000",
  "address": null,
  "network": null,
  "createTime": "2026-08-01T10:00:00",
  "updateTime": "2026-08-28T10:00:00",
  "remark": null,
  "status": "1"
}
```

> 当前下游接口返回单个 `AccountWalletVo`。如果一个用户存在多币种钱包，后续应明确默认币种或改为钱包列表接口。

## 7. 交易对查询

```http
GET /management/market/symbols
Authorization: Bearer <token>
```

响应 `data` 是交易对数组：

```json
[
  {
    "id": 1,
    "symbol": "BTCUSDT",
    "baseCurrency": "BTC",
    "quoteCurrency": "USDT",
    "status": 1,
    "sort": 1,
    "remark": null,
    "lastPrice": 76956.42,
    "priceChange": -950.12,
    "priceChangePercent": -1.22
  }
]
```

## 8. 正式后台建议补充的接口

下列内容是演进建议，不是当前已有接口：

- `POST /admin/auth/login`：管理员登录。
- `GET /management/users`：用户分页及条件筛选。
- `PUT /management/users/{id}/status`：冻结/解冻用户。
- `GET /management/kyc`：KYC 申请分页。
- `PUT /management/kyc/{id}/review`：KYC 审核。
- `GET /management/accounts`：账户及余额查询。
- `POST /management/accounts/{id}/adjustments`：受控调账。
- `POST/PUT /management/market/symbols`：交易对配置。
- `GET /management/audit-logs`：操作审计查询。

新增写接口时必须增加管理员 RBAC、操作审计、幂等键、数据库流水和敏感操作二次确认。
