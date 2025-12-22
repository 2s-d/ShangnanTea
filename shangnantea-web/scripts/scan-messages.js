#!/usr/bin/env node

/**
 * 消息扫描脚本 - 无外部依赖版本
 * 
 * 功能：
 * 1. 扫描所有 message.xxx('文本') 调用
 * 2. 生成迁移清单
 * 3. 按模块分类统计
 */

const fs = require('fs')
const path = require('path')

// 递归获取所有文件
function getAllFiles(dir, extensions = ['.vue', '.js']) {
  const files = []
  
  function traverse(currentDir) {
    const items = fs.readdirSync(currentDir)
    
    for (const item of items) {
      const fullPath = path.join(currentDir, item)
      const stat = fs.statSync(fullPath)
      
      if (stat.isDirectory()) {
        // 跳过 node_modules 等目录
        if (!['node_modules', '.git', 'dist', 'build'].includes(item)) {
          traverse(fullPath)
        }
      } else if (extensions.some(ext => item.endsWith(ext))) {
        files.push(fullPath)
      }
    }
  }
  
  traverse(dir)
  return files
}

// 模块映射
const MODULE_MAPPING = {
  'views/user/': 'user',
  'views/tea/': 'tea',
  'views/order/': 'order', 
  'views/forum/': 'forum',
  'views/shop/': 'shop',
  'views/message/': 'message',
  'composables/': 'common',
  'components/': 'common'
}

function getModuleFromPath(filePath) {
  const normalizedPath = filePath.replace(/\\/g, '/')
  
  for (const [pathPattern, module] of Object.entries(MODULE_MAPPING)) {
    if (normalizedPath.includes(pathPattern)) {
      return module
    }
  }
  return 'common'
}

function scanFile(filePath) {
  try {
    const content = fs.readFileSync(filePath, 'utf8')
    const relativePath = path.relative(process.cwd(), filePath)
    const module = getModuleFromPath(relativePath)
    
    const results = {
      file: relativePath,
      module,
      imports: [],
      calls: []
    }
    
    // 扫描 import 语句
    const importRegex = /import\s*{\s*message\s*}\s*from\s*['"]@\/components\/common['"]/g
    let importMatch
    while ((importMatch = importRegex.exec(content)) !== null) {
      results.imports.push({
        match: importMatch[0],
        line: getLineNumber(content, importMatch.index)
      })
    }
    
    // 扫描消息调用
    const messageRegex = /message\.(success|error|warning|info)\s*\(\s*['"`]([^'"`]+)['"`]\s*\)/g
    let callMatch
    while ((callMatch = messageRegex.exec(content)) !== null) {
      results.calls.push({
        type: callMatch[1],
        text: callMatch[2],
        fullMatch: callMatch[0],
        line: getLineNumber(content, callMatch.index)
      })
    }
    
    return results
    
  } catch (error) {
    console.error(`❌ 读取文件失败: ${filePath}`, error.message)
    return null
  }
}

function getLineNumber(content, index) {
  return content.substring(0, index).split('\n').length
}

function main() {
  console.log('🔍 扫描消息调用...')
  
  const srcDir = path.join(process.cwd(), 'src')
  if (!fs.existsSync(srcDir)) {
    console.error('❌ 找不到 src 目录')
    return
  }
  
  const files = getAllFiles(srcDir)
  console.log(`📁 找到 ${files.length} 个文件`)
  
  const results = []
  const summary = {
    totalFiles: 0,
    filesWithImports: 0,
    filesWithCalls: 0,
    totalCalls: 0,
    byModule: {}
  }
  
  for (const file of files) {
    const result = scanFile(file)
    if (!result) continue
    
    summary.totalFiles++
    
    if (result.imports.length > 0) {
      summary.filesWithImports++
      results.push(result)
    }
    
    if (result.calls.length > 0) {
      summary.filesWithCalls++
      summary.totalCalls += result.calls.length
      
      if (!summary.byModule[result.module]) {
        summary.byModule[result.module] = { success: 0, error: 0, warning: 0, info: 0, total: 0 }
      }
      
      result.calls.forEach(call => {
        summary.byModule[result.module][call.type]++
        summary.byModule[result.module].total++
      })
      
      if (result.imports.length === 0) {
        results.push(result)
      }
    }
  }
  
  // 输出统计
  console.log('\n📊 扫描结果')
  console.log('='.repeat(50))
  console.log(`📁 总文件数: ${summary.totalFiles}`)
  console.log(`📦 有 import 的文件: ${summary.filesWithImports}`)
  console.log(`💬 有消息调用的文件: ${summary.filesWithCalls}`)
  console.log(`🔢 总消息调用数: ${summary.totalCalls}`)
  
  console.log('\n📋 按模块分布:')
  Object.entries(summary.byModule)
    .sort(([,a], [,b]) => b.total - a.total)
    .forEach(([module, counts]) => {
      console.log(`  ${module.padEnd(10)}: ${counts.total.toString().padStart(3)} 个 (success: ${counts.success}, error: ${counts.error}, warning: ${counts.warning}, info: ${counts.info})`)
    })
  
  // 生成详细清单
  const reportPath = path.join(process.cwd(), 'scripts', 'message-scan-report.json')
  const reportData = {
    summary,
    files: results.filter(r => r.calls.length > 0 || r.imports.length > 0)
  }
  
  // 确保目录存在
  const scriptsDir = path.dirname(reportPath)
  if (!fs.existsSync(scriptsDir)) {
    fs.mkdirSync(scriptsDir, { recursive: true })
  }
  
  fs.writeFileSync(reportPath, JSON.stringify(reportData, null, 2))
  console.log(`\n📄 详细报告已保存: ${reportPath}`)
  
  // 生成简化的迁移清单
  generateMigrationList(summary.byModule)
}

function generateMigrationList(byModule) {
  console.log('\n📝 迁移优先级建议:')
  
  const modules = Object.entries(byModule)
    .sort(([,a], [,b]) => b.total - a.total)
    .map(([module, counts]) => ({ module, ...counts }))
  
  modules.forEach((mod, index) => {
    const priority = mod.total > 10 ? '🔥 高' : mod.total > 5 ? '⚡ 中' : '💤 低'
    console.log(`  ${index + 1}. ${mod.module} - ${priority} (${mod.total} 个调用)`)
  })
  
  console.log('\n💡 建议:')
  console.log('  1. 先处理高优先级模块 (>10 个调用)')
  console.log('  2. user 模块已完成，可跳过')
  console.log('  3. common 模块已有基础，可直接使用')
  console.log('  4. 其他模块可按需渐进式迁移')
}

if (require.main === module) {
  main()
}