/**
 * 旧消息系统检测脚本 v2.0
 * 
 * 功能：扫描 Vue 组件，检测需要迁移到 showByCode 的旧消息调用
 * 
 * 检测规则：
 * 1. 导入语句：import { xxxMessages } from '@/utils/xxxMessages'
 * 2. 调用语句：xxxSuccessMessages.showXxx() 或 xxxErrorMessages.showXxx()
 * 
 * 不检测（保留）：
 * - promptMessages（提示消息，不是 API 响应消息）
 * 
 * 用法：
 *   node tools/detect-old-messages.js              # 扫描整个 src 目录
 *   node tools/detect-old-messages.js [文件路径]   # 扫描指定文件
 *   node tools/detect-old-messages.js --summary    # 只显示摘要
 * 
 * 输出：
 *   - 控制台报告
 *   - tools/old-messages-report.json（详细 JSON 报告）
 */

const fs = require('fs')
const path = require('path')

// 检测模式
const PATTERNS = {
  // 旧的导入语句（需要替换）
  oldImports: [
    /import\s*\{[^}]*(?:Success|Error)Messages[^}]*\}\s*from\s*['"]@\/utils\/\w+Messages['"]/g,
    /import\s+\w+Messages\s+from\s*['"]@\/utils\/(?:common|user|tea|order|shop|forum|message)Messages['"]/g,
    /import\s*\{[^}]*\w+(?:Success|Error)Messages[^}]*\}\s*from\s*['"]@\/utils\/(?:common|user|tea|order|shop|forum|message)Messages['"]/g,
  ],
  
  // 旧的消息调用（需要替换）
  oldCalls: [
    /\w+SuccessMessages\.show\w+\([^)]*\)/g,
    /\w+ErrorMessages\.show\w+\([^)]*\)/g,
    /(?:common|user|tea|order|shop|forum|message)(?:Success|Error)Messages\.show\w+\([^)]*\)/g,
  ],
  
  // 排除的模式（promptMessages 保留）
  excludePatterns: [
    /promptMessages/i,
  ],
  
  // 已修改的新写法（用于检测已完成的文件）
  newPatterns: [
    /import\s*\{[^}]*showByCode[^}]*\}\s*from\s*['"]@\/utils\/apiMessages['"]/,
    /showByCode\s*\(\s*res\.code\s*\)/,
  ]
}

/**
 * 检测单个文件
 */
function detectFile(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  const results = {
    file: filePath,
    relativePath: path.relative(process.cwd(), filePath),
    imports: [],
    calls: [],
    total: 0,
    hasNewPattern: false,  // 是否已有新写法
    status: 'pending'      // pending | partial | done
  }
  
  // 检测是否已有新写法
  PATTERNS.newPatterns.forEach(pattern => {
    if (pattern.test(content)) {
      results.hasNewPattern = true
    }
  })
  
  lines.forEach((line, index) => {
    const lineNum = index + 1
    
    // 跳过注释行
    if (line.trim().startsWith('//') || line.trim().startsWith('*')) {
      return
    }
    
    // 检查是否应该排除（promptMessages）
    const shouldExclude = PATTERNS.excludePatterns.some(pattern => pattern.test(line))
    if (shouldExclude) {
      return
    }
    
    // 检测旧的导入语句
    PATTERNS.oldImports.forEach(pattern => {
      const matches = line.match(pattern)
      if (matches) {
        matches.forEach(match => {
          results.imports.push({
            line: lineNum,
            code: match.trim(),
            fullLine: line.trim()
          })
        })
      }
    })
    
    // 检测旧的消息调用
    PATTERNS.oldCalls.forEach(pattern => {
      // 重置正则的 lastIndex
      pattern.lastIndex = 0
      let match
      while ((match = pattern.exec(line)) !== null) {
        results.calls.push({
          line: lineNum,
          code: match[0],
          fullLine: line.trim()
        })
      }
    })
  })
  
  results.total = results.imports.length + results.calls.length
  
  // 判断状态
  if (results.total === 0) {
    results.status = 'done'
  } else if (results.hasNewPattern) {
    results.status = 'partial'  // 部分完成（有新写法但还有旧代码）
  } else {
    results.status = 'pending'
  }
  
  return results
}

/**
 * 递归扫描目录
 */
function scanDirectory(dirPath, filePattern = /\.(vue|js)$/) {
  const allResults = []
  
  function scan(dir) {
    const items = fs.readdirSync(dir)
    
    items.forEach(item => {
      const fullPath = path.join(dir, item)
      const stat = fs.statSync(fullPath)
      
      if (stat.isDirectory()) {
        // 跳过 node_modules 和隐藏目录
        if (!item.startsWith('.') && item !== 'node_modules') {
          scan(fullPath)
        }
      } else if (filePattern.test(item)) {
        const result = detectFile(fullPath)
        if (result.total > 0) {
          allResults.push(result)
        }
      }
    })
  }
  
  scan(dirPath)
  return allResults
}

/**
 * 格式化输出结果
 */
function formatResults(results, verbose = false, summaryOnly = false) {
  let output = []
  let totalFiles = results.length
  let totalIssues = 0
  let pendingFiles = results.filter(r => r.status === 'pending').length
  let partialFiles = results.filter(r => r.status === 'partial').length
  let doneFiles = results.filter(r => r.status === 'done').length
  
  output.push('=' .repeat(60))
  output.push('旧消息系统检测报告')
  output.push('=' .repeat(60))
  output.push('')
  
  if (!summaryOnly) {
    // 按状态分组显示
    const statusOrder = ['pending', 'partial']
    
    statusOrder.forEach(status => {
      const filesWithStatus = results.filter(r => r.status === status)
      if (filesWithStatus.length === 0) return
      
      const statusLabel = {
        pending: '⏳ 待修改',
        partial: '🔄 部分完成'
      }[status]
      
      output.push(`\n${statusLabel} (${filesWithStatus.length} 个文件)`)
      output.push('-'.repeat(40))
      
      filesWithStatus.forEach(result => {
        totalIssues += result.total
        
        output.push(`\n📁 ${result.relativePath}`)
        output.push(`   发现 ${result.total} 处需要修改`)
        
        if (verbose) {
          if (result.imports.length > 0) {
            output.push('   📦 旧导入语句:')
            result.imports.forEach(item => {
              output.push(`      行 ${item.line}: ${item.code.substring(0, 60)}${item.code.length > 60 ? '...' : ''}`)
            })
          }
          
          if (result.calls.length > 0) {
            output.push('   📞 旧消息调用:')
            result.calls.forEach(item => {
              output.push(`      行 ${item.line}: ${item.code}`)
            })
          }
        }
      })
    })
  } else {
    totalIssues = results.reduce((sum, r) => sum + r.total, 0)
  }
  
  output.push('')
  output.push('=' .repeat(60))
  output.push('📊 统计摘要')
  output.push('=' .repeat(60))
  output.push(`   扫描文件数: ${totalFiles}`)
  output.push(`   待修改文件: ${pendingFiles}`)
  output.push(`   部分完成:   ${partialFiles}`)
  output.push(`   已完成:     ${doneFiles}`)
  output.push(`   总问题数:   ${totalIssues}`)
  output.push('=' .repeat(60))
  
  return output.join('\n')
}

/**
 * 生成 JSON 报告
 */
function generateJsonReport(results) {
  return JSON.stringify({
    scanTime: new Date().toISOString(),
    summary: {
      totalFiles: results.length,
      totalIssues: results.reduce((sum, r) => sum + r.total, 0),
      totalImports: results.reduce((sum, r) => sum + r.imports.length, 0),
      totalCalls: results.reduce((sum, r) => sum + r.calls.length, 0)
    },
    files: results
  }, null, 2)
}

// 主程序
function main() {
  const args = process.argv.slice(2)
  const summaryOnly = args.includes('--summary')
  const filteredArgs = args.filter(a => !a.startsWith('--'))
  
  if (filteredArgs.length === 0) {
    // 扫描整个 src 目录
    console.log('扫描 src 目录...\n')
    const srcPath = path.join(__dirname, '..', 'src')
    const results = scanDirectory(srcPath)
    console.log(formatResults(results, !summaryOnly, summaryOnly))
    
    // 保存 JSON 报告
    const reportPath = path.join(__dirname, 'old-messages-report.json')
    fs.writeFileSync(reportPath, generateJsonReport(results))
    console.log(`\n详细报告已保存到: ${reportPath}`)
  } else {
    // 扫描指定文件
    const filePath = filteredArgs[0]
    if (!fs.existsSync(filePath)) {
      console.error(`文件不存在: ${filePath}`)
      process.exit(1)
    }
    
    const result = detectFile(filePath)
    console.log(formatResults([result], true, false))
  }
}

main()
