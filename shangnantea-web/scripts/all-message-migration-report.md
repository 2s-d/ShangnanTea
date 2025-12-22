# 全项目消息迁移报告

## 迁移统计

- 📁 处理文件数: 37
- 📦 更新导入数: 30  
- 🔄 标记调用数: 277
- ❌ 错误数量: 0

## 后续手动工作

需要手动处理标记为 `// TODO: 迁移到新消息系统` 的代码：

### 迁移模式

```javascript
// 旧方式
xxxMessages.success.showXxxSuccess()

// 新方式
const response = await api.someAction()
if (isSuccess(response.code)) {
  showByCode(response.code) // 自动显示成功消息
} else {
  showByCode(response.code) // 自动显示错误消息
}
```

## 错误日志



---
生成时间: 2025/12/22 15:22:57
