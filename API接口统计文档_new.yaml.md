# openapi_new.yaml 接口统计文档

本文档统计了 `openapi_new.yaml` 中的所有接口，按模块分类，包含行号、HTTP方法、接口路径和简单描述。

## 统计概览

- **总接口数**: 167个
- **用户模块**: 35个
- **茶叶模块**: 26个
- **店铺模块**: 26个
- **订单模块**: 20个
- **论坛模块**: 36个
- **消息模块**: 22个
- **公共模块**: 2个

---

## 一、用户模块（35个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 67 | POST | /user/login | 用户登录 |
| 2 | 103 | POST | /user/register | 用户注册 |
| 3 | 145 | POST | /user/logout | 退出登录 |
| 4 | 165 | POST | /user/refresh | 刷新令牌 |
| 5 | 186 | GET | /user/me | 获取当前用户信息 |
| 6 | 214 | GET | /user/{userId} | 获取指定用户信息 |
| 7 | 244 | PUT | /user | 更新用户信息 |
| 8 | 282 | POST | /user/avatar | 上传头像 |
| 9 | 322 | PUT | /user/password | 修改密码 |
| 10 | 360 | POST | /user/password/reset | 密码找回/重置 |
| 11 | 397 | GET | /user/addresses | 获取收货地址列表 |
| 12 | 423 | POST | /user/addresses | 添加收货地址 |
| 13 | 465 | PUT | /user/addresses/{id} | 更新收货地址 |
| 14 | 495 | DELETE | /user/addresses/{id} | 删除收货地址 |
| 15 | 521 | PUT | /user/addresses/{id}/default | 设置默认地址 |
| 16 | 547 | GET | /user/shop-certification | 获取商家认证状态 |
| 17 | 573 | POST | /user/shop-certification | 提交商家认证申请 |
| 18 | 604 | GET | /user/follows | 获取关注列表 |
| 19 | 635 | POST | /user/follows | 添加关注 |
| 20 | 664 | DELETE | /user/follows/{id} | 取消关注 |
| 21 | 690 | GET | /user/favorites | 获取收藏列表 |
| 22 | 721 | POST | /user/favorites | 添加收藏 |
| 23 | 758 | DELETE | /user/favorites/{id} | 取消收藏 |
| 24 | 784 | POST | /user/likes | 点赞 |
| 25 | 821 | DELETE | /user/likes/{id} | 取消点赞 |
| 26 | 847 | GET | /user/preferences | 获取用户偏好设置 |
| 27 | 868 | PUT | /user/preferences | 更新用户偏好设置 |
| 28 | 905 | GET | /user/admin/users | 获取用户列表（管理员） |
| 29 | 967 | POST | /user/admin/users | 创建管理员账号（管理员） |
| 30 | 998 | PUT | /user/admin/users/{userId} | 更新用户信息（管理员） |
| 31 | 1032 | DELETE | /user/admin/users/{userId} | 删除用户（管理员） |
| 32 | 1096 | PUT | /user/admin/users/{userId}/status | 启用/禁用用户（管理员） |
| 33 | 1134 | GET | /user/admin/certifications | 获取商家认证申请列表（管理员） |
| 35 | 1178 | PUT | /user/admin/certifications/{id} | 审核认证申请（管理员） |

---

## 二、茶叶模块（26个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 1228 | GET | /tea/list | 获取茶叶列表 |
| 2 | 1303 | POST | /tea/list | 添加茶叶（商家/管理员） |
| 3 | 1347 | GET | /tea/{id} | 获取茶叶详情 |
| 4 | 1380 | PUT | /tea/{id} | 更新茶叶信息（商家/管理员） |
| 5 | 1418 | DELETE | /tea/{id} | 删除茶叶（商家/管理员） |
| 6 | 1452 | GET | /tea/categories | 获取茶叶分类列表 |
| 7 | 1478 | POST | /tea/categories | 创建茶叶分类（管理员） |
| 8 | 1517 | PUT | /tea/categories/{id} | 更新茶叶分类（管理员） |
| 9 | 1555 | DELETE | /tea/categories/{id} | 删除茶叶分类（管理员） |
| 10 | 1589 | GET | /tea/{teaId}/reviews | 获取茶叶评价列表 |
| 11 | 1633 | GET | /tea/{teaId}/reviews/stats | 获取茶叶评价统计 |
| 12 | 1665 | POST | /tea/reviews | 提交茶叶评价 |
| 13 | 1705 | POST | /tea/reviews/{reviewId}/reply | 商家回复评价 |
| 14 | 1750 | POST | /tea/reviews/{reviewId}/like | 点赞评价 |
| 15 | 1784 | GET | /tea/{teaId}/specifications | 获取茶叶规格列表 |
| 16 | 1812 | POST | /tea/{teaId}/specifications | 添加茶叶规格 |
| 17 | 1857 | PUT | /tea/specifications/{specId} | 更新茶叶规格 |
| 18 | 1895 | DELETE | /tea/specifications/{specId} | 删除茶叶规格 |
| 19 | 1929 | PUT | /tea/specifications/{specId}/default | 设置默认规格 |
| 20 | 1963 | POST | /tea/{teaId}/images | 上传茶叶图片 |
| 21 | 2012 | DELETE | /tea/images/{imageId} | 删除茶叶图片 |
| 22 | 2046 | PUT | /tea/images/{imageId}/main | 设置主图 |
| 23 | 2072 | PUT | /tea/images/order | 更新图片顺序 |
| 24 | 2111 | PUT | /tea/{teaId}/status | 更新茶叶状态（上架/下架） |
| 25 | 2148 | PUT | /tea/batch-status | 批量更新茶叶状态 |
| 26 | 2192 | GET | /tea/recommend | 获取推荐茶叶 |

---

## 三、店铺模块（26个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 2233 | GET | /shop/list | 获取店铺列表 |
| 2 | 2295 | POST | /shop/list | 创建店铺 |
| 3 | 2333 | GET | /shop/{id} | 获取店铺详情 |
| 4 | 2364 | PUT | /shop/{id} | 更新店铺信息 |
| 5 | 2404 | GET | /shop/my | 获取我的店铺信息 |
| 6 | 2431 | GET | /shop/{shopId}/teas | 获取店铺茶叶列表 |
| 7 | 2469 | POST | /shop/{shopId}/teas | 添加茶叶到店铺 |
| 8 | 2501 | PUT | /shop/teas/{teaId} | 更新店铺茶叶 |
| 9 | 2531 | DELETE | /shop/teas/{teaId} | 删除店铺茶叶 |
| 10 | 2557 | PUT | /shop/teas/{teaId}/status | 茶叶上下架 |
| 11 | 2594 | GET | /shop/{shopId}/statistics | 获取店铺统计数据 |
| 12 | 2636 | POST | /shop/{shopId}/logo | 上传店铺Logo |
| 13 | 2682 | GET | /shop/{shopId}/banners | 获取店铺Banner列表 |
| 14 | 2709 | POST | /shop/{shopId}/banners | 上传店铺Banner |
| 15 | 2760 | PUT | /shop/banners/{bannerId} | 更新店铺Banner |
| 16 | 2805 | DELETE | /shop/banners/{bannerId} | 删除店铺Banner |
| 17 | 2839 | PUT | /shop/banners/order | 更新Banner顺序 |
| 18 | 2883 | GET | /shop/{shopId}/announcements | 获取店铺公告列表 |
| 19 | 2911 | POST | /shop/{shopId}/announcements | 创建店铺公告 |
| 20 | 2955 | PUT | /shop/announcements/{announcementId} | 更新店铺公告 |
| 21 | 2986 | DELETE | /shop/announcements/{announcementId} | 删除店铺公告 |
| 22 | 3012 | POST | /shop/{shopId}/follow | 关注店铺 |
| 23 | 3044 | DELETE | /shop/{shopId}/follow | 取消关注店铺 |
| 24 | 3078 | GET | /shop/{shopId}/follow-status | 获取店铺关注状态 |
| 25 | 3105 | GET | /shop/{shopId}/reviews | 获取店铺评价列表 |
| 26 | 3146 | POST | /shop/{shopId}/reviews | 提交店铺评价 |

---

## 四、订单模块（20个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 3194 | POST | /order/create | 创建订单 |
| 2 | 3238 | GET | /order/list | 获取订单列表 |
| 3 | 3281 | GET | /order/{id} | 获取订单详情 |
| 4 | 3321 | POST | /order/pay | 支付订单 |
| 5 | 3358 | POST | /order/cancel | 取消订单 |
| 6 | 3390 | POST | /order/confirm | 确认收货 |
| 7 | 3422 | POST | /order/{id}/ship | 发货 |
| 8 | 3458 | POST | /order/review | 评价订单 |
| 9 | 3503 | POST | /order/batch-ship | 批量发货 |
| 10 | 3543 | POST | /order/refund | 申请退款（兼容旧接口） |
| 11 | 3578 | GET | /order/{id}/refund | 获取退款详情 |
| 12 | 3609 | POST | /order/{id}/refund/process | 审批退款 |
| 13 | 3650 | GET | /order/{id}/logistics | 查询物流信息 |
| 14 | 3683 | GET | /order/cart | 获取购物车列表 |
| 15 | 3711 | POST | /order/cart/add | 添加商品到购物车 |
| 16 | 3753 | PUT | /order/cart/update | 更新购物车商品 |
| 17 | 3791 | DELETE | /order/cart/remove | 移除购物车商品 |
| 18 | 3817 | DELETE | /order/cart/clear | 清空购物车 |
| 19 | 3837 | GET | /order/export | 导出订单数据 |
| 20 | 3886 | GET | /order/statistics | 获取订单统计数据 |

---

## 五、论坛模块（36个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 3935 | GET | /forum/home | 获取论坛首页数据 |
| 2 | 3965 | PUT | /forum/home | 更新论坛首页数据（管理员） |
| 3 | 3992 | GET | /forum/banners | 获取论坛Banner列表 |
| 4 | 4014 | POST | /forum/banners | 上传论坛Banner（管理员） |
| 5 | 4048 | PUT | /forum/banners/{id} | 更新论坛Banner（管理员） |
| 6 | 4090 | DELETE | /forum/banners/{id} | 删除论坛Banner（管理员） |
| 7 | 4116 | PUT | /forum/banners/order | 更新Banner顺序（管理员） |
| 8 | 4149 | GET | /forum/topics | 获取版块列表 |
| 9 | 4172 | POST | /forum/topics | 创建版块（管理员） |
| 10 | 4212 | GET | /forum/topics/{id} | 获取版块详情 |
| 11 | 4241 | PUT | /forum/topics/{id} | 更新版块（管理员） |
| 12 | 4283 | DELETE | /forum/topics/{id} | 删除版块（管理员） |
| 13 | 4308 | GET | /forum/posts | 获取帖子列表 |
| 14 | 4366 | POST | /forum/posts | 发布帖子 |
| 15 | 4407 | GET | /forum/posts/pending | 获取待审核帖子列表（管理员） |
| 16 | 4443 | GET | /forum/posts/{id} | 获取帖子详情 |
| 17 | 4479 | PUT | /forum/posts/{id} | 更新帖子 |
| 18 | 4518 | DELETE | /forum/posts/{id} | 删除帖子 |
| 19 | 4551 | POST | /forum/posts/{id}/like | 点赞帖子 |
| 20 | 4584 | DELETE | /forum/posts/{id}/like | 取消点赞帖子 |
| 21 | 4610 | GET | /forum/posts/{id}/replies | 获取帖子回复列表 |
| 22 | 4653 | POST | /forum/posts/{id}/replies | 创建回复 |
| 23 | 4702 | DELETE | /forum/replies/{id} | 删除评论 |
| 24 | 4736 | POST | /forum/replies/{id}/like | 点赞回复 |
| 25 | 4761 | DELETE | /forum/replies/{id}/like | 取消点赞回复 |
| 26 | 4786 | POST | /forum/posts/{id}/favorite | 收藏帖子 |
| 27 | 4811 | DELETE | /forum/posts/{id}/favorite | 取消收藏帖子 |
| 28 | 4837 | POST | /forum/posts/{id}/approve | 审核通过帖子（管理员） |
| 29 | 4863 | POST | /forum/posts/{id}/reject | 审核拒绝帖子（管理员） |
| 30 | 4901 | PUT | /forum/posts/{id}/sticky | 设置帖子置顶（管理员） |
| 31 | 4932 | PUT | /forum/posts/{id}/essence | 设置帖子精华（管理员） |
| 32 | 4963 | GET | /forum/articles | 获取文章列表 |
| 33 | 5008 | POST | /forum/articles | 创建文章（管理员） |
| 34 | 5048 | GET | /forum/articles/{id} | 获取文章详情 |
| 35 | 5081 | PUT | /forum/articles/{id} | 更新文章（管理员） |
| 36 | 5111 | DELETE | /forum/articles/{id} | 删除文章（管理员） |

---

## 六、消息模块（22个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 5149 | GET | /message/list | 获取消息列表 |
| 2 | 5191 | GET | /message/{id} | 获取消息详情 |
| 3 | 5223 | POST | /message/send | 发送消息 |
| 4 | 5262 | POST | /message/read | 标记消息为已读 |
| 5 | 5296 | POST | /message/delete | 删除消息 |
| 6 | 5330 | GET | /message/unread-count | 获取未读消息数 |
| 7 | 5353 | GET | /message/list/sessions | 获取聊天会话列表 |
| 8 | 5382 | GET | /message/list/history | 获取聊天记录 |
| 9 | 5424 | POST | /message/sessions | 创建聊天会话 |
| 10 | 5456 | DELETE | /message/sessions/{sessionId} | 删除聊天会话 |
| 11 | 5482 | PUT | /message/sessions/{sessionId}/pin | 置顶聊天会话 |
| 12 | 5508 | POST | /message/messages/image | 发送图片消息 |
| 13 | 5543 | GET | /message/user/{userId} | 获取用户主页信息 |
| 14 | 5578 | GET | /message/user/{userId}/dynamic | 获取用户动态 |
| 15 | 5617 | GET | /message/user/{userId}/statistics | 获取用户统计数据 |
| 16 | 5645 | GET | /message/user/posts | 获取用户发布的帖子列表 |
| 17 | 5686 | GET | /message/user/reviews | 获取用户评价记录 |
| 18 | 5722 | GET | /message/notifications | 获取通知列表 |
| 19 | 5764 | GET | /message/notifications/{id} | 获取通知详情 |
| 20 | 5796 | DELETE | /message/notifications/{id} | 删除通知 |
| 21 | 5821 | PUT | /message/notifications/batch-read | 批量标记通知为已读 |
| 22 | 5850 | DELETE | /message/notifications/batch | 批量删除通知 |

---

## 七、公共模块（2个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 5880 | POST | /upload | 通用文件上传 |
| 2 | 5918 | POST | /upload/image | 图片上传 |

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
