# API 响应数据规范

## 统一响应格式

所有 API 接口必须返回以下统一格式：

```json
{
  "code": 200,
  "message": "success",
  "data": <业务数据>
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | integer | ✅ | 业务状态码，200 表示成功 |
| message | string | ✅ | 状态描述信息 |
| data | any | ❌ | 业务数据，可为 null |

## 状态码定义

| code | 含义 | 使用场景 |
|------|------|----------|
| 200 | 成功 | 请求处理成功 |
| 400 | 参数错误 | 请求参数校验失败 |
| 401 | 未认证 | 未登录或 token 无效 |
| 403 | 无权限 | 已登录但无操作权限 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 500 | 服务器错误 | 服务端异常 |

## 不同场景的 data 格式

### 1. 登录接口
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

### 2. 获取单个对象
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "cy100001",
    "username": "张三",
    "role": 2,
    "avatar": "https://..."
  }
}
```

### 3. 获取列表（不分页）
```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "id": 1, "name": "西湖龙井" },
    { "id": 2, "name": "碧螺春" }
  ]
}
```

### 4. 获取列表（分页）
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      { "id": 1, "name": "西湖龙井" },
      { "id": 2, "name": "碧螺春" }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

### 5. 创建/更新操作
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 123,
    "name": "新茶叶",
    "createTime": "2024-01-01 12:00:00"
  }
}
```

### 6. 删除/无数据返回
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 7. 错误响应
```json
{
  "code": 400,
  "message": "用户名不能为空",
  "data": null
}
```

## Apifox Mock 配置指南

### 基础结构配置

在 Apifox 中配置响应时，创建三个**同级**字段：

```
根节点
├── code (integer)     → Mock: 200
├── message (string)   → Mock: "success"
└── data (object/array/null)
```

### 字段 Mock 规则

| 字段 | 类型 | Mock 设置 |
|------|------|-----------|
| code | integer | 固定值: `200` |
| message | string | 固定值: `"success"` |
| data | - | 根据接口类型配置 |

### 登录接口特殊配置

`data` 下只需一个 `token` 字段：

```
data (object)
└── token (string) → 固定值: JWT字符串
```

**三种角色的 Mock Token：**

管理员 (role=1):
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjeTEwMDAwMSIsInJvbGUiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJqdGkiOiJtb2NrLXV1aWQiLCJpYXQiOjE3MzUwODQ4MDAsImV4cCI6MTc2NzIyNTYwMCwiaXNzIjoic2hhbmduYW50ZWEifQ.mock
```

普通用户 (role=2):
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjeTEwMDAwMiIsInJvbGUiOjIsInVzZXJuYW1lIjoidXNlciIsImp0aSI6Im1vY2stdXVpZCIsImlhdCI6MTczNTA4NDgwMCwiZXhwIjoxNzY3MjI1NjAwLCJpc3MiOiJzaGFuZ25hbnRlYSJ9.mock
```

商家 (role=3):
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjeTEwMDAwMyIsInJvbGUiOjMsInVzZXJuYW1lIjoic2hvcCIsImp0aSI6Im1vY2stdXVpZCIsImlhdCI6MTczNTA4NDgwMCwiZXhwIjoxNzY3MjI1NjAwLCJpc3MiOiJzaGFuZ25hbnRlYSJ9.mock
```

### 常见错误

❌ **错误：多层嵌套**
```json
{
  "code": 200,
  "data": {
    "data": {
      "token": "xxx"
    }
  }
}
```

❌ **错误：引用模型导致随机数据**
```
data → 引用 TokenVO  ← 会生成随机 token
```

✅ **正确：直接定义字段**
```
data (object)
└── token (string) → 固定值
```

## JWT Token 结构

后端生成的 JWT payload 包含：

```json
{
  "sub": "cy100002",      // 用户ID (必填)
  "role": 2,              // 角色: 1-管理员, 2-用户, 3-商家 (必填)
  "username": "user",     // 用户名 (必填)
  "jti": "uuid",          // Token唯一标识
  "iat": 1735084800,      // 签发时间 (Unix秒)
  "exp": 1767225600,      // 过期时间 (Unix秒)
  "iss": "shangnantea"    // 签发者
}
```

前端 `verifyToken()` 会验证：
- `sub` 必须存在
- `role` 必须是 1、2、3 之一
- `exp` 未过期（如果存在）

## 前端响应拦截器逻辑

响应拦截器位于 `src/api/index.js`，会自动处理响应数据：

```javascript
// 有 code 字段且成功
if (res.code === 200) {
  return res.data  // ✅ 返回 data 部分（已解包）
}

// 无 code 字段（兼容模式）
if (res.code === undefined) {
  return res  // 直接返回整个响应
}

// 错误处理
if ([401, 403].includes(res.code)) {
  // 清除登录状态，跳转登录页
}
```

### Store 模块响应处理规范

由于响应拦截器已经解包 `res.data`，Store 模块中应该：

✅ **正确做法：直接使用返回值**
```javascript
const res = await getTeaDetail(id)
commit('SET_CURRENT_TEA', res)  // res 已经是 data 内容
```

❌ **错误做法：重复解包**
```javascript
const res = await getTeaDetail(id)
const data = res?.data || res  // 不需要！拦截器已处理
```

❌ **错误做法：检查 code**
```javascript
const res = await getTeaDetail(id)
if (res.code === 200) {  // 不需要！拦截器已处理
  // ...
}
```

### 分页数据处理

对于分页接口，响应拦截器返回的是 `data` 对象：

```javascript
const res = await getTeas(params)
// res = { list: [...], total: 100, page: 1, pageSize: 20 }

commit('SET_TEA_LIST', res.list || [])
commit('SET_PAGINATION', {
  total: res.total || 0,
  currentPage: res.page || 1,
  pageSize: res.pageSize || 10
})
```

### 数组数据处理

对于返回数组的接口：

```javascript
const res = await getCategories()
// res = [{ id: 1, name: '绿茶' }, ...]

commit('SET_CATEGORIES', res || [])
// 或使用 Array.isArray 检查
commit('SET_CATEGORIES', Array.isArray(res) ? res : [])
```

## 接口示例汇总

| 接口 | Method | data 类型 | 示例 |
|------|--------|-----------|------|
| 登录 | POST | object | `{ token: "..." }` |
| 退出 | POST | null | `null` |
| 用户信息 | GET | object | `{ id, username, role, ... }` |
| 茶叶列表 | GET | object | `{ list: [...], total }` |
| 茶叶详情 | GET | object | `{ id, name, price, ... }` |
| 创建茶叶 | POST | object | 返回创建的对象 |
| 更新茶叶 | PUT | object | 返回更新后的对象 |
| 删除茶叶 | DELETE | null | `null` |
| 上传图片 | POST | object | `{ url: "..." }` |
