# exchange-business-spot

现货业务服务，负责现货订单、成交记录、现货交易对及账户流水。

## 运行参数

- 服务端口：`SPOT_PORT`，默认 `18473`
- 数据库：`MYSQL.SPOT.DB_URL`
- 数据库用户：`MYSQL.DB_USER`
- 数据库密码：`MYSQL.DB_PASSWORD`
- Nacos：`NACOS_SERVER_ADDR`、`NACOS_NAMESPACE`、`NACOS_USERNAME`、`NACOS_PASSWORD`

首次运行前需先创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS exchange_spot
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

启动后 Flyway 会自动创建 `spot_order`、`spot_trade`、`spot_symbol` 和
`spot_account_flow` 四张表。

## 基础接口

- `GET /spot/orders?userId={userId}`
- `GET /spot/trades?userId={userId}`
- `GET /spot/symbols`
- `GET /spot/account-flows?userId={userId}`
