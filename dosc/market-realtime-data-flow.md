# 行情实时数据链路实现

## 目标

行情实时链路从“每条 Binance WebSocket 数据直接写 Redis”调整为：

```text
datasource:
Binance WS -> ConcurrentHashMap -> RabbitMQ topic -> 每秒 Redis 快照

business-market:
RabbitMQ consumer -> ConcurrentHashMap -> 前端 WS
Redis 只做启动恢复和断流兜底
```

这样可以让 Redis 从高频实时主链路中退出来，降低每条行情的序列化、网络写入和 TTL 更新压力。

## datasource 流程

`exchange-module-datasource` 启动 `BinanceWsClient` 后订阅 Binance combined streams。

收到消息后按 `data.e` 分流：

| Binance event | 本地类型 | 说明 |
| --- | --- | --- |
| `24hrTicker` | `ticker` | 24小时行情 |
| `depthUpdate` | `depth` | 深度增量 |
| `trade` | `trade` | 最新成交 |
| `kline` | `kline` | K线 |

处理顺序：

1. 反序列化为 datasource domain 对象。
2. 写入 `MarketMemoryCache`。
3. 通过 `MarketDataPublisher` 发送 RabbitMQ topic 消息。
4. `MarketRedisSnapshotService` 每秒从内存读取最新值并写 Redis。

RabbitMQ：

| 项 | 值 |
| --- | --- |
| producer exchange | `exchange.market.data` |
| consumer queue | `queue.market.data.business` |
| routing key pattern | `market.binance.#` |
| routing key example | `market.binance.ticker.btcusdt` |
| bizType | `MARKET_DATA` |

datasource 只声明并发布到 topic exchange；business-market 声明自己的消费队列并绑定 `market.binance.#`。这样新增其它行情消费者时，只需要新增自己的队列绑定。

Redis 快照 key：

| 类型 | key |
| --- | --- |
| ticker | `market:ticker:{symbol}` |
| depth | `market:depth:{symbol}` |
| trade | `market:trade:{symbol}` |
| kline | `kline:latest:{symbol}:{interval}` |

## business-market 流程

`exchange-business-market` 通过 `MarketDataConsumer` 监听 `queue.market.data.business`。

收到 MQ 消息后：

1. 将 `MqMessage.payload` 转换为 `MarketStreamMessageVo`。
2. 调用 `MarketCacheService.update` 更新本地 `ConcurrentHashMap`。
3. 调用 `MarketWebSocketPublisher.publish`，把本次变化直接推送给匹配的前端订阅。
4. MQ 消息处理完成后进行 ACK。

服务启动时，`MarketCacheWarmupRunner` 从数据库读取启用的交易对，再从 Redis 预热这些交易对的本地缓存。数据库只负责交易对查询，不保存或查询 ticker、depth、trade、kline 行情。

WebSocket 相关类职责：

| 类 | 职责 |
| --- | --- |
| `MarketWebSocketHandler` | 处理连接、订阅、取消订阅和参数校验 |
| `MarketWebSocketSessionRegistry` | 保存会话和订阅，按 symbol/type/interval 匹配接收者 |
| `MarketWebSocketPublisher` | 发送连接响应、初始快照和实时增量消息 |

旧实现中的 200ms 定时扫描已删除。现在是事件到达即推送，不再由每个客户端按 `periodMs` 轮询本地缓存。

## 前端 WebSocket

连接地址：

```text
ws://{business-market-host}:8083/ws/market
```

订阅消息：

```json
{
  "action": "subscribe",
  "symbol": "BTCUSDT",
  "interval": "1m",
  "types": ["ticker", "depth", "trade", "kline"]
}
```

订阅成功后，服务端先发送一次完整快照：

```json
{
  "type": "snapshot",
  "timestamp": 1785990000000,
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

后续每收到一条 MQ 行情，只发送本次变化：

```json
{
  "type": "market",
  "timestamp": 1785990000100,
  "data": {
    "source": "binance",
    "type": "trade",
    "symbol": "BTCUSDT",
    "interval": null,
    "payload": {},
    "timestamp": 1785990000100
  }
}
```

取消订阅不会断开 WebSocket，客户端之后仍可重新订阅：

```json
{"action": "unsubscribe"}
```

## REST 查询

Binance 历史/快照数据仍通过 datasource REST 获取，business-market 代理给前端或其它业务模块。

datasource：

```text
GET /datasource/binance/ticker?symbol=BTCUSDT
GET /datasource/binance/depth?symbol=BTCUSDT&limit=100
GET /datasource/binance/trades?symbol=BTCUSDT&limit=100
GET /datasource/binance/klines?symbol=BTCUSDT&interval=1m&limit=500
```

business-market：

```text
GET /market/binance/ticker?symbol=BTCUSDT
GET /market/binance/depth?symbol=BTCUSDT&limit=100
GET /market/binance/trades?symbol=BTCUSDT&limit=100
GET /market/binance/klines?symbol=BTCUSDT&interval=1m&limit=500
GET /market/binance/cache?symbol=BTCUSDT&interval=1m&types=ticker,depth,trade,kline
```

## 当前取舍

- ticker、depth、kline 当前保存最新值。
- trade 当前保存最新一条成交。
- Redis 每秒写一次快照，用于服务重启恢复和 MQ 断流兜底。
- depth 目前缓存的是 Binance 增量消息，不是完整订单簿。后续如果要完整 order book，需要引入 REST depth snapshot + update id 连续性校验。

## 后续优化

1. trade 从“最新一条”扩展为每个 symbol 最近 N 条 ring buffer。
2. depth 从“最新增量”升级为本地 order book。
3. MQ 从 RabbitMQ 升级到 Kafka/Pulsar，以支持高吞吐和事件回放。
4. K线/trade 历史落 ClickHouse 或 TimescaleDB，支撑回测和历史查询。
