// 直接在浏览器控制台运行，测试Vuex数据流
// 前提：项目已经运行在 http://localhost:8080

// 测试用户登录
async function testUserLogin() {
  try {
    console.log('🔐 测试用户登录...');
    const result = await this.$store.dispatch('user/login', {
      username: 'admin',
      password: '123456'
    });
    console.log('✅ 登录成功:', result);
    console.log('📊 用户状态:', this.$store.state.user);
  } catch (error) {
    console.log('❌ 登录失败:', error.message);
  }
}

// 测试获取地址
async function testGetAddresses() {
  try {
    console.log('📍 测试获取地址...');
    const result = await this.$store.dispatch('user/fetchAddresses');
    console.log('✅ 获取成功:', result);
    console.log('📊 地址状态:', this.$store.state.user.addressList);
  } catch (error) {
    console.log('❌ 获取失败:', error.message);
  }
}

// 测试获取论坛帖子
async function testGetPosts() {
  try {
    console.log('📝 测试获取帖子...');
    const result = await this.$store.dispatch('forum/fetchForumPosts');
    console.log('✅ 获取成功:', result);
    console.log('📊 帖子状态:', this.$store.state.forum.forumPosts);
  } catch (error) {
    console.log('❌ 获取失败:', error.message);
  }
}

console.log('复制以下代码到浏览器控制台运行：');
console.log('testUserLogin.call(app)');
console.log('testGetAddresses.call(app)');
console.log('testGetPosts.call(app)');