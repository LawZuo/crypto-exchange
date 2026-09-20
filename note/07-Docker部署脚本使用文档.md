# Docker 部署脚本使用文档

## 1. 文档说明

项目使用以下脚本完成 Docker 打包、服务器部署和停机：

| 脚本 | 运行位置 | 作用 |
|---|---|---|
| `docker-package.sh` | 本地项目根目录 | Maven 打包并生成 Docker 发布目录 |
| `docker-server-deploy.sh` | 服务器发布目录 | 构建镜像、启停服务、查看日志和状态 |
| `stop-all.sh` | 服务器发布目录 | 只停止当前 Compose 项目的全部服务 |

## 2. 服务列表

发布包包含以下 10 个服务：

| Compose 服务 | JAR |
|---|---|
| `exchange-gateway` | `exchange-gateway.jar` |
| `exchange-web` | `exchange-web.jar` |
| `exchange-admin` | `exchange-admin.jar` |
| `exchange-auth` | `exchange-auth.jar` |
| `exchange-business-user` | `exchange-business-user.jar` |
| `exchange-business-account` | `exchange-business-account.jar` |
| `exchange-business-market` | `exchange-business-market.jar` |
| `exchange-business-spot` | `exchange-business-spot.jar` |
| `exchange-datasource` | `exchange-datasource.jar` |
| `exchange-resource` | `exchange-resource.jar` |

只向服务器宿主机映射 Gateway 端口，其他服务不对外暴露。

## 3. 环境要求

### 3.1 本地环境

- JDK 17
- Maven 3.8+
- Bash
- 可以访问 Maven 依赖仓库

### 3.2 服务器环境

- Linux
- Docker Engine
- Docker Compose v2，命令为 `docker compose`
- Bash
- 服务器可以访问 Nacos、MySQL、Redis 和 RabbitMQ

## 4. 本地生成发布包

在项目根目录执行：

```bash
chmod +x docker-package.sh
./docker-package.sh
```

脚本会先执行 Maven 打包，然后生成：

```text
dist/crypto-exchange-docker/
|-- jars/
|-- Dockerfile
|-- docker-compose.yml
|-- .dockerignore
|-- .env.example
|-- deploy.sh
`-- stop-all.sh
```

如果 JAR 已经打包完成，可以跳过 Maven：

```bash
./docker-package.sh --skip-build
```

离线打包：

```bash
MAVEN_OFFLINE=true ./docker-package.sh
```

## 5. 上传服务器

使用 `rsync`：

```bash
rsync -avP dist/crypto-exchange-docker/ \
  root@101.96.227.205:/opt/crypto-exchange/
```

或使用 `scp`：

```bash
scp -r dist/crypto-exchange-docker/* \
  root@101.96.227.205:/opt/crypto-exchange/
```

> `scp *` 不会上传 `.env.example` 等隐藏文件，建议使用 `rsync`。

## 6. 首次部署

登录服务器：

```bash
ssh root@101.96.227.205
cd /opt/crypto-exchange
chmod +x deploy.sh stop-all.sh
```

第一次执行：

```bash
./deploy.sh up
```

如果 `.env` 不存在，脚本会从 `.env.example` 创建 `.env` 并停止部署。编辑配置：

```bash
vi .env
```

至少检查：

```dotenv
IMAGE_TAG=latest
GATEWAY_HOST_PORT=18080

NACOS_SERVER_ADDR=106.12.183.54:8848
NACOS_NAMESPACE=<线上-namespace-id>
NACOS_USERNAME=nacos
NACOS_PASSWORD=<nacos-password>

REDIS_HOST=106.12.183.54
REDIS_PORT=63500
REDIS_PASSWORD=<redis-password>
REDIS_DATABASE=0
```

不能保留空值或 `change-me`。配置完成后再次执行：

```bash
./deploy.sh up
```

`up` 会依次：

1. 检查 Docker 和 Docker Compose。
2. 检查 `.env` 和所有 JAR。
3. 创建日志目录。
4. 校验 Compose 配置。
5. 构建 Docker 镜像。
6. 启动容器。
7. 显示容器状态。
8. 如有容器退出，输出最近 100 行日志并返回失败。

## 7. 常用命令

### 7.1 查看状态

```bash
./deploy.sh ps
```

### 7.2 查看全部日志

```bash
./deploy.sh logs
```

按 `Ctrl+C` 退出日志跟踪，不会停止服务。

### 7.3 查看指定服务日志

```bash
./deploy.sh logs exchange-business-market
./deploy.sh logs exchange-business-spot
```

### 7.4 重启全部服务

```bash
./deploy.sh restart
```

### 7.5 重启指定服务

```bash
./deploy.sh restart exchange-gateway exchange-web
```

### 7.6 停止服务

停止全部服务，保留容器：

```bash
./deploy.sh stop
```

只停止指定服务：

```bash
./deploy.sh stop exchange-datasource
```

重新启动已停止容器：

```bash
./deploy.sh start
```

### 7.7 删除项目容器和网络

```bash
./deploy.sh down
```

`down` 只影响当前 Compose 项目，不删除：

- Docker 镜像
- Docker volumes
- 其他 Compose 项目
- 其他 Docker 容器

### 7.8 校验 Compose 配置

```bash
./deploy.sh config
```

## 8. 只更新指定服务

例如只更新 Gateway 和 Market：

```bash
./deploy.sh up exchange-gateway exchange-business-market
```

脚本会构建并重建指定服务，不会重建其他已运行服务。

## 9. 快速停止全部项目服务

```bash
sh ./stop-all.sh
```

`stop-all.sh` 执行当前 `docker-compose.yml` 的 `stop`，只停止当前项目服务，不删除容器、网络、卷或镜像。

## 10. 服务访问

默认只对外暴露 Gateway：

```text
HTTP API:  http://<server>:18080/api/crypto-exchange/web/**
Admin API: http://<server>:18080/api/crypto-exchange/admin/**
WebSocket: ws://<server>:18080/ws/market
Swagger:   http://<server>:18080/swagger-ui.html
```

HTTPS 环境应使用 Nginx 或负载均衡器终止 TLS：

```text
HTTPS API: https://<domain>/api/crypto-exchange/web/**
WebSocket: wss://<domain>/ws/market
```

## 11. 常见问题

### 11.1 Missing file: jars/xxx.jar

发布包缺少 JAR。在本地重新执行：

```bash
./docker-package.sh
```

### 11.2 Docker daemon is unavailable

检查 Docker：

```bash
systemctl status docker
systemctl start docker
```

### 11.3 容器启动后退出

```bash
./deploy.sh ps
./deploy.sh logs <service-name>
```

重点检查：

- Nacos namespace 是否正确
- MySQL 和 Redis 配置是否存在
- RabbitMQ virtual host 和用户权限是否正确
- 服务器是否能访问配置中心和数据库

### 11.4 WebSocket 无法连接

确认 Gateway 和 Market 都在运行：

```bash
./deploy.sh ps
./deploy.sh logs exchange-gateway
./deploy.sh logs exchange-business-market
```

前端必须通过 Gateway 连接 `/ws/market`，不能直连 Market 容器端口。
