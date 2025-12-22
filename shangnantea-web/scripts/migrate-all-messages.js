#!/usr/bin/env node

/**
 * 全项目消息迁移脚本
 * 
 * 功能：
 * 1. 查找所有使用旧消息系统的文件（所有模块）
 * 2. 更新导入语句
 * 3. 标记 SUCCESS/ERROR 消息调用为 TODO（需手动迁移）
 * 4. 保留 PROMPT 消息调用（已经迁移到 promptMessages）
 * 
 * 支持模块：
 * - userMessages (用户模块)
 * - orderMessages (订单模块) 
 * - teaMessages (茶叶模块)
 * - shopMessages (店铺模块)
 * - forumMessages (论坛模块)
 * - messageMessages (消息模块)
 * - commonMessages (通用模块)
 */

const fs = require('fs')
const path = require('path')

class AllMessageMigrator {
  constructor() {
    this.srcDir = path.join(__dirname, '../src')
    this.results = {
      filesProcessed: 0,
      importsUpdated: 0,
      callsMarked: 0,
      errors: [],
      fileDetails: [],
      moduleStats: {
        user: { files: 0, imports: 0, calls: 0 },
        order: { files: 0, imports: 0, calls: 0 },
        tea: { files: 0, imports: 0, calls: 0 },
        shop: { files: 0, imports: 0, calls: 0 },
        forum: { files: 0, imports: 0, calls: 0 },
        message: { files: 0, imports: 0, calls: 0 },
        common: { files: 0, imports: 0, calls: 0 }
      }
    }
    
    // 所有需要迁移的模块配置
    this.modules = {
      user: {
        importName: 'userMessages',
        importPath: '@/utils/userMessages',
        promptName: 'UserPromptMessages',
        successCalls: [
          'userMessages.success.showLoginSuccess',
          'userMessages.success.showRegisterSuccess', 
          'userMessages.success.showLogoutSuccess',
          'userMessages.success.showProfileUpdateSuccess',
          'userMessages.success.showPasswordChangeSuccess',
          'userMessages.success.showAvatarUpdateSuccess',
          'userMessages.success.showPasswordResetSuccess',
          'userMessages.success.showUserDeleted',
          'userMessages.success.showUserStatusToggled',
          'userMessages.success.showUserUpdated',
          'userMessages.success.showAdminCreated',
          'userMessages.success.showSettingsRestored',
          'userMessages.success.showDataMigrationTriggered',
          'userMessages.success.showStorageReset',
          'userMessages.success.showCaptchaSent',
          'userSuccessMessages.showLoginSuccess',
          'userSuccessMessages.showRegisterSuccess',
          'userSuccessMessages.showLogoutSuccess',
          'userSuccessMessages.showProfileUpdateSuccess',
          'userSuccessMessages.showPasswordChangeSuccess',
          'userSuccessMessages.showAvatarUpdateSuccess'
        ],
        errorCalls: [
          'userMessages.error.showLoginFailure',
          'userMessages.error.showRegisterFailure',
          'userMessages.error.showProfileUpdateFailure',
          'userMessages.error.showPasswordChangeFailure',
          'userMessages.error.showPasswordMismatch',
          'userMessages.error.showAvatarUpdateFailure',
          'userMessages.error.showSessionExpired',
          'userMessages.error.showPermissionDenied',
          'userMessages.error.showAuthError',
          'userMessages.error.showPasswordResetFailure',
          'userMessages.error.showUserListFetchFailed',
          'userMessages.error.showUserDeleteFailed',
          'userMessages.error.showUserStatusToggleFailed',
          'userMessages.error.showUserFormSubmitFailed',
          'userMessages.error.showTokenInvalid',
          'userErrorMessages.showLoginFailure',
          'userErrorMessages.showRegisterFailure',
          'userErrorMessages.showProfileUpdateFailure',
          'userErrorMessages.showPasswordChangeFailure',
          'userErrorMessages.showSessionExpired',
          'userErrorMessages.showPermissionDenied'
        ]
      },
      order: {
        importName: 'orderMessages',
        importPath: '@/utils/orderMessages',
        promptName: 'OrderPromptMessages',
        successCalls: [
          'orderMessages.success.showOrderCreated',
          'orderMessages.success.showOrderCanceled',
          'orderMessages.success.showOrderConfirmed',
          'orderMessages.success.showOrderShipped',
          'orderMessages.success.showOrderPaid',
          'orderMessages.success.showAddedToCart',
          'orderMessages.success.showCartQuantityUpdated',
          'orderMessages.success.showCartCleared',
          'orderMessages.success.showPaymentSuccess',
          'orderMessages.success.showRefundSubmitted',
          'orderMessages.success.showBatchShipSuccess',
          'orderMessages.success.showAddressAdded',
          'orderMessages.success.showReviewSubmitted',
          'orderSuccessMessages.showOrderCreated',
          'orderSuccessMessages.showOrderCanceled',
          'orderSuccessMessages.showAddedToCart',
          'orderSuccessMessages.showPaymentSuccess'
        ],
        errorCalls: [
          'orderMessages.error.showOrderCreateFailed',
          'orderMessages.error.showOrderCancelFailed',
          'orderMessages.error.showCartLoadFailed',
          'orderMessages.error.showCartAddFailed',
          'orderMessages.error.showPaymentFailed',
          'orderMessages.error.showCartItemOutOfStock',
          'orderMessages.error.showInsufficientBalance',
          'orderMessages.error.showRefundSubmitFailed',
          'orderMessages.error.showAddressLoadFailed',
          'orderErrorMessages.showOrderCreateFailed',
          'orderErrorMessages.showCartLoadFailed',
          'orderErrorMessages.showPaymentFailed',
          'orderErrorMessages.showCartItemOutOfStock'
        ]
      },
      tea: {
        importName: 'teaMessages',
        importPath: '@/utils/teaMessages',
        promptName: 'TeaPromptMessages',
        successCalls: [
          'teaMessages.success.showAddedToFavorites',
          'teaMessages.success.showRemovedFromFavorites',
          'teaMessages.success.showAddedToCart',
          'teaMessages.success.showTeaCreated',
          'teaMessages.success.showTeaUpdated',
          'teaMessages.success.showTeaDeleted',
          'teaMessages.success.showCategoryCreated',
          'teaSuccessMessages.showAddedToFavorites',
          'teaSuccessMessages.showAddedToCart'
        ],
        errorCalls: [
          'teaMessages.error.showListFailed',
          'teaMessages.error.showDetailFailed',
          'teaMessages.error.showCartFailed',
          'teaMessages.error.showFavoriteFailed',
          'teaMessages.error.showTeaCreateFailed',
          'teaMessages.error.showTeaUpdateFailed',
          'teaMessages.error.showTeaDeleteFailed',
          'teaErrorMessages.showListFailed',
          'teaErrorMessages.showDetailFailed',
          'teaErrorMessages.showCartFailed'
        ]
      },
      shop: {
        importName: 'shopMessages',
        importPath: '@/utils/shopMessages',
        promptName: 'ShopPromptMessages',
        successCalls: [
          'shopMessages.success.showFollowSuccess',
          'shopMessages.success.showUnfollowSuccess',
          'shopMessages.success.showBannerAddSuccess',
          'shopMessages.success.showBannerUpdateSuccess',
          'shopMessages.success.showAnnouncementAddSuccess',
          'shopMessages.success.showLogoUploadSuccess',
          'shopSuccessMessages.showFollowSuccess',
          'shopSuccessMessages.showBannerAddSuccess'
        ],
        errorCalls: [
          'shopMessages.error.showShopInfoLoadFailed',
          'shopMessages.error.showTeaListLoadFailed',
          'shopMessages.error.showFollowFailed',
          'shopMessages.error.showBannerLoadFailed',
          'shopMessages.error.showBannerSaveFailed',
          'shopMessages.error.showLogoUploadFailed',
          'shopErrorMessages.showShopInfoLoadFailed',
          'shopErrorMessages.showFollowFailed'
        ]
      },
      forum: {
        importName: 'forumMessages',
        importPath: '@/utils/forumMessages',
        promptName: 'ForumPromptMessages',
        successCalls: [
          'forumMessages.success.showPostCreated',
          'forumMessages.success.showPostDeleted',
          'forumMessages.success.showPostLiked',
          'forumMessages.success.showPostFavorited',
          'forumMessages.success.showCommentCreated',
          'forumMessages.success.showTopicCreated',
          'forumMessages.success.showArticleCreated',
          'forumSuccessMessages.showPostCreated',
          'forumSuccessMessages.showCommentCreated'
        ],
        errorCalls: [
          'forumMessages.error.showPostCreateFailed',
          'forumMessages.error.showLoadPostsFailed',
          'forumMessages.error.showPostDeleteFailed',
          'forumMessages.error.showCommentCreateFailed',
          'forumMessages.error.showTopicCreateFailed',
          'forumMessages.error.showArticleCreateFailed',
          'forumErrorMessages.showPostCreateFailed',
          'forumErrorMessages.showLoadPostsFailed'
        ]
      },
      message: {
        importName: 'messageMessages',
        importPath: '@/utils/messageMessages',
        promptName: 'MessagePromptMessages',
        successCalls: [
          'messageMessages.success.showMessageSent',
          'messageMessages.success.showNotificationRead',
          'messageMessages.success.showAllNotificationsRead',
          'messageMessages.success.showSessionDeleted',
          'messageMessages.success.showMessageRecalled',
          'messageSuccessMessages.showMessageSent',
          'messageSuccessMessages.showNotificationRead'
        ],
        errorCalls: [
          'messageMessages.error.showSendFailed',
          'messageMessages.error.showLoadFailed',
          'messageMessages.error.showRecallFailed',
          'messageMessages.error.showUserInfoFailed',
          'messageErrorMessages.showSendFailed',
          'messageErrorMessages.showLoadFailed'
        ]
      },
      common: {
        importName: 'commonMessages',
        importPath: '@/utils/commonMessages',
        promptName: 'CommonPromptMessages',
        successCalls: [
          'commonMessages.success.showUploadSuccess',
          'commonMessages.success.showSubscribeSuccess',
          'commonMessages.success.showOperationSuccess',
          'commonMessages.success.showSaveSuccess',
          'commonMessages.success.showDeleteSuccess',
          'commonMessages.success.showUpdateSuccess',
          'commonMessages.success.showCopySuccess',
          'commonSuccessMessages.showUploadSuccess',
          'commonSuccessMessages.showOperationSuccess',
          'commonSuccessMessages.showSaveSuccess',
          'commonSuccessMessages.showDeleteSuccess'
        ],
        errorCalls: [
          'commonMessages.error.showFileTypeInvalid',
          'commonMessages.error.showFileSizeExceeded',
          'commonMessages.error.showUploadFailed',
          'commonMessages.error.showOperationFailed',
          'commonMessages.error.showLoadFailed',
          'commonMessages.error.showImageTypeInvalid',
          'commonErrorMessages.showFileTypeInvalid',
          'commonErrorMessages.showUploadFailed',
          'commonErrorMessages.showOperationFailed',
          'commonErrorMessages.showLoadFailed'
        ]
      }
    }
  }

  // 查找所有使用旧消息系统的文件
  findAllMessageFiles() {
    console.log('🔍 查找使用旧消息系统的文件...')
    
    const files = []
    
    const searchDir = (dir) => {
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
            
            // 检查是否包含任何模块的消息导入或调用
            const hasAnyMessage = Object.keys(this.modules).some(module => {
              const config = this.modules[module]
              return content.includes(config.importName) || 
                     content.includes(`from '${config.importPath}'`) ||
                     content.includes(`from "${config.importPath}"`)
            })
            
            if (hasAnyMessage) {
              files.push(fullPath)
            }
          } catch (error) {
            // 忽略读取错误
          }
        }
      }
    }
    
    searchDir(this.srcDir)
    console.log(`📁 找到 ${files.length} 个文件使用旧消息系统`)
    
    return files
  }


  // 分析文件内容，确定需要的迁移操作
  analyzeFile(filePath) {
    const content = fs.readFileSync(filePath, 'utf8')
    const analysis = {
      hasOldImports: false,
      successErrorCalls: [],
      promptCalls: [],
      needsApiMessages: false,
      needsPromptMessages: false,
      modulesFound: [],
      importStatements: []
    }

    // 检查每个模块
    Object.keys(this.modules).forEach(moduleName => {
      const config = this.modules[moduleName]
      
      // 检查导入语句 - 多种格式
      const importPatterns = [
        new RegExp(`import\\s+${config.importName}\\s+from\\s+['"]${config.importPath.replace(/\//g, '\\/')}['"]`, 'g'),
        new RegExp(`import\\s*{[^}]*}\\s*from\\s+['"]${config.importPath.replace(/\//g, '\\/')}['"]`, 'g'),
        new RegExp(`from\\s+['"]${config.importPath.replace(/\//g, '\\/')}['"]`, 'g')
      ]
      
      importPatterns.forEach(pattern => {
        const matches = content.match(pattern)
        if (matches) {
          analysis.hasOldImports = true
          if (!analysis.modulesFound.includes(moduleName)) {
            analysis.modulesFound.push(moduleName)
          }
          analysis.importStatements.push(...matches)
        }
      })

      // 检查 SUCCESS/ERROR 调用
      config.successCalls.forEach(call => {
        if (content.includes(call)) {
          analysis.successErrorCalls.push({ module: moduleName, call, type: 'success' })
          analysis.needsApiMessages = true
        }
      })
      
      config.errorCalls.forEach(call => {
        if (content.includes(call)) {
          analysis.successErrorCalls.push({ module: moduleName, call, type: 'error' })
          analysis.needsApiMessages = true
        }
      })

      // 检查 PROMPT 调用
      const promptPatterns = [
        `${config.importName}.prompt.`,
        `${moduleName}PromptMessages.`
      ]
      promptPatterns.forEach(pattern => {
        if (content.includes(pattern)) {
          analysis.promptCalls.push({ module: moduleName, pattern })
          analysis.needsPromptMessages = true
        }
      })
    })

    return analysis
  }

  // 更新单个文件
  updateFile(filePath) {
    const relativePath = path.relative(this.srcDir, filePath)
    console.log(`📝 处理文件: ${relativePath}`)
    
    let content = fs.readFileSync(filePath, 'utf8')
    const originalContent = content
    const analysis = this.analyzeFile(filePath)
    
    const fileResult = {
      path: relativePath,
      modulesFound: analysis.modulesFound,
      importsRemoved: [],
      importsAdded: [],
      callsMarked: []
    }

    if (!analysis.hasOldImports && analysis.successErrorCalls.length === 0) {
      console.log(`⏭️  跳过: ${relativePath} (无需更改)`)
      return
    }

    // 1. 移除旧的导入语句
    analysis.modulesFound.forEach(moduleName => {
      const config = this.modules[moduleName]
      
      // 移除各种格式的导入
      const removePatterns = [
        // import xxxMessages from '@/utils/xxxMessages'
        new RegExp(`import\\s+${config.importName}\\s+from\\s+['"]${config.importPath.replace(/\//g, '\\/')}['"];?\\n?`, 'g'),
        // import { xxxSuccessMessages, xxxErrorMessages, xxxPromptMessages } from '@/utils/xxxMessages'
        new RegExp(`import\\s*{[^}]*}\\s*from\\s+['"]${config.importPath.replace(/\//g, '\\/')}['"];?\\n?`, 'g')
      ]
      
      removePatterns.forEach(pattern => {
        const matches = content.match(pattern)
        if (matches) {
          matches.forEach(match => {
            fileResult.importsRemoved.push(match.trim())
          })
          content = content.replace(pattern, '')
        }
      })
      
      this.results.moduleStats[moduleName].files++
    })

    // 2. 添加新的导入语句
    const newImports = []
    
    if (analysis.needsApiMessages) {
      // 检查是否已有 apiMessages 导入
      if (!content.includes("from '@/utils/apiMessages'") && !content.includes('from "@/utils/apiMessages"')) {
        newImports.push("import { showByCode, isSuccess } from '@/utils/apiMessages'")
      }
    }
    
    if (analysis.needsPromptMessages) {
      // 收集需要的 PromptMessages
      const promptImports = []
      analysis.modulesFound.forEach(moduleName => {
        const config = this.modules[moduleName]
        if (analysis.promptCalls.some(c => c.module === moduleName)) {
          promptImports.push(config.promptName)
        }
      })
      
      if (promptImports.length > 0) {
        // 检查是否已有 promptMessages 导入
        if (!content.includes("from '@/utils/promptMessages'") && !content.includes('from "@/utils/promptMessages"')) {
          newImports.push(`import { ${promptImports.join(', ')} } from '@/utils/promptMessages'`)
        }
      }
    }

    // 插入新导入
    if (newImports.length > 0) {
      // 找到最后一个 import 语句的位置
      const importRegex = /^import\s+.*from\s+['"][^'"]+['"];?\s*$/gm
      let lastImportEnd = 0
      let match
      while ((match = importRegex.exec(content)) !== null) {
        lastImportEnd = match.index + match[0].length
      }
      
      if (lastImportEnd > 0) {
        content = content.slice(0, lastImportEnd) + '\n' + newImports.join('\n') + content.slice(lastImportEnd)
      } else {
        // 如果没有其他导入，在 <script> 标签后添加
        const scriptMatch = content.match(/<script[^>]*>\n?/)
        if (scriptMatch) {
          const insertPos = scriptMatch.index + scriptMatch[0].length
          content = content.slice(0, insertPos) + newImports.join('\n') + '\n' + content.slice(insertPos)
        } else {
          // 纯 JS 文件，在开头添加
          content = newImports.join('\n') + '\n' + content
        }
      }
      
      fileResult.importsAdded = newImports
      this.results.importsUpdated += newImports.length
    }

    // 3. 标记 SUCCESS/ERROR 调用为 TODO
    analysis.successErrorCalls.forEach(({ module, call, type }) => {
      // 避免重复标记
      if (content.includes(`// TODO: [${module}]`) && content.includes(call)) {
        return
      }
      
      // 使用正则匹配调用并添加 TODO 注释
      const escapedCall = call.replace(/\./g, '\\.')
      const callRegex = new RegExp(`([ \\t]*)(${escapedCall}\\([^)]*\\))`, 'g')
      
      if (callRegex.test(content)) {
        content = content.replace(callRegex, (match, indent, callExpr) => {
          fileResult.callsMarked.push({ module, call, type })
          this.results.callsMarked++
          this.results.moduleStats[module].calls++
          return `${indent}// TODO: [${module}] 迁移到 showByCode(response.code) - ${type}\n${indent}${callExpr}`
        })
      }
    })

    // 4. 清理多余空行
    content = content.replace(/\n{3,}/g, '\n\n')

    // 5. 保存文件
    if (content !== originalContent) {
      fs.writeFileSync(filePath, content, 'utf8')
      this.results.filesProcessed++
      this.results.fileDetails.push(fileResult)
      
      console.log(`✅ 已更新: ${relativePath}`)
      if (fileResult.importsRemoved.length > 0) {
        console.log(`   - 移除导入: ${fileResult.importsRemoved.length}`)
      }
      if (fileResult.importsAdded.length > 0) {
        console.log(`   - 添加导入: ${fileResult.importsAdded.length}`)
      }
      if (fileResult.callsMarked.length > 0) {
        console.log(`   - 标记调用: ${fileResult.callsMarked.length}`)
      }
    } else {
      console.log(`⏭️  跳过: ${relativePath} (无实际更改)`)
    }
  }


  // 生成迁移报告
  generateReport() {
    const reportPath = path.join(__dirname, 'all-message-migration-report.md')
    
    // 按模块统计
    const moduleReports = Object.keys(this.results.moduleStats)
      .filter(m => this.results.moduleStats[m].files > 0 || this.results.moduleStats[m].calls > 0)
      .map(m => {
        const stats = this.results.moduleStats[m]
        return `| ${m} | ${stats.files} | ${stats.calls} |`
      })
      .join('\n')

    // 文件详情
    const fileDetails = this.results.fileDetails.map(f => {
      let detail = `### ${f.path}\n`
      if (f.modulesFound.length > 0) {
        detail += `- 涉及模块: ${f.modulesFound.join(', ')}\n`
      }
      if (f.importsRemoved.length > 0) {
        detail += `- 移除导入: ${f.importsRemoved.length} 个\n`
      }
      if (f.importsAdded.length > 0) {
        detail += `- 添加导入:\n${f.importsAdded.map(i => `  - \`${i}\``).join('\n')}\n`
      }
      if (f.callsMarked.length > 0) {
        detail += `- 标记调用 (需手动迁移):\n`
        f.callsMarked.forEach(c => {
          detail += `  - \`${c.call}\` (${c.type})\n`
        })
      }
      return detail
    }).join('\n')

    const report = `# 全项目消息迁移报告

## 迁移统计

- 📁 处理文件数: ${this.results.filesProcessed}
- 📦 更新导入数: ${this.results.importsUpdated}  
- 🔄 标记调用数: ${this.results.callsMarked}
- ❌ 错误数量: ${this.results.errors.length}

## 模块统计

| 模块 | 涉及文件 | 标记调用 |
|------|----------|----------|
${moduleReports || '| - | - | - |'}

## 后续手动工作

需要手动处理标记为 \`// TODO: [模块名] 迁移到 showByCode(response.code)\` 的代码。

### 迁移模式

\`\`\`javascript
// 旧方式
userMessages.success.showLoginSuccess()
orderMessages.error.showOrderCreateFailed()

// 新方式 - 统一使用 showByCode
const response = await api.someAction()
showByCode(response.code) // 自动根据 code 显示对应消息

// 或者需要判断成功/失败时
if (isSuccess(response.code)) {
  // 成功逻辑
  showByCode(response.code)
} else {
  // 失败逻辑
  showByCode(response.code)
}
\`\`\`

### 状态码参考

参考 \`docs/code-message-mapping.md\` 中的完整状态码映射：

**用户模块 (2xxx)**
- 2000: 登录成功 | 2100: 登录失败
- 2001: 注册成功 | 2101: 注册失败
- 2010: 资料更新成功 | 2110: 资料更新失败

**茶叶模块 (3xxx)**
- 3000: 茶叶创建成功 | 3100: 茶叶创建失败
- 3001: 茶叶更新成功 | 3101: 茶叶更新失败

**订单模块 (4xxx)**
- 4000: 订单创建成功 | 4100: 订单创建失败
- 4001: 订单取消成功 | 4101: 订单取消失败

**店铺模块 (5xxx)**
- 5000: 店铺创建成功 | 5100: 店铺创建失败

**论坛模块 (6xxx)**
- 6000: 帖子发布成功 | 6100: 帖子发布失败

**消息模块 (7xxx)**
- 7000: 消息发送成功 | 7100: 消息发送失败

## 文件详情

${fileDetails || '无文件被处理'}

## 错误日志

${this.results.errors.length > 0 ? this.results.errors.map(e => `- ${e}`).join('\n') : '无错误'}

---
生成时间: ${new Date().toLocaleString('zh-CN')}
`

    fs.writeFileSync(reportPath, report, 'utf8')
    console.log(`📊 迁移报告已生成: ${reportPath}`)
  }

  // 执行迁移
  async migrate() {
    console.log('🚀 开始全项目消息迁移...\n')
    console.log('支持模块: user, order, tea, shop, forum, message, common\n')

    try {
      const files = this.findAllMessageFiles()
      
      if (files.length === 0) {
        console.log('✅ 没有找到需要迁移的文件')
        return
      }

      console.log('\n📋 开始处理文件...\n')
      for (const file of files) {
        try {
          this.updateFile(file)
        } catch (error) {
          console.error(`❌ 处理文件失败: ${file}`)
          console.error(`   ${error.message}`)
          this.results.errors.push(`${file}: ${error.message}`)
        }
      }

      console.log('\n📊 生成迁移报告...')
      this.generateReport()

      console.log('\n' + '='.repeat(50))
      console.log('🎉 全项目消息迁移完成!')
      console.log('='.repeat(50))
      console.log(`📁 处理了 ${this.results.filesProcessed} 个文件`)
      console.log(`📦 更新了 ${this.results.importsUpdated} 个导入`)
      console.log(`🔄 标记了 ${this.results.callsMarked} 个调用需要手动迁移`)
      
      if (this.results.errors.length > 0) {
        console.log(`❌ 遇到 ${this.results.errors.length} 个错误，请查看报告`)
      }

      console.log('\n📝 下一步：')
      console.log('1. 查看 scripts/all-message-migration-report.md')
      console.log('2. 搜索 "TODO: [" 找到需要手动迁移的代码')
      console.log('3. 将旧的消息调用替换为 showByCode(response.code)')
      console.log('4. 测试迁移后的功能')

    } catch (error) {
      console.error('❌ 迁移过程中发生错误:', error.message)
      this.results.errors.push(`Migration error: ${error.message}`)
    }
  }
}

// 执行迁移
if (require.main === module) {
  const migrator = new AllMessageMigrator()
  migrator.migrate().catch(console.error)
}

module.exports = AllMessageMigrator
