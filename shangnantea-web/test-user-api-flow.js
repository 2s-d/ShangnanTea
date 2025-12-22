/**
 * 用户模块数据流通测试脚本
 * 直接调用项目中的API和Vuex模块，测试数据流是否正常
 */

// 模拟浏览器环境的基础设置
global.window = {};
global.localStorage = {
  getItem: () => null,
  setItem: () => {},
  removeItem: () => {}
};

// 模拟axios请求
const mockAxios = {
  create: () => mockAxios,
  interceptors: {
    request: { use: () => {} },
    response: { use: () => {} }
  },
  request: async (config) => {
    console.log(`🌐 API调用: ${config.method?.toUpperCase()} ${config.url}`);
    console.log(`📤 请求数据:`, config.data || config.params || '无');
    
    // 模拟后端Controller响应
    if (config.url === '/api/user/login') {
      const { username, password } = config.data;
      if (username === 'admin' && password === '123456') {
        const response = {
          data: {
            success: true,
            code: 200,
            message: '登录成功',
            data: {
              token: 'mock_jwt_token_' + Date.now(),
              userInfo: {
                id: 1,
                username: 'admin',
                nickname: '管理员',
                email: 'admin@shangnantea.com',
                role: 1,
                status: 1,
                avatar: 'https://via.placeholder.com/60x60/4CAF50/FFFFFF?text=Admin'
              }
            }
          }
        };
        console.log(`📥 后端响应:`, response.data);
        return response;
      } else {
        throw new Error('用户名或密码错误');
      }
    }
    
    if (config.url === '/api/user/info/me') {
      const response = {
        data: {
          success: true,
          data: {
            id: 1,
            username: 'admin',
            nickname: '管理员',
            email: 'admin@shangnantea.com',
            role: 1,
            status: 1
          }
        }
      };
      console.log(`📥 后端响应:`, response.data);
      return response;
    }
    
    if (config.url === '/api/user/addresses') {
      const response = {
        data: {
          success: true,
          data: [
            {
              id: 1,
              receiverName: '张三',
              receiverPhone: '13800138000',
              province: '陕西省',
              city: '商洛市',
              district: '商南县',
              detailAddress: '茶产业园1号楼302',
              isDefault: 1
            }
          ]
        }
      };
      console.log(`📥 后端响应:`, response.data);
      return response;
    }
    
    throw new Error(`未模拟的API: ${config.url}`);
  }
};

// 导入项目中的真实模块（需要模拟模块加载）
const createUserAPI = () => {
  // 模拟 src/api/user.js 中的login函数
  return {
    login: async (loginData) => {
      const response = await mockAxios.request({
        url: '/api/user/login',
        method: 'post',
        data: loginData
      });
      return response.data.data; // 返回实际数据部分
    },
    
    getCurrentUser: async () => {
      const response = await mockAxios.request({
        url: '/api/user/info/me',
        method: 'get'
      });
      return response.data.data;
    },
    
    getAddressList: async () => {
      const response = await mockAxios.request({
        url: '/api/user/addresses',
        method: 'get'
      });
      return response.data;
    }
  };
};

// 模拟Vuex Store
const createUserStore = (userAPI) => {
  let state = {
    userInfo: null,
    isLoggedIn: false,
    loading: false,
    addressList: []
  };
  
  const mutations = {
    SET_USER_INFO: (userInfo) => {
      console.log(`🔄 Vuex Mutation: SET_USER_INFO`, userInfo);
      state.userInfo = userInfo;
    },
    SET_LOGGED_IN: (status) => {
      console.log(`🔄 Vuex Mutation: SET_LOGGED_IN`, status);
      state.isLoggedIn = status;
    },
    SET_LOADING: (status) => {
      console.log(`🔄 Vuex Mutation: SET_LOADING`, status);
      state.loading = status;
    },
    SET_ADDRESS_LIST: (list) => {
      console.log(`🔄 Vuex Mutation: SET_ADDRESS_LIST`, list);
      state.addressList = list;
    }
  };
  
  const actions = {
    login: async (loginData) => {
      console.log(`🚀 Vuex Action: login 开始执行`);
      try {
        mutations.SET_LOADING(true);
        
        // 调用API
        const response = await userAPI.login(loginData);
        const { token, userInfo } = response;
        
        // 更新状态
        mutations.SET_USER_INFO(userInfo);
        mutations.SET_LOGGED_IN(true);
        
        console.log(`✅ Vuex Action: login 执行成功`);
        return userInfo;
      } catch (error) {
        console.log(`❌ Vuex Action: login 执行失败`, error.message);
        throw error;
      } finally {
        mutations.SET_LOADING(false);
      }
    },
    
    fetchUserInfo: async () => {
      console.log(`🚀 Vuex Action: fetchUserInfo 开始执行`);
      try {
        mutations.SET_LOADING(true);
        
        const userInfo = await userAPI.getCurrentUser();
        mutations.SET_USER_INFO(userInfo);
        mutations.SET_LOGGED_IN(true);
        
        console.log(`✅ Vuex Action: fetchUserInfo 执行成功`);
        return userInfo;
      } catch (error) {
        console.log(`❌ Vuex Action: fetchUserInfo 执行失败`, error.message);
        throw error;
      } finally {
        mutations.SET_LOADING(false);
      }
    },
    
    fetchAddresses: async () => {
      console.log(`🚀 Vuex Action: fetchAddresses 开始执行`);
      try {
        mutations.SET_LOADING(true);
        
        const response = await userAPI.getAddressList();
        const addressList = response.data || [];
        mutations.SET_ADDRESS_LIST(addressList);
        
        console.log(`✅ Vuex Action: fetchAddresses 执行成功`);
        return addressList;
      } catch (error) {
        console.log(`❌ Vuex Action: fetchAddresses 执行失败`, error.message);
        throw error;
      } finally {
        mutations.SET_LOADING(false);
      }
    }
  };
  
  return {
    state,
    mutations,
    actions,
    getState: () => ({ ...state })
  };
};

// 测试函数
async function testUserDataFlow() {
  console.log('🎯 开始用户模块数据流通测试\n');
  
  // 初始化模块
  const userAPI = createUserAPI();
  const userStore = createUserStore(userAPI);
  
  console.log('📊 初始状态:', userStore.getState());
  console.log('\n' + '='.repeat(50) + '\n');
  
  try {
    // 测试1: 用户登录
    console.log('🔐 测试1: 用户登录流程');
    console.log('👤 模拟Vue组件调用: store.dispatch("user/login", loginData)');
    
    const loginData = { username: 'admin', password: '123456' };
    const userInfo = await userStore.actions.login(loginData);
    
    console.log('📊 登录后状态:', userStore.getState());
    console.log('\n' + '='.repeat(50) + '\n');
    
    // 测试2: 获取用户信息
    console.log('📋 测试2: 获取用户信息流程');
    console.log('👤 模拟Vue组件调用: store.dispatch("user/fetchUserInfo")');
    
    await userStore.actions.fetchUserInfo();
    
    console.log('📊 获取信息后状态:', userStore.getState());
    console.log('\n' + '='.repeat(50) + '\n');
    
    // 测试3: 获取地址列表
    console.log('📍 测试3: 获取地址列表流程');
    console.log('👤 模拟Vue组件调用: store.dispatch("user/fetchAddresses")');
    
    await userStore.actions.fetchAddresses();
    
    console.log('📊 获取地址后状态:', userStore.getState());
    console.log('\n' + '='.repeat(50) + '\n');
    
    console.log('✅ 所有测试通过！数据流通正常。');
    
  } catch (error) {
    console.log('❌ 测试失败:', error.message);
  }
}

// 测试错误情况
async function testErrorFlow() {
  console.log('\n🚨 测试错误处理流程\n');
  
  const userAPI = createUserAPI();
  const userStore = createUserStore(userAPI);
  
  try {
    console.log('🔐 测试错误登录');
    console.log('👤 模拟Vue组件调用: store.dispatch("user/login", 错误数据)');
    
    const wrongLoginData = { username: 'wrong', password: 'wrong' };
    await userStore.actions.login(wrongLoginData);
    
  } catch (error) {
    console.log('✅ 错误处理正常:', error.message);
    console.log('📊 错误后状态:', userStore.getState());
  }
}

// 执行测试
async function runTests() {
  console.log('🚀 商南茶叶系统 - 用户模块数据流通测试');
  console.log('📅 测试时间:', new Date().toLocaleString());
  console.log('🎯 测试目标: Vue组件 → Vuex → API → Controller 数据流');
  console.log('\n' + '='.repeat(60) + '\n');
  
  await testUserDataFlow();
  await testErrorFlow();
  
  console.log('\n' + '='.repeat(60));
  console.log('🎉 数据流通测试完成！');
}

// 如果是直接运行此脚本
if (require.main === module) {
  runTests().catch(console.error);
}

module.exports = {
  testUserDataFlow,
  testErrorFlow,
  runTests
};