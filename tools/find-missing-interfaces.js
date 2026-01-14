const fs = require('fs')
const path = require('path')

// 读取文件并提取所有接口（路径+方法）
function extractInterfaces(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8')
  const lines = content.split('\n')
  
  const interfaces = []
  let currentPath = ''
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 检测路径定义
    const pathMatch = line.match(/^\s+(\/[^:]+):\s*$/)
    if (pathMatch) {
      currentPath = pathMatch[1]
      continue
    }
    
    // 检测HTTP方法
    const methodMatch = line.match(/^\s+(get|post|put|delete):\s*$/i)
    if (methodMatch && currentPath) {
      const method = methodMatch[1].toLowerCase()
      interfaces.push({
        path: currentPath,
        method: method,
        line: i + 1
      })
    }
  }
  
  return interfaces
}

// 主函数
function main() {
  const backup3Path = path.join(__dirname, '../openapi.yaml.backup3')
  const newPath = path.join(__dirname, '../openapi_new.yaml')
  
  const backup3Interfaces = extractInterfaces(backup3Path)
  const newInterfaces = extractInterfaces(newPath)
  
  // 创建接口标识符（路径+方法）
  const backup3Set = new Set(backup3Interfaces.map(i => `${i.method.toUpperCase()} ${i.path}`))
  const newSet = new Set(newInterfaces.map(i => `${i.method.toUpperCase()} ${i.path}`))
  
  // 找出缺少的接口
  const missing = []
  for (const interface of backup3Interfaces) {
    const key = `${interface.method.toUpperCase()} ${interface.path}`
    if (!newSet.has(key)) {
      missing.push(interface)
    }
  }
  
  console.log(`=== 接口对比结果 ===\n`)
  console.log(`原始文档 (openapi.yaml.backup3): ${backup3Interfaces.length} 个接口`)
  console.log(`最新文档 (openapi_new.yaml): ${newInterfaces.length} 个接口`)
  console.log(`缺少的接口: ${missing.length} 个\n`)
  
  if (missing.length > 0) {
    console.log('缺少的接口列表：\n')
    missing.forEach((iface, index) => {
      console.log(`${index + 1}. ${iface.method.toUpperCase()} ${iface.path} (行号: ${iface.line})`)
    })
  }
  
  // 按模块分组显示
  const moduleGroups = {}
  missing.forEach(iface => {
    let module = '其他'
    if (iface.path.startsWith('/user')) module = '用户模块'
    else if (iface.path.startsWith('/tea')) module = '茶叶模块'
    else if (iface.path.startsWith('/shop')) module = '店铺模块'
    else if (iface.path.startsWith('/order')) module = '订单模块'
    else if (iface.path.startsWith('/forum')) module = '论坛模块'
    else if (iface.path.startsWith('/message')) module = '消息模块'
    else if (iface.path.startsWith('/upload')) module = '公共模块'
    
    if (!moduleGroups[module]) {
      moduleGroups[module] = []
    }
    moduleGroups[module].push(iface)
  })
  
  console.log('\n=== 按模块分组 ===\n')
  for (const [module, interfaces] of Object.entries(moduleGroups)) {
    console.log(`${module}: ${interfaces.length} 个`)
    interfaces.forEach(iface => {
      console.log(`  - ${iface.method.toUpperCase()} ${iface.path}`)
    })
  }
}

main()

