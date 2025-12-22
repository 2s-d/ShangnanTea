/**
 * 清除废弃的"开发UI时注释"代码块
 * 
 * 用法: node scripts/clean-deprecated-comments.js
 */

const fs = require('fs')
const path = require('path')

// 需要处理的文件列表
const filesToClean = [
  'src/views/forum/list/ForumListPage.vue',
  'src/views/forum/detail/ForumDetailPage.vue',
  'src/views/forum/manage/ForumManagePage.vue',
  'src/views/order/detail/OrderDetailPage.vue',
  'src/views/order/cart/CartPage.vue'
]

// 匹配废弃注释块的正则表达式
// 匹配 /* ... 真实代码...开发UI时注释 ... */ 格式的注释块
const deprecatedCommentPattern = /\s*\/\*\s*\n?\s*\/\/\s*真实代码[^*]*开发UI时注释[^]*?\*\/\s*\n?/g

// 也匹配单独的 /* // 真实代码（开发UI时注释）... */ 块
const deprecatedCommentPattern2 = /\s*\/\*\s*\n?\s*\/\/\s*真实代码（开发UI时注释）[^]*?\*\/\s*\n?/g

// 匹配 /* // 真实代码(开发UI时注释) ... */ 块（中文括号）
const deprecatedCommentPattern3 = /\s*\/\*\s*\n?\s*\/\/\s*真实代码\(开发UI时注释\)[^]*?\*\/\s*\n?/g

let totalRemoved = 0
let filesModified = 0

filesToClean.forEach(filePath => {
  const fullPath = path.join(__dirname, '..', filePath)
  
  if (!fs.existsSync(fullPath)) {
    console.log(`⚠️  文件不存在: ${filePath}`)
    return
  }
  
  let content = fs.readFileSync(fullPath, 'utf-8')
  const originalLength = content.length
  
  // 应用所有模式
  let matchCount = 0
  
  // 统计匹配数量
  const matches1 = content.match(deprecatedCommentPattern) || []
  const matches2 = content.match(deprecatedCommentPattern2) || []
  const matches3 = content.match(deprecatedCommentPattern3) || []
  
  matchCount = matches1.length + matches2.length + matches3.length
  
  // 执行替换
  content = content.replace(deprecatedCommentPattern, '\n')
  content = content.replace(deprecatedCommentPattern2, '\n')
  content = content.replace(deprecatedCommentPattern3, '\n')
  
  // 清理多余的空行（超过2个连续空行变成2个）
  content = content.replace(/\n{3,}/g, '\n\n')
  
  if (content.length !== originalLength) {
    fs.writeFileSync(fullPath, content, 'utf-8')
    const removed = originalLength - content.length
    console.log(`✅ ${filePath}`)
    console.log(`   移除了 ${matchCount} 个废弃注释块，减少 ${removed} 字符`)
    totalRemoved += removed
    filesModified++
  } else {
    console.log(`ℹ️  ${filePath} - 无需修改`)
  }
})

console.log('\n========================================')
console.log(`📊 总计: 修改了 ${filesModified} 个文件，移除了 ${totalRemoved} 字符`)
console.log('========================================')
