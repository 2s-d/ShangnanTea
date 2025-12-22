/**
 * 对比前端 API 和 OpenAPI spec 的差异
 * 准确提取并对比
 */
const fs = require('fs');
const path = require('path');

// 读取 apiConstants.js 获取常量映射
function getApiConstants() {
  const constantsPath = path.join(__dirname, '../src/api/apiConstants.js');
  const content = fs.readFileSync(constantsPath, 'utf-8');
  
  const constants = {};
  
  // 更精确的解析
  const lines = content.split('\n');
  let currentModule = null;
  
  for (const line of lines) {
    // 匹配模块开始 如 USER: {
    const moduleMatch = /^\s*(\w+):\s*\{/.exec(line);
    if (moduleMatch) {
      currentModule = moduleMatch[1];
      constants[currentModule] = {};
      continue;
    }
    
    // 匹配键值对 如 LOGIN: '/user/login',
    if (currentModule) {
      const kvMatch = /^\s*(\w+):\s*['"]([^'"]+)['"]/.exec(line);
      if (kvMatch) {
        constants[currentModule][kvMatch[1]] = kvMatch[2];
      }
    }
    
    // 匹配模块结束
    if (/^\s*\},?\s*$/.test(line)) {
      currentModule = null;
    }
  }
  
  return constants;
}

// 读取前端 API 文件，提取所有接口
function extractFrontendAPIs(constants) {
  const apiDir = path.join(__dirname, '../src/api');
  const apiFiles = ['user.js', 'tea.js', 'shop.js', 'order.js', 'forum.js', 'message.js', 'upload.js'];
  
  const apis = [];
  
  apiFiles.forEach(file => {
    const filePath = path.join(apiDir, file);
    if (!fs.existsSync(filePath)) return;
    
    const content = fs.readFileSync(filePath, 'utf-8');
    
    // 提取函数名
    const funcPattern = /export\s+function\s+(\w+)/g;
    
    let funcMatch;
    const functions = [];
    while ((funcMatch = funcPattern.exec(content)) !== null) {
      functions.push({
        name: funcMatch[1],
        index: funcMatch.index
      });
    }
    
    // 为每个函数找到对应的 url 和 method
    functions.forEach((func, i) => {
      const nextFuncIndex = functions[i + 1]?.index || content.length;
      const funcContent = content.slice(func.index, nextFuncIndex);
      
      // 找 url - 更精确的匹配
      let url = null;
      let rawUrl = null;
      
      // 匹配各种 url 格式
      const urlPatterns = [
        /url:\s*['"]([^'"]+)['"]/,                    // url: '/xxx'
        /url:\s*`([^`]+)`/,                           // url: `/xxx`
        /url:\s*(API\.\w+\.\w+)\s*\+\s*['"]([^'"]*)['"]/,  // url: API.X.Y + '/z'
        /url:\s*(API\.\w+\.\w+)\s*\+\s*`([^`]*)`/,   // url: API.X.Y + `/z`
        /url:\s*(API\.\w+\.\w+)\s*\+\s*(\w+)/,       // url: API.X.Y + id
        /url:\s*(API\.\w+\.\w+)/,                     // url: API.X.Y
      ];
      
      for (const pattern of urlPatterns) {
        const match = pattern.exec(funcContent);
        if (match) {
          if (match[1] && match[1].startsWith('API.')) {
            // 解析 API 常量
            const apiMatch = /API\.(\w+)\.(\w+)/.exec(match[1]);
            if (apiMatch && constants[apiMatch[1]] && constants[apiMatch[1]][apiMatch[2]]) {
              const base = constants[apiMatch[1]][apiMatch[2]];
              const suffix = match[2] || '';
              url = base + suffix.replace(/\$\{[^}]+\}/g, '{id}');
              rawUrl = match[0];
            }
          } else {
            url = match[1];
            rawUrl = match[0];
          }
          break;
        }
      }
      
      // 找 method
      const methodMatch = /method:\s*['"](\w+)['"]/.exec(funcContent);
      const method = methodMatch ? methodMatch[1].toUpperCase() : 'GET';
      
      if (url) {
        // 标准化路径参数
        url = url
          .replace(/\$\{[^}]+\}/g, '{id}')
          .replace(/\{[^}]+\}/g, '{id}')
          .replace(/\/\{id\}\/\{id\}/g, '/{id}');
        
        apis.push({
          file: file.replace('.js', ''),
          function: func.name,
          url,
          method
        });
      }
    });
  });
  
  return apis;
}

// 读取 apiConstants.js 获取常量映射
function getApiConstants() {
  const constantsPath = path.join(__dirname, '../src/api/apiConstants.js');
  const content = fs.readFileSync(constantsPath, 'utf-8');
  
  const constants = {};
  
  // 匹配 MODULE: { KEY: 'value' }
  const modulePattern = /(\w+):\s*\{([^}]+)\}/g;
  let moduleMatch;
  
  while ((moduleMatch = modulePattern.exec(content)) !== null) {
    const moduleName = moduleMatch[1];
    const moduleContent = moduleMatch[2];
    
    constants[moduleName] = {};
    
    const keyValuePattern = /(\w+):\s*['"`]([^'"`]+)['"`]/g;
    let kvMatch;
    while ((kvMatch = keyValuePattern.exec(moduleContent)) !== null) {
      constants[moduleName][kvMatch[1]] = kvMatch[2];
    }
  }
  
  return constants;
}

// 读取 OpenAPI spec，提取所有接口
function extractOpenAPIEndpoints() {
  const openapiPath = path.join(__dirname, '../../openapi.yaml');
  const content = fs.readFileSync(openapiPath, 'utf-8');
  
  const endpoints = [];
  const lines = content.split('\n');
  
  let currentPath = null;
  let inPaths = false;
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    
    if (line.startsWith('paths:')) {
      inPaths = true;
      continue;
    }
    
    if (!inPaths) continue;
    
    // 匹配路径定义 (2个空格开头)
    const pathMatch = /^  (\/[^:]+):/.exec(line);
    if (pathMatch) {
      currentPath = pathMatch[1];
      continue;
    }
    
    // 匹配 HTTP 方法 (4个空格开头)
    const methodMatch = /^    (get|post|put|delete|patch):/.exec(line);
    if (methodMatch && currentPath) {
      endpoints.push({
        path: currentPath,
        method: methodMatch[1].toUpperCase()
      });
    }
    
    // 检测是否离开 paths 部分
    if (/^[a-z]/.test(line) && !line.startsWith(' ')) {
      break;
    }
  }
  
  return endpoints;
}

// 解析前端 URL（将 API.XXX.YYY 替换为实际路径）
function resolveUrl(url, constants) {
  if (url.startsWith('API.')) {
    const match = /API\.(\w+)\.(\w+)/.exec(url);
    if (match) {
      const module = match[1];
      const key = match[2];
      if (constants[module] && constants[module][key]) {
        return constants[module][key];
      }
    }
    return url; // 无法解析
  }
  return url;
}

// 主函数
function main() {
  console.log('='.repeat(60));
  console.log('前端 API 与 OpenAPI Spec 对比分析');
  console.log('='.repeat(60));
  
  const constants = getApiConstants();
  const frontendAPIs = extractFrontendAPIs();
  const openapiEndpoints = extractOpenAPIEndpoints();
  
  console.log(`\n前端 API 函数数量: ${frontendAPIs.length}`);
  console.log(`OpenAPI 接口数量: ${openapiEndpoints.length}`);
  
  // 解析前端 URL
  const resolvedFrontendAPIs = frontendAPIs.map(api => ({
    ...api,
    resolvedUrl: resolveUrl(api.url, constants)
  }));
  
  // 标准化路径（移除路径参数的具体值）
  const normalizePath = (p) => {
    return p
      .replace(/\$\{[^}]+\}/g, '{id}')  // ${xxx} -> {id}
      .replace(/\{[^}]+\}/g, '{id}')     // {xxx} -> {id}
      .replace(/\/\d+/g, '/{id}')        // /123 -> /{id}
      .replace(/\/{id}\/{id}/g, '/{id}') // 合并多个 {id}
      .replace(/\/$/, '');               // 移除末尾斜杠
  };
  
  // 创建 OpenAPI 接口集合
  const openapiSet = new Set(
    openapiEndpoints.map(e => `${e.method} ${normalizePath(e.path)}`)
  );
  
  // 找出前端有但 OpenAPI 没有的
  const missingInOpenAPI = [];
  resolvedFrontendAPIs.forEach(api => {
    if (api.resolvedUrl.startsWith('API.')) return; // 跳过无法解析的
    
    const normalized = normalizePath(api.resolvedUrl);
    const key = `${api.method} ${normalized}`;
    
    if (!openapiSet.has(key)) {
      missingInOpenAPI.push({
        ...api,
        normalized
      });
    }
  });
  
  // 创建前端接口集合
  const frontendSet = new Set(
    resolvedFrontendAPIs
      .filter(api => !api.resolvedUrl.startsWith('API.'))
      .map(api => `${api.method} ${normalizePath(api.resolvedUrl)}`)
  );
  
  // 找出 OpenAPI 有但前端没有的
  const missingInFrontend = [];
  openapiEndpoints.forEach(endpoint => {
    const normalized = normalizePath(endpoint.path);
    const key = `${endpoint.method} ${normalized}`;
    
    if (!frontendSet.has(key)) {
      missingInFrontend.push({
        ...endpoint,
        normalized
      });
    }
  });
  
  // 输出结果
  console.log('\n' + '='.repeat(60));
  console.log('【前端有，OpenAPI 缺少】');
  console.log('='.repeat(60));
  
  if (missingInOpenAPI.length === 0) {
    console.log('无');
  } else {
    missingInOpenAPI.forEach(api => {
      console.log(`${api.method.padEnd(6)} ${api.resolvedUrl}`);
      console.log(`       函数: ${api.file}.${api.function}`);
    });
  }
  
  console.log('\n' + '='.repeat(60));
  console.log('【OpenAPI 有，前端缺少】');
  console.log('='.repeat(60));
  
  if (missingInFrontend.length === 0) {
    console.log('无');
  } else {
    missingInFrontend.forEach(endpoint => {
      console.log(`${endpoint.method.padEnd(6)} ${endpoint.path}`);
    });
  }
  
  // 统计
  console.log('\n' + '='.repeat(60));
  console.log('统计');
  console.log('='.repeat(60));
  console.log(`前端缺少: ${missingInFrontend.length} 个接口`);
  console.log(`OpenAPI 缺少: ${missingInOpenAPI.length} 个接口`);
}

main();
