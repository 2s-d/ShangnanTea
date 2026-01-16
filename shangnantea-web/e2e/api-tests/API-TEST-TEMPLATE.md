# API 测试模板

## 测试文件命名规范

```
e2e/api-tests/<模块>/<功能>.spec.js
```

例如：
- `e2e/api-tests/user/login.spec.js` - 用户登录
- `e2e/api-tests/user/register.spec.js` - 用户注册
- `e2e/api-tests/tea/list.spec.js` - 茶叶列表
- `e2e/api-tests/order/create.spec.js` - 创建订单

## 测试文件模板

```javascript
/**
 * API 测试 - <功能名称>
 * 
 * API: <HTTP方法> <API路径>
 * 操作ID: <operationId>
 * 
 * 测试目标：
 * 1. API 调用成功（返回正确的 code）
 * 2. 无控制台错误
 * 
 * 前置条件：<列出需要的前置测试或数据>
 */
const { test, expect } = require('@playwright/test');
const fs = require('fs');
const path = require('path');
const { setupErrorListeners, createErrorCollector } = require('../../helpers/error-collector');
const { mockLogin } = require('../../helpers/auth-helper');
const testData = require('../../fixtures/test-data.json');

// 创建错误收集器
const errorCollector = createErrorCollector('<模块名称>');

test.describe('API 测试 - <功能名称>', () => {
  
  test('<HTTP方法> <API路径> - <测试场景>', async ({ page }) => {
    const testName = '<HTTP方法> <API路径> - <测试场景>';
    const testContext = errorCollector.startTest(testName);
    
    // 设置错误监听
    setupErrorListeners(page, testName, testContext.errors);
    
    try {
      // 如果需要登录，先模拟登录
      // await mockLogin(page, 'user');
      
      // 访问页面
      await page.goto('<页面路径>', { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(1000);
      
      // 监控 API 调用
      const apiPromise = page.waitForResponse(
        response => response.url().includes('<API路径>') && response.request().method() === '<HTTP方法>',
        { timeout: 10000 }
      );
      
      // 执行触发 API 的操作（如点击按钮、填写表单等）
      // await page.click('button:has-text("提交")');
      
      // 等待 API 响应
      const response = await apiPromise;
      const responseData = await response.json();
      
      // 验证 API 响应
      console.log(`  ✓ API 状态码: ${response.status()}`);
      console.log(`  ✓ 业务状态码: ${responseData.code}`);
      
      // 断言：API 调用成功
      expect(response.status()).toBe(200);
      expect(responseData.code).toBe(<期望的业务状态码>);
      
      await page.waitForTimeout(1000);
      
      // 检查错误
      const errors = testContext.endTest();
      
      if (errors.length > 0) {
        console.log(`  ❌ 发现 ${errors.length} 个错误:`);
        errors.forEach((err, i) => {
          console.log(`     ${i + 1}. [${err.type}] ${err.message}`);
        });
        
        // 截图保存错误现场
        await page.screenshot({ 
          path: path.join(__dirname, '../../e2e-screenshots/api-<模块>-<功能>-error.png'),
          fullPage: true 
        });
      } else {
        console.log(`  ✅ 测试通过 - API 调用成功且无控制台错误`);
      }
      
      // 断言：无错误
      expect(errors.length, `发现 ${errors.length} 个控制台错误`).toBe(0);
      
    } catch (error) {
      console.log(`  ❌ 测试失败: ${error.message}`);
      testContext.endTest();
      throw error;
    }
  });
});

// 测试完成后生成报告
test.afterAll(async () => {
  const report = errorCollector.generateReport();
  
  console.log('\n' + '='.repeat(80));
  console.log(`📊 ${report.moduleName} - 测试报告`);
  console.log('='.repeat(80));
  console.log(`总测试数: ${report.totalTests}`);
  console.log(`通过: ${report.passedTests}`);
  console.log(`失败: ${report.failedTests}`);
  console.log(`总错误数: ${report.totalErrors}`);
  
  if (report.errors.length > 0) {
    console.log('\n❌ 错误详情:');
    report.errors.forEach((err, i) => {
      console.log(`  ${i + 1}. [${err.test}] [${err.type}]`);
      console.log(`     ${err.message}`);
    });
  }
  
  // 保存报告
  const reportDir = path.join(__dirname, '../../e2e-report/api-tests');
  if (!fs.existsSync(reportDir)) {
    fs.mkdirSync(reportDir, { recursive: true });
  }
  
  const reportPath = path.join(reportDir, '<模块>-<功能>.json');
  fs.writeFileSync(reportPath, JSON.stringify(report, null, 2), 'utf-8');
  
  console.log(`\n📄 详细报告: ${reportPath}`);
  console.log('='.repeat(80) + '\n');
});
```

## 测试编写指南

### 1. 确定 API 信息

从 `openapi_new.yaml` 中找到对应的 API：
- HTTP 方法（GET, POST, PUT, DELETE）
- API 路径（如 `/user/login`）
- operationId（如 `userLogin`）
- 成功状态码（如 `2000`）
- 失败状态码（如 `2100`）

### 2. 确定前置条件

- 是否需要登录？使用 `mockLogin(page, 'user')`
- 是否依赖其他 API 的数据？可以复用其他测试的结果

### 3. 确定触发方式

API 如何被触发？
- 页面加载时自动调用（如列表接口）
- 点击按钮触发（如提交表单）
- 填写表单后提交（如登录、注册）

### 4. 编写测试用例

每个 API 至少包含：
- **成功场景**：正常调用，返回成功状态码
- **失败场景**（可选）：错误输入，返回失败状态码

### 5. 验证标准

每个测试必须验证：
1. ✅ HTTP 状态码 = 200
2. ✅ 业务状态码 = 期望值（如 2000）
3. ✅ 无控制台错误（errors.length === 0）

## 测试组织结构

```
e2e/api-tests/
├── user/              # 用户模块
│   ├── login.spec.js
│   ├── register.spec.js
│   ├── profile.spec.js
│   └── ...
├── tea/               # 茶叶模块
│   ├── list.spec.js
│   ├── detail.spec.js
│   └── ...
├── order/             # 订单模块
│   ├── create.spec.js
│   ├── list.spec.js
│   └── ...
├── forum/             # 论坛模块
├── shop/              # 店铺模块
└── message/           # 消息模块
```

## 运行测试

```bash
# 运行所有 API 测试
npm run test:api

# 运行特定模块测试
npx playwright test e2e/api-tests/user

# 运行单个测试文件
npx playwright test e2e/api-tests/user/login.spec.js
```

## 测试报告

测试完成后，报告保存在：
- JSON 格式：`e2e-report/api-tests/<模块>-<功能>.json`
- 截图：`e2e-screenshots/api-<模块>-<功能>-error.png`（仅在有错误时）

## 注意事项

1. **增量开发**：一次实现一个 API 测试，确保通过后再继续下一个
2. **复用前置测试**：如果 API 需要先登录，使用 `mockLogin` 而不是重复测试登录流程
3. **数据隔离**：使用 `test-data.json` 中的测试数据，避免硬编码
4. **错误处理**：所有错误都会被自动收集，无需手动处理
5. **超时设置**：默认 10 秒超时，如果 API 较慢可以调整
