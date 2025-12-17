# 商南茶项目 Vue3 组合式API迁移指南

## 目录

1. [简介](#简介)
2. [组合式API基础](#组合式API基础)
3. [渐进式迁移策略](#渐进式迁移策略)
4. [已创建的组合式函数](#已创建的组合式函数)
5. [迁移最佳实践](#迁移最佳实践)
6. [混合模式开发](#混合模式开发)

## 简介

本指南旨在帮助开发团队将商南茶项目从Vue 2的选项式API（Options API）逐步迁移到Vue 3的组合式API（Composition API）。我们采用渐进式迁移策略，允许选项式API和组合式API在项目中共存，以确保平稳过渡。

## 组合式API基础

### 什么是组合式API

组合式API是Vue 3引入的新特性，它提供了一种更灵活的方式来组织组件逻辑。与选项式API按选项类型（data、methods、computed等）组织代码不同，组合式API允许我们按逻辑关注点组织代码。

### 核心概念

- **setup函数**：组件的入口点，在组件创建之前执行
- **响应式API**：ref、reactive、computed、watch
- **生命周期钩子**：onMounted、onUpdated、onUnmounted等
- **提供/注入**：provide、inject

### 基本结构

```vue
<script>
import { ref, computed, onMounted } from 'vue'

export default {
  setup() {
    // 状态
    const count = ref(0)
    
    // 计算属性
    const doubleCount = computed(() => count.value * 2)
    
    // 方法
    const increment = () => {
      count.value++
    }
    
    // 生命周期
    onMounted(() => {
      console.log('组件已挂载')
    })
    
    // 返回给模板使用的内容
    return {
      count,
      doubleCount,
      increment
    }
  }
}
</script>
```

## 渐进式迁移策略

我们采用以下四阶段的渐进式迁移策略：

### 第一阶段：基础设施和核心组件

- 创建基础组合式函数（useAuth, useCart等）
- 迁移核心框架组件（App.vue等）
- 将共享逻辑提取为组合式函数

### 第二阶段：高价值组件

- 优先迁移经常修改的组件
- 迁移用户交互频繁的组件（登录、购物流程等）

### 第三阶段：新组件开发标准

- 所有新组件使用组合式API开发
- 为团队成员提供培训和文档

### 第四阶段：渐进完成迁移

- 在常规维护中逐步迁移剩余组件
- 低优先级组件可保持选项式API

## 已创建的组合式函数

### useFormValidation

表单验证组合式函数，提供常用的表单验证规则和方法。

**主要功能**：
- 常用验证规则（必填、邮箱、手机号、密码强度等）
- 表单验证方法
- 表单重置功能

**使用示例**：

```js
import { useFormValidation } from '@/composables/useFormValidation'

export default {
  setup() {
    const { rules, validateForm, resetForm } = useFormValidation()
    const formRef = ref(null)
    
    const formRules = {
      username: [rules.required('请输入用户名')],
      email: [rules.required('请输入邮箱'), rules.email()]
    }
    
    const submitForm = () => {
      validateForm(formRef.value, () => {
        // 表单验证通过后的处理逻辑
      })
    }
    
    return { formRules, submitForm, formRef, resetForm }
  }
}
```

### useCart

购物车功能组合式函数，用于管理购物车操作。

**主要功能**：
- 添加商品到购物车
- 更新购物车商品数量
- 移除购物车商品
- 清空购物车
- 获取购物车商品列表

**使用示例**：

```js
import { useCart } from '@/composables/useCart'

export default {
  setup() {
    const { 
      cartItems, 
      loading, 
      addToCart, 
      updateCartItem, 
      removeFromCart 
    } = useCart()
    
    return { 
      cartItems, 
      loading, 
      addToCart, 
      updateCartItem, 
      removeFromCart 
    }
  }
}
```

### useAuth

用户认证组合式函数，管理用户登录、注册和权限。

**主要功能**：
- 用户登录/注册
- 注销登录
- 用户信息更新
- 修改密码
- 权限检查

**使用示例**：

```js
import { useAuth } from '@/composables/useAuth'

export default {
  setup() {
    const { 
      userInfo, 
      isLoggedIn, 
      login, 
      logout, 
      register, 
      hasRole 
    } = useAuth()
    
    // 检查权限
    const isAdmin = computed(() => hasRole(1))
    
    return { 
      userInfo, 
      isLoggedIn, 
      isAdmin, 
      login, 
      logout 
    }
  }
}
```

### useImageUpload

图片上传组合式函数，处理文件上传相关逻辑。

**主要功能**：
- 文件类型与大小验证
- 文件列表管理
- 上传单个或多个文件
- 管理上传状态和结果

**使用示例**：

```js
import { useImageUpload } from '@/composables/useImageUpload'

export default {
  setup() {
    const { 
      fileList, 
      uploading, 
      uploadFile, 
      uploadAll, 
      removeFile, 
      resetFiles 
    } = useImageUpload({
      maxSize: 5, // 5MB
      acceptTypes: ['image/jpeg', 'image/png']
    })
    
    return { 
      fileList, 
      uploading, 
      uploadFile, 
      uploadAll, 
      removeFile, 
      resetFiles 
    }
  }
}
```

### usePagination

分页功能组合式函数，简化分页逻辑处理。

**主要功能**：
- 页码和每页数量管理
- 排序和筛选管理
- 分页状态与路由同步
- 提供分页请求参数

**使用示例**：

```js
import { usePagination } from '@/composables/usePagination'

export default {
  setup() {
    const { 
      currentPage, 
      pageSize, 
      total, 
      handleCurrentChange, 
      handleSizeChange, 
      getRequestParams, 
      setTotal 
    } = usePagination({
      defaultPageSize: 10,
      syncWithRoute: true
    })
    
    const fetchData = async () => {
      const params = getRequestParams()
      const res = await someApi(params)
      setTotal(res.total)
    }
    
    return { 
      currentPage, 
      pageSize, 
      total, 
      handleCurrentChange, 
      handleSizeChange, 
      fetchData 
    }
  }
}
```

### useStorage

本地存储组合式函数，提供响应式的本地存储管理。

**主要功能**：
- localStorage的响应式封装
- 支持前缀和命名空间
- 支持过期时间设置
- 提供对象存储和字段管理

**使用示例**：

```js
import { useStorage, useObjectStorage } from '@/composables/useStorage'

export default {
  setup() {
    // 简单值存储
    const theme = useStorage('theme', 'light')
    
    // 对象存储
    const userPrefs = useObjectStorage('user_preferences', {
      fontSize: 14,
      darkMode: false
    })
    
    // 修改主题
    const toggleTheme = () => {
      theme.value = theme.value === 'light' ? 'dark' : 'light'
    }
    
    // 修改对象的一个字段
    const changeFontSize = (size) => {
      userPrefs.updateField('fontSize', size)
    }
    
    return { 
      theme, 
      userPrefs, 
      toggleTheme, 
      changeFontSize 
    }
  }
}
```

## 迁移最佳实践

### 开始迁移前

1. **理解组件逻辑**：在迁移前，确保完全理解组件的功能和数据流
2. **识别可重用逻辑**：确定哪些逻辑可以提取为组合式函数
3. **创建迁移计划**：按照组件的复杂度和优先级排序迁移顺序

### 迁移过程

1. **渐进式转换**：先保留选项式API结构，添加setup函数
2. **逐步替换**：一次替换一个选项（如先替换data，再替换methods等）
3. **利用现有组合式函数**：优先使用已创建的组合式函数
4. **添加详细注释**：在关键逻辑处添加注释，解释组合式API的用法

### 迁移检查清单

- [ ] 所有响应式数据是否正确声明（ref/reactive）
- [ ] 是否处理了.value的访问（对比选项式API中的直接访问）
- [ ] 生命周期钩子是否正确迁移
- [ ] computed和watch是否正确实现
- [ ] 所有需要在模板中使用的变量和方法是否在setup中返回
- [ ] 组件之间的通信（props、emits）是否正确处理
- [ ] 是否处理了this的引用（组合式API中没有this）

## 混合模式开发

在迁移期间，项目将同时包含选项式API和组合式API组件。以下是在混合模式下保持代码质量的建议：

### 命名约定

- 使用组合式API的组件在文件名后添加`.composition.vue`后缀
- 或者在组件内添加注释标记使用的API风格

### 组合式函数在选项式API中的使用

即使在选项式API组件中，也可以利用组合式函数来复用逻辑：

```js
import { useAuth } from '@/composables/useAuth'

export default {
  data() {
    const { isLoggedIn, userInfo } = useAuth()
    
    return {
      isUserLoggedIn: isLoggedIn.value,
      user: userInfo.value
    }
  },
  methods: {
    async handleLogin() {
      const { login } = useAuth()
      await login(this.credentials)
    }
  }
}
```

### 代码审查指南

- 确保新组件使用组合式API
- 检查是否有机会使用现有组合式函数
- 在修改现有组件时，考虑是否值得迁移到组合式API

---

通过遵循本指南，我们可以在保持项目稳定运行的同时，逐步迁移到Vue 3的组合式API，充分利用其提供的灵活性和可维护性优势。 