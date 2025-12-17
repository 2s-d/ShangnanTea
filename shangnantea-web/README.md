# 商南茶文化平台

商南茶文化推广与销售平台是一个集茶叶销售、茶文化推广、商家入驻、社区交流为一体的综合性平台。

## 项目重构更新

### 模块重构与问题修复 (v1.2.1)

1. **认证系统模块统一**
   - 移除了重复的认证API调用
   - 统一使用`auth.js`模块处理登录、注册和退出登录
   - 保留`user.js`模块用于用户信息管理

2. **购物车模块修复**
   - 修复了购物车功能从`user`模块迁移到专门的`cart`模块的问题
   - 更新了`CartPage.vue`使用正确的Vuex模块
   - 确保购物车相关功能使用专用API

3. **茶叶模块购物车功能修复**
   - 修复了`TeaDetailPage.vue`和`TeaCard.vue`中错误调用`user/addToCart`的问题
   - 更新为使用`cart/addToCart`，保持模块职责清晰
   - 调整传递给购物车模块的参数格式以符合API预期
   - 确保购物车操作在整个应用中保持一致

4. **Vuex 初始化兼容性修复**
   - 将 Vuex 初始化方式从 Vue 2 风格更新为 Vue 3 风格
   - 用 `createStore` 替代 `Vue.use(Vuex)` 和 `new Vuex.Store`
   - 修复了运行时的 `Cannot read properties of undefined (reading 'use')` 错误
   - 确保了与项目中使用的 Vue 3 框架的兼容性

### Vue 2 到 Vue 3 迁移进展 (v1.2.2)

本项目正在逐步从 Vue 2 风格代码迁移到 Vue 3 的组合式 API 风格，目标是充分利用 Vue 3 的新特性，并保持代码风格的一致性。

#### 已完成的迁移工作

1. **核心框架迁移**
   - 更新了 Vuex 初始化方式（从 `Vue.use(Vuex)` 到 `createStore`）
   - 更新了应用入口初始化方式（使用 `createApp`）
   - 确保路由使用 Vue 3 的 API（`createRouter` 和 `createWebHistory`）

2. **组件 API 迁移**
   - 将 `App.vue` 从选项式 API 更新为组合式 API（使用 `setup()` 函数）
   - 更新了 `ShopDetailPage.vue` 从选项式 API 到组合式 API
   - 替换了对 `this.$route` 的依赖为 `useRoute()` 钩子
   - 替换了 Vuex 的 `mapState` 和 `mapActions` 为 `useStore()` 钩子
   - 已迁移组件清单：
     - `CartPage.vue`：购物车页面
     - `ProfilePage.vue`：用户资料页面
     - `HomePage.vue`：首页
     - `OrderPayment.vue`：订单支付页面
     - `UserManagePage.vue`：用户管理页面
     - `MessagePage.vue`：消息中心页面
     - `OrdersPage.vue`：订单列表页面
     - `OrderManagePage.vue`：订单管理页面
     - `ForumListPage.vue`：论坛列表页面
     - `ForumManagePage.vue`：论坛管理页面
     - `403.vue`：权限错误页面
     - `ShopCard.vue`：店铺卡片组件
     - `ShopManagePage.vue`：店铺管理页面
     - `TeaListPage.vue`：茶叶列表页面
     - `TeaManagePage.vue`：茶叶管理页面
     - `404.vue`：404错误页面
     - `TeaCard.vue`：茶叶卡片组件
     - `ChangePasswordPage.vue`：修改密码页面
     - `ImageUploader.vue`：图片上传组件
     - `Footer.vue`：页脚组件
   - 已确认使用组合式 API 的组件：
     - `LoginPage.vue`：已经使用组合式 API
     - `TeaDetailPage.vue`：已经使用组合式 API
     - `NavBar.vue`：已经使用组合式 API

3. **生命周期钩子检查**
   - 检查并确认项目中没有使用 Vue 2 特有的生命周期钩子（如 `beforeDestroy` 和 `destroyed`）

4. **模板引用更新**
   - 检查并更新模板引用（从 `this.$refs` 到 `ref` 变量）
   - 确保所有模板引用都通过 `setup()` 返回
   - ✅ 已更新 `ChangePasswordPage` 和 `ImageUploader` 组件中的模板引用
   - ✅ 所有已发现的 `this.$refs` 引用都已迁移到组合式 API 的 `ref` 变量
   - 任务进度：100% 完成

5. **全局属性和方法更新**
   - 检查并更新对 `Vue.prototype` 的依赖
   - 使用 `app.config.globalProperties` 或提供组合式函数替代
   - ✅ 已将 `Footer.vue` 中的 `this.$message` 更新为 `ElMessage`
   - ✅ 未发现项目中使用 `Vue.prototype` 的情况
   - ✅ 已检查所有组件中的全局属性使用情况
   - 任务进度：100% 完成

6. **性能优化**
   - 利用 Vue 3 的 `watchEffect` 和 `computed` 函数简化依赖跟踪
   - 使用 `shallowRef` 或 `markRaw` 优化大型不可变对象的性能
   - ✅ 已将 `TeaListPage.vue` 中的多个 `watch` 合并为单个 `watchEffect`，简化依赖跟踪
   - ✅ 已优化 `ImageUploader.vue` 中的 `watch`，改为使用 `watchEffect`
   - ✅ 在 `TeaDetailPage.vue` 中使用 `shallowRef` 优化大型不可变对象的性能
   - ✅ 将所有深度嵌套的 `watch` 替换为更高效的 `watchEffect`
   - 任务进度：100% 完成

7. **API 风格统一**
   - 统一代码风格，采用 Vue 3 推荐的组合式 API 模式
   - 提供项目特定的组合式函数（Composables）代替混入（Mixins）
   - 编写迁移指南，帮助团队成员理解和采用新的编码风格
   - 已创建的组合式函数：
     - `useFormValidation`：表单验证相关功能
     - `useCart`：购物车操作相关功能
     - `useAuth`：用户认证相关功能
     - `useImageUpload`：图片上传相关功能
     - `usePagination`：分页和筛选相关功能
     - `useStorage`：本地存储相关功能
   - 使用组合式函数重构的组件：
     - `TeaDetailPage.vue`：使用`useCart`组合式函数
     - `ChangePasswordPage.vue`：使用`useFormValidation`和`useAuth`组合式函数
     - `ImageUploader.vue`：使用`useImageUpload`组合式函数
     - `Footer.vue`：使用`useStorage`组合式函数管理邮箱订阅
   - 组合式函数的主要优势：
     - 提高代码复用性，避免重复逻辑
     - 更好的类型推导和代码补全
     - 更清晰的关注点分离
     - 更易于测试和维护
   - 任务进度：70% 完成

#### 迁移注意事项

1. **兼容性检查**
   - 迁移后测试所有功能，确保行为一致性
   - 特别关注用户认证、权限控制和表单处理
   - 验证组件生命周期和数据响应性

2. **性能监控**
   - 监控组件重新渲染次数，减少不必要的渲染
   - 利用 Vue 3 的 Suspense 和异步组件改善加载体验
   - 考虑使用 `defineAsyncComponent` 替代 Vue 2 的异步组件

3. **代码质量**
   - 使用 ESLint 规则确保代码质量
   - 添加适当的 TypeScript 类型注解增强可维护性
   - 保持一致的代码风格和命名规范

### 认证系统重构 (v1.2.0)

认证系统已进行全面重构，主要改进包括：

1. **Token管理优化**
   - 采用JWT风格的token结构
   - 从token中解析用户信息，减少API调用
   - 实现token自动刷新机制
   - 统一token存储和管理

2. **权限系统增强**
   - 优化权限检查工具类
   - 添加权限指令，简化组件级权限控制
   - 基于RBAC的路由访问控制
   - 增强API请求和响应拦截器

3. **数据迁移与版本管理**
   - 实现数据版本检查机制
   - 自动进行数据结构迁移
   - 提供开发者工具进行手动迁移
   - 管理本地存储数据向下兼容

4. **API模块化**
   - 拆分认证和用户信息API
   - 统一API规范和错误处理
   - 优化API层与Vuex状态管理的交互

## 项目结构

```
shangnantea-web/
├── public/              # 静态资源
├── src/
│   ├── api/             # API接口
│   ├── assets/          # 项目资源
│   ├── components/      # 公共组件
│   ├── directives/      # 自定义指令
│   ├── router/          # 路由配置
│   ├── store/           # Vuex状态管理
│   ├── utils/           # 工具类
│   ├── views/           # 页面组件
│   ├── App.vue          # 根组件
│   └── main.js          # 入口文件
├── .env.*               # 环境变量配置
├── package.json         # 项目依赖
└── README.md            # 项目说明
```

## 技术栈

- Vue 3
- Vuex 4
- Vue Router 4
- Element Plus
- Axios

## 开发指南

### 安装依赖

```bash
npm install
```

### 开发环境启动

```bash
npm run serve
```

### 生产环境构建

```bash
npm run build
```

## 认证系统使用指南

### 1. 在组件中使用权限控制

```vue
<template>
  <!-- 所有已登录用户可见 -->
  <div v-permission>需要登录才能看到</div>
  
  <!-- 只有管理员可见 -->
  <div v-permission="{ roles: [1] }">只有管理员可见</div>
  
  <!-- 管理员和商家可见 -->
  <div v-permission="{ roles: [1, 3] }">管理员和商家可见</div>
  
  <!-- 使用自定义判断 -->
  <div v-permission="{ custom: () => canEditProduct }">编辑按钮</div>
</template>
```

### 2. 在代码中进行权限检查

```javascript
import permission from '@/utils/permission'

// 检查是否登录
if (permission.isLoggedIn()) {
  // 已登录的逻辑
}

// 检查角色
if (permission.isAdmin()) {
  // 管理员逻辑
}

// 高级权限检查
if (permission.canAccess({ roles: [1, 3], custom: myCustomCheck })) {
  // 有权限的逻辑
}
```

### 3. 路由权限控制

在路由配置中添加 `meta` 属性：

```javascript
{
  path: '/admin/users',
  component: AdminUserPage,
  meta: {
    requireAuth: true,
    roles: [1] // 只允许管理员访问
  }
}
```

## 更多文档

更多详细文档请参考 `/docs` 目录：

- [组合式API迁移指南](/docs/composition-api-migration.md) - Vue 3组合式API的详细使用指南与迁移策略
- [组合式API迁移案例与实践](/docs/composition-api-migration-examples.md) - 实际组件迁移案例与高级特性用法
- [测试指南](/docs/testing-guide.md) - 项目测试规范与流程

## 命令行运行

### 启动开发服务器

```bash
cd shangnantea-web
npm install  # 首次运行或依赖更新时
npm run serve
```

### 构建生产版本

```bash
cd shangnantea-web
npm run build
```

### 运行测试

```bash
cd shangnantea-web
npm run test:unit  # 运行单元测试
npm run lint       # 运行代码检查
``` 