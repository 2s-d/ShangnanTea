# 商南茶文化平台更新日志

## [未发布]

### 添加
- 新增 `messageHelper.js` 工具类，提供标准化消息处理和异步操作辅助函数
- 新增消息系统使用指南文档 `docs/message-system-guide.md`
- 新增消息使用检查工具 `tools/check-message-usage.js`
- 新增消息优化总结 `docs/fix-message-summary.md`

### 修复
- 修复重复消息提示问题
- 改进表单验证错误提示方式，避免重复提示
- 修复 `OrderPayment.vue` 组件中的消息处理
- 优化 `RegisterPage.vue` 组件中的错误处理

### 变更
- 修改 Element Plus 配置，设置表单验证只使用内联消息
- 增强消息组件封装，提高重复消息检测效率
- 更新 `useFormValidation.js` 和 `useImageUpload.js` 使用封装的消息组件
- 优化异步操作的消息处理逻辑

## [v0.1.0] - 2023-06-01

### 添加
- 初始版本发布
- 基础用户认证功能
- 茶叶展示和搜索功能
- 商家管理功能
- 购物车和订单管理 