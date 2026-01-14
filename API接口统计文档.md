# 商南茶城 API 接口统计文档

> 基于 `openapi.yaml.backup3` 文件统计  
> 统计时间：2025-01-15

## 接口统计总览

| 模块 | 接口数量 |
|------|---------|
| 用户模块 | 35 |
| 茶叶模块 | 26 |
| 店铺模块 | 26 |
| 订单模块 | 20 |
| 论坛模块 | 36 |
| 消息模块 | 22 |
| 公共模块 | 2 |
| **总计** | **167** |

---

## 一、用户模块（35个接口）

| 行号 | 方法 | 接口路径 | 接口名称 | 描述 |
|------|------|---------|---------|------|
| 53 | POST | `/user/login` | 用户登录 | 使用用户名和密码进行登录，返回 JWT Token |
| 79 | POST | `/user/register` | 用户注册 | 注册新用户账号 |
| 116 | POST | `/user/logout` | 退出登录 | 退出当前用户登录状态 |
| 131 | POST | `/user/refresh` | 刷新令牌 | 刷新JWT令牌 |
| 147 | GET | `/user/me` | 获取当前用户信息 | 获取当前登录用户的详细信息 |
| 170 | GET | `/user/{userId}` | 获取指定用户信息 | 根据用户ID获取用户公开信息 |
| 195 | PUT | `/user` | 更新用户信息 | 更新当前用户的个人信息 |
| 223 | POST | `/user/avatar` | 上传头像 | 上传用户头像图片 |
| 249 | PUT | `/user/password` | 修改密码 | 修改当前用户密码 |
| 278 | POST | `/user/password/reset` | 密码找回/重置 | 通过验证码重置密码 |
| 308 | GET | `/user/addresses` | 获取收货地址列表 | 获取当前用户的所有收货地址 |
| 329 | POST | `/user/addresses` | 添加收货地址 | 为当前用户添加新的收货地址 |
| 368 | PUT | `/user/addresses/{id}` | 更新收货地址 | 更新指定的收货地址信息 |
| 409 | DELETE | `/user/addresses/{id}` | 删除收货地址 | 删除指定的收货地址 |
| 430 | PUT | `/user/addresses/{id}/default` | 设置默认地址 | 将指定地址设置为默认收货地址 |
| 451 | GET | `/user/shop-certification` | 获取商家认证状态 | 获取当前用户的商家认证申请状态 |
| 466 | POST | `/user/shop-certification` | 提交商家认证申请 | 提交商家认证申请资料 |
| 496 | GET | `/user/follows` | 获取关注列表 | 获取当前用户的关注列表（用户或店铺） |
| 520 | POST | `/user/follows` | 添加关注 | 关注用户或店铺 |
| 548 | DELETE | `/user/follows/{id}` | 取消关注 | 取消关注用户或店铺 |
| 569 | GET | `/user/favorites` | 获取收藏列表 | 获取当前用户的收藏列表（茶叶、帖子、文章） |
| 593 | POST | `/user/favorites` | 添加收藏 | 收藏茶叶、帖子或文章 |
| 621 | DELETE | `/user/favorites/{id}` | 取消收藏 | 取消收藏茶叶、帖子或文章 |
| 642 | POST | `/user/likes` | 点赞 | 对帖子、评论等内容点赞 |
| 669 | DELETE | `/user/likes/{id}` | 取消点赞 | 取消对内容的点赞 |
| 690 | GET | `/user/preferences` | 获取用户偏好设置 | 获取当前用户的偏好设置（主题、显示模式等） |
| 705 | PUT | `/user/preferences` | 更新用户偏好设置 | 更新当前用户的偏好设置 |
| 731 | GET | `/user/admin/users` | 获取用户列表（管理员） | 管理员获取用户列表，支持筛选和分页 |
| 783 | POST | `/user/admin/users` | 创建管理员账号（管理员） | 管理员创建新的管理员账号 |
| 818 | PUT | `/user/admin/users/{userId}` | 更新用户信息（管理员） | 管理员更新指定用户的信息 |
| 855 | DELETE | `/user/admin/users/{userId}` | 删除用户（管理员） | 管理员删除指定用户 |
| 876 | PUT | `/user/admin/users/{userId}/role` | 更新用户角色（管理员，已废弃） | 管理员更新用户角色（此接口已废弃） |
| 909 | PUT | `/user/admin/users/{userId}/status` | 启用/禁用用户（管理员） | 管理员启用或禁用指定用户 |
| 941 | GET | `/user/admin/certifications` | 获取商家认证申请列表（管理员） | 管理员获取所有商家认证申请列表，支持状态筛选和分页 |
| 979 | PUT | `/user/admin/certifications/{id}` | 审核认证申请（管理员） | 管理员审核商家认证申请，通过或拒绝 |

---

## 二、茶叶模块（26个接口）

| 行号 | 方法 | 接口路径 | 接口名称 | 描述 |
|------|------|---------|---------|------|
| 1013 | GET | `/tea/list` | 获取茶叶列表 | 获取茶叶列表，支持分类、关键词、店铺筛选和排序 |
| 1072 | POST | `/tea/list` | 添加茶叶（商家/管理员） | 商家或管理员添加新茶叶 |
| 1115 | GET | `/tea/{id}` | 获取茶叶详情 | 根据茶叶ID获取茶叶详细信息 |
| 1143 | PUT | `/tea/{id}` | 更新茶叶信息（商家/管理员） | 商家或管理员更新茶叶信息 |
| 1188 | DELETE | `/tea/{id}` | 删除茶叶（商家/管理员） | 商家或管理员删除茶叶 |
| 1209 | GET | `/tea/categories` | 获取茶叶分类列表 | 获取所有茶叶分类 |
| 1230 | POST | `/tea/categories` | 创建茶叶分类（管理员） | 管理员创建新的茶叶分类 |
| 1260 | PUT | `/tea/categories/{id}` | 更新茶叶分类（管理员） | 管理员更新茶叶分类信息 |
| 1294 | DELETE | `/tea/categories/{id}` | 删除茶叶分类（管理员） | 管理员删除茶叶分类 |
| 1315 | GET | `/tea/{teaId}/reviews` | 获取茶叶评价列表 | 获取指定茶叶的评价列表，支持分页 |
| 1352 | GET | `/tea/{teaId}/reviews/stats` | 获取茶叶评价统计 | 获取茶叶评价的统计数据（平均评分、评分分布等） |
| 1379 | POST | `/tea/reviews` | 提交茶叶评价 | 用户提交对茶叶的评价 |
| 1416 | POST | `/tea/reviews/{reviewId}/reply` | 商家回复评价 | 商家对用户评价进行回复 |
| 1447 | POST | `/tea/reviews/{reviewId}/like` | 点赞评价 | 对茶叶评价进行点赞 |
| 1468 | GET | `/tea/{teaId}/specifications` | 获取茶叶规格列表 | 获取指定茶叶的所有规格 |
| 1491 | POST | `/tea/{teaId}/specifications` | 添加茶叶规格 | 为茶叶添加新的规格 |
| 1528 | PUT | `/tea/specifications/{specId}` | 更新茶叶规格 | 更新茶叶规格信息 |
| 1563 | DELETE | `/tea/specifications/{specId}` | 删除茶叶规格 | 删除指定的茶叶规格 |
| 1584 | PUT | `/tea/specifications/{specId}/default` | 设置默认规格 | 将指定规格设置为默认规格 |
| 1605 | POST | `/tea/{teaId}/images` | 上传茶叶图片 | 上传茶叶的图片 |
| 1640 | DELETE | `/tea/images/{imageId}` | 删除茶叶图片 | 删除指定的茶叶图片 |
| 1661 | PUT | `/tea/images/{imageId}/main` | 设置主图 | 将指定图片设置为主图 |
| 1682 | PUT | `/tea/images/order` | 更新图片顺序 | 更新茶叶图片的显示顺序 |
| 1715 | PUT | `/tea/{teaId}/status` | 更新茶叶状态（上架/下架） | 更新茶叶的上架/下架状态 |
| 1746 | PUT | `/tea/batch-status` | 批量更新茶叶状态 | 批量更新多个茶叶的状态 |
| 1775 | GET | `/tea/recommend` | 获取推荐茶叶 | 获取推荐茶叶列表（随机、相似、热门） |

---

## 三、店铺模块（26个接口）

| 行号 | 方法 | 接口路径 | 接口名称 | 描述 |
|------|------|---------|---------|------|
| 1809 | GET | `/shop/list` | 获取店铺列表 | 获取店铺列表，支持关键词、评分筛选和排序 |
| 1861 | POST | `/shop/list` | 创建店铺 | 创建新店铺 |
| 1889 | GET | `/shop/{id}` | 获取店铺详情 | 根据店铺ID获取店铺详细信息 |
| 1914 | PUT | `/shop/{id}` | 更新店铺信息 | 更新店铺的基本信息 |
| 1948 | GET | `/shop/my` | 获取我的店铺信息 | 获取当前用户的店铺信息 |
| 1970 | GET | `/shop/{shopId}/teas` | 获取店铺茶叶列表 | 获取指定店铺的茶叶列表 |
| 2001 | POST | `/shop/{shopId}/teas` | 添加茶叶到店铺 | 向店铺添加茶叶商品 |
| 2039 | PUT | `/shop/teas/{teaId}` | 更新店铺茶叶 | 更新店铺中的茶叶信息 |
| 2073 | DELETE | `/shop/teas/{teaId}` | 删除店铺茶叶 | 从店铺中删除茶叶 |
| 2094 | PUT | `/shop/teas/{teaId}/status` | 茶叶上下架 | 更新店铺中茶叶的上架/下架状态 |
| 2125 | GET | `/shop/{shopId}/statistics` | 获取店铺统计数据 | 获取店铺的统计数据（销售额、订单数等） |
| 2160 | POST | `/shop/{shopId}/logo` | 上传店铺Logo | 上传店铺Logo图片 |
| 2192 | GET | `/shop/{shopId}/banners` | 获取店铺Banner列表 | 获取店铺的Banner列表 |
| 2214 | POST | `/shop/{shopId}/banners` | 上传店铺Banner | 上传店铺Banner图片 |
| 2249 | PUT | `/shop/banners/{bannerId}` | 更新店铺Banner | 更新店铺Banner信息 |
| 2281 | DELETE | `/shop/banners/{bannerId}` | 删除店铺Banner | 删除指定的店铺Banner |
| 2302 | PUT | `/shop/banners/order` | 更新Banner顺序 | 更新店铺Banner的显示顺序 |
| 2333 | GET | `/shop/{shopId}/announcements` | 获取店铺公告列表 | 获取店铺的公告列表 |
| 2355 | POST | `/shop/{shopId}/announcements` | 创建店铺公告 | 创建新的店铺公告 |
| 2389 | PUT | `/shop/announcements/{announcementId}` | 更新店铺公告 | 更新店铺公告信息 |
| 2421 | DELETE | `/shop/announcements/{announcementId}` | 删除店铺公告 | 删除指定的店铺公告 |
| 2442 | POST | `/shop/{shopId}/follow` | 关注店铺 | 关注指定店铺 |
| 2461 | DELETE | `/shop/{shopId}/follow` | 取消关注店铺 | 取消关注指定店铺 |
| 2482 | GET | `/shop/{shopId}/follow-status` | 获取店铺关注状态 | 获取当前用户对店铺的关注状态 |
| 2504 | GET | `/shop/{shopId}/reviews` | 获取店铺评价列表 | 获取店铺的评价列表 |
| 2536 | POST | `/shop/{shopId}/reviews` | 提交店铺评价 | 提交对店铺的评价 |

---

## 四、订单模块（20个接口）

| 行号 | 方法 | 接口路径 | 接口名称 | 描述 |
|------|------|---------|---------|------|
| 2576 | GET | `/order/cart` | 获取购物车列表 | 获取当前用户的购物车列表 |
| 2599 | POST | `/order/cart/add` | 添加商品到购物车 | 将商品添加到购物车 |
| 2628 | PUT | `/order/cart/update` | 更新购物车商品 | 更新购物车中商品的数量或规格 |
| 2657 | DELETE | `/order/cart/remove` | 移除购物车商品 | 从购物车中移除商品 |
| 2678 | DELETE | `/order/cart/clear` | 清空购物车 | 清空当前用户的购物车 |
| 2693 | POST | `/order/create` | 创建订单 | 根据购物车创建订单 |
| 2735 | GET | `/order/list` | 获取订单列表 | 获取当前用户的订单列表，支持状态筛选和分页 |
| 2783 | GET | `/order/{id}` | 获取订单详情 | 根据订单ID获取订单详细信息 |
| 2815 | POST | `/order/pay` | 支付订单 | 支付指定订单 |
| 2843 | POST | `/order/cancel` | 取消订单 | 取消指定订单 |
| 2868 | POST | `/order/confirm` | 确认收货 | 确认订单已收货 |
| 2893 | POST | `/order/review` | 评价订单 | 对订单进行评价 |
| 2928 | POST | `/order/{id}/ship` | 发货 | 商家对订单进行发货操作 |
| 2959 | POST | `/order/batch-ship` | 批量发货 | 批量对多个订单进行发货 |
| 2990 | GET | `/order/{id}/logistics` | 获取订单物流信息 | 获取订单的物流跟踪信息 |
| 3018 | POST | `/order/refund` | 申请退款（兼容旧接口） | 申请订单退款 |
| 3045 | GET | `/order/{id}/refund` | 获取退款详情 | 获取订单退款详情 |
| 3071 | POST | `/order/{id}/refund/process` | 审批退款 | 商家或管理员审批退款申请 |
| 3104 | GET | `/order/statistics` | 获取订单统计数据 | 获取订单统计数据（订单数、金额、趋势等） |
| 3147 | GET | `/order/export` | 导出订单数据 | 导出订单数据为Excel或CSV格式 |

---

## 五、论坛模块（36个接口）

| 行号 | 方法 | 接口路径 | 接口名称 | 描述 |
|------|------|---------|---------|------|
| 3193 | GET | `/forum/home` | 获取论坛首页数据 | 获取论坛首页的数据（Banner、热门帖子、最新文章等） |
| 3217 | PUT | `/forum/home` | 更新论坛首页数据（管理员） | 管理员更新论坛首页数据 |
| 3238 | GET | `/forum/banners` | 获取论坛Banner列表 | 获取论坛Banner列表 |
| 3254 | POST | `/forum/banners` | 上传论坛Banner（管理员） | 管理员上传论坛Banner |
| 3283 | PUT | `/forum/banners/{id}` | 更新论坛Banner（管理员） | 管理员更新论坛Banner信息 |
| 3315 | DELETE | `/forum/banners/{id}` | 删除论坛Banner（管理员） | 管理员删除论坛Banner |
| 3336 | PUT | `/forum/banners/order` | 更新Banner顺序（管理员） | 管理员更新Banner显示顺序 |
| 3362 | GET | `/forum/articles` | 获取文章列表 | 获取文章列表，支持分类、关键词筛选和分页 |
| 3399 | POST | `/forum/articles` | 创建文章（管理员） | 管理员创建新文章 |
| 3432 | GET | `/forum/articles/{id}` | 获取文章详情 | 根据文章ID获取文章详细信息 |
| 3457 | PUT | `/forum/articles/{id}` | 更新文章（管理员） | 管理员更新文章信息 |
| 3491 | DELETE | `/forum/articles/{id}` | 删除文章（管理员） | 管理员删除文章 |
| 3512 | GET | `/forum/topics` | 获取版块列表 | 获取论坛版块列表 |
| 3528 | POST | `/forum/topics` | 创建版块（管理员） | 管理员创建新版块 |
| 3559 | GET | `/forum/topics/{id}` | 获取版块详情 | 根据版块ID获取版块详细信息 |
| 3581 | PUT | `/forum/topics/{id}` | 更新版块（管理员） | 管理员更新版块信息 |
| 3613 | DELETE | `/forum/topics/{id}` | 删除版块（管理员） | 管理员删除版块 |
| 3634 | GET | `/forum/posts` | 获取帖子列表 | 获取帖子列表，支持版块、关键词筛选和排序 |
| 3681 | POST | `/forum/posts` | 创建帖子 | 用户创建新帖子 |
| 3714 | GET | `/forum/posts/pending` | 获取待审核帖子列表（管理员） | 管理员获取待审核的帖子列表 |
| 3745 | GET | `/forum/posts/{id}` | 获取帖子详情 | 根据帖子ID获取帖子详细信息 |
| 3774 | PUT | `/forum/posts/{id}` | 更新帖子 | 更新帖子内容 |
| 3804 | DELETE | `/forum/posts/{id}` | 删除帖子 | 删除指定帖子 |
| 3825 | GET | `/forum/posts/{id}/replies` | 获取帖子回复列表 | 获取帖子的回复列表，支持分页 |
| 3858 | POST | `/forum/posts/{id}/replies` | 创建回复 | 对帖子进行回复 |
| 3891 | DELETE | `/forum/replies/{id}` | 删除回复 | 删除指定的回复 |
| 3912 | POST | `/forum/replies/{id}/like` | 点赞回复 | 对回复进行点赞 |
| 3931 | DELETE | `/forum/replies/{id}/like` | 取消点赞回复 | 取消对回复的点赞 |
| 3952 | POST | `/forum/posts/{id}/like` | 点赞帖子 | 对帖子进行点赞 |
| 3971 | DELETE | `/forum/posts/{id}/like` | 取消点赞帖子 | 取消对帖子的点赞 |
| 3992 | POST | `/forum/posts/{id}/favorite` | 收藏帖子 | 收藏指定帖子 |
| 4011 | DELETE | `/forum/posts/{id}/favorite` | 取消收藏帖子 | 取消收藏指定帖子 |
| 4032 | POST | `/forum/posts/{id}/approve` | 审核通过帖子（管理员） | 管理员审核通过帖子 |
| 4053 | POST | `/forum/posts/{id}/reject` | 审核拒绝帖子（管理员） | 管理员审核拒绝帖子 |
| 4084 | PUT | `/forum/posts/{id}/sticky` | 设置帖子置顶（管理员） | 管理员设置帖子置顶状态 |
| 4110 | PUT | `/forum/posts/{id}/essence` | 设置帖子精华（管理员） | 管理员设置帖子精华状态 |

---

## 六、消息模块（22个接口）

| 行号 | 方法 | 接口路径 | 接口名称 | 描述 |
|------|------|---------|---------|------|
| 4138 | GET | `/message/list` | 获取消息列表 | 获取消息列表，支持类型筛选和分页 |
| 4175 | GET | `/message/{id}` | 获取消息详情 | 根据消息ID获取消息详细信息 |
| 4202 | POST | `/message/send` | 发送消息 | 发送消息给指定用户 |
| 4232 | POST | `/message/read` | 标记消息为已读 | 批量标记消息为已读状态 |
| 4259 | POST | `/message/delete` | 删除消息 | 批量删除消息 |
| 4286 | GET | `/message/unread-count` | 获取未读消息数量 | 获取未读消息的数量统计 |
| 4304 | GET | `/message/notifications` | 获取通知列表 | 获取通知列表，支持类型筛选和分页 |
| 4341 | GET | `/message/notifications/{id}` | 获取通知详情 | 根据通知ID获取通知详细信息 |
| 4366 | DELETE | `/message/notifications/{id}` | 删除通知 | 删除指定通知 |
| 4387 | PUT | `/message/notifications/batch-read` | 批量标记通知为已读 | 批量标记通知为已读状态 |
| 4410 | DELETE | `/message/notifications/batch` | 批量删除通知 | 批量删除通知 |
| 4433 | GET | `/message/list/sessions` | 获取聊天会话列表 | 获取聊天会话列表 |
| 4457 | GET | `/message/list/history` | 获取聊天记录 | 获取与指定用户的聊天记录 |
| 4494 | POST | `/message/sessions` | 创建聊天会话 | 创建新的聊天会话 |
| 4521 | DELETE | `/message/sessions/{sessionId}` | 删除聊天会话 | 删除指定的聊天会话 |
| 4542 | PUT | `/message/sessions/{sessionId}/pin` | 置顶聊天会话 | 置顶或取消置顶聊天会话 |
| 4563 | POST | `/message/messages/image` | 发送图片消息 | 发送图片消息 |
| 4593 | GET | `/message/user/{userId}` | 获取用户主页信息 | 获取用户主页信息（用户信息、关注状态、统计数据） |
| 4623 | GET | `/message/user/{userId}/dynamic` | 获取用户动态 | 获取用户的动态列表 |
| 4657 | GET | `/message/user/{userId}/statistics` | 获取用户统计数据 | 获取用户的统计数据（帖子数、关注数、粉丝数等） |
| 4680 | GET | `/message/user/posts` | 获取用户发布的帖子列表 | 获取当前用户发布的帖子列表 |
| 4716 | GET | `/message/user/reviews` | 获取用户评价记录 | 获取当前用户的评价记录 |

---

## 七、公共模块（2个接口）

| 行号 | 方法 | 接口路径 | 接口名称 | 描述 |
|------|------|---------|---------|------|
| 4749 | POST | `/upload` | 通用文件上传 | 通用文件上传接口，支持多种文件类型 |
| 4779 | POST | `/upload/image` | 图片上传 | 专门用于图片上传的接口，支持生成缩略图 |

---

## 统计说明

1. **统计方法**：通过 grep 匹配所有 `get:`、`post:`、`put:`、`delete:` 行，并按模块标记行号分段统计
2. **行号说明**：行号指接口方法定义所在的行号（如 `post:`、`get:` 所在行）
3. **接口路径**：从 YAML 文件中提取的完整路径
4. **接口名称**：从 `summary` 字段提取的接口名称
5. **描述**：基于 `summary` 和 `description` 字段整理的简要描述

---

## 备注

- 本统计基于 `openapi.yaml.backup3` 文件
- 所有接口均包含在 OpenAPI 3.0.3 规范中
- 接口总数：**167个**

