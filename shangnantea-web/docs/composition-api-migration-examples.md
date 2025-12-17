# 商南茶项目组合式API迁移案例与实践

本文档提供了从选项式API到组合式API的实际迁移案例和详细指导，作为[组合式API迁移指南](/docs/composition-api-migration.md)的补充内容。

## 目录

1. [组件迁移案例](#组件迁移案例)
2. [常见迁移场景](#常见迁移场景)
3. [组合式API高级特性](#组合式API高级特性)
4. [性能优化实践](#性能优化实践)
5. [常见问题与解决方案](#常见问题与解决方案)

## 组件迁移案例

### 案例1：基础组件迁移 - 导航栏组件

#### 选项式API版本（迁移前）

```vue
<template>
  <div class="navbar">
    <el-menu :default-active="activeIndex" mode="horizontal" router>
      <el-menu-item index="/">首页</el-menu-item>
      <el-menu-item v-if="isAdmin" index="/admin">管理中心</el-menu-item>
      <el-menu-item v-if="isLoggedIn" index="/profile">{{ username }}</el-menu-item>
      <el-menu-item v-else index="/login">登录</el-menu-item>
    </el-menu>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'NavBar',
  data() {
    return {
      activeIndex: '/'
    }
  },
  computed: {
    ...mapGetters('user', ['isLoggedIn', 'isAdmin', 'username'])
  },
  methods: {
    ...mapActions('user', ['logout']),
    async handleLogout() {
      try {
        await this.logout()
        this.$message.success('退出登录成功')
        this.$router.push('/login')
      } catch (error) {
        this.$message.error('退出登录失败')
      }
    }
  },
  mounted() {
    this.activeIndex = this.$route.path
  },
  watch: {
    '$route.path'(newPath) {
      this.activeIndex = newPath
    }
  }
}
</script>
```

#### 组合式API版本（迁移后）

```vue
<template>
  <div class="navbar">
    <el-menu :default-active="activeIndex" mode="horizontal" router>
      <el-menu-item index="/">首页</el-menu-item>
      <el-menu-item v-if="isAdmin" index="/admin">管理中心</el-menu-item>
      <el-menu-item v-if="isLoggedIn" index="/profile">{{ username }}</el-menu-item>
      <el-menu-item v-else index="/login">登录</el-menu-item>
    </el-menu>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/composables/useAuth'

export default {
  name: 'NavBar',
  setup() {
    // 路由和状态管理
    const store = useStore()
    const router = useRouter()
    const route = useRoute()
    
    // 使用认证组合函数
    const { isLoggedIn, userInfo, hasRole, logout } = useAuth()
    
    // 响应式状态
    const activeIndex = ref('/')
    
    // 计算属性
    const isAdmin = computed(() => hasRole(1))
    const username = computed(() => userInfo.value?.username || '用户')
    
    // 方法
    const handleLogout = async () => {
      try {
        await logout()
        ElMessage.success('退出登录成功')
        router.push('/login')
      } catch (error) {
        ElMessage.error('退出登录失败')
      }
    }
    
    // 生命周期
    onMounted(() => {
      activeIndex.value = route.path
    })
    
    // 监听
    watch(() => route.path, (newPath) => {
      activeIndex.value = newPath
    })
    
    // 返回模板需要的内容
    return {
      activeIndex,
      isLoggedIn,
      isAdmin,
      username,
      handleLogout
    }
  }
}
</script>
```

#### 迁移要点说明

1. **导入API**：
   - 从Vue中导入所需的组合式API函数：`ref`, `computed`, `watch`, `onMounted`
   - 使用`useStore`, `useRouter`, `useRoute`代替直接访问this.$store和this.$router

2. **使用组合式函数**：
   - 引入`useAuth`组合式函数处理认证相关逻辑

3. **响应式数据**：
   - 使用`ref`代替`data`中的数据
   - 访问和修改时需要使用`.value`

4. **计算属性**：
   - 使用`computed`函数代替`computed`选项

5. **生命周期**：
   - 使用`onMounted`代替`mounted`选项

6. **监听器**：
   - 使用`watch`函数代替`watch`选项
   - 第一个参数是源（可以是getter函数），第二个参数是回调

### 案例2：表单组件迁移 - 修改密码页面

#### 选项式API版本（迁移前）

```vue
<template>
  <div class="change-password-form">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="当前密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password"></el-input>
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password"></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="loading">提交</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'ChangePasswordForm',
  data() {
    // 密码确认验证器
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.form.newPassword) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };
    
    return {
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        oldPassword: [
          { required: true, message: '请输入当前密码', trigger: 'blur' },
          { min: 6, message: '密码长度不少于6个字符', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不少于6个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      loading: false
    };
  },
  methods: {
    submitForm() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return;
        
        this.loading = true;
        try {
          await this.$store.dispatch('user/changePassword', {
            oldPassword: this.form.oldPassword,
            newPassword: this.form.newPassword
          });
          
          this.$message.success('密码修改成功');
          this.resetForm();
        } catch (error) {
          this.$message.error('密码修改失败：' + error.message);
        } finally {
          this.loading = false;
        }
      });
    },
    resetForm() {
      this.$refs.formRef.resetFields();
    }
  }
}
</script>
```

#### 组合式API版本（迁移后）

```vue
<template>
  <div class="change-password-form">
    <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
      <el-form-item label="当前密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password"></el-input>
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password"></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="loading">提交</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useFormValidation } from '@/composables/useFormValidation'
import { useAuth } from '@/composables/useAuth'

export default {
  name: 'ChangePasswordForm',
  setup() {
    // 使用表单验证和认证组合函数
    const { rules, validateForm, resetForm } = useFormValidation()
    const { loading, changePassword } = useAuth()
    
    // 表单引用
    const formRef = ref(null)
    
    // 表单数据
    const form = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    // 表单规则
    const formRules = {
      oldPassword: [
        rules.required('请输入当前密码'),
        rules.minLength(6, '密码长度不少于6个字符')
      ],
      newPassword: [
        rules.required('请输入新密码'),
        rules.minLength(6, '密码长度不少于6个字符')
      ],
      confirmPassword: [
        rules.required('请确认新密码'),
        {
          validator: (rule, value, callback) => {
            if (value !== form.newPassword) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }
    
    // 提交表单
    const submitForm = () => {
      validateForm(formRef.value, async () => {
        try {
          await changePassword({
            oldPassword: form.oldPassword,
            newPassword: form.newPassword
          })
          
          ElMessage.success('密码修改成功')
          handleReset()
        } catch (error) {
          ElMessage.error(`密码修改失败：${error.message}`)
        }
      })
    }
    
    // 重置表单
    const handleReset = () => {
      resetForm(formRef.value)
    }
    
    return {
      form,
      formRules,
      formRef,
      loading,
      submitForm,
      resetForm: handleReset
    }
  }
}
</script>
```

#### 迁移要点说明

1. **使用组合式函数**：
   - 使用`useFormValidation`处理表单验证
   - 使用`useAuth`处理认证和加载状态

2. **响应式数据**：
   - 使用`ref`创建引用类型数据
   - 使用`reactive`创建对象类型响应式数据
   - 对reactive对象的属性访问不需要.value

3. **表单验证**：
   - 使用抽象的验证规则，提高可维护性
   - 自定义验证器直接访问reactive对象

4. **事件处理**：
   - 直接定义函数，不再需要methods选项
   - 使用组合函数提供的方法减少代码重复

## 常见迁移场景

### 1. Props和Emits的迁移

#### 选项式API

```js
export default {
  props: {
    product: {
      type: Object,
      required: true
    }
  },
  emits: ['add-to-cart'],
  methods: {
    addToCart() {
      this.$emit('add-to-cart', this.product)
    }
  }
}
```

#### 组合式API

```js
import { defineProps, defineEmits } from 'vue'

export default {
  setup(props, { emit }) {
    // 方法一：在setup中使用props和emit
    const addToCart = () => {
      emit('add-to-cart', props.product)
    }
    
    return { addToCart }
  },
  // 方法二：使用宏定义（推荐）
  props: {
    product: {
      type: Object,
      required: true
    }
  },
  emits: ['add-to-cart']
}

// 或者使用<script setup>语法和宏定义（最简洁）
// <script setup>
// const props = defineProps({
//   product: {
//     type: Object,
//     required: true
//   }
// })
// const emit = defineEmits(['add-to-cart'])
// 
// const addToCart = () => {
//   emit('add-to-cart', props.product)
// }
// </script>
```

### 2. 计算属性和监听器的迁移

#### 选项式API

```js
export default {
  data() {
    return {
      firstName: 'John',
      lastName: 'Doe',
      age: 30
    }
  },
  computed: {
    fullName() {
      return `${this.firstName} ${this.lastName}`
    }
  },
  watch: {
    age(newValue, oldValue) {
      console.log(`Age changed from ${oldValue} to ${newValue}`)
    },
    // 深度监听
    'user.address': {
      handler(newValue) {
        this.saveAddress(newValue)
      },
      deep: true
    }
  }
}
```

#### 组合式API

```js
import { ref, computed, watch, watchEffect } from 'vue'

export default {
  setup() {
    // 响应式状态
    const firstName = ref('John')
    const lastName = ref('Doe')
    const age = ref(30)
    const user = reactive({
      address: {
        city: 'Beijing',
        street: '123 Main St'
      }
    })
    
    // 计算属性
    const fullName = computed(() => {
      return `${firstName.value} ${lastName.value}`
    })
    
    // 基本监听器
    watch(age, (newValue, oldValue) => {
      console.log(`Age changed from ${oldValue} to ${newValue}`)
    })
    
    // 深度监听
    watch(() => user.address, (newValue) => {
      saveAddress(newValue)
    }, { deep: true })
    
    // 使用watchEffect（自动跟踪依赖）
    watchEffect(() => {
      console.log(`Current age: ${age.value}`)
      console.log(`Current city: ${user.address.city}`)
    })
    
    // 方法
    const saveAddress = (address) => {
      // 保存地址的逻辑
    }
    
    return {
      firstName,
      lastName,
      age,
      user,
      fullName,
      saveAddress
    }
  }
}
```

### 3. 生命周期钩子的迁移

#### 选项式API

```js
export default {
  created() {
    console.log('Component created')
    this.fetchData()
  },
  mounted() {
    console.log('Component mounted')
    window.addEventListener('resize', this.handleResize)
  },
  updated() {
    console.log('Component updated')
  },
  beforeUnmount() {
    console.log('Component will unmount')
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    fetchData() {
      // 获取数据
    },
    handleResize() {
      // 处理窗口大小变化
    }
  }
}
```

#### 组合式API

```js
import { 
  onBeforeMount, 
  onMounted, 
  onBeforeUpdate, 
  onUpdated, 
  onBeforeUnmount, 
  onUnmounted 
} from 'vue'

export default {
  setup() {
    // 方法
    const fetchData = () => {
      // 获取数据
    }
    
    const handleResize = () => {
      // 处理窗口大小变化
    }
    
    // 生命周期钩子
    onBeforeMount(() => {
      console.log('Before component mount')
    })
    
    onMounted(() => {
      console.log('Component mounted')
      window.addEventListener('resize', handleResize)
    })
    
    onBeforeUpdate(() => {
      console.log('Component will update')
    })
    
    onUpdated(() => {
      console.log('Component updated')
    })
    
    onBeforeUnmount(() => {
      console.log('Component will unmount')
      window.removeEventListener('resize', handleResize)
    })
    
    onUnmounted(() => {
      console.log('Component unmounted')
    })
    
    // setup在created之前执行
    console.log('Component created (setup)')
    fetchData()
    
    return {
      fetchData,
      handleResize
    }
  }
}
```

## 组合式API高级特性

### 1. provide/inject（依赖注入）

在Vue 3中，provide和inject可以在组合式API中使用，使得组件之间的数据传递更加灵活：

```js
// 父组件
import { provide, reactive, readonly } from 'vue'

export default {
  setup() {
    // 创建响应式状态
    const theme = reactive({
      color: 'dark',
      fontSize: 'medium'
    })
    
    // 提供只读值，防止子组件修改
    provide('theme', readonly(theme))
    
    // 提供更新方法，允许子组件通过方法更新
    provide('updateTheme', (newTheme) => {
      Object.assign(theme, newTheme)
    })
    
    return { theme }
  }
}

// 子组件或深层嵌套组件
import { inject } from 'vue'

export default {
  setup() {
    // 注入值和方法
    const theme = inject('theme', { color: 'light', fontSize: 'small' }) // 默认值
    const updateTheme = inject('updateTheme')
    
    const changeTheme = () => {
      updateTheme({ color: 'light' })
    }
    
    return { theme, changeTheme }
  }
}
```

### 2. Teleport组件

Vue 3的`<teleport>`组件允许将内容渲染到DOM的其他位置，对于模态框、通知等非常有用：

```vue
<template>
  <div>
    <!-- 按钮依然在组件内 -->
    <button @click="showModal = true">打开模态框</button>
    
    <!-- 模态框内容被传送到body元素下 -->
    <teleport to="body">
      <div v-if="showModal" class="modal">
        <div class="modal-content">
          <h3>模态框标题</h3>
          <p>模态框内容...</p>
          <button @click="showModal = false">关闭</button>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script>
import { ref } from 'vue'

export default {
  setup() {
    const showModal = ref(false)
    return { showModal }
  }
}
</script>
```

### 3. Suspense组件

`<suspense>`组件可以处理异步组件，提供加载中和错误状态的处理：

```vue
<template>
  <suspense>
    <!-- 默认插槽显示异步组件 -->
    <template #default>
      <async-component />
    </template>
    
    <!-- fallback插槽显示加载状态 -->
    <template #fallback>
      <div class="loading">加载中...</div>
    </template>
  </suspense>
</template>

<script>
import { defineAsyncComponent } from 'vue'

export default {
  components: {
    AsyncComponent: defineAsyncComponent(() => import('./AsyncComponent.vue'))
  }
}
</script>
```

在异步组件中使用`async setup`：

```js
// AsyncComponent.vue
export default {
  async setup() {
    // 模拟异步操作
    const data = await fetchData()
    
    return { data }
  }
}
```

## 性能优化实践

### 1. 使用shallowRef和shallowReactive

对于大型不可变对象，使用浅响应式可以提高性能：

```js
import { shallowRef, shallowReactive } from 'vue'

// 适用于大型不可变对象（如API返回的大型数据）
const bigDataList = shallowRef([])

// 只跟踪第一层属性变化
const userState = shallowReactive({
  profile: { name: 'John', age: 30 },
  preferences: { theme: 'dark', notifications: true }
})

// 更新数据时，需要替换整个引用
const updateList = (newData) => {
  bigDataList.value = newData // 高效，不会深度转换为响应式
}
```

### 2. 使用watchEffect优化多依赖监听

当需要监听多个数据源并执行相同操作时，使用`watchEffect`代替多个`watch`：

```js
import { ref, watch, watchEffect } from 'vue'

const searchText = ref('')
const searchCategory = ref('')
const searchTags = ref([])

// 使用多个watch（不推荐）
watch(searchText, () => performSearch())
watch(searchCategory, () => performSearch())
watch(searchTags, () => performSearch())

// 使用watchEffect（推荐）
watchEffect(() => {
  // 自动追踪所有依赖
  performSearch(searchText.value, searchCategory.value, searchTags.value)
})
```

### 3. 使用v-memo减少模板重新渲染

`v-memo`指令可以记忆模板的一部分，只有当依赖变化时才重新渲染：

```vue
<template>
  <div>
    <!-- 只有当id和name变化时才重新渲染 -->
    <div v-memo="[item.id, item.name]">
      {{ item.name }} ({{ item.id }})
      <span>{{ expensiveComputation() }}</span>
    </div>
  </div>
</template>
```

### 4. 组件缓存与懒加载

使用`<keep-alive>`和动态引入组件可以提高应用性能：

```vue
<template>
  <div>
    <!-- 缓存切换的组件 -->
    <keep-alive>
      <component :is="currentTab"></component>
    </keep-alive>
    
    <button @click="loadLazyComponent">加载组件</button>
  </div>
</template>

<script>
import { ref, defineAsyncComponent } from 'vue'

export default {
  setup() {
    const currentTab = ref('TabHome')
    
    // 懒加载组件
    const LazyComponent = defineAsyncComponent(() => 
      import('./HeavyComponent.vue')
    )
    
    // 加载懒加载组件
    const loadLazyComponent = () => {
      currentTab.value = 'LazyComponent'
    }
    
    return {
      currentTab,
      LazyComponent,
      loadLazyComponent
    }
  },
  components: {
    TabHome: () => import('./TabHome.vue'),
    TabProfile: () => import('./TabProfile.vue')
  }
}
</script>
```

## 常见问题与解决方案

### 1. 响应式数据丢失

**问题**：经常忘记在使用ref变量时添加.value，导致响应式丢失。

**解决方案**：
- 使用ESLint插件检测缺少的.value
- 在业务逻辑中解构时要注意保持响应式

```js
import { ref, toRefs, reactive } from 'vue'

// 正确用法
const count = ref(0)
const increment = () => {
  count.value++ // 不要忘记.value
}

// 保持对象属性的响应式
const state = reactive({ name: 'John', age: 30 })
const { name, age } = toRefs(state) // 使用toRefs保持响应式

// 错误做法（会丢失响应式）
const { name, age } = state // 这样会丢失响应式
```

### 2. 生命周期中的异步操作

**问题**：在生命周期钩子中进行的异步操作可能导致组件卸载后仍然尝试更新状态。

**解决方案**：
- 使用ref标记组件是否已卸载
- 在异步操作前检查标记

```js
import { ref, onMounted, onBeforeUnmount } from 'vue'

export default {
  setup() {
    const isComponentMounted = ref(true)
    const data = ref(null)
    
    onMounted(async () => {
      try {
        const result = await fetchData()
        // 检查组件是否仍然挂载
        if (isComponentMounted.value) {
          data.value = result
        }
      } catch (error) {
        if (isComponentMounted.value) {
          // 处理错误
        }
      }
    })
    
    onBeforeUnmount(() => {
      isComponentMounted.value = false
    })
    
    return { data }
  }
}
```

### 3. 组件之间共享状态

**问题**：多个组件需要共享状态时，Vuex可能过于复杂。

**解决方案**：
- 使用provide/inject共享状态
- 创建专用的组合式函数

```js
// useSharedState.js
import { ref, readonly } from 'vue'

const sharedCount = ref(0)

export function useSharedState() {
  const increment = () => {
    sharedCount.value++
  }
  
  const decrement = () => {
    sharedCount.value--
  }
  
  return {
    count: readonly(sharedCount), // 只读，防止直接修改
    increment,
    decrement
  }
}

// 在组件中使用
import { useSharedState } from './useSharedState'

export default {
  setup() {
    const { count, increment, decrement } = useSharedState()
    
    return { count, increment, decrement }
  }
}
```

---

通过本文档中的实例和指导，您应该能够更加自信地将项目中的组件从选项式API迁移到组合式API。记住，迁移过程可以是渐进的，首先从高优先级和经常修改的组件开始，再逐步扩展到整个项目。 