# Web 端接口文档

## 1. 基本信息

| 项目 | 值 |
|---|---|
| 网关 Base URL | `http://<host>:18080/api/crypto-exchange/web` |
| Web 服务直连地址 | `http://<host>:18300`，仅限本地调试 |
| Content-Type | 默认 `application/json`；上传接口为 `multipart/form-data` |
| 鉴权方式 | `Authorization: Bearer <token>` |
| Swagger | `http://<host>:18080/swagger-ui.html` |

除特别说明外，接口都经网关访问。

## 2. 统一响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

常用业务状态码：

| code | 含义 |
|---:|---|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录、Token 无效或已过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务异常 |
| 10001 | 用户不存在 |
| 10002 | 用户已存在 |
| 10003 | 用户名或密码错误 |
| 10004 | 账号已禁用 |

## 3. 鉴权规则

以下路径由网关公开放行：

- `POST /auth/login`
- `POST /auth/register`
- `GET /market/**`
- `GET /spot/symbols`

其他 Web 接口需要 Token。请求示例：

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## 4. 接口总览

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/auth/login` | 否 | 登录 |
| POST | `/auth/register` | 否 | 注册 |
| POST | `/auth/logout` | 是 | 退出登录 |
| GET | `/user/{username}` | 是 | 查询用户 |
| GET | `/user/{userId}/wallet` | 是 | 查询钱包 |
| GET | `/market/symbols` | 否 | 查询交易对 |
| GET | `/market/ticker` | 否 | 查询 24 小时行情 |
| GET | `/market/depth` | 否 | 查询盘口深度 |
| GET | `/market/trades` | 否 | 查询近期成交 |
| GET | `/market/klines` | 否 | 查询 K 线 |
| GET | `/market/cache` | 否 | 查询行情缓存快照 |
| POST | `/spot/orders` | 是 | 创建现货订单 |
| GET | `/spot/orders` | 是 | 查询当前用户现货订单 |
| GET | `/spot/trades` | 是 | 查询当前用户现货成交记录 |
| GET | `/spot/symbols` | 否 | 查询现货交易对 |
| GET | `/spot/account-flows` | 是 | 查询当前用户现货账户流水 |
| POST | `/resource/upload/kyc` | 是 | 上传 KYC 图片 |
| POST | `/resource/upload/avatar` | 是 | 上传头像 |
| POST | `/resource/email/code` | 是 | 发送邮箱验证码 |

## 5. 认证接口

### 5.1 登录

```http
POST /auth/login
Content-Type: application/json
```

请求：

```json
{
  "username": "alice",
  "password": "123456"
}
```

成功响应的 `data`：

```json
{
  "id": "1",
  "username": "alice",
  "token": "<jwt-token>",
  "expireTime": "2026-08-29T10:00:00",
  "loginTime": "2026-08-28T10:00:00",
  "user": {
    "id": 1,
    "uid": "U10001",
    "username": "alice",
    "name": null,
    "email": "alice@example.com",
    "status": 1,
    "kycStatus": 0
  }
}
```

### 5.2 注册

```http
POST /auth/register
Content-Type: application/json
```

请求字段：

| 字段 | 类型 | 必填 | 规则 |
|---|---|---|---|
| username | string | 是 | 非空，最长 64 字符 |
| password | string | 是 | 6～64 字符 |
| email | string | 是 | 合法邮箱，最长 128 字符 |

```json
{
  "username": "alice",
  "password": "123456",
  "email": "alice@example.com"
}
```

成功时 `data` 为新用户 ID。

### 5.3 退出

```http
POST /auth/logout
Authorization: Bearer <token>
```

退出后当前 Token 从 Redis 会话中删除。

## 6. 用户与账户接口

### 6.1 查询用户

```http
GET /user/{username}
Authorization: Bearer <token>
```

响应数据字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | long | 数据库 ID |
| uid | string | 业务用户编号 |
| username | string | 登录账号 |
| name | string | 姓名 |
| email | string | 邮箱 |
| status | integer | 用户状态 |
| kycStatus | integer | KYC 状态 |
| lastLoginTime | datetime | 最后登录时间 |
| lastLoginIp | string | 最后登录 IP |
| registerIp | string | 注册 IP |
| createdTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

### 6.2 查询用户钱包

```http
GET /user/{userId}/wallet
Authorization: Bearer <token>
```

响应 `data` 包含：`id`、`userId`、`currency`、`walletType`、`balance`、`freezeBalance`、`availableBalance`、`totalBalance`、`address`、`network`、`status`、`remark`、`createTime`、`updateTime`。

> 当前接口按 `userId` 查询，代码层未强制校验路径用户 ID 必须等于当前登录用户 ID。正式上线前应补充资源归属校验。

## 7. 行情接口

行情接口均无需登录。交易对参数建议使用大写，例如 `BTCUSDT`。

### 7.1 交易对列表

```http
GET /market/symbols
```

每条记录包含 `symbol`、`baseCurrency`、`quoteCurrency`、价格/数量精度、最小下单数量、最小下单金额、状态和排序等信息。

### 7.2 24 小时行情

```http
GET /market/ticker?symbol=BTCUSDT
```

响应包含最新价、涨跌额、涨跌幅、买卖一价、最高价、最低价、成交量和统计时间等 Binance Ticker 字段。

### 7.3 深度

```http
GET /market/depth?symbol=BTCUSDT&limit=20
```

| 参数 | 必填 | 说明 |
|---|---|---|
| symbol | 是 | 交易对 |
| limit | 否 | 深度档位 |

响应：

```json
{
  "lastUpdateId": 123456,
  "bids": [[65000.10, 0.25]],
  "asks": [[65000.20, 0.18]]
}
```

### 7.4 近期成交

```http
GET /market/trades?symbol=BTCUSDT&limit=100
```

每条成交包含 `id`、`price`、`quantity`、`quoteQuantity`、`time`、`buyerMaker` 和 `bestMatch`。

### 7.5 K 线

```http
GET /market/klines?symbol=BTCUSDT&interval=1m&limit=100
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| symbol | string | 是 | 交易对 |
| interval | string | 是 | 周期，如 `1m`、`5m`、`1h`、`1d` |
| startTime | long | 否 | 开始时间，毫秒时间戳 |
| endTime | long | 否 | 结束时间，毫秒时间戳 |
| limit | integer | 否 | 返回条数 |

每条 K 线包含开收盘时间、开高低收、成交量、成交额、成交笔数和主动买入量。

### 7.6 行情缓存快照

```http
GET /market/cache?symbol=BTCUSDT&interval=1m&types=ticker,depth,trade,kline
```

`types` 是可选的字符串列表。也可使用重复参数形式：

```text
types=ticker&types=depth
```

响应 `data` 包含 `symbol`、`interval`、`ticker`、`depth`、`trade`、`kline`；未请求或无缓存的类型可能为 `null`。

## 8. 现货接口

除交易对列表外，现货接口都需要 Token。用户 ID 由 Web 服务从 Token 中获取，请求不接受客户端传入的 `userId`。

### 8.1 创建现货订单

```http
POST /spot/orders
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "symbol": "BTCUSDT",
  "side": 1,
  "orderType": 1,
  "quantity": 0.001,
  "price": 65000
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| symbol | string | 是 | 现货交易对，如 `BTCUSDT` |
| side | integer | 是 | `1` 买入，`2` 卖出 |
| orderType | integer | 是 | `1` 限价单，`2` 市价单 |
| quantity | decimal | 是 | 委托数量 |
| price | decimal | 限价单必填 | 委托价格；市价单可为 `0` 或不传 |

成功时 `data` 为订单号，格式为 `SP_O_XXXXXXXX`。

### 8.2 查询现货订单

```http
GET /spot/orders
Authorization: Bearer <token>
```

返回当前登录用户的订单列表，包含订单号、交易对、买卖方向、订单类型、委托价格、数量、成交数量、状态和时间等信息。

### 8.3 查询现货成交记录

```http
GET /spot/trades
Authorization: Bearer <token>
```

返回当前登录用户的成交记录，包含成交号、订单号、交易对、成交价格、数量、成交额、手续费及手续费币种等信息。

### 8.4 查询现货交易对

```http
GET /spot/symbols
```

该接口无需登录，返回 `spot_symbol` 中配置的现货交易对、基础币种、计价币种、精度、最小下单数量和状态等信息。

### 8.5 查询现货账户流水

```http
GET /spot/account-flows
Authorization: Bearer <token>
```

返回当前登录用户的资产变动流水，包含币种、变动金额、变动前后余额、业务类型和关联业务 ID。

## 9. 资源接口

### 9.1 上传 KYC 图片

```http
POST /resource/upload/kyc
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

表单字段：`file`。

### 9.2 上传头像

```http
POST /resource/upload/avatar
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

表单字段：`file`。

上传成功数据：

```json
{
  "url": "/uploads/xxx.png",
  "name": "xxx.png",
  "type": "image/png"
}
```

### 9.3 发送邮箱验证码

```http
POST /resource/email/code
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "email": "alice@example.com"
}
```

## 10. cURL 示例

```bash
BASE_URL=http://localhost:18080/api/crypto-exchange/web

curl -X POST "$BASE_URL/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"123456"}'

curl "$BASE_URL/market/ticker?symbol=BTCUSDT"

curl "$BASE_URL/spot/symbols"

curl -X POST "$BASE_URL/spot/orders" \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{"symbol":"BTCUSDT","side":1,"orderType":1,"quantity":0.001,"price":65000}'

curl "$BASE_URL/user/alice" \
  -H 'Authorization: Bearer <token>'
```
