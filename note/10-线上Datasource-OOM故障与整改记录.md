# 线上 Datasource OOM 故障与整改记录

## 1. 故障摘要

- 检查日期：2026-09-19。
- 应用服务器：`101.96.227.205`。
- 现象：交易对列表可返回，但行情字段为 `null`；ticker、深度、成交和 K 线接口返回业务 500。
- 直接原因：`exchange-datasource` Java 进程被 Linux OOM Killer 杀死，Nacos 中无 datasource 实例。
- Docker daemon 在 OOM 时丢失容器退出事件，导致容器错误显示为 `running`，但实际 PID 已不存在。

## 2. 故障证据

Datasource 先后两次被 OOM Killer 终止：

```text
2026-09-17 02:39:38  RSS 约 2.83GB
2026-09-18 15:10:28  RSS 约 2.87GB
```

服务器原配置：

```text
Swap: 0
Docker memory limit: unlimited
JAVA_OPTS: -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0
```

每个 Java 容器都能将整台 8GB 服务器的 75% 视为最大堆空间，无法形成服务间的内存隔离。

## 3. 代码风险

Datasource 通过 Binance WebSocket 订阅 15 个交易对的 K 线、`depth@100ms`、ticker 和 trade。原行情处理器使用 `Executors.newFixedThreadPool(4)`，内部队列没有容量上限。

当 RabbitMQ 发布或下游处理速度慢于 Binance 消息进入速度时，任务会持续堆积并消耗堆内存。

## 4. 已执行整改

### 4.1 线上应急处理

- 创建 2GB `/swapfile`，并写入 `/etc/fstab`，重启后自动挂载。
- 将 datasource 容器内存上限设置为 1GB，memory+swap 上限设置为 2GB。
- 重启 datasource，恢复 Nacos 注册和行情接口。

### 4.2 项目配置

- 通用 Java 服务默认限制为 768MB，JVM 堆为 512MB。
- market 服务限制为 1536MB，JVM 堆为 1024MB。
- datasource 限制为 1GB，JVM 堆为 768MB，memory+swap 限制为 2GB。
- Java 启用 `ExitOnOutOfMemoryError`，发生堆内存溢出时退出，由 Docker `unless-stopped` 重启。
- 所有服务增加 TCP healthcheck，能够识别“容器存在但服务端口不可用”。

### 4.3 行情队列

- 将无界队列替换为容量 2000 的有界队列。
- 队列满时丢弃最旧任务，保留最新行情，避免展示大量过期数据。
- 丢弃第 1 条及每累计 1000 条时输出告警，用于识别持续背压。

### 4.4 上线执行结果

- Datasource 修复包已重新构建并部署，SHA-256 为 `25463d11c06f69f4698b097cb196aeb0f628fd25099bde022a17ad050d740b77`。
- 已重建该项目的 10 个容器，使内存限制、JVM 参数和健康检查全部生效。
- 线上 `.env` 的通用 JVM 参数已改为 `-Xms128m -Xmx512m -XX:+ExitOnOutOfMemoryError`，原文件备份为 `.env.before-memory-fix-20260919`。
- 验收时所有容器均为 `healthy`，datasource 内存约 479MB/1GB，未使用 swap，`OOMKilled=false`。
- Gateway 下的 symbols、ticker、depth、trades 和 klines 接口均返回 HTTP 200 与成功业务码。
- 重建 market 容器时 datasource 曾出现短暂的服务发现告警；market 重新注册后接口已恢复，属于本次滚动重建的短暂现象。

## 5. 验收标准

- `docker stats` 中 datasource 内存不超过 1GB。
- `docker ps` 中 datasource 状态为 `healthy`。
- Nacos 中存在 `exchange-service-datasource` 实例。
- `/market/symbols` 返回非空行情字段。
- ticker、depth、trades 和 klines 接口均返回成功业务码。
- 持续观察 48 小时，无新的 OOM Killer 记录。

## 6. 后续建议

- 监控 datasource 队列长度、丢弃数、JVM 堆和 GC 暂停。
- 将 depth 进一步改为按交易对合并，只处理最新快照。
- 对容器 `unhealthy`和 OOM 事件配置告警，不仅依赖人工巡检。
