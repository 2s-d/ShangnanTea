# E2E 控制台错误修复需求

## 概述
通过 Playwright E2E 测试扫描发现的控制台错误和警告，需要逐一修复以提升应用质量。

## 修复进度

### ✅ US-2: 修复 OrderManagePage sortKey 未定义警告 - 已完成
**问题**: `sortKey` 在 setup() 中定义了但没有在 return 语句中返回
**修复**: 在 return 语句中添加了 `sortKey` 和 `refundDetail`
**文件**: `src/views/order/manage/OrderManagePage.vue`

---

### ⏸️ US-1: ElPagination size prop 警告 - 暂不处理
**原因**: 这是 Element Plus 2.2.6 版本内部组件的警告，不是我们代码的问题
**建议**: 升级 Element Plus 到最新版本可解决此问题
**影响**: 仅为警告，不影响功能

---

### ⏸️ US-3: 茶文化首页 API 404 错误 - 需要配置 Mock
**原因**: Apifox Mock 服务未配置以下接口:
- `/forum/home-contents` - 首页数据接口
- `/tea/recommend` - 推荐茶叶接口

**解决方案**: 在 Apifox 中配置这两个接口的 Mock 数据

---

### ⏸️ US-4: 购物车 ElementPlus 错误 - 暂不处理
**原因**: Element Plus 内部错误，可能与版本兼容性有关
**影响**: 仅为警告，不影响功能

---

## 技术说明

### ElPagination size prop 问题
Element Plus 的 ElPagination 组件内部子组件（ElPaginationJumper, ElPaginationSizes）在传递 size 属性时出现验证失败。这是组件库内部问题，建议升级到最新版本。

### sortKey 未定义问题 ✅ 已修复
Vue 3 Composition API 中，模板访问的属性必须在 setup 函数中通过 ref/reactive 定义并 return。

### API 404 问题
Mock 服务未配置对应接口，需要在 Apifox 中添加:
- GET `/forum/home-contents` - 返回首页数据
- GET `/tea/recommend` - 返回推荐茶叶列表

---

## 优先级
1. ✅ **高** - US-2 (sortKey 已修复)
2. ⏸️ **中** - US-3 (需要配置 Mock 接口)
3. ⏸️ **低** - US-1, US-4 (Element Plus 内部警告，建议升级版本)

## 测试报告来源
- E2E 测试脚本: `e2e/console-error-scan.spec.js`
- 报告目录: `e2e-report/data/`
