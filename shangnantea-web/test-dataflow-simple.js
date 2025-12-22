/**
 * 简单数据流通测试 - 直接调用Vuex store测试API连通性
 * 使用方法：在Vue项目运行时，在浏览器控制台中运行此脚本
 */

// 获取Vue应用实例的store
function getStore() {
  // 方法1：从Vue DevTools获取
  if (window.__VUE_DEVTOOLS_GLOBAL_HOOK__ && window.__VUE_DEVTOOLS_GLOBAL_HOOK__.apps) {
    const app = window.__VUE_DEVTOOLS_GLOBAL_HOOK__.apps[0]
    if (app && app.config.globalProperties.$store) {
      return app.config.globalProperties.$store
    }
  }
  
  // 方法2：从全局变量获取（如果项目有设置）
  if (window.$store) {
    return window.$store
  }
  
  // 方法3：从Vue实例获取
  const app = document.querySelector('#app').__vue_app__
  if (app && app.config.globalProperties.$store) {
    return app.config.globalProperties.$store
  }
  
  throw new Error('无法获取Vuex store，请确保Vue应用已启动')
}

// 测试用户登录数据流
async function testUserLogin() {
  try {
    console.log('🧪 测试用户登录...')
    
    const store = getStore()
    const result = await store.dispatch('user/login', {
      username: 'testuser',
      password: 'test123'
    })
    
    console.log('✅ 登录成功:', result)
    return result
  } catch (error) {
    console.log('❌ 登录失败:', error.message)
    return null
  }
}

// 测试获取用户信息数据流
async function testGetUserInfo() {
  try {
    console.log('🧪 测试获取用户信息...')
    
    const store = getStore()
    const result = await store.dispatch('user/getUserInfo')
    
    console.log('✅ 获取用户信息成功:', result)
    return result
  } catch (error) {
    console.log('❌ 获取用户信息失败:', error.message)
    return null
  }
}

// 测试获取地址列表数据流
async function testGetAddresses() {
  try {
    console.log('🧪 测试获取地址列表...')
    
    const store = getStore()
    const result = await store.dispatch('user/fetchAddresses')
    
    console.log('✅ 获取地址列表成功:', result)
    return result
  } catch (error) {
    console.log('❌ 获取地址列表失败:', error.message)
    return null
  }
}

// 运行所有测试
async function runAllTests() {
  console.log('🚀 开始数据流通测试...')
  
  await testUserLogin()
  await testGetUserInfo()
  await testGetAddresses()
  
  console.log('🏁 测试完成')
}

// 导出测试函数
window.testDataFlow = {
  testUserLogin,
  testGetUserInfo,
  testGetAddresses,
  runAllTests
}

console.log('📋 数据流测试脚本已加载，使用方法：')
console.log('- testDataFlow.testUserLogin() - 测试登录')
console.log('- testDataFlow.testGetUserInfo() - 测试获取用户信息')
console.log('- testDataFlow.testGetAddresses() - 测试获取地址')
console.log('- testDataFlow.runAllTests() - 运行所有测试')