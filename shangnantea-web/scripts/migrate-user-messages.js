#!/usr/bin/env node

/**
 * 用户模块消息迁移脚本
 * 
 * 功能：
 * 1. 查找所有使用 userMessages 的文件
 * 2. 更新导入语句
 * 3. 替换 SUCCESS/ERROR 消息调用为新的 showByCode 方式
 * 4. 保留 PROMPT 消息调用（已经迁移到 promptMessages）
 */

const fs = require('fs')
const path = require('path')
const { execSync } = require('child_process')

class UserMessageMigrator {
  constructor() {
    this.srcDir = path.join(__dirname, '../src')
    this.results = {
      filesProcessed: 0,
      importsUpdated: 0,
      callsReplaced: 0,
      errors: []
    }
  }

  // 查找所有使用 userMessages 的文件
  findUserMessageFiles() {
    console.log('🔍 查找使用 userMessages 的文件...')
    
    try {
      // 使用 grep 查找所有引用 userMessages 的文件
      const grepResult = execSync(
        `grep -r "userMessages" ${this.srcDir} --include="*.vue" --include="*.js" -l`,
        { encoding: 'utf8' }
      )
      
      const files = grepResult.trim().split('\n').filter(file => file.length > 0)
      console.log(`📁 找到 ${files.length} 个文件使用 userMessages`)
      
      return files
    } catch (error) {
      console.log('ℹ️  没有找到使用 userMessages 的文件')
      return []
    }
  }

  // 分析文件内容，确定需要的迁移操作
  analyzeFile(filePath) {
    const content = fs.readFileSync(filePath, 'utf8')
    const analysis = {
      hasUserMessagesImport: false,
      hasSuccessErrorCalls: false,
      hasPromptCalls: false,
      needsApiMessages: false,
      needsPromptMessages: false
    }

    // 检查导入语句
    if (content.includes("from '@/utils/userMessages'") || 
        content.includes('import userMessages')) {
      analysis.hasUserMessagesImport = true
    }

    // 检查 SUCCESS/ERROR 调用
    if (content.includes('userMessages.success.') || 
        content.includes('userMessages.error.') ||
        content.includes('userSuccessMessages.') ||
        content.includes('userErrorMessages.')) {
      analysis.hasSuccessErrorCalls = true
      analysis.needsApiMessages = true
    }

    // 检查 PROMPT 调用
    if (content.includes('userMessages.prompt.') || 
        content.includes('userPromptMessages.')) {
      analysis.hasPromptCalls = true
      analysis.needsPromptMessages = true
    }

    return analysis
  }

  // 更新单个文件
  updateFile(filePath) {
    console.log(`📝 处理文件: ${path.relative(this.srcDir, filePath)}`)
    
    let content = fs.readFileSync(filePath, 'utf8')
    const analysis = this.analyzeFile(filePath)
    let modified = false

    // 1. 更新导入语句
    if (analysis.hasUserMessagesImport) {
      // 移除旧的导入
      content = content.replace(
        /import\s+userMessages\s+from\s+['"]@\/utils\/userMessages['"]/g,
        ''
      )
      content = content.replace(
        /import\s*{\s*userSuccessMessages,?\s*userErrorMessages,?\s*userPromptMessages?\s*}\s*from\s+['"]@\/utils\/userMessages['"]/g,
        ''
      )

      // 添加新的导入
      const imports = []
      if (analysis.needsApiMessages) {
        imports.push("import { showByCode, isSuccess } from '@/utils/apiMessages'")
      }
      if (analysis.needsPromptMessages) {
        imports.push("import { userPromptMessages } from '@/utils/promptMessages'")
      }

      if (imports.length > 0) {
        // 找到其他导入语句的位置，在其后添加新导入
        const importRegex = /import.*from.*['"][^'"]*['"];?\n/g
        const matches = [...content.matchAll(importRegex)]
        if (matches.length > 0) {
          const lastImport = matches[matches.length - 1]
          const insertPos = lastImport.index + lastImport[0].length
          content = content.slice(0, insertPos) + imports.join('\n') + '\n' + content.slice(insertPos)
        } else {
          // 如果没有其他导入，在文件开头添加
          content = imports.join('\n') + '\n' + content
        }
      }

      modified = true
      this.results.importsUpdated++
    }

    // 2. 替换 SUCCESS/ERROR 消息调用
    if (analysis.hasSuccessErrorCalls) {
      // 这里需要手动处理，因为需要结合 API 调用上下文
      // 先标记需要手动处理的调用
      const successErrorCalls = [
        'userMessages.success.showLoginSuccess',
        'userMessages.success.showRegisterSuccess',
        'userMessages.success.showLogoutSuccess',
        'userMessages.success.showProfileUpdateSuccess',
        'userMessages.success.showPasswordChangeSuccess',
        'userMessages.success.showAvatarUpdateSuccess',
        'userMessages.error.showLoginFailure',
        'userMessages.error.showRegisterFailure',
        'userMessages.error.showProfileUpdateFailure',
        'userMessages.error.showPasswordChangeFailure',
        'userMessages.error.showSessionExpired',
        'userMessages.error.showPermissionDenied'
      ]

      successErrorCalls.forEach(call => {
        if (content.includes(call)) {
          // 添加注释提示需要手动迁移
          content = content.replace(
            new RegExp(`(\\s*)(${call.replace(/\./g, '\\.')})`, 'g'),
            `$1// TODO: 迁移到新消息系统 - 使用 showByCode(response.code)\n$1$2`
          )
          modified = true
          this.results.callsReplaced++
        }
      })
    }

    // 3. 清理空行
    content = content.replace(/\n\s*\n\s*\n/g, '\n\n')

    if (modified) {
      fs.writeFileSync(filePath, content, 'utf8')
      this.results.filesProcessed++
      console.log(`✅ 已更新: ${path.relative(this.srcDir, filePath)}`)
    } else {
      console.log(`⏭️  跳过: ${path.relative(this.srcDir, filePath)} (无需更改)`)
    }
  }

  // 生成迁移报告
  generateReport() {
    const reportPath = path.join(__dirname, 'user-message-migration-report.md')
    const report = `# 用户模块消息迁移报告

## 迁移统计

- 📁 处理文件数: ${this.results.filesProcessed}
- 📦 更新导入数: ${this.results.importsUpdated}  
- 🔄 标记调用数: ${this.results.callsReplaced}
- ❌ 错误数量: ${this.results.errors.length}

## 后续手动工作

需要手动处理标记为 \`// TODO: 迁移到新消息系统\` 的代码：

### 迁移模式

\`\`\`javascript
// 旧方式
userMessages.success.showLoginSuccess()

// 新方式
const response = await userApi.login(credentials)
if (isSuccess(response.code)) {
  showByCode(response.code) // 自动显示成功消息
} else {
  showByCode(response.code) // 自动显示错误消息
}
\`\`\`

### 状态码映射

参考 \`docs/code-message-mapping.md\` 中的用户模块状态码：

- 2000: 登录成功
- 2001: 注册成功，请登录  
- 2002: 已安全退出系统
- 2010: 个人资料更新成功
- 2011: 密码修改成功
- 2012: 头像更新成功
- 2100: 登录失败
- 2101: 注册失败
- 2110: 个人资料更新失败
- 2111: 密码修改失败
- 2112: 头像更新失败

## 错误日志

${this.results.errors.map(error => `- ${error}`).join('\n')}

---
生成时间: ${new Date().toLocaleString()}
`

    fs.writeFileSync(reportPath, report, 'utf8')
    console.log(`📊 迁移报告已生成: ${reportPath}`)
  }

  // 执行迁移
  async migrate() {
    console.log('🚀 开始用户模块消息迁移...\n')

    try {
      const files = this.findUserMessageFiles()
      
      if (files.length === 0) {
        console.log('✅ 没有找到需要迁移的文件')
        return
      }

      console.log('\n📋 开始处理文件...')
      for (const file of files) {
        try {
          this.updateFile(file)
        } catch (error) {
          console.error(`❌ 处理文件失败: ${file}`)
          console.error(error.message)
          this.results.errors.push(`${file}: ${error.message}`)
        }
      }

      console.log('\n📊 生成迁移报告...')
      this.generateReport()

      console.log('\n🎉 用户模块消息迁移完成!')
      console.log(`📁 处理了 ${this.results.filesProcessed} 个文件`)
      console.log(`📦 更新了 ${this.results.importsUpdated} 个导入`)
      console.log(`🔄 标记了 ${this.results.callsReplaced} 个调用需要手动迁移`)
      
      if (this.results.errors.length > 0) {
        console.log(`❌ 遇到 ${this.results.errors.length} 个错误，请查看报告`)
      }

      console.log('\n📝 下一步：')
      console.log('1. 查看生成的迁移报告')
      console.log('2. 手动处理标记为 TODO 的代码')
      console.log('3. 测试迁移后的功能')

    } catch (error) {
      console.error('❌ 迁移过程中发生错误:', error.message)
      this.results.errors.push(`Migration error: ${error.message}`)
    }
  }
}

// 执行迁移
if (require.main === module) {
  const migrator = new UserMessageMigrator()
  migrator.migrate().catch(console.error)
}

module.exports = UserMessageMigrator