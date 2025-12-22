/**
 * 消息提取脚本
 * 从6个消息模块中提取所有消息常量，生成分类清单
 * 
 * 运行方式: node extract-messages.js
 */

const fs = require('fs');
const path = require('path');

// 消息模块文件
const messageFiles = [
  'src/utils/userMessages.js',
  'src/utils/teaMessages.js',
  'src/utils/orderMessages.js',
  'src/utils/forumMessages.js',
  'src/utils/shopMessages.js',
  'src/utils/messageMessages.js'
];

// 提取结果
const results = [];
let globalIndex = 0;

// 解析消息常量
function extractMessages(filePath) {
  const content = fs.readFileSync(filePath, 'utf8');
  const fileName = path.basename(filePath, '.js');
  
  // 匹配 MESSAGES 对象中的三层结构
  // 匹配 API: {}, BUSINESS: {}, UI: {} 块
  const layerRegex = /(API|BUSINESS|UI):\s*\{([^}]+)\}/g;
  
  let match;
  while ((match = layerRegex.exec(content)) !== null) {
    const layer = match[1];
    const block = match[2];
    
    // 匹配每个消息常量 KEY: 'value' 或 KEY: "value"
    const msgRegex = /(\w+):\s*['"`]([^'"`]+)['"`]/g;
    let msgMatch;
    
    while ((msgMatch = msgRegex.exec(block)) !== null) {
      const key = msgMatch[1];
      const value = msgMatch[2];
      
      // 分析消息类型
      const suggestedType = analyzeMessageType(value, layer, key);
      
      results.push({
        index: globalIndex++,
        file: fileName,
        layer: layer,
        key: key,
        value: value,
        currentType: layer,
        suggestedType: suggestedType,
        newType: '' // 留空给用户填写
      });
    }
  }
}

// 分析消息类型，给出建议
function analyzeMessageType(value, layer, key) {
  const lowerValue = value.toLowerCase();
  const lowerKey = key.toLowerCase();
  
  // 确认类提示
  if (lowerValue.includes('确定') || lowerValue.includes('确认') || 
      lowerKey.includes('confirm') || lowerValue.includes('吗？')) {
    return 'CONFIRM';
  }
  
  // 表单验证类
  if (lowerValue.includes('请输入') || lowerValue.includes('请选择') ||
      lowerValue.includes('不能为空') || lowerValue.includes('格式') ||
      lowerValue.includes('必须') || lowerValue.includes('不正确') ||
      lowerKey.includes('required') || lowerKey.includes('invalid') ||
      lowerValue.includes('请先') || lowerValue.includes('不能超过')) {
    return 'VALIDATE';
  }
  
  // 加载状态类（建议移除或改用loading组件）
  if (lowerValue.includes('正在') || lowerValue.includes('加载中') ||
      lowerKey.includes('loading')) {
    return 'LOADING';
  }
  
  // 操作结果类
  if (lowerValue.includes('成功') || lowerValue.includes('失败') ||
      lowerValue.includes('已') || lowerKey.includes('success') ||
      lowerKey.includes('failed') || lowerKey.includes('error')) {
    return 'RESULT';
  }
  
  return 'RESULT'; // 默认归为结果类
}

// 生成清单
function generateReport() {
  let report = `# 消息分类清单

生成时间: ${new Date().toLocaleString()}
总消息数: ${results.length}

## 新分类说明
- RESULT: 操作结果类 - 操作完成后的反馈（成功/失败）
- VALIDATE: 表单验证类 - 输入校验提示
- CONFIRM: 确认提示类 - 需要用户确认的操作
- LOADING: 加载状态类 - 建议移除，改用loading组件

## 消息清单

| 索引 | 模块 | 原分类 | 键名 | 消息内容 | 建议分类 | 新分类 |
|------|------|--------|------|----------|----------|--------|
`;

  for (const item of results) {
    report += `| ${item.index} | ${item.file} | ${item.layer} | ${item.key} | ${item.value} | ${item.suggestedType} | |\n`;
  }

  // 按建议分类统计
  const stats = {};
  for (const item of results) {
    stats[item.suggestedType] = (stats[item.suggestedType] || 0) + 1;
  }

  report += `\n## 统计\n`;
  for (const [type, count] of Object.entries(stats)) {
    report += `- ${type}: ${count}条\n`;
  }

  return report;
}

// 生成JSON数据（用于后续脚本处理）
function generateJSON() {
  return JSON.stringify(results, null, 2);
}

// 主函数
function main() {
  console.log('开始提取消息...\n');
  
  for (const file of messageFiles) {
    const fullPath = path.join(__dirname, file);
    if (fs.existsSync(fullPath)) {
      console.log(`处理: ${file}`);
      extractMessages(fullPath);
    } else {
      console.log(`跳过(不存在): ${file}`);
    }
  }
  
  console.log(`\n共提取 ${results.length} 条消息\n`);
  
  // 生成报告
  const report = generateReport();
  fs.writeFileSync(path.join(__dirname, 'message-classification.md'), report, 'utf8');
  console.log('已生成: message-classification.md');
  
  // 生成JSON
  const json = generateJSON();
  fs.writeFileSync(path.join(__dirname, 'message-data.json'), json, 'utf8');
  console.log('已生成: message-data.json');
}

main();
