# 商南茶文化平台优化

本文档记录了对商南茶文化平台的代码优化，主要针对以下几个方面：

1. 统一token管理至`useTokenStorage`
2. 减少身份验证层级
3. 整合权限验证逻辑
4. 清理过渡性API文件

## 1. 统一token管理至`useTokenStorage`

### 变更内容

- 确保所有token操作统一使用`useTokenStorage` composable，而非直接操作localStorage
- 修改`versionManager.js`中的token处理逻辑，全部通过`useTokenStorage`进行
- 维护`tokenUtils.js`作为桥接文件，重定向到`useTokenStorage`，保持向后兼容性

### 好处

- 统一集中管理所有token相关操作
- 提高安全性，避免不同模块使用不同的token存储逻辑
- 简化token验证和解析逻辑
- 便于未来token存储方式变更时统一修改

## 2. 减少身份验证层级

### 变更内容

- 确保Vue组件通过Vuex Actions或组合式API存取用户数据，而非直接调用API层
- 让身份验证流程集中在useAuth组合式API中，提供统一的接口

### 好处

- 减少代码重复
- 简化权限验证
- 提高可维护性
- 更好的代码组织和层次结构

## 3. 整合权限验证逻辑

### 变更内容

- 将`permission.js`中的权限验证逻辑整合到`useAuth.js`中
- 维护`permission.js`作为桥接文件，指向`useAuth.js`的功能
- 更新`directives/permission.js`指令，使其使用`useAuth`
- 更新路由守卫，使其从`useAuth`导入`ROLES`常量

### 好处

- 权限逻辑集中在一处，避免分散在多个文件中
- 减少代码重复和可能的不一致性
- 简化未来对权限验证的修改
- 保持向后兼容性，防止破坏现有代码

## 5. 清理过渡性API文件

### 变更内容

- 检查`request.js`和`deprecatedRequest.js`等过渡性文件，确保它们正确重定向到`api/index.js`
- 为过渡性文件添加明确的弃用警告，引导开发者使用正确的API

### 好处

- 减少代码混乱
- 明确API使用规范
- 保持向后兼容性同时鼓励使用新API
- 便于后续清理过渡性文件

## 总结

这些优化减少了代码重复，提高了系统的可维护性和安全性，同时保持了对现有代码的向后兼容性。所有的更改都严格遵循模块划分原则，不跨模块边界。

### 后续建议

1. 逐步将直接使用`permission.js`的代码迁移至`useAuth`
2. 在适当时机完全移除过渡性文件（如`tokenUtils.js`和旧的`permission.js`）
3. 建立更严格的代码审查规则，确保新代码遵循优化后的规范

## 1. 模块依赖优化

### 1.1 整合权限验证系统
- 将 `utils/permission.js` 的功能整合到 `composables/useAuth.js`
- `useAuth.js` 现在提供统一的权限验证功能
- 组件中使用 `const { isAdmin, isShop, hasRole } = useAuth()` 进行权限验证

### 1.2 统一token管理
- `useTokenStorage` 现在是唯一的token管理方式
- 提供了完整的token生命周期管理，包括生成、验证、存储和删除
- 确保token验证方式统一，提高安全性

### 1.3 API请求统一化
- API请求统一通过 `src/api/index.js` 导出的请求实例进行
- 避免直接使用 axios 创建请求，保持请求拦截器的一致性
- 错误处理统一通过 API 层进行初步处理

## 2. 组合式API迁移

### 2.1 已迁移的组合式函数
- `useAuth`: 用户认证与权限验证
- `useTokenStorage`: token管理
- `useCart`: 购物车功能
- `useFormValidation`: 表单验证
- `usePagination`: 分页功能
- `useImageUpload`: 图片上传

### 2.2 待迁移的功能
- `messageHelper.js`: 消息提示辅助函数
- `dateUtils.js`: 日期格式化工具
- `region.js`: 区域数据处理

## 3. 兼容性处理

### 3.1 使用动态导入解决循环依赖
- 修改 UI 组件的导入方式，避免循环依赖
- 例如: `import { message } from '@/components/common'`
- 替代直接导入 Element Plus 组件的方式

### 3.2 版本管理与数据迁移
- 修复版本检查机制，确保localStorage数据格式一致性
- 优化版本升级的数据迁移逻辑

## 4. 删除过渡文件

在项目重构过程中，我们创建了一些过渡文件用于保持向后兼容性。随着重构的完成，这些文件已被删除：

### 4.1 已删除的过渡文件
- `utils/tokenUtils.js` - 已由 `composables/useStorage.js` 中的 `useTokenStorage` 完全替代
- `utils/permission.js` - 已由 `composables/useAuth.js` 中的 `useAuth` 完全替代
- `utils/request.js` - 已由 `api/index.js` 完全替代
- `utils/deprecatedRequest.js` - 已由 `api/index.js` 完全替代

### 4.2 相关组件更新
- 所有使用这些过渡文件的组件都已更新，直接使用新的API：
  - 使用 `import { useAuth } from '@/composables/useAuth'` 替代 `import permission from '@/utils/permission'`
  - 使用 `import { useTokenStorage } from '@/composables/useStorage'` 替代 `import tokenUtils from '@/utils/tokenUtils'`
  - 使用 `import request from '@/api'` 替代 `import request from '@/utils/request'`

## 5. 后续优化方向

### 5.1 完全迁移到组合式API
- 继续将选项式API组件迁移到组合式API
- 完成所有工具函数向组合式API的迁移

### 5.2 代码结构优化
- 进一步整合类似功能的组件
- 提取更多可复用的组合式函数

### 5.3 性能优化
- 组件懒加载策略优化
- 资源打包体积优化 