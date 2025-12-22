#!/usr/bin/env node

/**
 * 消息系统迁移脚本
 * 
 * 功能：
 * 1. 扫描所有 message.xxx('文本') 调用
 * 2. 按模块分类生成消息常量
 * 3. 替换简单的 import 语句
 * 4. 生成迁移报告
 */

const fs = require('fs')
const path = require('path')
const glob = require('glob')

// 模块映射规则
const MODULE_MAPPING = {
  '/views/user/': 'user',
  '/views/tea/': 'tea', 
  '/views/order/': 'order',
  '/views/forum/': 'forum',
  '/views/shop/': 'shop',
  '/views/message/': 'message',
  '/composables/': 'common',
  '/components/': 'common'
}

// 消息类型映射
const MESSAGE_TYPE_MAPPING = {
  'success': 'SUCCESS',
  'error': 'ERROR', 
  'warning': 'PROMPT',
  'info': 'PROMPT'
}

class MessageMigrator {
  constructor() {
    this.results = {
      scanned: 0,
      found: 0,
      byModule: {},
      imports: [],
      calls: []
    }
  }

  // 扫描文件
  scanFiles() {
    const pattern = 'src/**/*.{vue,js}'
    const files = glob.sync(pattern, { cwd: process.cwd() })
    
    console.log(`📁 扫描 ${files.length} 个文件...`)
    
    files.forEach(file => {
      this.scanFile(file)
    })
    
    this.generateReport()
  }

  // 扫描单个文件
  scanFile(filePath) {
    this.results.scanned++
    
    try {
      const content = fs.readFileSync(filePath, 'utf8')
      const module = this.getModuleFromPath(filePath)
      
      // 扫描 import 语句
      this.scanImports(filePath, content)
      
      // 扫描消息调用
      this.scanMessageCalls(filePath, content, module)
      
    } catch (error) {
      console.error(`❌ 读取文件失败: ${filePath}`, error.message)
    }
  }

  // 根据路径判断模块
  getModuleFromPath(filePath) {
    for (const [pathPattern, module] of Object.entries(MODULE_MAPPING)) {
      if (filePath.includes(pathPattern)) {
        return module
      }
    }
    return 'common'
  }

  // 扫描 import 语句
  scanImports(filePath, content) {
    const importRegex = /import\s*{\s*message\s*}\s*from\s*['"]@\/components\/common['"]/g
    const matches = content.match(importRegex)
    
    if (matches) {
      this.results.imports.push({
        file: filePath,
        matches: matches.length
      })
    }
  }

  // 扫描消息调用
  scanMessageCalls(filePath, content, module) {
    // 匹配 message.xxx('文本') 或 message.xxx("文本")
    const messageRegex = /message\.(success|error|warning|info)\s*\(\s*['"`]([^'"`]+)['"`]\s*\)/g
    let match
    
    while ((match = messageRegex.exec(content)) !== null) {
      const [fullMatch, type, text] = match
      
      this.results.found++
      this.results.calls.push({
        file: filePath,
        module,
        type,
        text,
        fullMatch,
        line: this.getLineNumber(content, match.index)
      })
      
      // 按模块统计
      if (!this.results.byModule[module]) {
        this.results.byModule[module] = { success: 0, error: 0, warning: 0, info: 0 }
      }
      this.results.byModule[module][type]++
    }
  }

  // 获取行号
  getLineNumber(content, index) {
    return content.substring(0, index).split('\n').length
  }

  // 生成消息常量
  generateMessageConstants(module, calls) {
    const constants = {
      SUCCESS: {},
      ERROR: {},
      PROMPT: {}
    }
    
    calls.forEach((call, index) => {
      const category = MESSAGE_TYPE_MAPPING[call.type]
      const constantName = this.generateConstantName(call.text, index)
      constants[category][constantName] = call.text
    })
    
    return constants
  }

  // 生成常量名
  generateConstantName(text, index) {
    // 简单的常量名生成逻辑
    let name = text
      .replace(/[^\u4e00-\u9fa5a-zA-Z0-9]/g, '_') // 替换特殊字符
      .replace(/_{2,}/g, '_') // 合并多个下划线
      .replace(/^_|_$/g, '') // 去掉首尾下划线
      .toUpperCase()
    
    // 如果太长或包含中文，使用索引
    if (name.length > 30 || /[\u4e00-\u9fa5]/.test(name)) {
      name = `MESSAGE_${index + 1}`
    }
    
    return name
  }

  // 生成函数名
  generateFunctionName(constantName, type) {
    const prefix = {
      'success': 'show',
      'error': 'show', 
      'warning': 'show',
      'info': 'show'
    }[type]
    
    return `${prefix}${constantName.charAt(0) + constantName.slice(1).toLowerCase()}`
  }

  // 生成迁移报告
  generateReport() {
    console.log('\n📊 迁移分析报告')
    console.log('='.repeat(50))
    console.log(`📁 扫描文件: ${this.results.scanned}`)
    console.log(`🔍 发现消息调用: ${this.results.found}`)
    console.log(`📦 需要更新 import: ${this.results.imports.length}`)
    
    console.log('\n📋 按模块分布:')
    Object.entries(this.results.byModule).forEach(([module, counts]) => {
      const total = Object.values(counts).reduce((a, b) => a + b, 0)
      console.log(`  ${module}: ${total} 个 (success: ${counts.success}, error: ${counts.error}, warning: ${counts.warning}, info: ${counts.info})`)
    })
    
    // 生成详细报告文件
    this.generateDetailedReport()
    
    // 生成消息常量文件
    this.generateMessageFiles()
  }

  // 生成详细报告文件
  generateDetailedReport() {
    const reportPath = 'scripts/migration-report.json'
    fs.writeFileSync(reportPath, JSON.stringify(this.results, null, 2))
    console.log(`\n📄 详细报告已保存: ${reportPath}`)
  }

  // 生成消息常量文件
  generateMessageFiles() {
    Object.entries(this.results.byModule).forEach(([module, counts]) => {
      if (module === 'user' || module === 'common') return // 已处理
      
      const moduleCalls = this.results.calls.filter(call => call.module === module)
      if (moduleCalls.length === 0) return
      
      const constants = this.generateMessageConstants(module, moduleCalls)
      const fileContent = this.generateMessageFileContent(module, constants, moduleCalls)
      
      const outputPath = `scripts/generated-${module}Messages.js`
      fs.writeFileSync(outputPath, fileContent)
      console.log(`📝 生成消息文件: ${outputPath}`)
    })
  }

  // 生成消息文件内容
  generateMessageFileContent(module, constants, calls) {
    const moduleName = module.toUpperCase()
    
    let content = `/**
 * ${module} 模块消息工具 (自动生成)
 * 
 * 分类说明：
 * - SUCCESS: 操作成功反馈
 * - ERROR: 操作失败/错误提示  
 * - PROMPT: 用户提示（表单验证+确认）
 */

import { successMessage, errorMessage, promptMessage } from './messageManager'

// ${module} 模块消息常量
export const ${moduleName}_MESSAGES = ${JSON.stringify(constants, null, 2)}

// 成功消息函数
export const ${module}SuccessMessages = {
`
    
    // 生成成功消息函数
    Object.entries(constants.SUCCESS).forEach(([key, text]) => {
      const funcName = this.generateFunctionName(key, 'success')
      content += `  ${funcName}() {\n    successMessage.show(${moduleName}_MESSAGES.SUCCESS.${key})\n  },\n`
    })
    
    content += `}

// 错误消息函数  
export const ${module}ErrorMessages = {
`
    
    // 生成错误消息函数
    Object.entries(constants.ERROR).forEach(([key, text]) => {
      const funcName = this.generateFunctionName(key, 'error')
      content += `  ${funcName}() {\n    errorMessage.show(${moduleName}_MESSAGES.ERROR.${key})\n  },\n`
    })
    
    content += `}

// 提示消息函数
export const ${module}PromptMessages = {
`
    
    // 生成提示消息函数
    Object.entries(constants.PROMPT).forEach(([key, text]) => {
      const funcName = this.generateFunctionName(key, 'warning')
      content += `  ${funcName}() {\n    promptMessage.show(${moduleName}_MESSAGES.PROMPT.${key})\n  },\n`
    })
    
    content += `}

// 默认导出
export default {
  success: ${module}SuccessMessages,
  error: ${module}ErrorMessages, 
  prompt: ${module}PromptMessages,
  ${moduleName}_MESSAGES
}
`
    
    return content
  }

  // 执行简单替换
  performSimpleReplacements() {
    console.log('\n🔄 执行简单替换...')
    
    // 替换 import 语句
    this.results.imports.forEach(item => {
      this.replaceImports(item.file)
    })
    
    console.log(`✅ 完成 ${this.results.imports.length} 个文件的 import 替换`)
  }

  // 替换 import 语句
  replaceImports(filePath) {
    try {
      let content = fs.readFileSync(filePath, 'utf8')
      const module = this.getModuleFromPath(filePath)
      
      if (module === 'user' || module === 'common') return // 已处理
      
      // 替换 import 语句
      content = content.replace(
        /import\s*{\s*message\s*}\s*from\s*['"]@\/components\/common['"]/g,
        `import ${module}Messages from '@/utils/${module}Messages'`
      )
      
      fs.writeFileSync(filePath, content)
    } catch (error) {
      console.error(`❌ 替换失败: ${filePath}`, error.message)
    }
  }
}

// 执行脚本
if (require.main === module) {
  const migrator = new MessageMigrator()
  
  console.log('🚀 开始消息系统迁移分析...')
  migrator.scanFiles()
  
  // 询问是否执行简单替换
  const readline = require('readline')
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
  })
  
  rl.question('\n❓ 是否执行简单的 import 替换? (y/N): ', (answer) => {
    if (answer.toLowerCase() === 'y') {
      migrator.performSimpleReplacements()
    }
    
    console.log('\n✨ 分析完成！请查看生成的文件和报告。')
    rl.close()
  })
}

module.exports = MessageMigrator