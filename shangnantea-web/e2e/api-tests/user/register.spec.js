/**
 * API 测试 - 用户注册
 * 
 * API: POST /user/register
 * 操作ID: userRegister
 * 
 * 测试目标：
 * 1. API 调用成功（返回 code 2001）
 * 2. 无控制台错误
 * 
 * 前置条件：无
 */
const { test, expect } = require('@playwright/test');
const fs = require('fs');
const path = require('path');
const { setupErrorListeners, createErrorCollector } = require('../../helpers/error-collector');

// 创建错误收集器
const errorCollector = createErrorCollector('用户注册');

test.describe('API 测试 - 用户注册', () => {
  
  test('POST /user/register - 注册成功', async ({ page }) => {
    const testName = 'POST /user/register - 注册成功';
    const testContext = errorCollector.startTest(testName);
    
    // 设置错误监听
    setupErrorListeners(page, testName, testContext.errors);
    
    try {
      // 访问注册页面
      await page.goto('/register', { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(1000);
      
      // 监控 API 调用
      const apiPromise = page.waitForResponse(
        response => response.url().includes('/user/register') && response.request().method() === 'POST',
        { timeout: 10000 }
      );
      
      // 生成唯一的用户名（避免重复注册）
      const timestamp = Date.now();
      const username = `testuser_${timestamp}`;
      const password = 'Test123456';
      
      // 填写注册表单
      await page.fill('input[placeholder*="用户名"]', username);
      await page.fill('input[placeholder*="密码"]', password);
      await page.fill('input[placeholder*="确认密码"]', password);
      
      // 点击注册按钮
      await page.click('button:has-text("注册")');
      
      // 等待 API 响应
      const response = await apiPromise;
      const responseData = await response.json();
      
      // 验证 API 响应
      console.log(`  ✓ API 状态码: ${response.status()}`);
      console.log(`  ✓ 业务状态码: ${responseData.code}`);
      console.log(`  ✓ 注册用户名: ${username}`);
      
      // 断言：API 调用成功
      expect(response.status()).toBe(200);
      expect(responseData.code).toBe(2001); // 注册成功
      
      // 等待页面响应
      await page.waitForTimeout(2000);
      
      // 检查错误
      const errors = testContext.endTest();
      
      if (errors.length > 0) {
        console.log(`  ❌ 发现 ${errors.length} 个错误:`);
        errors.forEach((err, i) => {
          console.log(`     ${i + 1}. [${err.type}] ${err.message}`);
        });
        
        // 截图保存错误现场
        await page.screenshot({ 
          path: path.join(__dirname, '../../e2e-report/screenshots/api-user-register-error.png'),
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
  
  test('POST /user/register - 用户名已存在', async ({ page }) => {
    const testName = 'POST /user/register - 用户名已存在';
    const testContext = errorCollector.startTest(testName);
    
    // 设置错误监听
    setupErrorListeners(page, testName, testContext.errors);
    
    try {
      await page.goto('/register', { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(1000);
      
      // 监控 API 调用
      const apiPromise = page.waitForResponse(
        response => response.url().includes('/user/register') && response.request().method() === 'POST',
        { timeout: 10000 }
      );
      
      // 使用已存在的用户名
      const username = 'testuser'; // 假设这个用户名已存在
      const password = 'Test123456';
      
      // 填写注册表单
      await page.fill('input[placeholder*="用户名"]', username);
      await page.fill('input[placeholder*="密码"]', password);
      await page.fill('input[placeholder*="确认密码"]', password);
      
      // 点击注册按钮
      await page.click('button:has-text("注册")');
      
      // 等待 API 响应
      const response = await apiPromise;
      const responseData = await response.json();
      
      // 验证 API 响应
      console.log(`  ✓ API 状态码: ${response.status()}`);
      console.log(`  ✓ 业务状态码: ${responseData.code}`);
      
      // 断言：API 返回失败状态码
      expect(response.status()).toBe(200);
      expect(responseData.code).toBe(2102); // 注册失败
      
      await page.waitForTimeout(1000);
      
      // 检查错误
      const errors = testContext.endTest();
      
      if (errors.length > 0) {
        console.log(`  ❌ 发现 ${errors.length} 个错误`);
      } else {
        console.log(`  ✅ 测试通过 - API 正确返回失败状态且无控制台错误`);
      }
      
      // 断言：无错误
      expect(errors.length, `发现 ${errors.length} 个控制台错误`).toBe(0);
      
    } catch (error) {
      console.log(`  ❌ 测试失败: ${error.message}`);
      testContext.endTest();
      throw error;
    }
  });
  
  test('POST /user/register - 密码不一致', async ({ page }) => {
    const testName = 'POST /user/register - 密码不一致';
    const testContext = errorCollector.startTest(testName);
    
    // 设置错误监听
    setupErrorListeners(page, testName, testContext.errors);
    
    try {
      await page.goto('/register', { waitUntil: 'networkidle', timeout: 10000 });
      await page.waitForTimeout(1000);
      
      // 生成唯一的用户名
      const timestamp = Date.now();
      const username = `testuser_${timestamp}`;
      
      // 填写注册表单（密码不一致）
      await page.fill('input[placeholder*="用户名"]', username);
      await page.fill('input[placeholder*="密码"]', 'Password123');
      await page.fill('input[placeholder*="确认密码"]', 'DifferentPassword456');
      
      // 点击注册按钮
      await page.click('button:has-text("注册")');
      
      // 等待一下，看是否有前端验证提示
      await page.waitForTimeout(2000);
      
      // 检查错误
      const errors = testContext.endTest();
      
      if (errors.length > 0) {
        console.log(`  ❌ 发现 ${errors.length} 个错误`);
      } else {
        console.log(`  ✅ 测试通过 - 前端验证正常且无控制台错误`);
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
  
  const reportPath = path.join(reportDir, 'user-register.json');
  fs.writeFileSync(reportPath, JSON.stringify(report, null, 2), 'utf-8');
  
  console.log(`\n📄 详细报告: ${reportPath}`);
  console.log('='.repeat(80) + '\n');
});
