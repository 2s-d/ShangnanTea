# API测试问题汇总 (2026-02-11 17:50:31)

## 测试概况
- 测试时间: 2026-02-11 17:50:31 (北京时间)
- 测试接口数: 157个
- 测试用户: 已登录用户 (有效token)

## 问题分类

### 🔴 严重问题 (CRITICAL) - 18个500错误

所有500错误都是由于**JSON解析失败**导致的，原因是PUT/POST请求发送了空的或格式错误的请求体。

#### 茶叶模块 (Tea) - 6个500错误
1. `PUT /api/tea/images/order` - 500
2. `PUT /api/tea/batch-status` - 500  
3. `PUT /api/tea/1` - 500
4. `PUT /api/tea/specifications/1` - 500
5. `POST /api/tea/tea1000001/images` - 500 (文件上传问题)
6. `PUT /api/tea/tea1000001/status` - 500

#### 店铺模块 (Shop) - 7个500错误
7. `PUT /api/shop/banners/order` - 500
8. `POST /api/shop/shop100001/teas` - 500
9. `PUT /api/shop/teas/tea1000001` - 500
10. `PUT /api/shop/teas/tea1000001/status` - 500
11. `PUT /api/shop/banners/1` - 500
12. `PUT /api/shop/announcements/1` - 500

#### 订单模块 (Order) - 2个500错误
13. `DELETE /api/order/cart/remove` - 500
14. `GET /api/order/export` - 500 (序列化问题)

#### 论坛模块 (Forum) - 3个500错误
15. `PUT /api/forum/banners/order` - 500
16. `PUT /api/forum/banners/1` - 500
17. `POST /api/forum/posts/1/replies` - 500 (返回1001通用错误)

### ⚠️ 业务逻辑错误

#### 茶叶模块
- `GET /api/tea/list` - 返回3000 (应该返回200)
  - 问题: 成功码错误，应该使用200而不是3000

#### 权限问题 (403) - 23个接口
需要管理员/商家权限的接口，普通用户无法访问（这是正常的）

### ✅ 修复方案

#### 1. 修复GET /api/tea/list返回码
**文件**: `TeaServiceImpl.java`
**位置**: `getTeas()` 方法
**修改**: 将成功码从3000改为200

#### 2. 修复500错误的根本原因
所有500错误都是因为Controller没有正确处理空请求体或参数验证。需要：
- 添加@RequestBody参数验证
- 添加空值检查
- 返回正确的业务错误码而不是让系统抛出500

#### 3. 修复POST /api/forum/posts/1/replies
**问题**: 返回1001通用错误码
**需要**: 检查具体失败原因并返回正确的错误码

## 下一步行动

1. ✅ 修复 `GET /api/tea/list` 返回码 (3000 → 200)
2. 🔧 修复所有500错误接口的参数验证
3. 🔧 修复 `POST /api/forum/posts/1/replies` 的错误码
4. 📋 重新测试所有修复的接口
