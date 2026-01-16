/**
 * API 测试辅助函数
 * 
 * 提供统一的 API 调用监控和错误收集功能
 */

/**
 * 监控页面中的 API 调用
 * @param {Page} page - Playwright page 对象
 * @param {string} apiPath - 要监控的 API 路径（支持通配符）
 * @param {string} method - HTTP 方法（GET, POST, PUT, DELETE）
 * @returns {Promise<Object>} 返回 API 响应信息
 */
async function monitorApiCall(page, apiPath, method = 'GET') {
  return new Promise((resolve) => {
    const handler = async (response) => {
      const url = response.url();
      const reqMethod = response.request().method();
      
      // 检查是否匹配目标 API
      if (url.includes(apiPath) && reqMethod === method) {
        const status = response.status();
        let responseData = null;
        
        try {
          responseData = await response.json();
        } catch (e) {
          // 非 JSON 响应
        }
        
        // 移除监听器
        page.off('response', handler);
        
        resolve({
          url,
          method: reqMethod,
          status,
          data: responseData,
          success: status >= 200 && status < 300
        });
      }
    };
    
    page.on('response', handler);
  });
}

/**
 * 等待 API 调用完成
 * @param {Page} page - Playwright page 对象
 * @param {string} apiPath - API 路径
 * @param {number} timeout - 超时时间（毫秒）
 */
async function waitForApi(page, apiPath, timeout = 10000) {
  try {
    await page.waitForResponse(
      response => response.url().includes(apiPath),
      { timeout }
    );
    return true;
  } catch (error) {
    console.error(`等待 API ${apiPath} 超时`);
    return false;
  }
}

/**
 * 触发 API 调用并监控结果
 * @param {Page} page - Playwright page 对象
 * @param {Function} triggerAction - 触发 API 的操作（如点击按钮）
 * @param {string} apiPath - 要监控的 API 路径
 * @param {string} method - HTTP 方法
 * @returns {Promise<Object>} API 响应信息
 */
async function triggerAndMonitorApi(page, triggerAction, apiPath, method = 'GET') {
  const apiPromise = monitorApiCall(page, apiPath, method);
  await triggerAction();
  return await apiPromise;
}

module.exports = {
  monitorApiCall,
  waitForApi,
  triggerAndMonitorApi
};
