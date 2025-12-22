#!/usr/bin/env node

/**
 * 简化版消息迁移脚本
 */

const fs = require('fs')
const path = require('path')

console.log('🚀 开始全项目消息迁移...\n')

const srcDir = path.join(__dirname, '../src')
const results = {
  filesProcessed: 0,
  importsUpdated: 0,
  callsReplaced: 0,
  errors: []
}

// 查找所有使用旧消息系统的文件
function findAllMessageFiles() {
  console.log('🔍 查找使用旧消息系统的文件...')
  
  const files = []
  
  function searchDir(dir) {
    const entries = fs.readdirSync(dir, { withFileTypes: true })
    
    for (const entry of entries) {
      const fullPath = path.join(dir, entry.name)
      
      if (entry.isDirectory()) {
        if (!['node_modules', '.git', 'dist', 'build'].includes(entry.name)) {
          searchDir(fullPath)
        }
      } else if (entry.isFile() && (entry.name.endsWith('.vue') || entry.name.endsWith('.js'))) {
        try {
          const content = fs.readFileSync(fullPath, 'utf8')
          // 检查是否包含任何旧消息系统的引用
          const hasOldMessages = [
            'commonMessages', 'userMessages', 'teaMessages', 
            'orderMessages', 'shopMessages', 'forumMessages', 'messageMessages'
          ].some(module => 
            content.includes(`${module}`) || 
            content.includes(`${module.replace('Messages', 'SuccessMessages')}`) ||
            content.includes(`${module.replace('Messages', 'ErrorMessages')}`)
          )
          
          if (hasOldMessages) {
            files.push(fullPath)
          }
        } catch (error) {
          // 忽略读取错误
        }
      }
    }
  }
  
  searchDir(srcDir)
  console.log(`📁 找到 ${files.length} 个文件使用旧消息系统`)
  
  return files
}

// 更新单个文件
function updateFile(filePath) {
  console.log(`📝 处理文件: ${path.relative(srcDir, filePath)}`)
  
  let content = fs.readFileSync(filePath, 'utf8')
  let modified = false

  // 1. 移除旧的导入语句
  const oldImports = [
    /import\s+commonMessages\s+from\s+['"]@\/utils\/commonMessages['"]/g,
    /import\s+userMessages\s+from\s+['"]@\/utils\/userMessages['"]/g,
    /import\s+teaMessages\s+from\s+['"]@\/utils\/teaMessages['"]/g,
    /import\s+orderMessages\s+from\s+['"]@\/utils\/orderMessages['"]/g,
    /import\s+shopMessages\s+from\s+['"]@\/utils\/shopMessages['"]/g,
    /import\s+forumMessages\s+from\s+['"]@\/utils\/forumMessages['"]/g,
    /import\s+messageMessages\s+from\s+['"]@\/utils\/messageMessages['"]/g,
    /import\s*{\s*[^}]*SuccessMessages[^}]*}\s*from\s+['"]@\/utils\/[^'"]*Messages['"]/g,
    /import\s*{\s*[^}]*ErrorMessages[^}]*}\s*from\s+['"]@\/utils\/[^'"]*Messages['"]/g
  ]

  oldImports.forEach(pattern => {
    if (content.match(pattern)) {
      content = content.replace(pattern, '')
      modified = true
      results.importsUpdated++
    }
  })

  // 2. 添加新的导入语句（如果需要）
  const needsApiMessages = content.includes('Messages.success.') || 
                           content.includes('Messages.error.') ||
                           content.includes('SuccessMessages.') ||
                           content.includes('ErrorMessages.')

  if (needsApiMessages && !content.includes("from '@/utils/apiMessages'")) {
    // 找到其他导入语句的位置，在其后添加新导入
    const importRegex = /import.*from.*['"][^'"]*['"];?\n/g
    const matches = [...content.matchAll(importRegex)]
    if (matches.length > 0) {
      const lastImport = matches[matches.length - 1]
      const insertPos = lastImport.index + lastImport[0].length
      content = content.slice(0, insertPos) + 
                "import { showByCode, isSuccess } from '@/utils/apiMessages'\n" + 
                content.slice(insertPos)
      modified = true
    }
  }

  // 3. 标记需要手动处理的调用
  const callPatterns = [
    'Messages.success.',
    'Messages.error.',
    'SuccessMessages.',
    'ErrorMessages.'
  ]

  callPatterns.forEach(pattern => {
    const regex = new RegExp(`(\\s*)(\\w*${pattern.replace(/\./g, '\\.')}\\w+)`, 'g')
    const matches = [...content.matchAll(regex)]
    
    if (matches.length > 0) {
      content = content.replace(regex, 
        `$1// TODO: 迁移到新消息系统 - 使用 showByCode(response.code)\n$1$2`
      )
      modified = true
      results.callsReplaced += matches.length
    }
  })

  // 4. 清理空行
  content = content.replace(/\n\s*\n\s*\n/g, '\n\n')

  if (modified) {
    fs.writeFileSync(filePath, content, 'utf8')
    results.filesProcessed++
    console.log(`✅ 已更新: ${path.relative(srcDir, filePath)}`)
  } else {
    console.log(`⏭️  跳过: ${path.relative(srcDir, filePath)} (无需更改)`)
  }
}

// 生成迁移报告
function generateReport() {
  const reportPath = path.join(__dirname, 'all-message-migration-report.md')
  
  const report = `# 全项目消息迁移报告

## 迁移统计

- 📁 处理文件数: ${results.filesProcessed}
- 📦 更新导入数: ${results.importsUpdated}  
- 🔄 标记调用数: ${results.callsReplaced}
- ❌ 错误数量: ${results.errors.length}

## 后续手动工作

需要手动处理标记为 \`// TODO: 迁移到新消息系统\` 的代码：

### 迁移模式

\`\`\`javascript
// 旧方式
xxxMessages.success.showXxxSuccess()

// 新方式
const response = await api.someAction()
if (isSuccess(response.code)) {
  showByCode(response.code) // 自动显示成功消息
} else {
  showByCode(response.code) // 自动显示错误消息
}
\`\`\`

## 错误日志

${results.errors.map(error => `- ${error}`).join('\n')}

---
生成时间: ${new Date().toLocaleString()}
`

  fs.writeFileSync(reportPath, report, 'utf8')
  console.log(`📊 迁移报告已生成: ${reportPath}`)
}

// 执行迁移
async function migrate() {
  try {
    const files = findAllMessageFiles()
    
    if (files.length === 0) {
      console.log('✅ 没有找到需要迁移的文件')
      return
    }

    console.log('\n📋 开始处理文件...')
    for (const file of files) {
      try {
        updateFile(file)
      } catch (error) {
        console.error(`❌ 处理文件失败: ${file}`)
        console.error(error.message)
        results.errors.push(`${file}: ${error.message}`)
      }
    }

    console.log('\n📊 生成迁移报告...')
    generateReport()

    console.log('\n🎉 全项目消息迁移完成!')
    console.log(`📁 处理了 ${results.filesProcessed} 个文件`)
    console.log(`📦 更新了 ${results.importsUpdated} 个导入`)
    console.log(`🔄 标记了 ${results.callsReplaced} 个调用需要手动迁移`)
    
    if (results.errors.length > 0) {
      console.log(`❌ 遇到 ${results.errors.length} 个错误，请查看报告`)
    }

    console.log('\n📝 下一步：')
    console.log('1. 查看生成的迁移报告')
    console.log('2. 手动处理标记为 TODO 的代码')
    console.log('3. 测试迁移后的功能')

  } catch (error) {
    console.error('❌ 迁移过程中发生错误:', error.message)
    results.errors.push(`Migration error: ${error.message}`)
  }
}

// 运行迁移
migrate().catch(console.error)