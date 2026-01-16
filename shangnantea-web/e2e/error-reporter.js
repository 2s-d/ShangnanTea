/**
 * 自定义 Playwright Reporter - 错误汇总报告生成器
 * 
 * 这个 reporter 会在测试结束后自动生成 error-summary.json
 * 即使 afterAll 钩子没有执行，也能确保报告被生成
 */
const fs = require('fs');
const path = require('path');

class ErrorSummaryReporter {
  constructor(options = {}) {
    this.outputFile = options.outputFile || path.join(__dirname, '..', 'e2e-report', 'error-summary.json');
    this.errors = [];
  }

  onBegin(config, suite) {
    console.log(`\n🚀 开始运行 ${suite.allTests().length} 个测试...\n`);
  }

  onTestEnd(test, result) {
    // 从测试输出中提取错误信息
    if (result.status === 'failed' || result.errors.length > 0) {
      const testName = test.title;
      const testPath = test.location.file;
      
      result.errors.forEach(error => {
        this.errors.push({
          testName,
          testPath,
          message: error.message || error.value,
          stack: error.stack,
          timestamp: new Date().toISOString()
        });
      });
    }
    
    // 从控制台输出中提取错误（如果测试有附加输出）
    if (result.stdout && result.stdout.length > 0) {
      result.stdout.forEach(output => {
        if (output.includes('❌') || output.includes('[ERROR]')) {
          this.errors.push({
            testName: test.title,
            type: 'console',
            message: output,
            timestamp: new Date().toISOString()
          });
        }
      });
    }
  }

  onEnd(result) {
    console.log(`\n✅ 测试完成！`);
    console.log(`   总测试数: ${result.allTests().length}`);
    console.log(`   通过: ${result.allTests().filter(t => t.outcome() === 'expected').length}`);
    console.log(`   失败: ${result.allTests().filter(t => t.outcome() === 'unexpected').length}`);
    console.log(`   跳过: ${result.allTests().filter(t => t.outcome() === 'skipped').length}`);
    
    // 生成错误汇总报告
    const summary = {
      totalTests: result.allTests().length,
      passed: result.allTests().filter(t => t.outcome() === 'expected').length,
      failed: result.allTests().filter(t => t.outcome() === 'unexpected').length,
      skipped: result.allTests().filter(t => t.outcome() === 'skipped').length,
      totalErrors: this.errors.length,
      errors: this.errors,
      generatedAt: new Date().toISOString()
    };
    
    // 确保输出目录存在
    const outputDir = path.dirname(this.outputFile);
    if (!fs.existsSync(outputDir)) {
      fs.mkdirSync(outputDir, { recursive: true });
    }
    
    // 写入文件
    try {
      fs.writeFileSync(this.outputFile, JSON.stringify(summary, null, 2), 'utf-8');
      console.log(`\n📄 错误汇总报告已保存: ${this.outputFile}\n`);
    } catch (error) {
      console.error(`\n❌ 保存报告失败: ${error.message}\n`);
    }
  }
}

module.exports = ErrorSummaryReporter;
