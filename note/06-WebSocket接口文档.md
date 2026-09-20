# WebSocket 接口文档

## 1. 基本信息

| 项目 | 值 |
|---|---|
| 用途 | 订阅现货实时行情 |
| 服务 | `exchange-business-market` |
| 协议 | WebSocket |
| 路径 | `/ws/market` |
| 本地网关 | `ws://localhost:18080/ws/market` |
| 服务器网关 | `ws://101.96.227.205:18080/ws/market` |
| HTTPS 环境 | `wss://<domain>/ws/market` |

> 本地和线上前端都必须通过 Gateway 连接，不允许前端直连 Market 服务的 `18470` 端口。

## 2. 连接流程

```text
前端建立 WebSocket 连接
-> 服务端返回 connected
-> 前端发送 subscribe
-> 服务端返回 subscribed
-> 服务端返回 snapshot 初始快照
-> 服务端持续推送 market 实时行情
```

同一个连接只保存一个订阅。重新发送 `subscribe` 会覆盖旧订阅。

## 3. 订阅接口

### 3.1 订阅请求

```json
{
  "action": "subscribe",
  "symbol": "BTCUSDT",
  "interval": "1m",
  "types": ["ticker", "depth", "trade", "kline"]
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| action | string | 否 | `subscribe` 或 `unsubscribe`，默认 `subscribe` |
| symbol | string | 是 | 交易对，如 `BTCUSDT`；服务端自动转大写 |
| interval | string | 否 | K 线周期，如 `1m`、`5m`、`15m`、`1h`，默认 `1m` |
| types | string[] | 否 | `ticker`、`depth`、`trade`、`kline`，默认全部 |

### 3.2 只订阅 Ticker

```json
{
  "symbol": "BTCUSDT",
  "types": ["ticker"]
}
```

### 3.3 取消订阅

```json
{
  "action": "unsubscribe"
}
```

取消订阅不会断开连接，后续可以再次发送 `subscribe`。

## 4. 服务端消息

### 4.1 连接成功

```json
{
  "type": "connected",
  "sessionId": "xxx"
}
```

### 4.2 订阅成功

```json
{
  "type": "subscribed",
  "symbol": "BTCUSDT",
  "interval": "1m",
  "types": ["ticker", "depth", "trade", "kline"]
}
```

### 4.3 初始行情快照

```json
{
  "type": "snapshot",
  "timestamp": 1786065600000,
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

### 4.4 实时行情

```json
{
  "type": "market",
  "timestamp": 1786065600000,
  "data": {
    "source": "binance",
    "type": "ticker",
    "symbol": "BTCUSDT",
    "interval": null,
    "payload": {},
    "timestamp": 1786065600000
  }
}
```

| data 字段 | 说明 |
|---|---|
| source | 行情来源，当前为 `binance` |
| type | `ticker`、`depth`、`trade` 或 `kline` |
| symbol | 交易对 |
| interval | K 线周期；非 K 线行情可为 `null` |
| payload | 实际行情数据 |
| timestamp | 行情时间戳，单位毫秒 |

`depth` 的 `bids` 和 `asks` 为二维数组，每一项格式为 `[计价币USDT价格, 当前币种数量]`，数组内元素为 JSON number。

### 4.5 取消订阅成功

```json
{
  "type": "unsubscribed"
}
```

### 4.6 错误消息

```json
{
  "type": "error",
  "message": "symbol不能为空"
}
```

| message | 原因 |
|---|---|
| `symbol不能为空` | 订阅请求未传 `symbol` |
| `action只支持subscribe或unsubscribe` | `action` 不合法 |
| `不支持的行情类型: xxx` | `types` 中存在不支持的类型 |
| `行情快照暂不可用，后续实时数据不受影响` | 初始快照读取失败 |

## 5. 前端 JavaScript 示例

```javascript
const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
const ws = new WebSocket(`${protocol}://${window.location.host}/ws/market`);

ws.onopen = () => {
  ws.send(JSON.stringify({
    action: 'subscribe',
    symbol: 'BTCUSDT',
    interval: '1m',
    types: ['ticker', 'depth', 'trade', 'kline']
  }));
};

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);

  switch (message.type) {
    case 'connected':
      console.log('WebSocket 连接成功', message.sessionId);
      break;
    case 'subscribed':
      console.log('订阅成功', message);
      break;
    case 'snapshot':
      console.log('初始快照', message.data);
      break;
    case 'market':
      console.log('实时行情', message.data.type, message.data.payload);
      break;
    case 'error':
      console.error('WebSocket 错误', message.message);
      break;
  }
};

ws.onerror = (error) => console.error('WebSocket 连接异常', error);
ws.onclose = (event) => console.log('WebSocket 已断开', event.code, event.reason);
```

本地调试时，通过本地 Gateway 连接：

```javascript
const ws = new WebSocket('ws://localhost:18080/ws/market');
```

## 6. 调试

使用 `wscat` 连接：

```bash
npx wscat -c ws://localhost:18080/ws/market
```

连接后发送：

```json
{"action":"subscribe","symbol":"BTCUSDT","interval":"1m","types":["ticker","depth","trade","kline"]}
```

## 7. 注意事项

- HTTPS 页面必须使用 `wss://`，不能连接 `ws://`。
- `ticker`、`depth`、`trade` 按 `symbol + type` 匹配推送。
- `kline` 按 `symbol + type + interval` 匹配推送。
- 实时行情按 `symbol + type + interval` 保留最新一条，每 1 秒推送一次；1 秒内的同类高频数据会被最新数据覆盖。
- 收到 `snapshot` 但没有 `market` 时，应检查 Datasource、RabbitMQ 和 Market 消费链路。
- WebSocket 当前未实现心跳消息，前端应在断开后实现延迟重连。
