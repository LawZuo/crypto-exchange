# Redis 缓存优化说明

## 1. 文档目的

本文档记录当前项目 Redis 缓存的主要问题、可能影响、优化方案和建议实施顺序。

本次检查以项目代码为主，未包含线上 Redis 的内存占用、命中率、慢命令和实时连接数据。

## 2. 当前 Redis 用途

项目中 Redis 主要用于：

- 用户登录 Token 校验。
- 接口幂等和防重复提交。
- MQ 消息消费去重。
- 行情 ticker、深度、最新成交和 K 线快照。
- 24 小时涨幅榜、跌幅榜和成交额榜。
- 现货下单时读取最新行情。

## 3. 优化项

### 3.1 Market 本地缓存可能永久返回过期数据

#### 现状

`MarketCacheServiceImpl` 通过多个 `ConcurrentHashMap` 保存 ticker、depth、trade 和 kline 数据。这些本地缓存没有过期时间、最大容量和清理机制。

#### 影响

- Binance 或 datasource 断线后，market 服务可能一直返回旧行情。
- Redis 中的 180 秒 TTL 无法约束本地缓存。
- 交易对被禁用或删除后，其数据仍可能保留在内存中。
- 长期运行时，缓存条目可能持续增长。

#### 优化方案

将 `ConcurrentHashMap` 替换为 Caffeine 缓存，并设置最大容量和过期时间：

| 缓存类型 | 建议 TTL |
| --- | --- |
| ticker | 5～10 秒 |
| depth | 3～5 秒 |
| trade | 5～10 秒 |
| kline | 1～5 分钟，按周期调整 |

交易对被禁用、删除或取消订阅时，应主动清理对应的本地缓存。

### 3.2 榜单刷新产生大量 Redis 网络请求

#### 现状

`MarketRankingSnapshotService` 每秒刷新榜单，对每个交易对分别执行两次 `ZADD`，然后读取完整 ZSet 清理失效成员。

例如有 500 个交易对时，每秒可能产生 1000 次以上的独立 Redis 请求。

#### 优化方案

- 使用 Redis Pipeline 批量提交榜单更新。
- 使用 `ZSetOperations.add(Set<TypedTuple>)` 批量写入排序成员。
- 仅在有效交易对集合发生变化时清理失效成员。
- 将 ZSet 更新、过期时间和榜单更新时间放在同一个 Pipeline 中执行。

### 3.3 榜单查询存在全量读取和 N+1 问题

#### 现状

榜单接口即使只需要返回 10 条，仍会通过 `ZRANGE 0 -1` 读取完整榜单，随后再对每个交易对单独读取 ticker。

同时，榜单查询调用 `listSymbols(1)` 时已经读取过一次 ticker，后续又会重复读取。

#### 优化方案

- 根据 `limit` 限制 ZSet 读取范围，例如 `0` 到 `limit - 1`。
- 如果需要过滤过期成员，可一次读取 `limit * 2` 条，不要读取全部成员。
- RedisService 增加 `multiGet` 方法，一次读取多个 ticker。
- 榜单内部查询交易对时，不应调用会自动填充 ticker 的 `listSymbols`。

### 3.4 交易对列表存在 Redis N+1 查询

#### 现状

`MarketSymbolServiceImpl#listSymbols` 会遍历交易对，并对每个交易对单独读取 ticker 缓存。

当返回 100 个交易对时，将产生 100 次 Redis 往返请求。

#### 优化方案

- 先生成所有 ticker key。
- 通过 `multiGet` 一次获取所有 ticker。
- 按 key 或交易对的顺序将行情数据回填到返回结果。

### 3.5 幂等锁存在过期后误删其他请求锁的风险

#### 现状

`IdempotentAspect` 的锁值固定为 `1`。业务失败时直接删除锁 key，没有校验锁的所有者。

当第一个请求执行时间超过锁 TTL 时，锁可能过期并被第二个请求重新获取。此时第一个请求如果失败，会删除第二个请求的锁。

#### 优化方案

- 每次获取锁时使用随机 UUID 作为锁值。
- 释放锁时通过 Lua 脚本先比较锁值，一致时才删除。
- 如后续分布式锁场景增多，可考虑引入 Redisson。

### 3.6 Redis Key 和 TTL 定义分散

#### 现状

Redis key 和 TTL 分散定义在 `RedisKeyConstants`、`KlineCacheService`、`MarketRankingSnapshotService` 及 Nacos 配置中。部分缓存 TTL 直接在 Java 代码中硬编码为 180 秒。

#### 优化方案

建议统一 key 格式：

```text
exchange:{environment}:{module}:{type}:{business-key}
```

示例：

```text
exchange:prod:market:ticker:btcusdt
exchange:prod:market:depth:btcusdt
exchange:prod:market:rank:change:24h
exchange:prod:auth:token:{tokenHash}
```

具体建议：

- 通用 key 前缀放入 `RedisKeyConstants`。
- TTL 放入 Nacos 或各服务配置文件。
- 对 symbol 统一使用小写格式。
- 在 key 中加入环境名，防止本地、测试和生产环境互相覆盖。

### 3.7 不同性质的缓存应区分运行策略

行情缓存允许过期和淘汰，但登录 Token、幂等锁和 MQ 去重数据被提前淘汰可能导致认证或重复消费问题。

建议：

- 行情缓存可使用允许 LRU/LFU 淘汰的 Redis 实例。
- Token、幂等锁和 MQ 去重建议使用 `noeviction` 策略或独立的 Redis 实例。
- Redis database 编号只能进行逻辑隔离，不能隔离内存和淘汰策略。

## 4. 建议实施顺序

| 优先级 | 优化项 | 目标 |
| --- | --- | --- |
| P0 | 本地行情缓存增加 TTL 和容量限制 | 防止返回永久过期数据 |
| P0 | 修复幂等锁的安全释放逻辑 | 防止误删其他请求的锁 |
| P1 | 榜单写入改为 Pipeline/批量 ZADD | 降低 Redis QPS 和网络开销 |
| P1 | 榜单和交易对行情改为 `multiGet` | 消除 N+1 Redis 查询 |
| P2 | 统一 Redis key 和 TTL 配置 | 降低维护成本和环境冲突风险 |
| P2 | 拆分行情和业务关键缓存 | 区分内存淘汰与可用性策略 |

## 5. 优化后验证项

- datasource 断开 Binance 后，超过 TTL 不再返回旧行情。
- 禁用 market 交易对后，本地缓存和 Redis 榜单中的相关数据能够被清理。
- 交易对数量增加时，榜单每秒不再线性增加大量 Redis 往返请求。
- `limit=10` 的榜单查询不会读取完整 ZSet。
- 交易对列表使用批量方式读取 ticker。
- 幂等锁过期后被其他请求重新获取时，原请求不能删除新锁。
- 通过 Redis `INFO`、`SLOWLOG`、命中率和客户端连接数持续检查优化效果。

