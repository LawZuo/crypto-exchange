# Crypto Exchange Web API 接口文档

## 1. 基本信息

| 项目 | 值 |
| --- | --- |
| 协议 | HTTP / HTTPS（由部署环境决定） |
| 当前服务器 | `http://101.96.227.205:18080` |
| Web API 前缀 | `/api/crypto-exchange/web` |
| Content-Type | 默认 `application/json`，上传接口使用 `multipart/form-data` |
| 字符编码 | UTF-8 |

当前部署环境的完整基础地址：

```text
http://101.96.227.205:18080/api/crypto-exchange/web
```

前端建议通过环境变量配置基础地址，不要把服务器 IP 硬编码在业务代码中。

## 2. 认证方式

需要登录的接口在请求头中携带登录接口返回的 JWT：

```http
Authorization: Bearer <token>
```

网关也兼容不带 `Bearer` 前缀的 Token，但前端统一使用 Bearer 格式。

以下接口免登录：

- `POST /auth/login`
- `POST /auth/register`
- `GET /market/**`

用户、钱包、上传和邮件验证码接口当前均需要登录。

## 3. 通用响应

所有业务接口使用统一响应结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | integer | 业务状态码；`200` 表示成功 |
| `message` | string | 响应信息 |
| `data` | any / null | 业务数据；失败时通常为 `null` |

常用状态码：

| code | 含义 |
| --- | --- |
| `200` | 操作成功 |
| `400` | 请求参数错误 |
| `401` | 未登录、Token 无效或登录已过期 |
| `403` | 无权限访问 |
| `404` | 资源不存在 |
| `500` | 操作失败或服务器内部错误 |
| `10001` | 用户不存在 |
| `10002` | 用户已存在 |
| `10003` | 用户名或密码错误 |
| `10004` | 账号已被禁用 |

鉴权失败示例（HTTP 状态码同时为 `401`）：

```json
{
  "code": 401,
  "message": "未提供认证token",
  "data": null
}
```

---

## 4. 认证接口

### 4.1 用户登录

```http
POST /auth/login
```

鉴权：否

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 用户名 |
| `password` | string | 是 | 登录密码 |

请求示例：

```json
{
  "username": "demo",
  "password": "123456"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1",
    "username": "demo",
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expireTime": "1787832000000",
    "loginTime": "1787745600000",
    "user": {
      "id": 1,
      "uid": "U10000001",
      "username": "demo",
      "name": "Demo User",
      "email": "demo@example.com",
      "status": 1,
      "kycStatus": 0,
      "lastLoginTime": "2026-08-27T10:00:00",
      "lastLoginIp": "127.0.0.1",
      "registerIp": "127.0.0.1",
      "createdTime": "2026-08-01T10:00:00",
      "updateTime": "2026-08-27T10:00:00"
    }
  }
}
```

前端登录成功后保存 `data.token`，后续受保护请求放入 `Authorization` 请求头。

### 4.2 用户注册

```http
POST /auth/register
```

鉴权：否

请求体：

| 字段 | 类型 | 必填 | 规则 |
| --- | --- | --- | --- |
| `username` | string | 是 | 最大 64 个字符 |
| `password` | string | 是 | 6～64 个字符 |
| `email` | string | 是 | 有效邮箱，最大 128 个字符 |

请求示例：

```json
{
  "username": "demo",
  "password": "123456",
  "email": "demo@example.com"
}
```

成功响应中的 `data` 为新用户 ID：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 10001
}
```

相同用户名的注册请求在 30 秒内具有幂等保护，请勿连续重复提交。

### 4.3 用户登出

```http
POST /auth/logout
```

鉴权：是

```http
Authorization: Bearer <token>
```

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "logout"
}
```

后端会删除 Redis 中的登录 Token，使其立即失效。前端收到成功响应后也应清除本地 Token。

---

## 5. 行情接口

行情接口均免登录。`symbol` 建议使用大写交易对，例如 `BTCUSDT`。

### 5.1 查询交易对

```http
GET /market/symbols
```

请求参数：无

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "symbol": "BTCUSDT",
      "baseCurrency": "BTC",
      "quoteCurrency": "USDT",
      "pricePrecision": 2,
      "quantityPrecision": 6,
      "minOrderQuantity": 0.00001,
      "minOrderAmount": 5,
      "status": 1,
      "sort": 1,
      "remark": null,
      "createTime": "2026-08-01T10:00:00",
      "updateTime": "2026-08-01T10:00:00"
    }
  ]
}
```

### 5.2 查询 24 小时行情

```http
GET /market/ticker?symbol=BTCUSDT
```

| 参数 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `symbol` | query | string | 是 | 交易对 |

`data` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `symbol` | string | 交易对 |
| `priceChange` | decimal | 价格变化 |
| `priceChangePercent` | decimal | 价格变化百分比 |
| `weightedAvgPrice` | decimal | 加权平均价 |
| `prevClosePrice` | decimal | 前收盘价 |
| `lastPrice` | decimal | 最新成交价 |
| `lastQuantity` | decimal | 最新成交量 |
| `bidPrice` / `bidQuantity` | decimal | 最优买价 / 数量 |
| `askPrice` / `askQuantity` | decimal | 最优卖价 / 数量 |
| `openPrice` | decimal | 开盘价 |
| `highPrice` / `lowPrice` | decimal | 最高价 / 最低价 |
| `volume` | decimal | 基础资产成交量 |
| `quoteVolume` | decimal | 计价资产成交量 |
| `openTime` / `closeTime` | integer(int64) | 毫秒时间戳 |
| `firstTradeId` / `lastTradeId` | integer(int64) | 首笔 / 末笔成交 ID |
| `tradeCount` | integer(int64) | 成交笔数 |

### 5.3 查询盘口深度

```http
GET /market/depth?symbol=BTCUSDT&limit=20
```

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `symbol` | string | 是 | 交易对 |
| `limit` | integer | 否 | 返回档位数量 |

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "lastUpdateId": 123456789,
    "bids": [[65000.10, 0.125], [64999.90, 0.300]],
    "asks": [[65000.20, 0.080], [65001.00, 0.500]]
  }
}
```

`bids` 和 `asks` 的每一项结构均为 `[价格, 数量]`。

### 5.4 查询最近成交

```http
GET /market/trades?symbol=BTCUSDT&limit=20
```

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `symbol` | string | 是 | 交易对 |
| `limit` | integer | 否 | 返回数量 |

成功响应中的成交项：

```json
{
  "id": 987654321,
  "price": 65000.10,
  "quantity": 0.001,
  "quoteQuantity": 65.0001,
  "time": 1787745600000,
  "buyerMaker": false,
  "bestMatch": true
}
```

### 5.5 查询 K 线

```http
GET /market/klines?symbol=BTCUSDT&interval=1m&limit=100
```

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `symbol` | string | 是 | 交易对 |
| `interval` | string | 是 | K 线周期，例如 `1m`、`5m`、`15m`、`1h`、`1d` |
| `startTime` | integer(int64) | 否 | 开始时间，毫秒时间戳 |
| `endTime` | integer(int64) | 否 | 结束时间，毫秒时间戳 |
| `limit` | integer | 否 | 返回数量 |

成功响应中的 K 线项：

```json
{
  "openTime": 1787745600000,
  "openPrice": 64900.00,
  "highPrice": 65100.00,
  "lowPrice": 64850.00,
  "closePrice": 65000.00,
  "volume": 120.50,
  "closeTime": 1787745659999,
  "quoteAssetVolume": 7820000.00,
  "tradeCount": 4500,
  "takerBuyBaseAssetVolume": 60.20,
  "takerBuyQuoteAssetVolume": 3910000.00
}
```

### 5.6 查询行情缓存快照

```http
GET /market/cache?symbol=BTCUSDT&interval=1m&types=ticker,depth,trade,kline
```

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `symbol` | string | 是 | 交易对 |
| `interval` | string | 否 | K 线周期 |
| `types` | string / string[] | 否 | 数据类型列表：`ticker`、`depth`、`trade`、`kline` |

数组参数也可重复传递：

```text
?symbol=BTCUSDT&types=ticker&types=depth
```

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "symbol": "BTCUSDT",
    "interval": "1m",
    "ticker": {},
    "depth": {},
    "trade": {},
    "kline": {}
  }
}
```

未请求或暂无数据的类型可能返回 `null`。

---

## 6. 用户与钱包接口

### 6.1 查询用户信息

```http
GET /user/{username}
Authorization: Bearer <token>
```

| 参数 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `username` | path | string | 是 | 用户名 |

成功响应的 `data` 为 `UserVo`：

```json
{
  "id": 1,
  "uid": "U10000001",
  "username": "demo",
  "name": "Demo User",
  "email": "demo@example.com",
  "status": 1,
  "kycStatus": 0,
  "lastLoginTime": "2026-08-27T10:00:00",
  "lastLoginIp": "127.0.0.1",
  "registerIp": "127.0.0.1",
  "createdTime": "2026-08-01T10:00:00",
  "updateTime": "2026-08-27T10:00:00"
}
```

### 6.2 查询钱包余额

```http
GET /user/{userId}/wallet
Authorization: Bearer <token>
```

| 参数 | 位置 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| `userId` | path | integer(int64) | 是 | 用户 ID |

钱包字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | integer(int64) | 钱包记录 ID |
| `userId` | integer(int64) | 用户 ID |
| `currency` | string | 币种 |
| `walletType` | integer | 钱包类型 |
| `balance` | string | 余额 |
| `freezeBalance` | string | 冻结余额 |
| `availableBalance` | string | 可用余额 |
| `totalBalance` | string | 总余额 |
| `address` | string | 钱包地址 |
| `network` | string | 网络 |
| `createTime` / `updateTime` | string | 创建 / 更新时间 |
| `remark` | string | 备注 |
| `status` | string | 状态 |

金额字段使用字符串，前端计算时应使用高精度 Decimal 库，不要直接使用 JavaScript `Number`。

---

## 7. 资源接口

### 7.1 上传 KYC 图片

```http
POST /resource/upload/kyc
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `file` | binary | 是 | 图片文件 |

cURL 示例：

```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/kyc.jpg" \
  "http://101.96.227.205:18080/api/crypto-exchange/web/resource/upload/kyc"
```

### 7.2 上传头像

```http
POST /resource/upload/avatar
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

请求字段与 KYC 上传相同。

上传成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "url": "/uploads/user/avatar/example.jpg",
    "name": "example.jpg",
    "type": "image/jpeg"
  }
}
```

### 7.3 发送邮箱验证码

```http
POST /resource/email/code
Authorization: Bearer <token>
Content-Type: application/json
```

请求体：

```json
{
  "email": "demo@example.com"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 8. 前端调用示例

### Fetch

```javascript
const API_BASE = 'http://101.96.227.205:18080/api/crypto-exchange/web';

export async function request(path, options = {}) {
  const token = localStorage.getItem('token');
  const headers = new Headers(options.headers || {});

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  if (options.body && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });
  const result = await response.json();

  if (response.status === 401) {
    localStorage.removeItem('token');
  }
  if (!response.ok || result.code !== 200) {
    throw new Error(result.message || '请求失败');
  }
  return result.data;
}
```

调用示例：

```javascript
const ticker = await request('/market/ticker?symbol=BTCUSDT');

const login = await request('/auth/login', {
  method: 'POST',
  body: JSON.stringify({ username: 'demo', password: '123456' }),
});
localStorage.setItem('token', login.token);
```

## 9. Swagger

网关 Swagger UI：

```text
http://101.96.227.205:18080/swagger-ui.html
```

Web OpenAPI JSON：

```text
http://101.96.227.205:18080/api/crypto-exchange/web/v3/api-docs
```

## 10. 当前实现注意事项

1. 钱包金额以字符串传输，避免浮点精度损失。
2. 时间字段有两种格式：行情通常为毫秒时间戳，用户信息通常为 ISO-8601 字符串。
3. `GET /user/{username}` 和 `GET /user/{userId}/wallet` 当前允许查询路径参数指定的用户，前端不要将其视为服务端资源归属校验。
4. CORS 当前允许任意来源；正式生产环境建议限制允许的域名。
