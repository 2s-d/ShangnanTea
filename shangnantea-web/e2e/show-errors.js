/**
 * 显示 E2E 测试错误汇总
 * 
 * 用法: node e2e/show-errors.js
 */
const fs = require('fs');
const path = require('path');

const reportPath = path.join(__dirname, '..', 'e2e-report', 'error-summary.json');

if (!fs.existsSync(reportPath)) {
  console.log('❌ 错误汇总文件不存在，请先运行 E2E 测试');
  console.log(`   期望路径: ${reportPath}`);
  process.exit(1);
}

try {
  const summary = JSON.parse(fs.readFileSync(reportPath, 'utf-8'));
  
  console.log('\n' + '='.repeat(80));
  console.log('📊 E2E 测试错误汇总报告');
  console.log('='.repeat(80));
  console.log(`总测试页面数: ${summary.totalPages}`);
  console.log(`有错误的页面: ${summary.pagesWithErrors}`);
  console.log(`总错误数: ${summary.totalErrors}`);
  
  console.log('\n错误类型分布:');
  console.log(`  - 控制台错误: ${summary.errorsByType.console}`);
  console.log(`  - 网络请求错误: ${summary.errorsByType.network}`);
  console.log(`  - 资源加载错误: ${summary.errorsByType.resource}`);
  console.log(`  - 运行时错误: ${summary.errorsByType.runtime}`);
  
  if (summary.pagesWithErrors > 0) {
    console.log('\n❌ 有错误的页面详情:');
    Object.entries(summary.errorsByPage).forEach(([pageName, errors]) => {
      console.log(`\n  ${pageName} (${errors.length} 个错误):`);
      errors.forEach((err, i) => {
        console.log(`    ${i + 1}. [${err.type}] ${err.message}`);
        if (err.stack) {
          // 只显示堆栈的前3行
          const stackLines = err.stack.split('\n').slice(0, 3);
          stackLines.forEach(line => {
            console.log(`       ${line.trim()}`);
          });
        }
      });
    });
  } else {
    console.log('\n✅ 所有页面都没有错误！');
  }
  
  console.log('\n' + '='.repeat(80));
  console.log(`📄 完整报告: ${reportPath}`);
  console.log('='.repeat(80) + '\n');
  
} catch (error) {
  console.error('❌ 读取错误汇总文件失败:', error.message);
  process.exit(1);
}
