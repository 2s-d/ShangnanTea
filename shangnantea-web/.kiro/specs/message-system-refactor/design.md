# Design Document: 消息系统重构

## Overview

将现有 7 个分散的消息文件重构为 2 个文件：
- `promptMessages.js` - 纯前端提示消息
- `apiMessages.js` - 基于状态码映射的 API 消息

## Architecture

```
后端 ResultCode.java → { code, data } → 前端拦截器 → apiMessages.js → messageManager.js
                                                    ↓
前端验证 → promptMessages.js → messageManager.js
```

## 状态码分段规则

| 范围 | 模块 |
|------|------|
| 200, 400-500 | HTTP 语义码 |
| 1000-1999 | 通用业务码 |
| 2000-2999 | 用户模块 |
| 3000-3999 | 茶叶模块 |
| 4000-4999 | 订单模块 |
| 5000-5999 | 店铺模块 |
| 6000-6999 | 论坛模块 |
| 7000-7999 | 消息模块 |
