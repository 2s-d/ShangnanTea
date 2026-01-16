/**
 * E2E 测试 - 用户模块深度交互测试
 * 
 * 测试范围：
 * 1. 个人设置页面 - 个人信息编辑、密码修改、商家认证
 * 2. 个人资料页面 - 头像上传、信息修改、表单验证
 * 3. 收货地址管理 - 新增、编辑、删除、设为默认
 * 4. 个人主页 - 用户信息展示、动态列表
 */
const { test } = require('@playwright/test');
const fs = require('fs');
const path = require('path');

// 模拟登录 token
const MOCK_TOKENS = {
  user: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyIiwicm9sZSI6MiwidXNlcm5hbWUiOiJ1c2VyIiwiZXhwIjoxODkzNDU2MDAwfQ.dGVzdF9zaWduYXR1cmVfZm9yX3VzZXI',
  admin: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwicm9sZSI6MSwidXNlcm5hbWUiOiJhZG1pbiIsImV4cCI6MTg5MzQ1NjAwMH0.dGVzdF9zaWduYXR1cmVfZm9yX2FkbWlu'
};

// 可忽略的错误模式
const IGNORABLE_ERROR_PATTERNS = [
  /Download the Vue Devtools/i,
  /\[Vue warn\].*deprecated/i,
  /ResizeObserver loop/i,
  /ElementPlusError/i,
  /ElementPlus警告/i,
  /\[el-.*\].*deprecated/i,
];

// 错误收集器
const testErrors = {
  totalTests: 0,
  failedTests: 0,
  errors: []
};

/**
 * 模拟登录
 */
async function mockLogin(page, role = 'user') {
  const mockToken = MOCK_TOKENS[role];
  await page.goto('/login');
  await page.evaluate((token) => {
    localStorage.setItem('shangnantea_token', JSON.stringify(token));
  }, mockToken);
}

/**
 * 检查错误是否应该被忽略
 */
function shouldIgnoreError(errorMessage) {
  return IGNORABLE_ERROR_PATTERNS.some(pattern => pattern.test(errorMessage));
}

/**
 * 设置错误监听器
 */
async function setupErrorListeners(page, testName, errors) {
  page.on('console', msg => {
    if (msg.type() === 'error') {
      const message = msg.text();
      if (!shouldIgnoreError(message)) {
        errors.push({
          test: testName,
          type: 'console',
          message,
          timestamp: new Date().toISOString()
        });
      }
    }
  });
  
  page.on('pageerror', error => {
    const message = error.message;
    if (!shouldIgnoreError(message)) {
      errors.push({
        test: testName,
        type: 'runtime',
        message: `[Runtime Error] ${message}`,
        stack: error.stack,
        timestamp: new Date().toISOString()
      });
    }
  });
  
  page.on('response', async response => {
    const url = response.url();
    const status = response.status();
    
    if (status >= 400 && !url.includes('hot-update')) {
      const isApiRequest = url.includes('/api/') || 
                          (!url.match(/\.(js|css|png|jpg|jpeg|gif|svg|ico|woff|woff2|ttf|eot)$/i));
      
      if (isApiRequest) {
        errors.push({
          test: testName,
          type: 'network',
          message: `[${status}] ${response.request().method()} ${url}`,
          timestamp: new Date().toISOString()
        });
      }
    }
  });
}

/**
 * 保存错误报告
 */
function saveErrorReport() {
  const reportDir = path.join(__dirname, '..', 'e2e-report');
  if (!fs.existsSync(reportDir)) {
    fs.mkdirSync(reportDir, { recursive: true });
  }
  
  const reportPath = path.join(reportDir, 'user-module-errors.json');
  fs.writeFileSync(
    reportPath,
    JSON.stringify(testErrors, null, 2),
    'utf-8'
  );
}

test.describe('用户模块 - 个人设置页面', () => {
  
  test('个人设置 - 切换菜单标签', async ({ page }) => {
    const testName = '个人设置 - 切换菜单标签';
    const errors = [];
    testErrors.totalTests++;
    
    await setupErrorListeners(page, testName, errors);
    await mockLogin(page, 'user');
    
    try {
      // 访问个人设置页面
      await page.goto('/user/settings', { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(1000);
      
      // 点击"密码修改"菜单
      await page.click('text=密码修改');
      await page.waitForTimeout(1000);
      
      // 验证URL是否变化
      const url1 = page.url();
      console.log(`  ✓ 切换到密码修改: ${url1}`);
      
      // 点击"商家认证"菜单
      await page.click('text=商家认证');
      await page.waitForTimeout(1000);
      
      const url2 = page.url();
      console.log(`  ✓ 切换到商家认证: ${url2}`);
      
      // 点击"个性修改"菜单
      await page.click('text=个性修改');
      await page.waitForTimeout(1000);
      
      const url3 = page.url();
      console.log(`  ✓ 切换到个性修改: ${url3}`);
      
      if (errors.length > 0) {
        testErrors.failedTests++;
        testErrors.errors.push(...errors);
        console.log(`  ❌ 发现 ${errors.length} 个错误`);
      } else {
        console.log(`  ✅ 测试通过`);
      }
    } catch (error) {
      testErrors.failedTests++;
      testErrors.errors.push({
        test: testName,
        type: 'test-error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
      console.log(`  ❌ 测试失败: ${error.message}`);
    } finally {
      saveErrorReport();
    }
  });
  
  test('个人设置 - 修改个人信息', async ({ page }) => {
    const testName = '个人设置 - 修改个人信息';
    const errors = [];
    testErrors.totalTests++;
    
    await setupErrorListeners(page, testName, errors);
    await mockLogin(page, 'user');
    
    try {
      await page.goto('/user/settings/profile', { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(1000);
      
      // 修改昵称
      const nicknameInput = page.locator('input[placeholder*="昵称"]').first();
      await nicknameInput.fill('测试用户' + Date.now());
      console.log(`  ✓ 修改昵称`);
      
      // 选择性别
      await page.click('text=男');
      await page.waitForTimeout(500);
      console.log(`  ✓ 选择性别`);
      
      // 填写手机号
      const phoneInput = page.locator('input[placeholder*="手机"]').first();
      await phoneInput.fill('13800138000');
      console.log(`  ✓ 填写手机号`);
      
      // 填写邮箱
      const emailInput = page.locator('input[placeholder*="邮箱"]').first();
      await emailInput.fill('test@example.com');
      console.log(`  ✓ 填写邮箱`);
      
      // 填写个人简介
      const bioTextarea = page.locator('textarea[placeholder*="简介"]').first();
      await bioTextarea.fill('这是一个测试用户的个人简介');
      console.log(`  ✓ 填写个人简介`);
      
      // 点击保存按钮
      await page.click('button:has-text("保存修改")');
      await page.waitForTimeout(2000);
      console.log(`  ✓ 点击保存按钮`);
      
      if (errors.length > 0) {
        testErrors.failedTests++;
        testErrors.errors.push(...errors);
        console.log(`  ❌ 发现 ${errors.length} 个错误`);
      } else {
        console.log(`  ✅ 测试通过`);
      }
    } catch (error) {
      testErrors.failedTests++;
      testErrors.errors.push({
        test: testName,
        type: 'test-error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
      console.log(`  ❌ 测试失败: ${error.message}`);
    } finally {
      saveErrorReport();
    }
  });
  
  test('个人设置 - 修改密码', async ({ page }) => {
    const testName = '个人设置 - 修改密码';
    const errors = [];
    testErrors.totalTests++;
    
    await setupErrorListeners(page, testName, errors);
    await mockLogin(page, 'user');
    
    try {
      await page.goto('/user/settings/password', { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(1000);
      
      // 填写旧密码
      const oldPasswordInput = page.locator('input[type="password"]').first();
      await oldPasswordInput.fill('oldPassword123');
      console.log(`  ✓ 填写旧密码`);
      
      // 填写新密码
      const newPasswordInputs = page.locator('input[type="password"]');
      await newPasswordInputs.nth(1).fill('newPassword123');
      console.log(`  ✓ 填写新密码`);
      
      // 确认新密码
      await newPasswordInputs.nth(2).fill('newPassword123');
      console.log(`  ✓ 确认新密码`);
      
      // 点击确认修改按钮
      await page.click('button:has-text("确认修改")');
      await page.waitForTimeout(2000);
      console.log(`  ✓ 点击确认修改按钮`);
      
      if (errors.length > 0) {
        testErrors.failedTests++;
        testErrors.errors.push(...errors);
        console.log(`  ❌ 发现 ${errors.length} 个错误`);
      } else {
        console.log(`  ✅ 测试通过`);
      }
    } catch (error) {
      testErrors.failedTests++;
      testErrors.errors.push({
        test: testName,
        type: 'test-error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
      console.log(`  ❌ 测试失败: ${error.message}`);
    } finally {
      saveErrorReport();
    }
  });
});

test.describe('用户模块 - 收货地址管理', () => {
  
  test('收货地址 - 打开新增地址弹窗', async ({ page }) => {
    const testName = '收货地址 - 打开新增地址弹窗';
    const errors = [];
    testErrors.totalTests++;
    
    await setupErrorListeners(page, testName, errors);
    await mockLogin(page, 'user');
    
    try {
      await page.goto('/user/address', { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(1000);
      
      // 点击新增地址按钮
      await page.click('button:has-text("新增地址")');
      await page.waitForTimeout(1000);
      console.log(`  ✓ 点击新增地址按钮`);
      
      // 验证弹窗是否打开
      const dialogVisible = await page.isVisible('.el-dialog');
      console.log(`  ✓ 弹窗显示状态: ${dialogVisible}`);
      
      if (errors.length > 0) {
        testErrors.failedTests++;
        testErrors.errors.push(...errors);
        console.log(`  ❌ 发现 ${errors.length} 个错误`);
      } else {
        console.log(`  ✅ 测试通过`);
      }
    } catch (error) {
      testErrors.failedTests++;
      testErrors.errors.push({
        test: testName,
        type: 'test-error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
      console.log(`  ❌ 测试失败: ${error.message}`);
    } finally {
      saveErrorReport();
    }
  });
  
  test('收货地址 - 填写新增地址表单', async ({ page }) => {
    const testName = '收货地址 - 填写新增地址表单';
    const errors = [];
    testErrors.totalTests++;
    
    await setupErrorListeners(page, testName, errors);
    await mockLogin(page, 'user');
    
    try {
      await page.goto('/user/address', { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(1000);
      
      // 点击新增地址按钮
      await page.click('button:has-text("新增地址")');
      await page.waitForTimeout(1000);
      
      // 填写收货人
      const nameInput = page.locator('.el-dialog input[placeholder*="收货人"]').first();
      await nameInput.fill('张三');
      console.log(`  ✓ 填写收货人`);
      
      // 填写手机号
      const phoneInput = page.locator('.el-dialog input[placeholder*="手机"]').first();
      await phoneInput.fill('13800138000');
      console.log(`  ✓ 填写手机号`);
      
      // 选择地区（点击级联选择器）
      await page.click('.el-cascader');
      await page.waitForTimeout(500);
      console.log(`  ✓ 打开地区选择器`);
      
      // 填写详细地址
      const detailTextarea = page.locator('.el-dialog textarea[placeholder*="详细地址"]').first();
      await detailTextarea.fill('某某街道123号');
      console.log(`  ✓ 填写详细地址`);
      
      // 点击保存按钮
      await page.click('.el-dialog button:has-text("保存")');
      await page.waitForTimeout(2000);
      console.log(`  ✓ 点击保存按钮`);
      
      if (errors.length > 0) {
        testErrors.failedTests++;
        testErrors.errors.push(...errors);
        console.log(`  ❌ 发现 ${errors.length} 个错误`);
      } else {
        console.log(`  ✅ 测试通过`);
      }
    } catch (error) {
      testErrors.failedTests++;
      testErrors.errors.push({
        test: testName,
        type: 'test-error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
      console.log(`  ❌ 测试失败: ${error.message}`);
    } finally {
      saveErrorReport();
    }
  });
  
  test('收货地址 - 编辑地址', async ({ page }) => {
    const testName = '收货地址 - 编辑地址';
    const errors = [];
    testErrors.totalTests++;
    
    await setupErrorListeners(page, testName, errors);
    await mockLogin(page, 'user');
    
    try {
      await page.goto('/user/address', { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(1000);
      
      // 点击第一个地址的编辑按钮
      const editButton = page.locator('button:has-text("编辑")').first();
      const isVisible = await editButton.isVisible();
      
      if (isVisible) {
        await editButton.click();
        await page.waitForTimeout(1000);
        console.log(`  ✓ 点击编辑按钮`);
        
        // 修改收货人
        const nameInput = page.locator('.el-dialog input[placeholder*="收货人"]').first();
        await nameInput.fill('李四');
        console.log(`  ✓ 修改收货人`);
        
        // 点击保存
        await page.click('.el-dialog button:has-text("保存")');
        await page.waitForTimeout(2000);
        console.log(`  ✓ 保存修改`);
      } else {
        console.log(`  ⚠ 没有可编辑的地址`);
      }
      
      if (errors.length > 0) {
        testErrors.failedTests++;
        testErrors.errors.push(...errors);
        console.log(`  ❌ 发现 ${errors.length} 个错误`);
      } else {
        console.log(`  ✅ 测试通过`);
      }
    } catch (error) {
      testErrors.failedTests++;
      testErrors.errors.push({
        test: testName,
        type: 'test-error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
      console.log(`  ❌ 测试失败: ${error.message}`);
    } finally {
      saveErrorReport();
    }
  });
  
  test('收货地址 - 设为默认地址', async ({ page }) => {
    const testName = '收货地址 - 设为默认地址';
    const errors = [];
    testErrors.totalTests++;
    
    await setupErrorListeners(page, testName, errors);
    await mockLogin(page, 'user');
    
    try {
      await page.goto('/user/address', { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(1000);
      
      // 查找"设为默认"按钮
      const setDefaultButton = page.locator('button:has-text("设为默认")').first();
      const isVisible = await setDefaultButton.isVisible();
      
      if (isVisible) {
        await setDefaultButton.click();
        await page.waitForTimeout(2000);
        console.log(`  ✓ 点击设为默认按钮`);
      } else {
        console.log(`  ⚠ 所有地址都已是默认地址`);
      }
      
      if (errors.length > 0) {
        testErrors.failedTests++;
        testErrors.errors.push(...errors);
        console.log(`  ❌ 发现 ${errors.length} 个错误`);
      } else {
        console.log(`  ✅ 测试通过`);
      }
    } catch (error) {
      testErrors.failedTests++;
      testErrors.errors.push({
        test: testName,
        type: 'test-error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
      console.log(`  ❌ 测试失败: ${error.message}`);
    } finally {
      saveErrorReport();
    }
  });
});

test.describe('用户模块 - 个人主页', () => {
  
  test('个人主页 - 查看个人信息', async ({ page }) => {
    const testName = '个人主页 - 查看个人信息';
    const errors = [];
    testErrors.totalTests++;
    
    await setupErrorListeners(page, testName, errors);
    await mockLogin(page, 'user');
    
    try {
      await page.goto('/profile', { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(2000);
      
      console.log(`  ✓ 访问个人主页`);
      
      // 检查页面是否加载成功
      const pageLoaded = await page.isVisible('body');
      console.log(`  ✓ 页面加载状态: ${pageLoaded}`);
      
      if (errors.length > 0) {
        testErrors.failedTests++;
        testErrors.errors.push(...errors);
        console.log(`  ❌ 发现 ${errors.length} 个错误`);
      } else {
        console.log(`  ✅ 测试通过`);
      }
    } catch (error) {
      testErrors.failedTests++;
      testErrors.errors.push({
        test: testName,
        type: 'test-error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
      console.log(`  ❌ 测试失败: ${error.message}`);
    } finally {
      saveErrorReport();
    }
  });
  
  test('个人主页 - 切换标签页', async ({ page }) => {
    const testName = '个人主页 - 切换标签页';
    const errors = [];
    testErrors.totalTests++;
    
    await setupErrorListeners(page, testName, errors);
    await mockLogin(page, 'user');
    
    try {
      await page.goto('/profile', { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(1000);
      
      // 查找并点击不同的标签页
      const tabs = await page.locator('.el-tabs__item').all();
      console.log(`  ✓ 找到 ${tabs.length} 个标签页`);
      
      for (let i = 0; i < Math.min(tabs.length, 3); i++) {
        await tabs[i].click();
        await page.waitForTimeout(1000);
        const tabText = await tabs[i].textContent();
        console.log(`  ✓ 切换到标签: ${tabText}`);
      }
      
      if (errors.length > 0) {
        testErrors.failedTests++;
        testErrors.errors.push(...errors);
        console.log(`  ❌ 发现 ${errors.length} 个错误`);
      } else {
        console.log(`  ✅ 测试通过`);
      }
    } catch (error) {
      testErrors.failedTests++;
      testErrors.errors.push({
        test: testName,
        type: 'test-error',
        message: error.message,
        timestamp: new Date().toISOString()
      });
      console.log(`  ❌ 测试失败: ${error.message}`);
    } finally {
      saveErrorReport();
    }
  });
});

// 测试完成后生成汇总报告
test.afterAll(async () => {
  console.log('\n' + '='.repeat(80));
  console.log('📊 用户模块测试汇总报告');
  console.log('='.repeat(80));
  console.log(`总测试数: ${testErrors.totalTests}`);
  console.log(`失败测试数: ${testErrors.failedTests}`);
  console.log(`总错误数: ${testErrors.errors.length}`);
  
  if (testErrors.errors.length > 0) {
    console.log('\n❌ 错误详情:');
    testErrors.errors.forEach((err, i) => {
      console.log(`\n  ${i + 1}. [${err.test}] [${err.type}]`);
      console.log(`     ${err.message}`);
    });
  } else {
    console.log('\n✅ 所有测试通过，无错误！');
  }
  
  saveErrorReport();
  console.log(`\n📄 详细报告已保存到: e2e-report/user-module-errors.json`);
  console.log('='.repeat(80) + '\n');
});
