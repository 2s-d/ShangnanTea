# Redis配置说明 - 为什么需要这个配置？

## 简单理解

### 之前为什么不需要？

**之前的掉线方式：**
1. **关闭窗口** → WebSocket连接立即断开 → 后端检测到连接关闭 → **立即推送离线状态** ✅
2. **退出登录** → WebSocket连接立即断开 → 后端检测到连接关闭 → **立即推送离线状态** ✅

**关键点：** 这些情况下，WebSocket连接会立即关闭，后端在 `afterConnectionClosed()` 方法中直接调用 `notifyUserOnlineChanged(userId, false)` 推送离线状态，所以**不需要监听Redis事件**。

### 现在为什么需要？

**新增的延迟掉线方式：**
1. **页面隐藏30秒** → WebSocket连接**不断开**，只是停止心跳 → Redis key在30秒后过期 → **需要推送离线状态** ❌
2. **无操作1分钟** → WebSocket连接**不断开**，只是停止心跳 → Redis key在30秒后过期 → **需要推送离线状态** ❌

**问题：** 
- WebSocket连接还在，所以不会触发 `afterConnectionClosed()`
- Redis key过期了，但是**Redis默认不会通知任何人**
- 其他用户不知道这个用户已经离线了

**解决方案：**
- 配置 `notify-keyspace-events Ex`，让Redis在key过期时**发布事件**
- `UserOnlineExpiredListener` 监听这个事件
- 当 `online:user:{userId}` key过期时，自动推送离线状态给其他用户

## 工作流程对比

### 之前（立即掉线）
```
用户关闭窗口
  ↓
WebSocket连接断开
  ↓
afterConnectionClosed() 被调用
  ↓
直接调用 notifyUserOnlineChanged(userId, false)
  ↓
其他用户收到离线通知 ✅
```

### 现在（延迟掉线）
```
页面隐藏/无操作
  ↓
停止心跳（WebSocket连接不断开）
  ↓
30秒后 Redis key 过期
  ↓
【需要】Redis发布过期事件 ← 这就是为什么需要配置
  ↓
UserOnlineExpiredListener 监听到事件
  ↓
调用 notifyUserOnlineChanged(userId, false)
  ↓
其他用户收到离线通知 ✅
```

## 配置的作用

`notify-keyspace-events Ex` 的作用：
- **E** = 启用键空间事件（keyspace events）
- **x** = 启用键过期事件（expired events）

配置后，当Redis中的key过期时，会发布事件到 `__keyevent@0__:expired` 频道，我们的监听器就能收到并处理。

## 总结

- **之前不需要**：因为立即掉线时WebSocket会断开，后端能直接检测到并推送状态
- **现在需要**：因为延迟掉线时WebSocket不断开，只能通过监听Redis key过期事件来知道用户离线

这个配置是**新增功能**（延迟掉线）的必需配置，不影响原有功能。
