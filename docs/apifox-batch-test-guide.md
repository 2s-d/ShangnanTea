# Apifox 批量测试所有接口指南

## 目标
一次性运行所有接口，快速发现并修复问题接口。

## 步骤1：配置环境变量

### 1.1 设置环境变量
在 Apifox 中：
1. 点击右上角「环境管理」
2. 选择或创建「本地测试环境」
3. 添加以下变量：
   - `baseUrl`: `http://localhost:8080/api`
   - `token`: （留空，会自动更新）

## 步骤2：配置登录接口自动提取Token

### 2.1 设置登录接口后置操作
1. 打开「用户登录」接口（`POST /user/login`）
2. 点击「后置操作」→「提取变量」
3. 添加变量提取：
   - 变量名：`token`
   - 提取表达式：`$.data.token` 或 `$.data.data.token`（根据实际响应调整）
   - 提取到：环境变量

### 2.2 配置需要认证的接口
对于所有需要认证的接口：
1. 打开接口
2. 「认证」→「Bearer Token」
3. Token值：`{{token}}`

## 步骤3：创建全量测试套件

### 3.1 创建测试套件
1. 左侧菜单「测试套件」→「新建测试套件」
2. 名称：「商南茶城-全量接口测试」
3. 描述：「一次性测试所有接口，筛选问题」

### 3.2 添加所有接口（按模块分组）

#### 用户模块
- [ ] POST /user/login（先运行，获取token）
- [ ] POST /user/register（跳过，需要验证码）
- [ ] POST /user/logout
- [ ] POST /user/refresh
- [ ] GET /user/me
- [ ] GET /user/{userId}
- [ ] PUT /user
- [ ] POST /user/avatar
- [ ] PUT /user/password
- [ ] POST /user/password/reset（跳过，需要验证码）
- [ ] GET /user/addresses
- [ ] POST /user/addresses
- [ ] PUT /user/addresses/{addressId}
- [ ] DELETE /user/addresses/{addressId}
- [ ] PUT /user/addresses/{addressId}/default
- [ ] POST /user/verification-code/send（跳过，短信服务问题）
- [ ] GET /user/preferences
- [ ] PUT /user/preferences
- [ ] POST /user/shop-certification
- [ ] GET /user/shop-certification
- [ ] POST /user/certification-image
- [ ] GET /user/follows
- [ ] POST /user/follows
- [ ] DELETE /user/follows
- [ ] GET /user/favorites
- [ ] POST /user/favorites
- [ ] DELETE /user/favorites
- [ ] POST /user/likes
- [ ] DELETE /user/likes

#### 茶叶模块
- [ ] GET /tea/list
- [ ] GET /tea/{teaId}
- [ ] GET /tea/{teaId}/reviews
- [ ] POST /tea/{teaId}/reviews
- [ ] GET /tea/categories
- [ ] GET /tea/search
- [ ] GET /tea/recommendations
- [ ] GET /tea/hot
- [ ] GET /tea/new

#### 店铺模块
- [ ] GET /shop/list
- [ ] GET /shop/{shopId}
- [ ] GET /shop/{shopId}/teas
- [ ] GET /shop/{shopId}/reviews
- [ ] GET /shop/{shopId}/statistics

#### 订单模块
- [ ] GET /order/list
- [ ] GET /order/{orderId}
- [ ] POST /order/create
- [ ] POST /order/{orderId}/cancel
- [ ] POST /order/{orderId}/pay
- [ ] GET /order/statistics
- [ ] GET /cart
- [ ] POST /cart/add
- [ ] PUT /cart/{itemId}
- [ ] DELETE /cart/{itemId}
- [ ] DELETE /cart/clear

#### 论坛模块
- [ ] GET /forum/home
- [ ] GET /forum/banners
- [ ] GET /forum/posts
- [ ] GET /forum/posts/{postId}
- [ ] POST /forum/posts
- [ ] PUT /forum/posts/{postId}
- [ ] DELETE /forum/posts/{postId}
- [ ] GET /forum/posts/{postId}/replies
- [ ] POST /forum/posts/{postId}/replies
- [ ] GET /forum/articles
- [ ] GET /forum/articles/{articleId}

#### 消息模块
- [ ] GET /message/chat/list
- [ ] GET /message/chat/{chatId}/messages
- [ ] POST /message/chat/send
- [ ] GET /message/notifications
- [ ] PUT /message/notifications/{notificationId}/read

### 3.3 设置测试顺序
1. 第一个必须是「用户登录」（获取token）
2. 其他接口可以并行运行（Apifox会自动处理依赖）

## 步骤4：批量运行并筛选问题

### 4.1 运行测试套件
1. 点击「运行」
2. 选择「本地测试环境」
3. 点击「开始运行」

### 4.2 查看结果
运行完成后，查看：
- ✅ 绿色：成功（code=2000-2999）
- ❌ 红色：失败（HTTP错误或业务错误）

### 4.3 筛选问题接口
按以下标准筛选：

#### 必须修复的问题：
1. **HTTP 500错误**：服务器内部错误
2. **HTTP 404错误**：接口路径错误
3. **HTTP 401错误**：认证问题（token未传递）
4. **业务错误码 21xx/31xx/41xx等**：业务逻辑错误

#### 可以暂时忽略的：
1. **HTTP 400错误**：参数错误（可能是测试数据问题）
2. **业务错误码 2102**：验证码相关（已跳过）
3. **数据为空**：可能是数据库没有测试数据

### 4.4 导出问题列表
1. 点击「导出报告」
2. 筛选失败的接口
3. 记录问题：
   - 接口路径
   - 错误码
   - 错误信息
   - 响应内容

## 步骤5：批量修复问题

### 5.1 按错误类型分组修复
1. **认证问题**：检查token传递
2. **路径问题**：检查Controller路径映射
3. **参数问题**：检查DTO验证
4. **业务逻辑问题**：检查Service实现

### 5.2 修复后重新运行
修复后，只运行失败的接口，验证修复效果。

## 快速操作清单

- [ ] 配置环境变量（baseUrl, token）
- [ ] 设置登录接口提取token
- [ ] 创建测试套件
- [ ] 添加所有接口到套件
- [ ] 批量运行
- [ ] 筛选失败接口
- [ ] 导出问题列表
- [ ] 批量修复
- [ ] 重新验证

## 注意事项

1. **先登录**：确保第一个接口是登录，获取token
2. **跳过验证码接口**：注册、重置密码等需要验证码的接口先跳过
3. **测试数据**：部分接口可能需要先创建测试数据
4. **并发限制**：如果接口太多，可以分批运行（每次50个接口）
5. **查看日志**：后端日志会显示详细的错误信息，便于定位问题
