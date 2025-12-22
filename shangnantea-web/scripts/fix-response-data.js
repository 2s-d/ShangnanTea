/**
 * 修复 store 文件中的 res.data 问题
 * 
 * 问题：响应拦截器在 code===200 时已经返回 res.data，
 * 但 store 里又取了一次 .data，导致数据丢失
 * 
 * 解决：简单地把 res.data 改成 res
 * 
 * 运行: node scripts/fix-response-data.js
 */

const fs = require('fs')
const path = require('path')

const storeDir = path.join(__dirname, '../src/store/modules')
const files = ['order.js', 'tea.js', 'user.js', 'shop.js', 'forum.js', 'message.js']

let totalChanges = 0

files.forEach(file => {
  const filePath = path.join(storeDir, file)
  
  if (!fs.existsSync(filePath)) {
    console.log(`跳过: ${file} (文件不存在)`)
    return
  }
  
  let content = fs.readFileSync(filePath, 'utf-8')
  const originalContent = content
  
  // 统计修改次数
  const countBefore = (content.match(/res\.data/g) || []).length
  const countCodeCheck = (content.match(/res\.code === 200/g) || []).length
  
  // 1. res.data 改成 res（但要排除 res.data?.xxx 这种情况先单独处理）
  content = content.replace(/res\.data\?\./g, 'res?.')
  content = content.replace(/res\.data/g, 'res')
  
  // 2. res.code === 200 && res 这种判断改成 if (res)
  content = content.replace(/if \(res\.code === 200 && res\)/g, 'if (res)')
  content = content.replace(/res\.code === 200 && res/g, 'res')
  
  const countAfter = (content.match(/res\.data/g) || []).length
  const changes = countBefore - countAfter + countCodeCheck
  
  if (content !== originalContent) {
    fs.writeFileSync(filePath, content, 'utf-8')
    console.log(`✅ ${file}: ${changes} 处修改`)
    totalChanges += changes
  } else {
    console.log(`⏭️ ${file}: 无需修改`)
  }
})

console.log(`\n总计修改: ${totalChanges} 处`)
console.log('\n⚠️ 请手动检查以下情况:')
console.log('1. 分页数据 res.list / res.total 可能需要调整')
console.log('2. 某些 API 可能返回对象而非数组，需要确认')
