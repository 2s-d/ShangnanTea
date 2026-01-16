# API 测试快速开始

## 🚀 5 分钟快速上手

### 1. 启动开发服务器

```bash
cd shangnantea/shangnantea-web
npm run dev
```

确保服务器运行在 `http://localhost:8083`

### 2. 运行示例测试

打开新的终端窗口：

```bash
cd shangnantea/shangnantea-web/e2e

# 运行用户登录测试
run-api-test.bat user/login

# 或运行用户注册测试
run-api-test.bat user/register
```

### 3. 查看测试结果

测试完成后，查看：
- **控制台输出**: 实时测试结果
- **JSON 报告**: `e2e-report/api-tests/user-login.json`
- **错误截图**: `e2e-screenshots/` (仅在有错误时)

## 📝 创建新测试（3 步）

### 步骤 1: 找到 API 定义

打开 `shangnantea/openapi_new.yaml`，找到你要测试的 API：

```yaml
/user/logout:
  post:
    operationId: userLogout
    summary: 退出登录
    responses:
      '200':
        # code: 2002 表示成功
        # code: 2103 表示失败
```

记录：
- **路径**: `/user/logout`
- **方法**: `POST`
- **成功码**: `2002`
- **失败码**: `2103`

### 步骤 2: 复制模板

```bash
# 复制模板文件
copy e2e\api-tests\user\login.spec.js e2e\api-tests\user\logout.spec.js
```

### 步骤 3: 修改测试代码

打开 `logout.spec.js`，修改以下内容：

```javascript
// 1. 修改文件头注释
/**
 * API 测试 - 退出登录
 * API: POST /user/logout
 * 操作ID: userLogout
 */

// 2. 修改错误收集器名称
const errorCollector = createErrorCollector('退出登录');

// 3. 修改测试描述
test.describe('API 测试 - 退出登录', () => {
  
  test('POST /user/logout - 退出成功', async ({ page }) => {
    const testName = 'POST /user/logout - 退出成功';
    
    // 4. 先登录（因为退出需要先登录）
    await mockLogin(page, 'user');
    
    // 5. 访问需要触发退出的页面
    await page.goto('/user/settings', { waitUntil: 'networkidle' });
    
    // 6. 监控 API
    const apiPromise = page.waitForResponse(
      response => response.url().includes('/user/logout') && 
                  response.request().method() === 'POST',
      { timeout: 10000 }
    );
    
    // 7. 触发退出操作（点击退出按钮）
    await page.click('button:has-text("退出登录")');
    
    // 8. 验证响应
    const response = await apiPromise;
    const responseData = await response.json();
    
    expect(response.status()).toBe(200);
    expect(responseData.code).toBe(2002); // 退出成功
    
    // ... 其余代码保持不变
  });
});
```

### 步骤 4: 运行测试

```bash
run-api-test.bat user/logout
```

## 🎯 测试模式

### 模式 1: 页面加载时自动调用的 API

例如：获取用户信息、获取列表

```javascript
// 直接访问页面，API 会自动调用
await page.goto('/user/profile', { waitUntil: 'networkidle' });

const apiPromise = page.waitForResponse(
  response => response.url().includes('/user/me')
);

const response = await apiPromise;
```

### 模式 2: 点击按钮触发的 API

例如：提交表单、删除操作

```javascript
// 先设置监听
const apiPromise = page.waitForResponse(
  response => response.url().includes('/user/addresses') && 
              response.request().method() === 'POST'
);

// 再触发操作
await page.click('button:has-text("保存")');

// 等待响应
const response = await apiPromise;
```

### 模式 3: 填写表单后提交的 API

例如：登录、注册、编辑信息

```javascript
// 填写表单
await page.fill('input[name="username"]', 'testuser');
await page.fill('input[name="password"]', 'password123');

// 监听 API
const apiPromise = page.waitForResponse(
  response => response.url().includes('/user/login')
);

// 提交表单
await page.click('button[type="submit"]');

// 验证响应
const response = await apiPromise;
```

## 🔧 常用辅助函数

### 登录

```javascript
const { mockLogin } = require('../../helpers/auth-helper');

// 以普通用户身份登录
await mockLogin(page, 'user');

// 以管理员身份登录
await mockLogin(page, 'admin');

// 以商家身份登录
await mockLogin(page, 'shop');
```

### 使用测试数据

```javascript
const testData = require('../../fixtures/test-data.json');

// 使用用户数据
await page.fill('input[name="username"]', testData.users.user.username);

// 使用地址数据
await page.fill('input[name="phone"]', testData.address.sample.receiverPhone);
```

### 错误收集

```javascript
const { setupErrorListeners, createErrorCollector } = require('../../helpers/error-collector');

// 创建收集器
const errorCollector = createErrorCollector('模块名');

// 在测试中使用
const testContext = errorCollector.startTest(testName);
setupErrorListeners(page, testName, testContext.errors);

// 测试结束时检查错误
const errors = testContext.endTest();
expect(errors.length).toBe(0);
```

## 📋 检查清单

创建新测试时，确保：

- [ ] 从 `openapi_new.yaml` 获取了正确的 API 信息
- [ ] 文件命名符合规范：`<模块>/<功能>.spec.js`
- [ ] 测试名称清晰：`<方法> <路径> - <场景>`
- [ ] 包含错误收集器
- [ ] 验证了 HTTP 状态码 (200)
- [ ] 验证了业务状态码 (如 2000)
- [ ] 验证了无控制台错误 (errors.length === 0)
- [ ] 添加了适当的等待时间
- [ ] 测试通过后更新了 `API-TEST-IMPLEMENTATION-STATUS.md`

## 🐛 常见问题

### Q: 测试超时怎么办？

A: 增加超时时间：

```javascript
const apiPromise = page.waitForResponse(
  response => response.url().includes('/api/path'),
  { timeout: 20000 } // 增加到 20 秒
);
```

### Q: 找不到页面元素怎么办？

A: 使用 Playwright Inspector 调试：

```bash
npx playwright test --debug
```

### Q: API 路径不匹配怎么办？

A: 检查实际的 API 路径：

```javascript
page.on('response', response => {
  console.log('API 调用:', response.url());
});
```

### Q: 如何跳过某个测试？

A: 使用 `test.skip`：

```javascript
test.skip('POST /user/logout - 退出成功', async ({ page }) => {
  // 暂时跳过这个测试
});
```

## 📚 更多资源

- **完整指南**: `API-TESTING-GUIDE.md`
- **测试模板**: `API-TEST-TEMPLATE.md`
- **实施状态**: `API-TEST-IMPLEMENTATION-STATUS.md`
- **API 定义**: `../openapi_new.yaml`

## 💡 提示

1. **一次一个**: 不要一次实现太多测试，确保每个测试都通过
2. **复用代码**: 使用辅助函数，避免重复代码
3. **清晰命名**: 测试名称要能清楚表达测试内容
4. **及时更新**: 完成测试后更新进度文档
5. **保存报告**: 测试报告可以帮助追踪问题

---

**开始你的第一个测试吧！** 🚀
