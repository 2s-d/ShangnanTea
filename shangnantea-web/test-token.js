/**
 * Token 解析测试脚本
 * 用于验证 Mock JWT token 是否能被前端正确解析
 * 
 * 运行方式: node test-token.js
 */

// 模拟前端的 decodeToken 函数
const decodeToken = (tokenStr) => {
  if (!tokenStr) return null;
  
  try {
    const tokenParts = tokenStr.split('.');
    if (tokenParts.length < 2) {
      console.log('❌ Token格式错误：不是有效的JWT格式（需要3部分）');
      return null;
    }
    
    const payload = tokenParts[1];
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      Buffer.from(base64, 'base64').toString('utf8')
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.log('❌ 解析token失败:', error.message);
    return null;
  }
};

// 模拟前端的 verifyToken 函数
const verifyToken = (tokenStr) => {
  if (!tokenStr) {
    console.log('❌ Token为空');
    return null;
  }
  
  try {
    const payload = decodeToken(tokenStr);
    if (!payload) return null;
    
    console.log('\n📦 Token Payload:');
    console.log(JSON.stringify(payload, null, 2));
    
    // 检查 sub 字段
    if (!payload.sub) {
      console.log('\n❌ Token缺少用户ID(sub)字段');
      return null;
    }
    console.log('\n✅ sub字段存在:', payload.sub);
    
    // 检查 role 字段
    if (![1, 2, 3].includes(Number(payload.role))) {
      console.log('❌ Token包含无效的角色值:', payload.role);
      return null;
    }
    console.log('✅ role字段有效:', payload.role);
    
    // 检查 exp 字段
    const now = Math.floor(Date.now() / 1000);
    if (payload.exp && payload.exp < now) {
      console.log('❌ Token已过期');
      console.log('   当前时间:', new Date(now * 1000).toISOString());
      console.log('   过期时间:', new Date(payload.exp * 1000).toISOString());
      return null;
    }
    if (payload.exp) {
      console.log('✅ Token未过期，过期时间:', new Date(payload.exp * 1000).toISOString());
    }
    
    // 构建用户信息
    const userInfo = {
      id: payload.sub,
      role: Number(payload.role),
      username: payload.username || payload.preferred_username,
      exp: payload.exp ? payload.exp * 1000 : undefined
    };
    
    console.log('\n🎉 验证成功！用户信息:');
    console.log(JSON.stringify(userInfo, null, 2));
    
    return userInfo;
  } catch (error) {
    console.log('❌ 验证token失败:', error.message);
    return null;
  }
};

// 测试用的 Mock Token（我之前提供的）
const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjeTEwMDAwMiIsInJvbGUiOjIsInVzZXJuYW1lIjoidXNlciIsImp0aSI6Im1vY2stdXVpZCIsImlhdCI6MTczNTA4NDgwMCwiZXhwIjoxNzY3MjI1NjAwLCJpc3MiOiJzaGFuZ25hbnRlYSJ9.mock_signature';

console.log('='.repeat(60));
console.log('Token 解析测试');
console.log('='.repeat(60));
console.log('\n📝 测试Token:');
console.log(mockToken.substring(0, 50) + '...');

verifyToken(mockToken);

console.log('\n' + '='.repeat(60));
console.log('如果你在 Apifox 中配置的 token 不同，请替换上面的 mockToken 变量');
console.log('='.repeat(60));
