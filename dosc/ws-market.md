# exchange-business-market WebSocket 连接文档

## 1. 连接地址

`exchange-business-market` 当前端口为 `8083`，WebSocket 地址：

```text
ws://localhost:8083/ws/market
```

如果通过网关或服务器部署，把 `localhost:8083` 换成实际域名和端口：

```text
ws://{business-market-host}:{port}/ws/market
wss://{domain}/ws/market
```

后端注册位置：

```text
exchange-business/exchange-business-market/src/main/java/coin/exchange/business/market/ws/MarketWebSocketConfig.java
```

核心处理类：

```text
MarketWebSocketHandler: 处理连接、订阅、取消订阅、参数校验
MarketWebSocketSessionRegistry: 保存连接和订阅关系
MarketWebSocketPublisher: 推送 connected/subscribed/snapshot/market/error 消息
MarketDataConsumer: 消费 RabbitMQ 行情并触发推送
```

## 2. 连接流程

```text
前端建立 WebSocket 连接
-> 后端返回 connected
-> 前端发送 subscribe
-> 后端返回 subscribed
-> 后端返回 snapshot 初始快照
-> RabbitMQ 有新行情时，后端持续推送 market
```

取消订阅不会断开 WebSocket 连接，前端之后可以继续发送新的 `subscribe`。

当前实现是“一个 WebSocket 连接保存一个订阅”。同一个连接重复发送 `subscribe`，新的订阅会覆盖旧订阅。

## 3. 订阅请求

### 订阅全部行情

```json
{
  "action": "subscribe",
  "symbol": "BTCUSDT",
  "interval": "1m",
  "types": ["ticker", "depth", "trade", "kline"]
}
```

字段说明：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `action` | 否 | `subscribe` 或 `unsubscribe`。不传默认 `subscribe` |
| `symbol` | 订阅时必填 | 交易对，例如 `BTCUSDT`。后端会自动转大写 |
| `interval` | 否 | K线周期，例如 `1m`、`5m`、`15m`、`1h`。不传默认 `1m` |
| `types` | 否 | 行情类型数组。支持 `ticker`、`depth`、`trade`、`kline`。不传默认订阅全部 |

### 只订阅 ticker

```json
{
  "symbol": "BTCUSDT",
  "types": ["ticker"]
}
```

### 只订阅 depth 和 trade

```json
{
  "symbol": "ETHUSDT",
  "types": ["depth", "trade"]
}
```

### 取消订阅

```json
{
  "action": "unsubscribe"
}
```

## 4. 后端响应

### connected

连接成功后立即返回：

```json
{
  "type": "connected",
  "sessionId": "xxx"
}
```

### subscribed

订阅成功后返回：

```json
{
  "type": "subscribed",
  "symbol": "BTCUSDT",
  "interval": "1m",
  "types": ["ticker", "depth", "trade", "kline"]
}
```

### snapshot

订阅成功后，后端会从本地缓存或 Redis 兜底缓存读取一次初始快照：

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

`data` 字段对应 `MarketCacheSnapshotVo`：

| 字段 | 说明 |
| --- | --- |
| `symbol` | 交易对 |
| `interval` | K线周期 |
| `ticker` | 最新 ticker |
| `depth` | 最新盘口深度 |
| `trade` | 最新成交 |
| `kline` | 最新 K线 |

如果订阅时只传了部分 `types`，快照里只保证这些类型会被查询，未订阅类型可能为空。

### market

RabbitMQ 收到新的 Binance 行情后，后端会推送实时增量：

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

`data` 字段对应 `MarketStreamMessageVo`：

| 字段 | 说明 |
| --- | --- |
| `source` | 数据源，当前为 `binance` |
| `type` | 行情类型：`ticker`、`depth`、`trade`、`kline` |
| `symbol` | 交易对 |
| `interval` | K线周期。只有 `kline` 类型需要重点关注 |
| `payload` | 实际行情数据 |
| `timestamp` | 行情时间戳 |

推送匹配规则：

```text
ticker/depth/trade: symbol + type 匹配就推送
kline: symbol + type + interval 全部匹配才推送
```

### unsubscribed

取消订阅成功：

```json
{
  "type": "unsubscribed"
}
```

### error

请求参数错误或后端处理失败：

```json
{
  "type": "error",
  "message": "symbol不能为空"
}
```

常见错误：

| message | 原因 |
| --- | --- |
| `symbol不能为空` | 订阅请求缺少 `symbol` |
| `action只支持subscribe或unsubscribe` | `action` 不是 `subscribe` 或 `unsubscribe` |
| `不支持的行情类型: xxx` | `types` 里包含非支持类型 |
| `行情快照暂不可用，后续实时数据不受影响` | 初始快照读取失败，实时推送仍会继续 |

## 5. payload 数据结构

### ticker payload

```json
{
  "symbol": "BTCUSDT",
  "priceChange": "123.45",
  "priceChangePercent": "0.12",
  "weightedAvgPrice": "65000.00",
  "prevClosePrice": "64800.00",
  "lastPrice": "65123.45",
  "lastQuantity": "0.01",
  "bidPrice": "65123.44",
  "bidQuantity": "1.2",
  "askPrice": "65123.45",
  "askQuantity": "0.8",
  "openPrice": "64800.00",
  "highPrice": "66000.00",
  "lowPrice": "64000.00",
  "volume": "1234.56",
  "quoteVolume": "80123456.78",
  "openTime": 1785979200000,
  "closeTime": 1786065600000,
  "firstTradeId": 100,
  "lastTradeId": 200,
  "tradeCount": 101
}
```

### depth payload

```json
{
  "lastUpdateId": 123456789,
  "bids": [["65123.44", "1.2"]],
  "asks": [["65123.45", "0.8"]]
}
```

`bids` 和 `asks` 每一项格式：

```text
[price, quantity]
```

### trade payload

```json
{
  "id": 123456,
  "price": "65123.45",
  "quantity": "0.01",
  "quoteQuantity": "651.2345",
  "time": 1786065600000,
  "buyerMaker": false,
  "bestMatch": true
}
```

### kline payload

```json
{
  "openTime": 1786065540000,
  "openPrice": "65100.00",
  "highPrice": "65150.00",
  "lowPrice": "65080.00",
  "closePrice": "65123.45",
  "volume": "12.34",
  "closeTime": 1786065599999,
  "quoteAssetVolume": "803456.78",
  "tradeCount": 320,
  "takerBuyBaseAssetVolume": "6.12",
  "takerBuyQuoteAssetVolume": "398765.43"
}
```

## 6. 前端示例

### 原生 JavaScript

```js
const ws = new WebSocket('ws://localhost:8083/ws/market');

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
      console.log('WS connected', message.sessionId);
      break;
    case 'subscribed':
      console.log('subscribed', message);
      break;
    case 'snapshot':
      console.log('initial snapshot', message.data);
      break;
    case 'market':
      console.log('market update', message.data.type, message.data.payload);
      break;
    case 'error':
      console.error('market ws error', message.message);
      break;
    default:
      console.log('unknown message', message);
  }
};

ws.onerror = (error) => {
  console.error('WS error', error);
};

ws.onclose = (event) => {
  console.log('WS closed', event.code, event.reason);
};
```

### 切换交易对

当前实现同一个连接只有一个订阅。切换交易对时，可以直接发送新的 `subscribe` 覆盖旧订阅：

```js
ws.send(JSON.stringify({
  action: 'subscribe',
  symbol: 'ETHUSDT',
  interval: '1m',
  types: ['ticker', 'depth', 'trade', 'kline']
}));
```

也可以先取消再订阅：

```js
ws.send(JSON.stringify({ action: 'unsubscribe' }));

ws.send(JSON.stringify({
  action: 'subscribe',
  symbol: 'ETHUSDT',
  interval: '1m',
  types: ['ticker']
}));
```

## 7. 调试方式

### wscat

安装：

```bash
npm install -g wscat
```

连接：

```bash
wscat -c ws://localhost:8083/ws/market
```

发送订阅：

```json
{"action":"subscribe","symbol":"BTCUSDT","interval":"1m","types":["ticker","depth","trade","kline"]}
```

取消订阅：

```json
{"action":"unsubscribe"}
```

## 8. 排查 checklist

1. 确认 `exchange-business-market` 已启动，端口是 `8083`。
2. 启动日志里应该能看到 `【exchange-business-market】注册Websocket`。
3. 前端连接地址必须是 `/ws/market`。
4. `symbol` 必须传，例如 `BTCUSDT`。
5. `types` 只能传 `ticker`、`depth`、`trade`、`kline`。
6. 如果只有 `snapshot` 没有后续 `market`，检查 RabbitMQ consumer 是否收到 datasource 广播。
7. 如果 `snapshot` 为空，检查 business-market 本地缓存是否已被 MQ 更新，或者 Redis 快照是否存在。
8. 如果订阅 K线没有更新，检查前端订阅的 `interval` 是否和 datasource 推送的 `interval` 一致。

## 9. 后端链路

```text
datasource Binance WS
-> datasource ConcurrentHashMap
-> RabbitMQ topic
-> business-market MarketDataConsumer
-> business-market ConcurrentHashMap
-> MarketWebSocketPublisher
-> 前端 WebSocket
```

Redis 只做启动恢复和断流兜底，不是前端 WS 的主推送链路。
