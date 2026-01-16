/**
 * 认证辅助函数
 * 
 * 提供登录、token 管理等认证相关功能
 */

// 模拟登录 token（过期时间设置为 2030-01-01）
const MOCK_TOKENS = {
  // admin token: {"sub":"1","role":1,"username":"admin","exp":1893456000}
  admin: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwicm9sZSI6MSwidXNlcm5hbWUiOiJhZG1pbiIsImV4cCI6MTg5MzQ1NjAwMH0.dGVzdF9zaWduYXR1cmVfZm9yX2FkbWlu',
  
  // user token: {"sub":"2","role":2,"username":"user","exp":1893456000}
  user: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyIiwicm9sZSI6MiwidXNlcm5hbWUiOiJ1c2VyIiwiZXhwIjoxODkzNDU2MDAwfQ.dGVzdF9zaWduYXR1cmVfZm9yX3VzZXI',
  
  // shop token: {"sub":"3","role":3,"username":"shop","exp":1893456000}
  shop: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIzIiwicm9sZSI6MywidXNlcm5hbWUiOiJzaG9wIiwiZXhwIjoxODkzNDU2MDAwfQ.dGVzdF9zaWduYXR1cmVfZm9yX3Nob3A'
};

/**
 * 模拟登录（通过设置 localStorage）
 * @param {Page} page - Playwright page 对象
 * @param {string} role - 用户角色（admin, user, shop）
 */
async function mockLogin(page, role = 'user') {
  const mockToken = MOCK_TOKENS[role] || MOCK_TOKENS.user;
  
  // 先访问登录页，确保页面已加载
  await page.goto('/login', { waitUntil: 'domcontentloaded' });
  
  // 设置 token 到 localStorage
  await page.evaluate((token) => {
    localStorage.setItem('shangnantea_token', JSON.stringify(token));
  }, mockToken);
  
  return mockToken;
}

/**
 * 清除登录状态
 * @param {Page} page - Playwright page 对象
 */
async function clearAuth(page) {
  await page.evaluate(() => {
    localStorage.removeItem('shangnantea_token');
  });
}

/**
 * 获取当前 token
 * @param {Page} page - Playwright page 对象
 * @returns {Promise<string|null>} 当前 token
 */
async function getToken(page) {
  return await page.evaluate(() => {
    const token = localStorage.getItem('shangnantea_token');
    return token ? JSON.parse(token) : null;
  });
}

/**
 * 检查是否已登录
 * @param {Page} page - Playwright page 对象
 * @returns {Promise<boolean>} 是否已登录
 */
async function isLoggedIn(page) {
  const token = await getToken(page);
  return !!token;
}

module.exports = {
  MOCK_TOKENS,
  mockLogin,
  clearAuth,
  getToken,
  isLoggedIn
};
