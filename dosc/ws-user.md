# 用户私有化 WebSocket 连接实现计划

## Summary
新增独立通知中心，不写具体充值/提现/KYC业务逻辑，只提供通用入口：业务方发 MQ 事件，通知模块负责落库、实时 WebSocket 推送、离线历史查询和已读状态。

默认模块：
- `exchange-api-notification`
- `exchange-module-notification`

默认服务：
- application name：`service-notification`
- port：`8084`
- WebSocket：`/ws/notification?token={jwt}`

## Key Changes
- 在 `exchange-api` 下新增 `exchange-api-notification`，并注册到 `exchange-api/pom.xml`。
- 在 `exchange-module` 下新增 `exchange-module-notification`，并注册到 `exchange-module/pom.xml`。
- 新增通用事件模型 `NotificationEventDto`：
    - `eventId`
    - `targetType`: `USER` / `ALL`
    - `userId`: `targetType=USER` 时必填
    - `eventType`: `RECHARGE_ARRIVED`、`WITHDRAW_SUCCESS`、`KYC_APPROVED`、`SYSTEM_ANNOUNCEMENT` 等
    - `title`
    - `content`
    - `payload`
    - `occurredAt`
- 新增 MQ：
    - exchange：`exchange.notification`
    - queue：`queue.notification.business`
    - routing key：`notification.event.#`
    - biz type：`NOTIFICATION_EVENT`
- 新增表：
    - `notification_message`：保存用户私有通知。
    - `notification_announcement`：保存全体公告。
    - `notification_read`：保存用户对通知/公告的已读状态。
- MQ 消费规则：
    - `targetType=USER`：按 `eventId` 幂等，保存用户通知，推送该用户所有在线 WS 连接。
    - `targetType=ALL`：保存公告，推送当前所有在线用户，不给每个用户批量插入私有通知。
    - 重复 `eventId` 不重复落库、不重复推送。

## WebSocket & REST
- WS 握手时从 query token 解析 `userId`：
    - 成功：保存 `userId -> 多个 WebSocketSession`。
    - 失败：拒绝连接。
- WS 推送格式：

```json
{
  "type": "notification",
  "eventType": "RECHARGE_ARRIVED",
  "timestamp": 1786065600000,
  "data": {
    "id": 10001,
    "targetType": "USER",
    "title": "充值到账",
    "content": "您的 USDT 充值已到账",
    "payload": {}
  }
}
```

- REST 接口：
    - `GET /notification/list?readStatus=&pageNum=1&pageSize=20`
    - `GET /notification/unread-count`
    - `POST /notification/read/{targetType}/{id}`
    - `POST /notification/read-all`
- REST 用户身份从网关透传 header / `SecurityContextHolder` 获取，不允许前端传 `userId` 查询别人的通知。

## Test Plan
- WS：
    - token 有效连接成功，并绑定正确 `userId`。
    - token 无效连接失败。
    - 同一用户多端连接时全部收到通知。
    - 用户断开后 session 被移除。
- MQ：
    - `USER` 事件落库并只推送目标用户。
    - `ALL` 公告落库并推送所有在线用户。
    - 重复 `eventId` 不重复插入。
- REST：
    - 用户只能查询自己的私有通知和全体公告。
    - 单条已读、全部已读、未读数正确。
- 编译：
    - `mvn -q -pl exchange-api/exchange-api-notification,exchange-module/exchange-module-notification -am compile`

## Assumptions
- 本次只实现通知模块和通用 MQ/WS/REST 能力，不改充值、提现、KYC 业务流程。
- 后续业务模块只需要按 `NotificationEventDto` 发 MQ，即可接入通知中心。
- 公告不为全体用户逐条落库，避免用户量大时产生批量写入压力；用户已读状态单独记录在 `notification_read`。
