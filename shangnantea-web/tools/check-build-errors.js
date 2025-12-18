#!/usr/bin/env node

/**
 * 编译错误检查脚本
 * 用于检查项目编译错误，并格式化输出错误信息
 * 
 * 用法：
 *   node tools/check-build-errors.js
 *   node tools/check-build-errors.js --fix  # 尝试自动修复（如果可能）
 */

const { execSync, spawn } = require('child_process')
const path = require('path')
const fs = require('fs')

// 颜色输出
const colors = {
  reset: '\x1b[0m',
  red: '\x1b[31m',
  yellow: '\x1b[33m',
  green: '\x1b[34m',
  cyan: '\x1b[36m',
  gray: '\x1b[90m'
}

function log(message, color = 'reset') {
  console.log(`${colors[color]}${message}${colors.reset}`)
}

// 解析错误信息
function parseError(errorOutput) {
  const errors = []
  const lines = errorOutput.split('\n')
  
  let currentError = null
  let currentFile = null
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()
    
    // 匹配文件路径错误 (例如: ERROR in ./src/components/TeaCard.vue)
    const fileMatch = line.match(/ERROR\s+in\s+(.+)/i)
    if (fileMatch) {
      if (currentError) {
        errors.push(currentError)
      }
      currentFile = fileMatch[1]
      currentError = {
        file: currentFile,
        messages: [],
        raw: [line]
      }
      continue
    }
    
    // 匹配详细错误信息 (例如: Module not found: Can't resolve '@/api/tea')
    if (currentError) {
      if (line && !line.startsWith('webpack compiled') && !line.startsWith('webpack')) {
        currentError.raw.push(line)
        
        // 提取错误类型和消息
        if (line.includes('Module not found') || line.includes("Can't resolve")) {
          const match = line.match(/Can't resolve ['"]([^'"]+)['"]/i) || line.match(/Module not found: (.+)/i)
          if (match) {
            currentError.messages.push({
              type: 'module_not_found',
              message: `模块未找到: ${match[1]}`,
              detail: line
            })
          }
        } else if (line.includes('Parsing error') || line.includes('SyntaxError')) {
          currentError.messages.push({
            type: 'syntax_error',
            message: '语法错误',
            detail: line
          })
        } else if (line.match(/\d+:\d+/)) {
          // 匹配行号:列号格式 (例如: 12:5)
          const match = line.match(/(\d+):(\d+)/)
          if (match) {
            currentError.messages.push({
              type: 'position',
              line: parseInt(match[1]),
              column: parseInt(match[2]),
              detail: line
            })
          }
        } else if (line && !line.startsWith('@') && !line.startsWith('npm')) {
          // 其他错误消息
          if (!currentError.messages.some(m => m.detail === line)) {
            currentError.messages.push({
              type: 'other',
              message: line,
              detail: line
            })
          }
        }
      }
    }
  }
  
  if (currentError) {
    errors.push(currentError)
  }
  
  return errors
}

// 格式化输出错误
function formatErrors(errors) {
  if (errors.length === 0) {
    log('\n✅ 未发现编译错误！', 'green')
    return
  }
  
  log(`\n❌ 发现 ${errors.length} 个编译错误：\n`, 'red')
  
  errors.forEach((error, index) => {
    log(`\n[错误 ${index + 1}] ${error.file}`, 'red')
    log('─'.repeat(60), 'gray')
    
    if (error.messages.length === 0) {
      log('原始错误信息：', 'yellow')
      error.raw.forEach(line => log(`  ${line}`, 'gray'))
    } else {
      error.messages.forEach(msg => {
        if (msg.type === 'module_not_found') {
          log(`  🔍 ${msg.message}`, 'yellow')
          log(`     ${msg.detail}`, 'gray')
        } else if (msg.type === 'syntax_error') {
          log(`  ⚠️  ${msg.message}`, 'yellow')
          log(`     ${msg.detail}`, 'gray')
        } else if (msg.type === 'position') {
          log(`  📍 位置: 第 ${msg.line} 行, 第 ${msg.column} 列`, 'cyan')
          log(`     ${msg.detail}`, 'gray')
        } else {
          log(`  ℹ️  ${msg.message}`, 'gray')
        }
      })
    }
  })
  
  log('\n' + '═'.repeat(60), 'gray')
  log(`总计: ${errors.length} 个错误`, 'red')
}

// 主函数
function main() {
  const args = process.argv.slice(2)
  const shouldFix = args.includes('--fix')
  const quickCheck = args.includes('--quick') || args.includes('-q')
  
  log('🔍 开始检查编译错误...\n', 'cyan')
  
  const projectRoot = path.resolve(__dirname, '..')
  const originalCwd = process.cwd()
  
  try {
    process.chdir(projectRoot)
    
    // 快速检查模式：先运行 ESLint
    if (quickCheck) {
      log('⚡ 快速检查模式：先运行 ESLint...', 'cyan')
      try {
        const lintOutput = execSync(
          'npm run lint 2>&1',
          { 
            cwd: projectRoot,
            encoding: 'utf8',
            maxBuffer: 10 * 1024 * 1024
          }
        )
        log('✅ ESLint 检查通过', 'green')
      } catch (lintError) {
        const lintOutput = (lintError.stdout || '') + (lintError.stderr || '')
        log('❌ ESLint 发现错误：', 'red')
        log('─'.repeat(60), 'gray')
        const errorLines = lintOutput.split('\n').filter(line => 
          line.trim() && (
            line.includes('error') || 
            line.includes('Error') ||
            line.includes('✖')
          )
        )
        errorLines.slice(0, 50).forEach(line => log(`  ${line}`, 'gray'))
        log('\n💡 提示：运行完整编译检查请使用: npm run check:build', 'yellow')
        process.exit(1)
      }
    }
    
    // 运行编译检查（使用 build 命令）
    log('正在运行编译检查...', 'cyan')
    log('（这可能需要一些时间，请耐心等待...）', 'gray')
    
    try {
      // 使用 vue-cli-service build 进行编译检查
      const output = execSync(
        'npm run build 2>&1',
        { 
          cwd: projectRoot,
          encoding: 'utf8',
          maxBuffer: 10 * 1024 * 1024 // 10MB buffer
        }
      )
      
      // 检查是否有编译错误（区分 ERROR 和 WARNING）
      // 匹配格式：✖ 709 problems (0 errors, 709 warnings)
      const errorMatch = output.match(/(\d+)\s+errors?/i)
      const warningMatch = output.match(/(\d+)\s+warnings?/i)
      const hasErrors = output.includes('ERROR') || 
                       (errorMatch && parseInt(errorMatch[1]) > 0) ||
                       output.includes('Module not found') ||
                       output.includes("Can't resolve") ||
                       output.includes('SyntaxError') ||
                       output.includes('Parsing error')
      
      const hasWarnings = warningMatch && parseInt(warningMatch[1]) > 0
      
      if (hasErrors) {
        const errors = parseError(output)
        formatErrors(errors)
        
        // 输出原始错误信息（用于调试）
        log('\n📋 原始错误输出：', 'yellow')
        log('─'.repeat(60), 'gray')
        const errorLines = output.split('\n').filter(line => 
          line.includes('ERROR') || 
          line.includes('error') || 
          line.includes('Module not found') ||
          line.includes("Can't resolve") ||
          line.includes('SyntaxError') ||
          line.includes('Parsing error')
        )
        errorLines.slice(0, 50).forEach(line => log(`  ${line}`, 'gray'))
        
        process.exit(1)
      } else if (hasWarnings) {
        // 有警告但没有错误
        const warningMatch = output.match(/(\d+)\s+warnings?/i)
        const warningCount = warningMatch ? parseInt(warningMatch[1]) : 0
        
        log('\n⚠️  编译通过，但有警告：', 'yellow')
        log(`   发现 ${warningCount} 个警告`, 'yellow')
        log('\n💡 提示：警告不会阻止编译，但建议修复', 'gray')
        
        // 显示部分警告信息
        const warningLines = output.split('\n').filter(line => 
          line.includes('warning') || 
          line.includes('Warning') ||
          line.includes('WARN')
        )
        if (warningLines.length > 0) {
          log('\n📋 部分警告信息：', 'yellow')
          log('─'.repeat(60), 'gray')
          warningLines.slice(0, 20).forEach(line => log(`  ${line}`, 'gray'))
        }
        
        process.exit(0)
      } else {
        log('\n✅ 编译检查通过，未发现错误和警告！', 'green')
        process.exit(0)
      }
    } catch (error) {
      // execSync 抛出错误通常意味着编译失败
      const errorOutput = error.stdout || error.stderr || error.message
      const errors = parseError(errorOutput)
      
      if (errors.length > 0) {
        formatErrors(errors)
      } else {
        // 如果解析失败，输出原始错误
        log('\n❌ 编译失败：', 'red')
        log('─'.repeat(60), 'gray')
        log(errorOutput, 'gray')
      }
      
      // 输出完整错误信息（用于调试）
      log('\n📋 完整错误输出：', 'yellow')
      log('─'.repeat(60), 'gray')
      const fullOutput = (error.stdout || '') + (error.stderr || '') + (error.message || '')
      const errorLines = fullOutput.split('\n').filter(line => 
        line.trim() && (
          line.includes('ERROR') || 
          line.includes('error') || 
          line.includes('Module not found') ||
          line.includes("Can't resolve") ||
          line.includes('SyntaxError') ||
          line.includes('Parsing error')
        )
      )
      errorLines.slice(0, 100).forEach(line => log(`  ${line}`, 'gray'))
      
      process.exit(1)
    }
  } catch (error) {
    log(`\n❌ 脚本执行出错: ${error.message}`, 'red')
    console.error(error)
    process.exit(1)
  } finally {
    process.chdir(originalCwd)
  }
}

// 运行主函数
if (require.main === module) {
  main()
}

module.exports = { parseError, formatErrors }

