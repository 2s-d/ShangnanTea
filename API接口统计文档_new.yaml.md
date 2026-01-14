# openapi_new.yaml 接口统计文档

本文档统计了 `openapi_new.yaml` 中的所有接口，按模块分类，包含行号、HTTP方法、接口路径和简单描述。

## 统计概览

- **总接口数**: 168个
- **用户模块**: 35个
- **茶叶模块**: 26个
- **店铺模块**: 26个
- **订单模块**: 21个
- **论坛模块**: 36个
- **消息模块**: 22个
- **公共模块**: 2个

---

## 一、用户模块（35个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 244 | PUT | /user | 更新用户信息 |
| 2 | 397 | GET | /user/addresses | 获取收货地址列表 |
| 3 | 423 | POST | /user/addresses | 添加收货地址 |
| 4 | 495 | DELETE | /user/addresses/{id} | 删除收货地址 |
| 5 | 465 | PUT | /user/addresses/{id} | 更新收货地址 |
| 6 | 521 | PUT | /user/addresses/{id}/default | 设置默认地址 |
| 7 | 1134 | GET | /user/admin/certifications | 获取商家认证申请列表（管理员） |
| 8 | 1178 | PUT | /user/admin/certifications/{id} | 审核认证申请（管理员） |
| 9 | 905 | GET | /user/admin/users | 获取用户列表（管理员） |
| 10 | 967 | POST | /user/admin/users | 创建管理员账号（管理员） |
| 11 | 1032 | DELETE | /user/admin/users/{userId} | 删除用户（管理员） |
| 12 | 998 | PUT | /user/admin/users/{userId} | 更新用户信息（管理员） |
| 13 | 1058 | PUT | /user/admin/users/{userId}/role | 更新用户角色（管理员，已废弃） |
| 14 | 1096 | PUT | /user/admin/users/{userId}/status | 启用/禁用用户（管理员） |
| 15 | 282 | POST | /user/avatar | 上传头像 |
| 16 | 690 | GET | /user/favorites | 获取收藏列表 |
| 17 | 721 | POST | /user/favorites | 添加收藏 |
| 18 | 758 | DELETE | /user/favorites/{id} | 取消收藏 |
| 19 | 604 | GET | /user/follows | 获取关注列表 |
| 20 | 635 | POST | /user/follows | 添加关注 |
| 21 | 664 | DELETE | /user/follows/{id} | 取消关注 |
| 22 | 784 | POST | /user/likes | 点赞 |
| 23 | 821 | DELETE | /user/likes/{id} | 取消点赞 |
| 24 | 67 | POST | /user/login | 用户登录 |
| 25 | 145 | POST | /user/logout | 退出登录 |
| 26 | 186 | GET | /user/me | 获取当前用户信息 |
| 27 | 322 | PUT | /user/password | 修改密码 |
| 28 | 360 | POST | /user/password/reset | 密码找回/重置 |
| 29 | 847 | GET | /user/preferences | 获取用户偏好设置 |
| 30 | 868 | PUT | /user/preferences | 更新用户偏好设置 |
| 31 | 165 | POST | /user/refresh | 刷新令牌 |
| 32 | 103 | POST | /user/register | 用户注册 |
| 33 | 547 | GET | /user/shop-certification | 获取商家认证状态 |
| 34 | 573 | POST | /user/shop-certification | 提交商家认证申请 |
| 35 | 214 | GET | /user/{userId} | 获取指定用户信息 |

---

## 二、茶叶模块（26个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 2148 | PUT | /tea/batch-status | 批量更新茶叶状态 |
| 2 | 1452 | GET | /tea/categories | 获取茶叶分类列表 |
| 3 | 1478 | POST | /tea/categories | 创建茶叶分类（管理员） |
| 4 | 1555 | DELETE | /tea/categories/{id} | 删除茶叶分类（管理员） |
| 5 | 1517 | PUT | /tea/categories/{id} | 更新茶叶分类（管理员） |
| 6 | 2072 | PUT | /tea/images/order | 更新图片顺序 |
| 7 | 2012 | DELETE | /tea/images/{imageId} | 删除茶叶图片 |
| 8 | 2046 | PUT | /tea/images/{imageId}/main | 设置主图 |
| 9 | 1228 | GET | /tea/list | 获取茶叶列表 |
| 10 | 1303 | POST | /tea/list | 添加茶叶（商家/管理员） |
| 11 | 2192 | GET | /tea/recommend | 获取推荐茶叶 |
| 12 | 1665 | POST | /tea/reviews | 提交茶叶评价 |
| 13 | 1750 | POST | /tea/reviews/{reviewId}/like | 点赞评价 |
| 14 | 1705 | POST | /tea/reviews/{reviewId}/reply | 商家回复评价 |
| 15 | 1895 | DELETE | /tea/specifications/{specId} | 删除茶叶规格 |
| 16 | 1857 | PUT | /tea/specifications/{specId} | 更新茶叶规格 |
| 17 | 1929 | PUT | /tea/specifications/{specId}/default | 设置默认规格 |
| 18 | 1418 | DELETE | /tea/{id} | 删除茶叶（商家/管理员） |
| 19 | 1347 | GET | /tea/{id} | 获取茶叶详情 |
| 20 | 1380 | PUT | /tea/{id} | 更新茶叶信息（商家/管理员） |
| 21 | 1963 | POST | /tea/{teaId}/images | 上传茶叶图片 |
| 22 | 1589 | GET | /tea/{teaId}/reviews | 获取茶叶评价列表 |
| 23 | 1633 | GET | /tea/{teaId}/reviews/stats | 获取茶叶评价统计 |
| 24 | 1784 | GET | /tea/{teaId}/specifications | 获取茶叶规格列表 |
| 25 | 1812 | POST | /tea/{teaId}/specifications | 添加茶叶规格 |
| 26 | 2111 | PUT | /tea/{teaId}/status | 更新茶叶状态（上架/下架） |

---

## 三、店铺模块（26个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 2986 | DELETE | /shop/announcements/{announcementId} | 删除店铺公告 |
| 2 | 2955 | PUT | /shop/announcements/{announcementId} | 更新店铺公告 |
| 3 | 2839 | PUT | /shop/banners/order | 更新Banner顺序 |
| 4 | 2805 | DELETE | /shop/banners/{bannerId} | 删除店铺Banner |
| 5 | 2760 | PUT | /shop/banners/{bannerId} | 更新店铺Banner |
| 6 | 2233 | GET | /shop/list | 获取店铺列表 |
| 7 | 2295 | POST | /shop/list | 创建店铺 |
| 8 | 2404 | GET | /shop/my | 获取我的店铺信息 |
| 9 | 2531 | DELETE | /shop/teas/{teaId} | 删除店铺茶叶 |
| 10 | 2501 | PUT | /shop/teas/{teaId} | 更新店铺茶叶 |
| 11 | 2557 | PUT | /shop/teas/{teaId}/status | 茶叶上下架 |
| 12 | 2333 | GET | /shop/{id} | 获取店铺详情 |
| 13 | 2364 | PUT | /shop/{id} | 更新店铺信息 |
| 14 | 2883 | GET | /shop/{shopId}/announcements | 获取店铺公告列表 |
| 15 | 2911 | POST | /shop/{shopId}/announcements | 创建店铺公告 |
| 16 | 2682 | GET | /shop/{shopId}/banners | 获取店铺Banner列表 |
| 17 | 2709 | POST | /shop/{shopId}/banners | 上传店铺Banner |
| 18 | 3044 | DELETE | /shop/{shopId}/follow | 取消关注店铺 |
| 19 | 3012 | POST | /shop/{shopId}/follow | 关注店铺 |
| 20 | 3078 | GET | /shop/{shopId}/follow-status | 获取店铺关注状态 |
| 21 | 2636 | POST | /shop/{shopId}/logo | 上传店铺Logo |
| 22 | 3105 | GET | /shop/{shopId}/reviews | 获取店铺评价列表 |
| 23 | 3146 | POST | /shop/{shopId}/reviews | 提交店铺评价 |
| 24 | 2594 | GET | /shop/{shopId}/statistics | 获取店铺统计数据 |
| 25 | 2431 | GET | /shop/{shopId}/teas | 获取店铺茶叶列表 |
| 26 | 2469 | POST | /shop/{shopId}/teas | 添加茶叶到店铺 |

---

## 四、订单模块（21个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 3503 | POST | /order/batch-ship | 批量发货 |
| 2 | 3358 | POST | /order/cancel | 取消订单 |
| 3 | 3683 | GET | /order/cart | 获取购物车列表 |
| 4 | 3711 | POST | /order/cart/add | 添加商品到购物车 |
| 5 | 3817 | DELETE | /order/cart/clear | 清空购物车 |
| 6 | 3791 | DELETE | /order/cart/remove | 移除购物车商品 |
| 7 | 3753 | PUT | /order/cart/update | 更新购物车商品 |
| 8 | 3390 | POST | /order/confirm | 确认收货 |
| 9 | 3194 | POST | /order/create | 创建订单 |
| 10 | 3885 | GET | /order/export | 导出订单数据 |
| 11 | 3238 | GET | /order/list | 获取订单列表 |
| 12 | 3321 | POST | /order/pay | 支付订单 |
| 13 | 3543 | POST | /order/refund | 申请退款（兼容旧接口） |
| 14 | 3458 | POST | /order/review | 评价订单 |
| 15 | 3837 | GET | /order/shop/{shopId} | 获取店铺订单列表（商家） |
| 16 | 3934 | GET | /order/statistics | 获取订单统计数据 |
| 17 | 3281 | GET | /order/{id} | 获取订单详情 |
| 18 | 3650 | GET | /order/{id}/logistics | 查询物流信息 |
| 19 | 3578 | GET | /order/{id}/refund | 获取退款详情 |
| 20 | 3609 | POST | /order/{id}/refund/process | 审批退款 |
| 21 | 3422 | POST | /order/{id}/ship | 发货 |

---

## 五、论坛模块（36个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 5011 | GET | /forum/articles | 获取文章列表 |
| 2 | 5056 | POST | /forum/articles | 创建文章（管理员） |
| 3 | 5158 | DELETE | /forum/articles/{id} | 删除文章（管理员） |
| 4 | 5096 | GET | /forum/articles/{id} | 获取文章详情 |
| 5 | 5128 | PUT | /forum/articles/{id} | 更新文章（管理员） |
| 6 | 4040 | GET | /forum/banners | 获取论坛Banner列表 |
| 7 | 4062 | POST | /forum/banners | 上传论坛Banner（管理员） |
| 8 | 4164 | PUT | /forum/banners/order | 更新Banner顺序（管理员） |
| 9 | 4138 | DELETE | /forum/banners/{id} | 删除论坛Banner（管理员） |
| 10 | 4096 | PUT | /forum/banners/{id} | 更新论坛Banner（管理员） |
| 11 | 3983 | GET | /forum/home | 获取论坛首页数据 |
| 12 | 4013 | PUT | /forum/home | 更新论坛首页数据（管理员） |
| 13 | 4356 | GET | /forum/posts | 获取帖子列表 |
| 14 | 4413 | POST | /forum/posts | 发布帖子 |
| 15 | 4455 | GET | /forum/posts/pending | 获取待审核帖子列表（管理员） |
| 16 | 4565 | DELETE | /forum/posts/{id} | 删除帖子 |
| 17 | 4491 | GET | /forum/posts/{id} | 获取帖子详情 |
| 18 | 4527 | PUT | /forum/posts/{id} | 更新帖子 |
| 19 | 4885 | POST | /forum/posts/{id}/approve | 审核通过帖子（管理员） |
| 20 | 4980 | PUT | /forum/posts/{id}/essence | 设置帖子精华（管理员） |
| 21 | 4859 | DELETE | /forum/posts/{id}/favorite | 取消收藏帖子 |
| 22 | 4834 | POST | /forum/posts/{id}/favorite | 收藏帖子 |
| 23 | 4632 | DELETE | /forum/posts/{id}/like | 取消点赞帖子 |
| 24 | 4599 | POST | /forum/posts/{id}/like | 点赞帖子 |
| 25 | 4911 | POST | /forum/posts/{id}/reject | 审核拒绝帖子（管理员） |
| 26 | 4658 | GET | /forum/posts/{id}/replies | 获取帖子回复列表 |
| 27 | 4700 | POST | /forum/posts/{id}/replies | 创建回复 |
| 28 | 4949 | PUT | /forum/posts/{id}/sticky | 设置帖子置顶（管理员） |
| 29 | 4750 | DELETE | /forum/replies/{id} | 删除评论 |
| 30 | 4808 | DELETE | /forum/replies/{id}/like | 取消点赞回复 |
| 31 | 4784 | POST | /forum/replies/{id}/like | 点赞回复 |
| 32 | 4197 | GET | /forum/topics | 获取版块列表 |
| 33 | 4219 | POST | /forum/topics | 创建版块（管理员） |
| 34 | 4330 | DELETE | /forum/topics/{id} | 删除版块（管理员） |
| 35 | 4260 | GET | /forum/topics/{id} | 获取版块详情 |
| 36 | 4288 | PUT | /forum/topics/{id} | 更新版块（管理员） |

---

## 六、消息模块（22个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 5344 | POST | /message/delete | 删除消息 |
| 2 | 5197 | GET | /message/list | 获取消息列表 |
| 3 | 5430 | GET | /message/list/history | 获取聊天记录 |
| 4 | 5401 | GET | /message/list/sessions | 获取聊天会话列表 |
| 5 | 5556 | POST | /message/messages/image | 发送图片消息 |
| 6 | 5770 | GET | /message/notifications | 获取通知列表 |
| 7 | 5898 | DELETE | /message/notifications/batch | 批量删除通知 |
| 8 | 5869 | PUT | /message/notifications/batch-read | 批量标记通知为已读 |
| 9 | 5843 | DELETE | /message/notifications/{id} | 删除通知 |
| 10 | 5812 | GET | /message/notifications/{id} | 获取通知详情 |
| 11 | 5310 | POST | /message/read | 标记消息为已读 |
| 12 | 5271 | POST | /message/send | 发送消息 |
| 13 | 5472 | POST | /message/sessions | 创建聊天会话 |
| 14 | 5504 | DELETE | /message/sessions/{sessionId} | 删除聊天会话 |
| 15 | 5530 | PUT | /message/sessions/{sessionId}/pin | 置顶聊天会话 |
| 16 | 5378 | GET | /message/unread-count | 获取未读消息数 |
| 17 | 5693 | GET | /message/user/posts | 获取用户发布的帖子列表 |
| 18 | 5734 | GET | /message/user/reviews | 获取用户评价记录 |
| 19 | 5591 | GET | /message/user/{userId} | 获取用户主页信息 |
| 20 | 5626 | GET | /message/user/{userId}/dynamic | 获取用户动态 |
| 21 | 5665 | GET | /message/user/{userId}/statistics | 获取用户统计数据 |
| 22 | 5239 | GET | /message/{id} | 获取消息详情 |

---

## 七、公共模块（2个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 5928 | POST | /upload | 通用文件上传 |
| 2 | 5966 | POST | /upload/image | 图片上传 |

---

## 统计说明

1. **行号**：指HTTP方法定义所在的行号（即get:、post:等所在的行）
2. **HTTP方法**：接口使用的HTTP方法（GET、POST、PUT、DELETE）
3. **接口路径**：完整的API路径，包含路径参数
4. **接口描述**：来自OpenAPI文档中的 `summary` 字段

## 备注

- 部分接口路径包含路径参数（如 `{id}`、`{userId}` 等），在实际调用时需要替换为具体值
- 部分接口需要认证（Bearer Token），在请求头中需要添加 `Authorization: Bearer <token>`
- 所有接口统一返回HTTP 200状态码，通过响应体中的 `code` 字段判断业务结果

