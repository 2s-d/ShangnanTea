/**
 * 消息使用检查工具
 * 
 * 用于扫描项目中直接使用ElMessage的地方，帮助开发者找出需要替换为封装message的位置
 * 
 * 使用方法：
 * 1. 确保已安装依赖: npm install glob chalk
 * 2. 运行: node tools/check-message-usage.js
 */

const fs = require('fs');
const path = require('path');
const glob = require('glob');
const chalk = require('chalk');

// 需要检查的文件模式
const FILE_PATTERNS = [
  'src/**/*.js',
  'src/**/*.vue',
  'src/**/*.ts',
  'src/**/*.jsx',
  'src/**/*.tsx'
];

// 不需要检查的目录
const EXCLUDE_DIRS = [
  'node_modules',
  'dist',
  'build',
  'public'
];

// 直接使用ElMessage的正则表达式
const DIRECT_USAGE_PATTERNS = [
  /ElMessage\.(success|error|warning|info)\(/g,
  /import[\s\n]+{[\s\n]*ElMessage[\s\n]*}[\s\n]+from[\s\n]+['"]element-plus['"]/g
];

// 计数器
let totalFiles = 0;
let affectedFiles = 0;
let totalIssues = 0;

// 结果集
const issues = [];

/**
 * 检查文件
 * @param {string} filePath 文件路径
 */
function checkFile(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8');
  let fileHasIssue = false;
  const fileIssues = [];
  
  // 检查每个模式
  DIRECT_USAGE_PATTERNS.forEach(pattern => {
    let match;
    const regex = new RegExp(pattern);
    
    while ((match = regex.exec(content)) !== null) {
      // 获取行号
      const lineNumber = getLineNumber(content, match.index);
      
      fileIssues.push({
        type: pattern.toString().includes('import') ? '导入' : '调用',
        line: lineNumber,
        match: match[0]
      });
      
      fileHasIssue = true;
      totalIssues++;
    }
  });
  
  if (fileHasIssue) {
    issues.push({
      file: filePath,
      issues: fileIssues
    });
    affectedFiles++;
  }
  
  totalFiles++;
}

/**
 * 获取字符位置所在的行号
 * @param {string} content 文件内容
 * @param {number} index 字符位置
 * @returns {number} 行号（从1开始）
 */
function getLineNumber(content, index) {
  const lines = content.substr(0, index).split('\n');
  return lines.length;
}

/**
 * 打印结果
 */
function printResults() {
  console.log(chalk.yellow('\n========== 消息使用检查结果 ==========\n'));
  
  if (issues.length === 0) {
    console.log(chalk.green('✓ 很棒！未发现直接使用ElMessage的情况。'));
  } else {
    console.log(chalk.yellow(`! 发现 ${totalIssues} 个不规范的消息使用，分布在 ${affectedFiles} 个文件中。\n`));
    
    issues.forEach(file => {
      console.log(chalk.cyan(`文件: ${file.file}`));
      
      file.issues.forEach(issue => {
        console.log(`  第 ${issue.line} 行: ${chalk.red(issue.type)} ${issue.match}`);
      });
      
      console.log('');
    });
    
    console.log(chalk.yellow('请将直接使用的ElMessage替换为封装的message组件:'));
    console.log(chalk.green("import { message } from '@/components/common'"));
    console.log(chalk.green("message.success('成功消息')"));
    console.log(chalk.green("message.error('错误消息')"));
    console.log('\n或者使用更强大的messageHelper:');
    console.log(chalk.green("import { handleAsyncOperation } from '@/utils/messageHelper'"));
    
    console.log(chalk.yellow('\n详细使用说明请参考: docs/message-system-guide.md'));
  }
  
  console.log(chalk.yellow('\n======================================\n'));
}

/**
 * 主函数
 */
function main() {
  console.log(chalk.cyan('正在检查项目中的消息使用情况...'));
  
  // 获取所有需要检查的文件
  const files = [];
  
  FILE_PATTERNS.forEach(pattern => {
    const matches = glob.sync(pattern, {
      ignore: EXCLUDE_DIRS.map(dir => `**/${dir}/**`)
    });
    
    files.push(...matches);
  });
  
  // 去重
  const uniqueFiles = [...new Set(files)];
  
  // 检查每个文件
  uniqueFiles.forEach(file => {
    checkFile(file);
  });
  
  // 打印结果
  printResults();
}

// 运行主函数
main(); 