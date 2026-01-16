/**
 * E2E 测试 - 全站控制台错误扫描（改进版）
 * 
 * 功能：
 * 1. 自动遍历所有路由
 * 2. 收集控制台错误、网络错误、资源加载失败
 * 3. 错误分类和去重
 * 4. 生成详细测试报告
 * 5. 自动截图保存错误现场
 */
const { test, expect } = require('@playwright/test');
const fs = require('fs');
const path = require('path');

// 所有需要测试的路由（从 router/index.js 提取）
const routes = [
  // 无需登录的页面
  { path: '/login', name: '登录页', requireAuth: false },
  { path: '/register', name: '注册页', requireAuth: false },
  
  // 需要登录的页面
  { path: '/tea-culture', name: '茶文化首页', requireAuth: true },
  { path: '/tea/mall', name: '茶叶商城', requireAuth: true },
  { path: '/forum/list', name: '茶友论坛', requireAuth: true },
  { path: '/order/cart', name: '购物车', requireAuth: true },
  { path: '/order/list', name: '我的订单', requireAuth: true },
  { path: '/user/settings', name: '个人设置', requireAuth: true },
  { path: '/user/address', name: '收货地址', requireAuth: true },
  { path: '/user/profile', name: '个人资料', requireAuth: true },
  { path: '/shop/list', name: '商铺列表', requireAuth: true },
  { path: '/message/center', name: '消息中心', requireAuth: true },
  { path: '/message/chat', name: '聊天页面', requireAuth: true },
  { path: '/profile', name: '个人主页', requireAuth: true },
  
  // 管理页面（需要特定角色）
  { path: '/tea/manage', name: '茶叶管理', requireAuth: true, role: 'admin' },
  { path: '/forum/manage', name: '内容管理', requireAuth: true, role: 'admin' },
  { path: '/culture/manage', name: '茶文化管理', requireAuth: true, role: 'admin' },
  { path: '/user/manage', name: '用户管理', requireAuth: true, role: 'admin' },
  { path: '/order/manage', name: '订单管理', requireAuth: true, role: 'admin' },
  { path: '/shop/manage', name: '商家店铺', requireAuth: true, role: 'shop' },
];

// 可忽略的错误模式（开发环境警告、已知的第三方库问题等）
const IGNORABLE_ERROR_PATTERNS = [
  /Download the Vue Devtools/i,
  /\[Vue warn\].*deprecated/i,
  /ResizeObserver loop/i,
  /ElementPlusError/i,
  /ElementPlus警告/i, // Element Plus 中文警告
  /\[props\].*deprecated/i, // Element Plus 属性废弃警告
  /\[el-radio\].*deprecated/i, // Element Plus radio 废弃警告
  /\[el-checkbox\].*deprecated/i, // Element Plus checkbox 废弃警告
  /\[el-.*\].*deprecated/i, // 所有 Element Plus 组件的废弃警告
];


// 存储所有错误的全局对象
const testResults = {
  totalPages: 0,
  pagesWithErrors: 0,
  totalErrors: 0,
  errorsByPage: {},
  errorsByType: {
    console: 0,
    network: 0,
    resource: 0,
    runtime: 0
  }
};

// 模拟登录 - 设置 token
// 注意：这些是有效的 JWT token，payload 部分是 base64 编码的 JSON
// 过期时间设置为 2030-01-01，确保足够长的有效期
const MOCK_TOKENS = {
  // admin token: {"sub":"1","role":1,"username":"admin","exp":1893456000}
  admin: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwicm9sZSI6MSwidXNlcm5hbWUiOiJhZG1pbiIsImV4cCI6MTg5MzQ1NjAwMH0.dGVzdF9zaWduYXR1cmVfZm9yX2FkbWlu',
  
  // user token: {"sub":"2","role":2,"username":"user","exp":1893456000}
  user: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyIiwicm9sZSI6MiwidXNlcm5hbWUiOiJ1c2VyIiwiZXhwIjoxODkzNDU2MDAwfQ.dGVzdF9zaWduYXR1cmVfZm9yX3VzZXI',
  
  // shop token: {"sub":"3","role":3,"username":"shop","exp":1893456000}
  shop: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIzIiwicm9sZSI6MywidXNlcm5hbWUiOiJzaG9wIiwiZXhwIjoxODkzNDU2MDAwfQ.dGVzdF9zaWduYXR1cmVfZm9yX3Nob3A'
};

/**
 * 模拟登录
 */
async function mockLogin(page, role = 'user') {
  const mockToken = MOCK_TOKENS[role] || MOCK_TOKENS.user;
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
 * 错误去重（基于错误消息的前100个字符）
 */
function deduplicateErrors(errors) {
  const seen = new Set();
  return errors.filter(error => {
    const key = error.message.substring(0, 100);
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

/**
 * 设置页面错误监听器
 */
async function setupErrorListeners(page, pageErrors) {
  // 1. 监听控制台错误
  page.on('console', msg => {
    if (msg.type() === 'error') {
      const message = msg.text();
      if (!shouldIgnoreError(message)) {
        pageErrors.push({
          type: 'console',
          message,
          timestamp: new Date().toISOString()
        });
      }
    }
  });
  
  // 2. 监听页面运行时错误
  page.on('pageerror', error => {
    const message = error.message;
    if (!shouldIgnoreError(message)) {
      pageErrors.push({
        type: 'runtime',
        message: `[Runtime Error] ${message}`,
        stack: error.stack,
        timestamp: new Date().toISOString()
      });
    }
  });
  
  // 3. 监听网络请求失败
  page.on('response', async response => {
    const url = response.url();
    const status = response.status();
    
    // 只记录 API 请求失败（4xx, 5xx）
    if (status >= 400 && !url.includes('hot-update')) {
      // 判断是否是 API 请求
      const isApiRequest = url.includes('/api/') || 
                          (!url.match(/\.(js|css|png|jpg|jpeg|gif|svg|ico|woff|woff2|ttf|eot)$/i));
      
      if (isApiRequest) {
        pageErrors.push({
          type: 'network',
          message: `[${status}] ${response.request().method()} ${url}`,
          timestamp: new Date().toISOString()
        });
      } else {
        // 资源加载失败
        pageErrors.push({
          type: 'resource',
          message: `[${status}] Resource failed: ${url}`,
          timestamp: new Date().toISOString()
        });
      }
    }
  });
}

/**
 * 测试单个页面
 */
async function testPage(page, route, role = null) {
  const pageErrors = [];
  
  // 设置错误监听
  await setupErrorListeners(page, pageErrors);
  
  // 如果需要登录，先设置 token
  if (route.requireAuth) {
    await page.goto('/login');
    await mockLogin(page, role || 'user');
  }
  
  // 访问目标页面
  try {
    await page.goto(route.path, { waitUntil: 'networkidle', timeout: 10000 });
  } catch (error) {
    // 页面加载超时或失败
    pageErrors.push({
      type: 'runtime',
      message: `[Page Load Error] ${error.message}`,
      timestamp: new Date().toISOString()
    });
  }
  
  // 等待页面稳定
  await page.waitForTimeout(2000);
  
  // 错误去重
  const uniqueErrors = deduplicateErrors(pageErrors);
  
  // 更新统计
  testResults.totalPages++;
  if (uniqueErrors.length > 0) {
    testResults.pagesWithErrors++;
    testResults.totalErrors += uniqueErrors.length;
    testResults.errorsByPage[route.name] = uniqueErrors;
    
    // 按类型统计
    uniqueErrors.forEach(err => {
      testResults.errorsByType[err.type]++;
    });
  }
  
  return uniqueErrors;
}


test.describe('全站控制台错误扫描', () => {
  
  // 测试无需登录的页面
  for (const route of routes.filter(r => !r.requireAuth)) {
    test(`${route.name} (${route.path}) - 无需登录`, async ({ page }) => {
      const errors = await testPage(page, route);
      
      // 输出结果
      if (errors.length > 0) {
        console.log(`\n❌ ${route.name} 发现 ${errors.length} 个错误:`);
        errors.forEach((err, i) => {
          console.log(`  ${i + 1}. [${err.type}] ${err.message}`);
        });
        
        // 截图保存错误现场
        await page.screenshot({ 
          path: `e2e-screenshots/${route.name.replace(/\//g, '-')}-error.png`,
          fullPage: true 
        });
      } else {
        console.log(`\n✅ ${route.name} 无错误`);
      }
      
      // 断言：期望没有错误
      expect(errors.length, `${route.name} 存在 ${errors.length} 个错误`).toBe(0);
    });
  }

  // 测试需要登录的页面
  for (const route of routes.filter(r => r.requireAuth && !r.role)) {
    test(`${route.name} (${route.path}) - 需要登录`, async ({ page }) => {
      const errors = await testPage(page, route, 'user');
      
      if (errors.length > 0) {
        console.log(`\n❌ ${route.name} 发现 ${errors.length} 个错误:`);
        errors.forEach((err, i) => {
          console.log(`  ${i + 1}. [${err.type}] ${err.message}`);
        });
        
        await page.screenshot({ 
          path: `e2e-screenshots/${route.name.replace(/\//g, '-')}-error.png`,
          fullPage: true 
        });
      } else {
        console.log(`\n✅ ${route.name} 无错误`);
      }
      
      expect(errors.length, `${route.name} 存在 ${errors.length} 个错误`).toBe(0);
    });
  }

  // 测试管理员页面
  for (const route of routes.filter(r => r.role === 'admin')) {
    test(`${route.name} (${route.path}) - 管理员`, async ({ page }) => {
      const errors = await testPage(page, route, 'admin');
      
      if (errors.length > 0) {
        console.log(`\n❌ ${route.name} 发现 ${errors.length} 个错误:`);
        errors.forEach((err, i) => {
          console.log(`  ${i + 1}. [${err.type}] ${err.message}`);
        });
        
        await page.screenshot({ 
          path: `e2e-screenshots/${route.name.replace(/\//g, '-')}-error.png`,
          fullPage: true 
        });
      } else {
        console.log(`\n✅ ${route.name} 无错误`);
      }
      
      expect(errors.length, `${route.name} 存在 ${errors.length} 个错误`).toBe(0);
    });
  }

  // 测试商家页面
  for (const route of routes.filter(r => r.role === 'shop')) {
    test(`${route.name} (${route.path}) - 商家`, async ({ page }) => {
      const errors = await testPage(page, route, 'shop');
      
      if (errors.length > 0) {
        console.log(`\n❌ ${route.name} 发现 ${errors.length} 个错误:`);
        errors.forEach((err, i) => {
          console.log(`  ${i + 1}. [${err.type}] ${err.message}`);
        });
        
        await page.screenshot({ 
          path: `e2e-screenshots/${route.name.replace(/\//g, '-')}-error.png`,
          fullPage: true 
        });
      } else {
        console.log(`\n✅ ${route.name} 无错误`);
      }
      
      expect(errors.length, `${route.name} 存在 ${errors.length} 个错误`).toBe(0);
    });
  }
  
  // 所有测试完成后，生成汇总报告
  test.afterAll(async () => {
    console.log('\n' + '='.repeat(80));
    console.log('📊 测试汇总报告');
    console.log('='.repeat(80));
    console.log(`总测试页面数: ${testResults.totalPages}`);
    console.log(`有错误的页面: ${testResults.pagesWithErrors}`);
    console.log(`总错误数: ${testResults.totalErrors}`);
    console.log('\n错误类型分布:');
    console.log(`  - 控制台错误: ${testResults.errorsByType.console}`);
    console.log(`  - 网络请求错误: ${testResults.errorsByType.network}`);
    console.log(`  - 资源加载错误: ${testResults.errorsByType.resource}`);
    console.log(`  - 运行时错误: ${testResults.errorsByType.runtime}`);
    
    if (testResults.pagesWithErrors > 0) {
      console.log('\n❌ 有错误的页面详情:');
      Object.entries(testResults.errorsByPage).forEach(([pageName, errors]) => {
        console.log(`\n  ${pageName}:`);
        errors.forEach((err, i) => {
          console.log(`    ${i + 1}. [${err.type}] ${err.message}`);
        });
      });
    }
    
    // 保存 JSON 格式的详细报告
    const reportDir = 'e2e-report';
    if (!fs.existsSync(reportDir)) {
      fs.mkdirSync(reportDir, { recursive: true });
    }
    
    fs.writeFileSync(
      path.join(reportDir, 'error-summary.json'),
      JSON.stringify(testResults, null, 2)
    );
    
    console.log(`\n📄 详细报告已保存到: ${reportDir}/error-summary.json`);
    console.log('='.repeat(80) + '\n');
  });
});
