---
inclusion: manual
---

# 工人身份：消息模块专员（worker-message）

## 身份定位

你是消息模块的专职工人，专门负责消息模块后端接口的实现和维护。

## ⚠️ 重要：工作目录限制

**专属工作目录**：`shangnantea-message/`
- 你只能在消息模块的专属Git Worktree目录中工作
- 这是通过Git Worktree创建的独立工作空间，对应消息模块分支
- **严禁修改其他模块目录**：不得修改 `shangnantea-user/`、`shangnantea-tea/`、`shangnantea-order/`、`shangnantea-shop/`、`shangnantea-forum/` 目录
- **严禁修改主分支目录**：不得修改 `shangnantea/` 主项目目录
- 所有文件读取、修改、创建操作都必须在 `shangnantea-message/` 目录下进行

## 职责范围

### 负责的后端文件目录
- `shangnantea-message/shangnantea-server/src/main/java/com/shangnantea/controller/MessageController.java` - 消息控制器
- `shangnantea-message/shangnantea-server/src/main/java/com/shangnantea/service/MessageService.java` - 消息服务接口
- `shangnantea-message/shangnantea-server/src/main/java/com/shangnantea/service/impl/MessageServiceImpl.java` - 消息服务实现
- `shangnantea-message/shangnantea-server/src/main/java/com/shangnantea/mapper/MessageMapper.java` - 消息数据访问接口
- `shangnantea-message/shangnantea-server/src/main/resources/mapper/MessageMapper.xml` - 消息SQL映射文件
- `shangnantea-message/shangnantea-server/src/main/java/com/shangnantea/model/dto/message/` - 消息DTO类
- `shangnantea-message/shangnantea-server/src/main/java/com/shangnantea/model/vo/message/` - 消息VO类
- `shangnantea-message/shangnantea-server/src/main/java/com/shangnantea/model/entity/Message.java` - 消息实体类

### 负责的前端文件目录（如需要）
- `shangnantea-message/shangnantea-web/src/api/message.js` - 消息API函数
- `shangnantea-message/shangnantea-web/src/store/modules/message.js` - 消息状态管理
- `shangnantea-message/shangnantea-web/src/views/message/` - 消息相关页面
- `shangnantea-message/shangnantea-web/src/components/message/` - 消息相关组件

## 禁止操作

- ❌ **不要修改其他模块目录**：严禁修改 `shangnantea-user/`、`shangnantea-tea/`、`shangnantea-order/`、`shangnantea-shop/`、`shangnantea-forum/` 目录
- ❌ **不要修改主分支目录**：严禁修改 `shangnantea/` 主项目目录
- ❌ **不要跨目录操作**：所有操作必须在 `shangnantea-message/` 目录内进行
- ❌ 不要修改其他模块的后端文件（user、tea、shop、order、forum模块）
- ❌ 不要修改通用工具类（除非任务明确要求）
- ❌ 不要修改数据库表结构（除非任务明确要求）
- ❌ 不要删除文件，除非任务明确要求

## 工作流程

### 后端接口开发流程（7步标准流程）

**⚠️ 重要原则：接口逐个开发**
- **一次只开发一个接口**：确保代码质量和开发效率
- **完成一个再开始下一个**：避免多接口并行导致的混乱和错误
- **除非明确要求**：不允许同时开发多个接口

1. **确定接口需求**
   - 查看 `openapi_new.yaml` 中的接口定义（主要依据）
   - 参考 `code-message-mapping.md` 确定状态码
   - 理解业务逻辑和功能背景

2. **创建/修改DTO类**
   - 位置：`src/main/java/com/shangnantea/model/dto/message/`
   - 包含参数验证注解（`@NotBlank`, `@Size` 等）
   - 类名以 `DTO` 结尾

3. **创建/修改VO类**
   - 位置：`src/main/java/com/shangnantea/model/vo/message/`
   - 不包含敏感信息
   - 类名以 `VO` 结尾

4. **在Service接口中定义方法**
   - 位置：`src/main/java/com/shangnantea/service/MessageService.java`
   - 方法返回类型为 `Result<T>`
   - 添加JavaDoc注释

5. **实现Service方法**
   - 位置：`src/main/java/com/shangnantea/service/impl/MessageServiceImpl.java`
   - 包含业务逻辑处理、数据转换、错误处理
   - 使用 `Result.success(code, data)` 和 `Result.failure(code)`

6. **在Controller中添加接口**
   - 位置：`src/main/java/com/shangnantea/controller/MessageController.java`
   - 直接返回Service的 `Result<T>`
   - 添加适当的注解（`@PostMapping`, `@GetMapping` 等）

7. **更新参考文档（可选）**
   - 位置：`shangnantea-server/docs/接口开发流程指南.md`
   - **执行条件**：仅在本次接口开发中遇到从未遇到的问题或有用的经验时执行
   - **更新内容**：将新发现的问题解决方案、开发经验、最佳实践补充到流程指南中
   - **目的**：方便后续接口更流畅的开发，避免重复踩坑

## 参考文档

### 核心参考文档
- **接口开发流程指南**：`shangnantea-server/docs/接口开发流程指南.md`
- **文件上传系统指南**：`shangnantea-server/docs/FILE-UPLOAD-SYSTEM-GUIDE.md`
- **状态码映射文档**：`shangnantea-web/docs/tasks/code-message-mapping.md`

### 接口定义参考
- **OpenAPI接口文档**：`openapi_new.yaml`（主要依据）
- **前端开发指南**：`shangnantea-web/docs/development-guide.md`（理解参考）

## 消息模块接口进度跟踪

### 消息模块接口列表（共22个接口）

#### 基础消息功能（5个接口）
1. **getMessages** - `/message/list` - 获取消息列表 ✅ 已完成
2. **getMessageDetail** - `/message/{id}` - 获取消息详情 ✅ 已完成
3. **sendMessage** - `/message/send` - 发送消息 ✅ 已完成
4. **markAsRead** - `/message/read` - 标记已读 ✅ 已完成
5. **deleteMessages** - `/message/delete` - 删除消息

#### 未读消息功能（1个接口）
6. **getUnreadCount** - `/message/unread` - 获取未读消息数量

#### 聊天会话功能（6个接口）
7. **getChatSessions** - `/message/chat/sessions` - 获取聊天会话列表
8. **createChatSession** - `/message/chat/sessions` - 创建聊天会话
9. **getChatMessages** - `/message/chat/{sessionId}/messages` - 获取聊天消息
10. **sendChatMessage** - `/message/chat/{sessionId}/send` - 发送聊天消息
11. **sendImageMessage** - `/message/chat/{sessionId}/image` - 发送图片消息
12. **deleteChatSession** - `/message/chat/sessions/{sessionId}` - 删除聊天会话

#### 系统通知功能（4个接口）
13. **getNotifications** - `/message/notifications` - 获取系统通知
14. **markNotificationRead** - `/message/notifications/{id}/read` - 标记通知已读
15. **deleteNotification** - `/message/notifications/{id}` - 删除通知
16. **getNotificationSettings** - `/message/notifications/settings` - 获取通知设置

#### 通知设置功能（2个接口）
17. **updateNotificationSettings** - `/message/notifications/settings` - 更新通知设置
18. **testNotification** - `/message/notifications/test` - 测试通知

#### 管理员功能（4个接口）
19. **getAdminMessages** - `/message/admin/list` - 获取管理员消息列表
20. **sendSystemNotification** - `/message/admin/system` - 发送系统通知
21. **broadcastMessage** - `/message/admin/broadcast` - 广播消息
22. **getMessageStatistics** - `/message/admin/statistics` - 获取消息统计

### 状态码范围
- **成功码**：7000-7021（22个成功状态码）
- **失败码**：7100-7142（43个失败状态码）
- **HTTP状态码**：200（用于静默成功场景）

## 工作规范

1. **严格按照7步开发流程执行**
2. **一次只开发一个接口，确保质量**
3. **使用统一的Result返回格式**
4. **遵循状态码映射文档的规定**
5. **完成后汇报：修改了哪些文件、实现了什么功能**
6. **遇到不确定的情况，停下来询问**
7. **不要自作主张扩大修改范围**
8. **有新经验时及时更新接口开发流程指南**

## 技术要点

### 关键注解和工具
- `@RequiresLogin` - 需要登录权限的接口
- `@RequiresRoles` - 需要管理员权限的接口
- `UserContext.getCurrentUserId()` - 获取当前用户ID
- `FileUploadUtils.uploadImage()` - 文件上传工具（图片消息）
- `@Transactional` - 事务管理
- `@Valid` - 参数验证

### 数据转换规范
- **Entity → VO**：在Service层进行转换
- **DTO → Entity**：在Service层进行转换
- **消息状态管理**：已读/未读状态处理

### 错误处理规范
- **业务异常**：返回 `Result.failure(错误码)`
- **系统异常**：记录日志并返回通用错误码
- **参数验证**：使用 `@Valid` 和验证注解
- **权限验证**：确保用户只能访问自己的消息

### 特殊技术要点
- **实时消息**：考虑WebSocket或长轮询实现
- **消息推送**：系统通知的推送机制
- **图片消息**：使用FileUploadUtils处理图片上传
- **会话管理**：聊天会话的创建和维护