/**
 * E2E 测试 - 全站控制台错误扫描
 * 
 * 功能：
 * 1. 自动遍历所有路由
 * 2. 收集每个页面的控制台错误
 * 3. 生成测试报告
 */
const { test, expect } = require('@playwright/test');

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

// 存储所有错误
const allErrors = [];

// 模拟登录 - 设置 token
// 直接使用 Apifox mock 数据中的 token，确保格式完全一致
const MOCK_TOKENS = {
  // admin token: {"sub":"1","role":1,"username":"admin","exp":1767225600}
  admin: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwicm9sZSI6MSwidXNlcm5hbWUiOiJhZG1pbiIsImV4cCI6MTc2NzIyNTYwMH0.mock',
  // user token: {"sub":"2","role":2,"username":"user","exp":1767225600}
  user: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyIiwicm9sZSI6MiwidXNlcm5hbWUiOiJ1c2VyIiwiZXhwIjoxNzY3MjI1NjAwfQ.mock',
  // shop token: {"sub":"3","role":3,"username":"shop","exp":1767225600}
  shop: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIzIiwicm9sZSI6MywidXNlcm5hbWUiOiJzaG9wIiwiZXhwIjoxNzY3MjI1NjAwfQ.mock'
};

async function mockLogin(page, role = 'user') {
  const mockToken = MOCK_TOKENS[role] || MOCK_TOKENS.user;
  
  // 设置 localStorage - 注意：
  // 1. useStorage 使用 'shangnantea_' 前缀
  // 2. useStorage 使用 JSON.stringify/JSON.parse 序列化，所以需要 JSON.stringify 包装
  await page.evaluate((token) => {
    localStorage.setItem('shangnantea_token', JSON.stringify(token));
  }, mockToken);
}

test.describe('全站控制台错误扫描', () => {
  
  test.beforeEach(async ({ page }) => {
    // 监听控制台错误
    page.on('console', msg => {
      if (msg.type() === 'error') {
        allErrors.push({
          page: page.url(),
          message: msg.text(),
          time: new Date().toISOString()
        });
      }
    });
    
    // 监听页面错误
    page.on('pageerror', error => {
      allErrors.push({
        page: page.url(),
        message: `[PageError] ${error.message}`,
        time: new Date().toISOString()
      });
    });
  });

  // 测试无需登录的页面
  for (const route of routes.filter(r => !r.requireAuth)) {
    test(`${route.name} (${route.path}) - 无需登录`, async ({ page }) => {
      const pageErrors = [];
      
      page.on('console', msg => {
        if (msg.type() === 'error') {
          pageErrors.push(msg.text());
        }
      });
      
      await page.goto(route.path);
      await page.waitForLoadState('networkidle');
      await page.waitForTimeout(2000); // 等待异步操作完成
      
      // 输出该页面的错误
      if (pageErrors.length > 0) {
        console.log(`\n❌ ${route.name} 发现 ${pageErrors.length} 个错误:`);
        pageErrors.forEach((err, i) => console.log(`  ${i + 1}. ${err}`));
      } else {
        console.log(`\n✅ ${route.name} 无控制台错误`);
      }
      
      // 断言：期望没有错误（如果有错误测试会失败但会继续执行其他测试）
      expect(pageErrors.length, `${route.name} 存在控制台错误`).toBe(0);
    });
  }

  // 测试需要登录的页面
  for (const route of routes.filter(r => r.requireAuth && !r.role)) {
    test(`${route.name} (${route.path}) - 需要登录`, async ({ page }) => {
      const pageErrors = [];
      
      page.on('console', msg => {
        if (msg.type() === 'error') {
          pageErrors.push(msg.text());
        }
      });
      
      // 先访问登录页设置 token
      await page.goto('/login');
      await mockLogin(page, 'user');
      
      // 访问目标页面
      await page.goto(route.path);
      await page.waitForLoadState('networkidle');
      await page.waitForTimeout(2000);
      
      if (pageErrors.length > 0) {
        console.log(`\n❌ ${route.name} 发现 ${pageErrors.length} 个错误:`);
        pageErrors.forEach((err, i) => console.log(`  ${i + 1}. ${err}`));
      } else {
        console.log(`\n✅ ${route.name} 无控制台错误`);
      }
      
      expect(pageErrors.length, `${route.name} 存在控制台错误`).toBe(0);
    });
  }

  // 测试管理员页面
  for (const route of routes.filter(r => r.role === 'admin')) {
    test(`${route.name} (${route.path}) - 管理员`, async ({ page }) => {
      const pageErrors = [];
      
      page.on('console', msg => {
        if (msg.type() === 'error') {
          pageErrors.push(msg.text());
        }
      });
      
      await page.goto('/login');
      await mockLogin(page, 'admin');
      
      await page.goto(route.path);
      await page.waitForLoadState('networkidle');
      await page.waitForTimeout(2000);
      
      if (pageErrors.length > 0) {
        console.log(`\n❌ ${route.name} 发现 ${pageErrors.length} 个错误:`);
        pageErrors.forEach((err, i) => console.log(`  ${i + 1}. ${err}`));
      } else {
        console.log(`\n✅ ${route.name} 无控制台错误`);
      }
      
      expect(pageErrors.length, `${route.name} 存在控制台错误`).toBe(0);
    });
  }

  // 测试商家页面
  for (const route of routes.filter(r => r.role === 'shop')) {
    test(`${route.name} (${route.path}) - 商家`, async ({ page }) => {
      const pageErrors = [];
      
      page.on('console', msg => {
        if (msg.type() === 'error') {
          pageErrors.push(msg.text());
        }
      });
      
      await page.goto('/login');
      await mockLogin(page, 'shop');
      
      await page.goto(route.path);
      await page.waitForLoadState('networkidle');
      await page.waitForTimeout(2000);
      
      if (pageErrors.length > 0) {
        console.log(`\n❌ ${route.name} 发现 ${pageErrors.length} 个错误:`);
        pageErrors.forEach((err, i) => console.log(`  ${i + 1}. ${err}`));
      } else {
        console.log(`\n✅ ${route.name} 无控制台错误`);
      }
      
      expect(pageErrors.length, `${route.name} 存在控制台错误`).toBe(0);
    });
  }
});
