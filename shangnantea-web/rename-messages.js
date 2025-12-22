/**
 * 消息系统重命名脚本
 * 将 api/business/ui 改为 result/confirm/form
 * 
 * 运行方式: node rename-messages.js
 */

const fs = require('fs');
const path = require('path');

// 需要处理的目录
const srcDir = path.join(__dirname, 'src');

// 替换规则
const replacements = [
  // 外部调用替换 (如 userMessages.business.xxx)
  { from: /\.api\./g, to: '.result.' },
  { from: /\.business\./g, to: '.confirm.' },
  { from: /\.ui\./g, to: '.form.' },
  
  // 常量定义替换 (如 API: {, BUSINESS: {, UI: {)
  { from: /API:\s*\{/g, to: 'RESULT: {' },
  { from: /BUSINESS:\s*\{/g, to: 'CONFIRM: {' },
  { from: /UI:\s*\{/g, to: 'FORM: {' },
  
  // 常量引用替换 (如 MESSAGES.API., MESSAGES.BUSINESS., MESSAGES.UI.)
  { from: /MESSAGES\.API\./g, to: 'MESSAGES.RESULT.' },
  { from: /MESSAGES\.BUSINESS\./g, to: 'MESSAGES.CONFIRM.' },
  { from: /MESSAGES\.UI\./g, to: 'MESSAGES.FORM.' },
  
  // 导出对象中的键名替换
  { from: /api:\s*(\w+ApiMessages|apiMessages)/g, to: 'result: $1' },
  { from: /business:\s*(\w+BusinessMessages|businessMessages)/g, to: 'confirm: $1' },
  { from: /ui:\s*(\w+UIMessages|uiMessages)/g, to: 'form: $1' },
  
  // messageManager 中的导出
  { from: /apiMessage,/g, to: 'resultMessage,' },
  { from: /vuexMessage,/g, to: 'confirmMessage,' },
  { from: /uiMessage,/g, to: 'formMessage,' },
  
  // messageManager 中的定义
  { from: /export const apiMessage/g, to: 'export const resultMessage' },
  { from: /export const vuexMessage/g, to: 'export const confirmMessage' },
  { from: /export const uiMessage/g, to: 'export const formMessage' },
  
  // import 语句
  { from: /import \{ apiMessage, vuexMessage, uiMessage \}/g, to: 'import { resultMessage, confirmMessage, formMessage }' },
  
  // 内部使用
  { from: /apiMessage\./g, to: 'resultMessage.' },
  { from: /vuexMessage\./g, to: 'confirmMessage.' },
  { from: /uiMessage\./g, to: 'formMessage.' },
  
  // messageManager 引用
  { from: /messageManager\.api\./g, to: 'messageManager.result.' },
  { from: /messageManager\.business\./g, to: 'messageManager.confirm.' },
  { from: /messageManager\.ui\./g, to: 'messageManager.form.' },
  { from: /messageManager\.apiMessage/g, to: 'messageManager.resultMessage' },
  { from: /messageManager\.vuexMessage/g, to: 'messageManager.confirmMessage' },
  { from: /messageManager\.uiMessage/g, to: 'messageManager.formMessage' },
];

// 需要处理的文件扩展名
const extensions = ['.js', '.vue', '.ts'];

// 递归获取所有文件
function getAllFiles(dir, files = []) {
  const items = fs.readdirSync(dir);
  for (const item of items) {
    const fullPath = path.join(dir, item);
    const stat = fs.statSync(fullPath);
    if (stat.isDirectory()) {
      // 跳过 node_modules
      if (item !== 'node_modules' && item !== 'dist') {
        getAllFiles(fullPath, files);
      }
    } else if (extensions.includes(path.extname(item))) {
      files.push(fullPath);
    }
  }
  return files;
}

// 处理单个文件
function processFile(filePath) {
  let content = fs.readFileSync(filePath, 'utf8');
  let modified = false;
  
  for (const { from, to } of replacements) {
    if (from.test(content)) {
      content = content.replace(from, to);
      modified = true;
    }
  }
  
  if (modified) {
    fs.writeFileSync(filePath, content, 'utf8');
    console.log('Modified:', path.relative(srcDir, filePath));
  }
}

// 主函数
function main() {
  console.log('开始重命名消息系统...\n');
  
  const files = getAllFiles(srcDir);
  console.log(`找到 ${files.length} 个文件\n`);
  
  let modifiedCount = 0;
  for (const file of files) {
    const before = fs.readFileSync(file, 'utf8');
    processFile(file);
    const after = fs.readFileSync(file, 'utf8');
    if (before !== after) modifiedCount++;
  }
  
  console.log(`\n完成! 修改了 ${modifiedCount} 个文件`);
}

main();
