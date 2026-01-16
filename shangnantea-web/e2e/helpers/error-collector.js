/**
 * 错误收集器
 * 
 * 统一收集和管理测试过程中的错误
 */

// 可忽略的错误模式
const IGNORABLE_ERROR_PATTERNS = [
  /Download the Vue Devtools/i,
  /\[Vue warn\].*deprecated/i,
  /ResizeObserver loop/i,
  /ElementPlusError/i,
  /ElementPlus警告/i,
  /\[el-.*\].*deprecated/i,
  /hot-update/i
];

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
 * 为页面设置错误监听器
 * @param {Page} page - Playwright page 对象
 * @param {string} testName - 测试名称
 * @param {Array} errorArray - 错误数组（用于存储收集到的错误）
 */
function setupErrorListeners(page, testName, errorArray) {
  // 1. 监听控制台错误
  page.on('console', msg => {
    if (msg.type() === 'error') {
      const message = msg.text();
      if (!shouldIgnoreError(message)) {
        // 尝试从控制台消息中提取更多信息
        const location = msg.location();
        const errorDetail = {
          test: testName,
          type: 'console',
          message,
          timestamp: new Date().toISOString()
        };
        
        // 如果有位置信息，添加到错误详情中
        if (location && location.url) {
          errorDetail.url = location.url;
          errorDetail.line = location.lineNumber;
          errorDetail.column = location.columnNumber;
        }
        
        errorArray.push(errorDetail);
      }
    }
  });
  
  // 2. 监听页面运行时错误
  page.on('pageerror', error => {
    const message = error.message;
    if (!shouldIgnoreError(message)) {
      errorArray.push({
        test: testName,
        type: 'runtime',
        message: `[Runtime Error] ${message}`,
        stack: error.stack,
        timestamp: new Date().toISOString()
      });
    }
  });
  
  // 3. 监听网络请求失败（包括 404）
  page.on('response', async response => {
    const url = response.url();
    const status = response.status();
    
    // 记录所有失败的请求（4xx, 5xx），包括静态资源
    if (status >= 400 && !url.includes('hot-update')) {
      const method = response.request().method();
      const resourceType = response.request().resourceType();
      
      errorArray.push({
        test: testName,
        type: 'network',
        message: `[${status}] ${method} ${url}`,
        resourceType,
        timestamp: new Date().toISOString()
      });
    }
  });
}

/**
 * 创建错误收集器实例
 * @param {string} moduleName - 模块名称
 * @returns {Object} 错误收集器对象
 */
function createErrorCollector(moduleName) {
  const errors = [];
  const stats = {
    moduleName,
    totalTests: 0,
    passedTests: 0,
    failedTests: 0,
    totalErrors: 0
  };
  
  return {
    /**
     * 开始一个新测试
     */
    startTest(testName) {
      stats.totalTests++;
      const testErrors = [];
      
      return {
        errors: testErrors,
        
        /**
         * 结束测试，统计结果
         */
        endTest() {
          const uniqueErrors = deduplicateErrors(testErrors);
          
          if (uniqueErrors.length > 0) {
            stats.failedTests++;
            stats.totalErrors += uniqueErrors.length;
            errors.push(...uniqueErrors);
          } else {
            stats.passedTests++;
          }
          
          return uniqueErrors;
        }
      };
    },
    
    /**
     * 获取统计信息
     */
    getStats() {
      return { ...stats };
    },
    
    /**
     * 获取所有错误
     */
    getErrors() {
      return [...errors];
    },
    
    /**
     * 生成报告
     */
    generateReport() {
      return {
        ...stats,
        errors: errors,
        timestamp: new Date().toISOString()
      };
    }
  };
}

module.exports = {
  shouldIgnoreError,
  deduplicateErrors,
  setupErrorListeners,
  createErrorCollector
};
