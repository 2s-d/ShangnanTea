/**
 * API 测试 - 用户登录
 * 
 * API: POST /user/login
 * 操作ID: userLogin
 * 
 * 测试目标：
 * 1. API 调用成功（返回 code 2000）
 * 2. 无控制台错误
 * 
 * 前置条件：无
 */
const { test, expect } = require('@playwright/test')
const fs = require('fs')
const path = require('path')
const { setupErrorListeners, createErrorCollector } = require('../../helpers/error-collector')
const testData = require('../../fixtures/test-data.json')

// 创建错误收集器
const errorCollector = createErrorCollector('用户登录')

test.describe('API 测试 - 用户登录', () => {
  
  test('POST /user/login - 用户登录成功', async ({ page }) => {
    const testName = 'POST /user/login - 用户登录成功'
    const testContext = errorCollector.startTest(testName)
    
    // 设置错误监听
    setupErrorListeners(page, testName, testContext.errors)
    
    try {
      // 访问登录页面
      await page.goto('/login', { waitUntil: 'networkidle', timeout: 10000 })
      await page.waitForTimeout(1000)
      
      // 监控 API 调用
      const apiPromise = page.waitForResponse(
        response => response.url().includes('/user/login') && response.request().method() === 'POST',
        { timeout: 10000 }
      )
      
      // 填写登录表单
      await page.fill('input[placeholder*="用户名"]', testData.users.user.username)
      await page.fill('input[type="password"]', testData.users.user.password)
      
      // 点击登录按钮
      await page.click('button:has-text("登录")')
      
      // 等待 API 响应
      const response = await apiPromise
      const responseData = await response.json()
      
      // 验证 API 响应
      console.log(`  ✓ API 状态码: ${response.status()}`)
      console.log(`  ✓ 业务状态码: ${responseData.code}`)
      
      // 断言：API 调用成功
      expect(response.status()).toBe(200)
      expect(responseData.code).toBe(2000)
      expect(responseData.data).toHaveProperty('token')
      
      // 等待页面跳转
      await page.waitForTimeout(2000)
      
      // 检查是否有错误
      const errors = testContext.endTest()
      
      if (errors.length > 0) {
        console.log(`  ❌ 发现 ${errors.length} 个错误:`)
        errors.forEach((err, i) => {
          console.log(`     ${i + 1}. [${err.type}] ${err.message}`)
        })
        
        // 截图保存错误现场
        await page.screenshot({ 
          path: path.join(__dirname, '../../e2e-report/error-screenshot.png'),
          fullPage: true 
        })
      } else {
        console.log('  ✅ 测试通过 - API 调用成功且无控制台错误')
      }
      
      // 断言：无错误
      expect(errors.length, `发现 ${errors.length} 个控制台错误`).toBe(0)
      
    } catch (error) {
      console.log(`  ❌ 测试失败: ${error.message}`)
      testContext.endTest()
      throw error
    }
  })
  
  test('POST /user/login - 错误的用户名或密码', async ({ page }) => {
    const testName = 'POST /user/login - 错误的用户名或密码'
    const testContext = errorCollector.startTest(testName)
    
    // 设置错误监听
    setupErrorListeners(page, testName, testContext.errors)
    
    try {
      await page.goto('/login', { waitUntil: 'networkidle', timeout: 10000 })
      await page.waitForTimeout(1000)
      
      // 监控 API 调用
      const apiPromise = page.waitForResponse(
        response => response.url().includes('/user/login') && response.request().method() === 'POST',
        { timeout: 10000 }
      )
      
      // 填写错误的登录信息
      await page.fill('input[placeholder*="用户名"]', 'wronguser')
      await page.fill('input[type="password"]', 'wrongpassword')
      
      // 点击登录按钮
      await page.click('button:has-text("登录")')
      
      // 等待 API 响应
      const response = await apiPromise
      const responseData = await response.json()
      
      // 验证 API 响应
      console.log(`  ✓ API 状态码: ${response.status()}`)
      console.log(`  ✓ 业务状态码: ${responseData.code}`)
      
      // 断言：API 返回失败状态码
      expect(response.status()).toBe(200)
      expect(responseData.code).toBe(2100) // 登录失败
      
      await page.waitForTimeout(1000)
      
      // 检查错误
      const errors = testContext.endTest()
      
      if (errors.length > 0) {
        console.log(`  ❌ 发现 ${errors.length} 个错误`)
      } else {
        console.log('  ✅ 测试通过 - API 正确返回失败状态且无控制台错误')
      }
      
      // 断言：无错误
      expect(errors.length, `发现 ${errors.length} 个控制台错误`).toBe(0)
      
    } catch (error) {
      console.log(`  ❌ 测试失败: ${error.message}`)
      testContext.endTest()
      throw error
    }
  })
})

// 测试完成后生成报告
test.afterAll(async () => {
  const report = errorCollector.generateReport()
  
  console.log('\n' + '='.repeat(80))
  console.log(`📊 ${report.moduleName} - 测试报告`)
  console.log('='.repeat(80))
  console.log(`总测试数: ${report.totalTests}`)
  console.log(`通过: ${report.passedTests}`)
  console.log(`失败: ${report.failedTests}`)
  console.log(`总错误数: ${report.totalErrors}`)
  
  if (report.errors.length > 0) {
    console.log('\n❌ 错误详情:')
    report.errors.forEach((err, i) => {
      console.log(`  ${i + 1}. [${err.test}] [${err.type}]`)
      console.log(`     ${err.message}`)
    })
  }
  
  // 保存报告
  const reportDir = path.join(__dirname, '../../e2e-report')
  if (!fs.existsSync(reportDir)) {
    fs.mkdirSync(reportDir, { recursive: true })
  }
  
  const reportPath = path.join(reportDir, 'test-report.json')
  fs.writeFileSync(reportPath, JSON.stringify(report, null, 2), 'utf-8')
  
  console.log(`\n📄 详细报告: ${reportPath}`)
  console.log('='.repeat(80) + '\n')
})
