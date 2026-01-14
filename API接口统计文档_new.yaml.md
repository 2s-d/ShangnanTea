# openapi_new.yaml 接口统计文档

本文档统计了 `openapi_new.yaml` 中的所有接口，按模块分类，包含行号、HTTP方法、接口路径和简单描述。

## 统计概览

- **总接口数**: 155个
- **用户模块**: 35个
- **茶叶模块**: 26个
- **店铺模块**: 25个
- **订单模块**: 19个
- **论坛模块**: 31个
- **消息模块**: 17个
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
| 32 | 1058 | PUT | /user/admin/users/{userId}/role | 更新用户角色（管理员，已废弃） |
| 33 | 1096 | PUT | /user/admin/users/{userId}/status | 启用/禁用用户（管理员） |
| 34 | 1134 | GET | /user/admin/certifications | 获取商家认证申请列表（管理员） |
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

## 三、店铺模块（25个接口）

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

---

## 四、订单模块（19个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 3148 | POST | /order/create | 创建订单 |
| 2 | 3192 | GET | /order/list | 获取订单列表 |
| 3 | 3235 | GET | /order/{id} | 获取订单详情 |
| 4 | 3275 | PUT | /order/{id}/cancel | 取消订单 |
| 5 | 3309 | POST | /order/{id}/pay | 支付订单 |
| 6 | 3355 | PUT | /order/{id}/confirm | 确认收货 |
| 7 | 3389 | PUT | /order/{id}/ship | 发货（商家） |
| 8 | 3437 | POST | /order/{id}/refund | 申请退款 |
| 9 | 3487 | PUT | /order/{id}/refund/approve | 同意退款（商家） |
| 10 | 3521 | PUT | /order/{id}/refund/reject | 拒绝退款（商家） |
| 11 | 3566 | GET | /order/{id}/express | 查询物流信息 |
| 12 | 3599 | GET | /order/cart | 获取购物车 |
| 13 | 3627 | POST | /order/cart | 添加到购物车 |
| 14 | 3673 | PUT | /order/cart/{itemId} | 更新购物车商品 |
| 15 | 3710 | DELETE | /order/cart/{itemId} | 从购物车删除 |
| 16 | 3736 | DELETE | /order/cart/clear | 清空购物车 |
| 17 | 3756 | GET | /order/shop/{shopId} | 获取店铺订单列表（商家） |
| 18 | 3804 | GET | /order/statistics | 获取订单统计（商家/管理员） |
| 19 | 3846 | GET | /order/admin/list | 获取所有订单（管理员） |

---

## 五、论坛模块（31个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 3896 | GET | /forum/posts | 获取帖子列表 |
| 2 | 3953 | POST | /forum/posts | 发布帖子 |
| 3 | 3995 | GET | /forum/posts/{id} | 获取帖子详情 |
| 4 | 4031 | PUT | /forum/posts/{id} | 更新帖子 |
| 5 | 4069 | DELETE | /forum/posts/{id} | 删除帖子 |
| 6 | 4103 | POST | /forum/posts/{id}/like | 点赞帖子 |
| 7 | 4136 | DELETE | /forum/posts/{id}/like | 取消点赞帖子 |
| 8 | 4162 | GET | /forum/posts/{id}/comments | 获取帖子评论列表 |
| 9 | 4204 | POST | /forum/posts/{id}/comments | 发表评论 |
| 10 | 4254 | DELETE | /forum/comments/{id} | 删除评论 |
| 11 | 4288 | POST | /forum/comments/{id}/like | 点赞评论 |
| 12 | 4312 | DELETE | /forum/comments/{id}/like | 取消点赞评论 |
| 13 | 4338 | GET | /forum/categories | 获取论坛分类列表 |
| 14 | 4360 | POST | /forum/categories | 创建论坛分类（管理员） |
| 15 | 4399 | PUT | /forum/categories/{id} | 更新论坛分类（管理员） |
| 16 | 4429 | DELETE | /forum/categories/{id} | 删除论坛分类（管理员） |
| 17 | 4455 | GET | /forum/articles | 获取文章列表 |
| 18 | 4500 | POST | /forum/articles | 创建文章（管理员） |
| 19 | 4540 | GET | /forum/articles/{id} | 获取文章详情 |
| 20 | 4572 | PUT | /forum/articles/{id} | 更新文章（管理员） |
| 21 | 4602 | DELETE | /forum/articles/{id} | 删除文章（管理员） |
| 22 | 4628 | GET | /forum/posts/my | 获取我的帖子列表 |
| 23 | 4667 | GET | /forum/posts/user/{userId} | 获取用户帖子列表 |
| 24 | 4709 | GET | /forum/posts/hot | 获取热门帖子 |
| 25 | 4738 | GET | /forum/posts/recommend | 获取推荐帖子 |
| 26 | 4766 | POST | /forum/posts/{id}/view | 增加帖子浏览量 |
| 27 | 4790 | PUT | /forum/posts/{id}/pin | 置顶帖子（管理员） |
| 28 | 4814 | DELETE | /forum/posts/{id}/pin | 取消置顶帖子（管理员） |
| 29 | 4840 | PUT | /forum/posts/{id}/recommend | 推荐帖子（管理员） |
| 30 | 4864 | DELETE | /forum/posts/{id}/recommend | 取消推荐帖子（管理员） |
| 31 | 4890 | GET | /forum/search | 搜索论坛内容 |

---

## 六、消息模块（17个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 4942 | GET | /message/conversations | 获取会话列表 |
| 2 | 4982 | GET | /message/conversations/{userId} | 获取会话消息列表 |
| 3 | 5026 | POST | /message/conversations/{userId} | 发送消息 |
| 4 | 5071 | PUT | /message/conversations/{userId}/read | 标记会话已读 |
| 5 | 5097 | DELETE | /message/conversations/{userId}/delete | 删除会话 |
| 6 | 5123 | GET | /message/unread-count | 获取未读消息数 |
| 7 | 5146 | GET | /message/notifications | 获取系统通知列表 |
| 8 | 5192 | PUT | /message/notifications/{id}/read | 标记通知已读 |
| 9 | 5218 | PUT | /message/notifications/read-all | 标记所有通知已读 |
| 10 | 5238 | DELETE | /message/notifications/{id} | 删除通知 |
| 11 | 5264 | DELETE | /message/notifications/clear | 清空通知 |
| 12 | 5284 | POST | /message/system | 发送系统消息（管理员） |
| 13 | 5329 | GET | /message/settings | 获取消息设置 |
| 14 | 5352 | PUT | /message/settings | 更新消息设置 |
| 15 | 5384 | GET | /message/blacklist | 获取黑名单列表 |
| 16 | 5406 | POST | /message/blacklist | 添加到黑名单 |
| 17 | 5445 | DELETE | /message/blacklist/{userId} | 从黑名单移除 |

---

## 七、公共模块（2个接口）

| 序号 | 行号 | HTTP方法 | 接口路径 | 接口描述 |
|------|------|----------|----------|----------|
| 1 | 5472 | POST | /common/upload | 上传文件 |
| 2 | 5516 | GET | /common/captcha | 获取验证码 |

---

## 统计说明

1. **行号**：指接口路径定义所在的行号（即路径开始的行）
2. **HTTP方法**：接口使用的HTTP方法（GET、POST、PUT、DELETE）
3. **接口路径**：完整的API路径，包含路径参数
4. **接口描述**：来自OpenAPI文档中的 `summary` 字段

## 备注

- 部分接口路径包含路径参数（如 `{id}`、`{userId}` 等），在实际调用时需要替换为具体值
- 部分接口需要认证（Bearer Token），在请求头中需要添加 `Authorization: Bearer <token>`
- 所有接口统一返回HTTP 200状态码，通过响应体中的 `code` 字段判断业务结果

