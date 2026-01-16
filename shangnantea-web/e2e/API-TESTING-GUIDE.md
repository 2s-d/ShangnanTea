# API 测试指南

## 测试策略

### 核心理念

**166 个 API = 166 个测试**

每个 API 端点对应一个独立的测试，测试成功的标准：
1. ✅ API 调用成功（返回正确的业务状态码）
2. ✅ 无控制台错误

### 为什么选择 API 测试而不是 UI 测试？

| 对比项 | UI 测试 | API 测试 |
|--------|---------|----------|
| 测试目标 | 验证页面交互 | 验证 API 调用 + 控制台错误 |
| 测试稳定性 | 低（依赖 UI 元素选择器） | 高（只关注 API 响应） |
| 测试速度 | 慢（需要等待页面渲染） | 快（直接监控网络请求） |
| 维护成本 | 高（UI 变化需要更新选择器） | 低（API 变化较少） |
| 错误覆盖 | 部分（只能测试到的页面） | 全面（所有 API 都测试） |

### 测试复用机制

测试可以引用前置测试的结果，避免重复：

```javascript
// 示例：创建帖子测试可以复用登录测试
test('POST /forum/posts - 创建帖子', async ({ page }) => {
  // 复用登录（使用 mockLogin 而不是重新测试登录流程）
  await mockLogin(page, 'user');
  
  // 然后测试创建帖子 API
  // ...
});
```

## 目录结构

```
e2e/
├── api-tests/                    # API 测试目录
│   ├── user/                     # 用户模块测试
│   │   ├── login.spec.js         # ✅ 已完成
│   │   ├── register.spec.js      # 待实现
│   │   ├── profile.spec.js       # 待实现
│   │   └── ...
│   ├── tea/                      # 茶叶模块测试
│   ├── order/                    # 订单模块测试
│   ├── forum/                    # 论坛模块测试
│   ├── shop/                     # 店铺模块测试
│   ├── message/                  # 消息模块测试
│   └── API-TEST-TEMPLATE.md      # 测试模板文档
├── helpers/                      # 辅助工具
│   ├── api-helper.js             # API 调用监控
│   ├── auth-helper.js            # 认证辅助（登录、token）
│   └── error-collector.js        # 错误收集器
├── fixtures/                     # 测试数据
│   └── test-data.json            # 测试用数据
├── console-error-scan.spec.js    # 旧的浅层扫描测试
├── user-module.spec.js           # 旧的用户模块测试
├── run-api-test.bat              # API 测试启动脚本
├── run-e2e.ps1                   # PowerShell 测试脚本
└── API-TESTING-GUIDE.md          # 本文档
```

## 快速开始

### 1. 运行示例测试

```bash
# 进入项目目录
cd shangnantea/shangnantea-web

# 确保开发服务器运行在 8083 端口
npm run dev

# 在另一个终端运行 API 测试
cd e2e
run-api-test.bat user/login
```

### 2. 查看测试结果

测试完成后，查看：
- 控制台输出：测试通过/失败信息
- JSON 报告：`e2e-report/api-tests/user-login.json`
- 错误截图：`e2e-screenshots/api-user-login-error.png`（仅在有错误时）

### 3. 编写新的 API 测试

参考 `API-TEST-TEMPLATE.md` 中的模板，步骤：

1. **查找 API 定义**：在 `openapi_new.yaml` 中找到 API 信息
2. **创建测试文件**：按照命名规范创建 `.spec.js` 文件
3. **复制模板代码**：从模板复制基础结构
4. **填写 API 信息**：HTTP 方法、路径、状态码等
5. **实现触发逻辑**：如何触发这个 API（点击按钮、填写表单等）
6. **运行测试**：验证测试通过
7. **提交代码**：测试通过后提交

## API 列表与测试进度

### 用户模块（User Module）

| API 路径 | 方法 | 操作ID | 状态 | 测试文件 |
|---------|------|--------|------|---------|
| /user/login | POST | userLogin | ✅ 完成 | user/login.spec.js |
| /user/register | POST | userRegister | ⏳ 待实现 | user/register.spec.js |
| /user/logout | POST | userLogout | ⏳ 待实现 | user/logout.spec.js |
| /user/refresh | POST | refreshToken | ⏳ 待实现 | user/refresh.spec.js |
| /user/me | GET | getUserMe | ⏳ 待实现 | user/me.spec.js |
| /user/{userId} | GET | getUserById | ⏳ 待实现 | user/get-by-id.spec.js |
| /user | PUT | updateUser | ⏳ 待实现 | user/update.spec.js |
| /user/avatar | POST | uploadAvatar | ⏳ 待实现 | user/avatar.spec.js |
| /user/password | PUT | updatePassword | ⏳ 待实现 | user/password.spec.js |
| /user/password/reset | POST | resetPassword | ⏳ 待实现 | user/reset-password.spec.js |
| /user/addresses | GET | getAddresses | ⏳ 待实现 | user/addresses-list.spec.js |
| /user/addresses | POST | addAddress | ⏳ 待实现 | user/addresses-add.spec.js |
| /user/addresses/{id} | PUT | updateAddress | ⏳ 待实现 | user/addresses-update.spec.js |
| /user/addresses/{id} | DELETE | deleteAddress | ⏳ 待实现 | user/addresses-delete.spec.js |
| /user/addresses/{id}/default | PUT | setDefaultAddress | ⏳ 待实现 | user/addresses-default.spec.js |
| ... | ... | ... | ⏳ 待实现 | ... |

### 茶叶模块（Tea Module）

| API 路径 | 方法 | 操作ID | 状态 | 测试文件 |
|---------|------|--------|------|---------|
| /tea/list | GET | getTeaList | ⏳ 待实现 | tea/list.spec.js |
| /tea/list | POST | addTea | ⏳ 待实现 | tea/add.spec.js |
| /tea/{id} | GET | getTeaDetail | ⏳ 待实现 | tea/detail.spec.js |
| /tea/{id} | PUT | updateTea | ⏳ 待实现 | tea/update.spec.js |
| /tea/{id} | DELETE | deleteTea | ⏳ 待实现 | tea/delete.spec.js |
| ... | ... | ... | ⏳ 待实现 | ... |

### 订单模块（Order Module）

| API 路径 | 方法 | 操作ID | 状态 | 测试文件 |
|---------|------|--------|------|---------|
| 待补充 | ... | ... | ⏳ 待实现 | ... |

### 论坛模块（Forum Module）

| API 路径 | 方法 | 操作ID | 状态 | 测试文件 |
|---------|------|--------|------|---------|
| 待补充 | ... | ... | ⏳ 待实现 | ... |

### 店铺模块（Shop Module）

| API 路径 | 方法 | 操作ID | 状态 | 测试文件 |
|---------|------|--------|------|---------|
| 待补充 | ... | ... | ⏳ 待实现 | ... |

### 消息模块（Message Module）

| API 路径 | 方法 | 操作ID | 状态 | 测试文件 |
|---------|------|--------|------|---------|
| 待补充 | ... | ... | ⏳ 待实现 | ... |

## 实施计划

### 阶段 1：核心流程（优先级最高）

1. ✅ 用户登录 - `POST /user/login`
2. ⏳ 用户注册 - `POST /user/register`
3. ⏳ 获取用户信息 - `GET /user/me`
4. ⏳ 茶叶列表 - `GET /tea/list`
5. ⏳ 茶叶详情 - `GET /tea/{id}`
6. ⏳ 创建订单 - `POST /order`
7. ⏳ 订单支付 - `POST /order/{id}/pay`

### 阶段 2：用户功能

- 个人信息管理
- 收货地址管理
- 密码修改
- 关注/收藏功能

### 阶段 3：商品功能

- 茶叶分类
- 茶叶评价
- 茶叶规格

### 阶段 4：订单功能

- 购物车
- 订单列表
- 订单详情
- 订单管理

### 阶段 5：社交功能

- 论坛帖子
- 评论回复
- 点赞收藏
- 消息通知

### 阶段 6：管理功能

- 用户管理
- 商品管理
- 订单管理
- 内容审核

## 最佳实践

### 1. 测试命名

```javascript
// ✅ 好的命名
test('POST /user/login - 用户登录成功', async ({ page }) => {});
test('POST /user/login - 错误的用户名或密码', async ({ page }) => {});

// ❌ 不好的命名
test('test1', async ({ page }) => {});
test('login', async ({ page }) => {});
```

### 2. 错误处理

```javascript
// ✅ 使用统一的错误收集器
const testContext = errorCollector.startTest(testName);
setupErrorListeners(page, testName, testContext.errors);

// ❌ 不要手动捕获错误
page.on('console', msg => {
  if (msg.type() === 'error') {
    // 手动处理...
  }
});
```

### 3. 前置条件

```javascript
// ✅ 使用 mockLogin 快速登录
await mockLogin(page, 'user');

// ❌ 不要重复测试登录流程
await page.goto('/login');
await page.fill('input[name="username"]', 'user');
await page.fill('input[name="password"]', 'password');
await page.click('button[type="submit"]');
```

### 4. 数据管理

```javascript
// ✅ 使用 test-data.json
const testData = require('../../fixtures/test-data.json');
await page.fill('input[name="username"]', testData.users.user.username);

// ❌ 不要硬编码
await page.fill('input[name="username"]', 'testuser');
```

### 5. 断言标准

```javascript
// ✅ 完整的断言
expect(response.status()).toBe(200);
expect(responseData.code).toBe(2000);
expect(errors.length).toBe(0);

// ❌ 不完整的断言
expect(response.status()).toBe(200);
// 缺少业务状态码和错误检查
```

## 常见问题

### Q1: 如何处理需要多步操作的 API？

A: 将每一步拆分为独立的测试，后续测试可以复用前面的结果。

```javascript
// 测试 1：创建帖子
test('POST /forum/posts - 创建帖子', async ({ page }) => {
  // 创建帖子并保存 postId
});

// 测试 2：评论帖子（复用 postId）
test('POST /forum/posts/{postId}/comments - 评论帖子', async ({ page }) => {
  // 使用之前创建的 postId
});
```

### Q2: 如何测试需要文件上传的 API？

A: 使用 Playwright 的文件上传功能：

```javascript
await page.setInputFiles('input[type="file"]', 'path/to/test-file.jpg');
```

### Q3: 如何测试分页 API？

A: 测试第一页即可，验证 API 调用成功和无错误：

```javascript
await page.goto('/tea/list?page=1&pageSize=10');
// 监控 API 调用...
```

### Q4: 测试失败了怎么办？

A: 按以下步骤排查：
1. 查看控制台输出的错误信息
2. 查看 JSON 报告中的详细错误
3. 查看错误截图
4. 检查开发服务器是否正常运行
5. 检查 API 路径和参数是否正确

## 进度跟踪

- **总 API 数量**: 166
- **已完成测试**: 1
- **进行中**: 0
- **待实现**: 165
- **完成度**: 0.6%

---

**下一步**: 实现用户注册 API 测试 (`user/register.spec.js`)
