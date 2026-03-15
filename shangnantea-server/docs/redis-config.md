# Redis 配置说明

## 在线状态功能所需的 Redis 配置

为了支持用户在线状态的实时推送功能，需要在 Redis 服务器中启用过期事件监听。

### 配置方法

#### 方法1：修改 Redis 配置文件（推荐，永久生效）

在 Redis 配置文件（通常是 `redis.conf`）中添加或修改：

```conf
notify-keyspace-events Ex
```

然后重启 Redis 服务。

#### 方法2：通过 Redis 命令行临时配置（重启后失效）

连接到 Redis 服务器后执行：

```bash
CONFIG SET notify-keyspace-events Ex
```

#### 方法3：启动 Redis 时指定配置

```bash
redis-server --notify-keyspace-events Ex
```

### 配置说明

- `E`：启用键空间事件（keyspace events）
- `x`：启用键过期事件（expired events）

配置后，当 Redis 中的 key 过期时，会发布事件到 `__keyevent@0__:expired` 频道，`UserOnlineExpiredListener` 会监听到这些事件并推送用户离线状态。

### 验证配置

可以通过以下命令验证配置是否生效：

```bash
# 连接到 Redis
redis-cli

# 查看当前配置
CONFIG GET notify-keyspace-events

# 应该返回：notify-keyspace-events Ex
```

### 注意事项

1. 如果使用 Docker 运行 Redis，需要在启动容器时设置环境变量或挂载配置文件
2. 某些 Redis 托管服务可能默认已启用此配置，请查看服务商文档
3. 此配置不会影响 Redis 性能，可以安全启用
