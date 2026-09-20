# 云服务器 Docker 发布

## 1. 本地生成发布包

本地只执行 Maven，不调用 Docker：

```bash
./docker-package.sh
```

生成目录：

```text
dist/crypto-exchange-docker/
```

## 2. 上传到云服务器

```bash
scp -r dist/crypto-exchange-docker root@服务器IP:/opt/
```

也可以使用支持断点续传的 `rsync`：

```bash
rsync -avP dist/crypto-exchange-docker/ root@服务器IP:/opt/crypto-exchange-docker/
```

## 3. 进入服务器发布目录

```bash
cd /opt/crypto-exchange-docker
```

## 4. 初始化环境配置

首次执行会生成 `.env` 并停止：

```bash
./deploy.sh up
```

编辑 `.env`，至少确认以下配置：

```dotenv
NACOS_SERVER_ADDR=106.12.183.54:8848
NACOS_NAMESPACE=09a6d4e7-6ba4-410c-9451-822410f9fbe6
NACOS_USERNAME=nacos
NACOS_PASSWORD=你的密码
REDIS_HOST=106.12.183.54
REDIS_PORT=63500
REDIS_PASSWORD=你的密码
GATEWAY_HOST_PORT=18080
```

## 5. 启动服务

```bash
./deploy.sh up
```

常用命令：

```bash
./deploy.sh ps
./deploy.sh logs
./deploy.sh restart
./deploy.sh stop
./deploy.sh down
```

也可以只操作指定服务：

```bash
./deploy.sh up exchange-business-spot
./deploy.sh restart exchange-gateway exchange-web
./deploy.sh logs exchange-business-market
./deploy.sh stop exchange-datasource
```

`stop` 只停止容器，不删除容器、网络、卷和镜像；`down` 只删除当前 Compose 项目的容器和网络，不删除卷和镜像。

仅网关端口会映射到宿主机。默认访问地址：

```text
http://服务器IP:18080/api/crypto-exchange/web/**
http://服务器IP:18080/api/crypto-exchange/admin/**
```

Nacos 2.x 除 `8848` 外通常还需要客户端能够访问其 gRPC 端口。部署前确认云服务器到 Nacos、MySQL、Redis、RabbitMQ 的网络和安全组均已放通。
