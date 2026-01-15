# 状态码消息映射文档

## 状态码分段规则

| 范围 | 模块 | 说明 |
|------|------|------|
| 200 | HTTP | 成功 |
| 400 | HTTP | 请求错误 |
| 401 | HTTP | 未认证 |
| 403 | HTTP | 无权限 |
| 404 | HTTP | 资源不存在 |
| 500 | HTTP | 服务器错误 |
| 1xxx | 通用 | 通用操作 |
| 2xxx | 用户 | 用户模块 |
| 3xxx | 茶叶 | 茶叶模块 |
| 4xxx | 订单 | 订单模块 |
| 5xxx | 店铺 | 店铺模块 |
| 6xxx | 论坛 | 论坛模块 |
| 7xxx | 消息 | 消息模块 |

---

## 通用模块 (1xxx)

**说明**: 以下状态码从167个接口中提取，仅包含实际使用的状态码。

## 使用说明

1. 成功码一般为 x0xx 格式
2. 失败码一般为 x1xx 格式
3. 前端根据 code 查找对应消息显示
4. 后端返回格式: `{ code: number, data: any }`


---

# 接口状态码映射

本章节列出所有API接口及其对应的状态码，方便后端开发时参考。

## 一、用户模块接口 (user.js) - 共35个接口

### 1.1 认证相关 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 1 | login | POST | /user/login | 2000 | 2100, 2105 | 用户登录 |
| 2 | register | POST | /user/register | 2001 | 2101 | 用户注册 |
| 3 | logout | POST | /user/logout | 2002 | 2103 | 退出登录 |
| 4 | getCurrentUser | GET | /user/me | 200 | 2103 | 获取当前用户 |
| 5 | refreshToken | POST | /user/refresh | 200 | 2102, 2103 | 刷新令牌 |

### 1.2 用户信息相关 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 6 | getUserInfo | GET | /user/{userId} | 200 | 2120 | 获取用户信息 |
| 7 | updateUserInfo | PUT | /user | 2010 | 2110 | 更新用户信息 |
| 8 | uploadAvatar | POST | /user/avatar | 2012 | 2112, 1103, 1104 | 上传头像 |
| 9 | changePassword | PUT | /user/password | 2011 | 2111, 2113 | 修改密码 |
| 10 | resetPassword | POST | /user/password/reset | 2004 | 2104 | 密码重置 |

### 1.3 用户地址相关 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 11 | getAddressList | GET | /user/addresses | 200 | 4160 | 获取地址列表 |
| 12 | addAddress | POST | /user/addresses | 4060 | 4161 | 添加地址 |
| 13 | updateAddress | PUT | /user/addresses/{id} | 1004 | 4161 | 更新地址 |
| 14 | deleteAddress | DELETE | /user/addresses/{id} | 1003 | 1100 | 删除地址 |
| 15 | setDefaultAddress | PUT | /user/addresses/{id}/default | 1004 | 1100 | 设置默认地址 |

### 1.4 商家认证相关 (2个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 16 | submitShopCertification | POST | /user/shop-certification | 1000 | 1100 | 提交商家认证 |
| 17 | getShopCertificationStatus | GET | /user/shop-certification | 200 | 1102 | 获取认证状态 |

### 1.5 用户互动相关 (8个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 18 | getFollowList | GET | /user/follows | 200 | 1102 | 获取关注列表 |
| 19 | addFollow | POST | /user/follows | 5000 | 5102 | 添加关注 |
| 20 | removeFollow | DELETE | /user/follows/{id} | 5001 | 5102 | 取消关注 |
| 21 | getFavoriteList | GET | /user/favorites | 200 | 1102 | 获取收藏列表 |
| 22 | addFavorite | POST | /user/favorites | 3010, 6012 | 3110, 6112 | 添加收藏 |
| 23 | removeFavorite | DELETE | /user/favorites/{id} | 3011, 6013 | 3110, 6113 | 取消收藏 |
| 24 | addLike | POST | /user/likes | 6010 | 6110 | 点赞 |
| 25 | removeLike | DELETE | /user/likes/{id} | 6011 | 6111 | 取消点赞 |

### 1.6 用户偏好设置 (2个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 26 | getUserPreferences | GET | /user/preferences | 200 | 1102 | 获取用户偏好设置 |
| 27 | updateUserPreferences | PUT | /user/preferences | 2013 | 2114 | 更新用户偏好设置 |

### 1.7 管理员用户管理 (7个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 28 | getAdminUserList | GET | /user/admin/users | 200 | 2120, 2124 | 获取用户列表 |
| 29 | createAdmin | POST | /user/admin/users | 2023 | 2123, 2124 | 创建管理员 |
| 30 | updateUser | PUT | /user/admin/users/{userId} | 2022 | 2123, 2124 | 更新用户 |
| 31 | deleteUser | DELETE | /user/admin/users/{userId} | 2020 | 2121, 2124 | 删除用户 |
| 32 | updateUserRole | PUT | /user/admin/users/{userId}/role | 1000 | 1100, 2124 | 更新用户角色（已废弃） |
| 33 | toggleUserStatus | PUT | /user/admin/users/{userId}/status | 2021 | 2122, 2124 | 切换用户状态 |
| 34 | getCertificationList | GET | /user/admin/certifications | 200 | 1102, 2124 | 获取认证列表 |
| 35 | processCertification | PUT | /user/admin/certifications/{id} | 1000 | 1100, 2124 | 审核认证 |


## 二、茶叶模块接口 (tea.js) - 共26个接口

### 2.1 茶叶基础操作 (6个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 36 | getTeas | GET | /tea/list | 3000 | 3100 | 获取茶叶列表 |
| 37 | addTea | POST | /tea/list | 3026 | 3125 | 添加茶叶 |
| 38 | getTeaDetail | GET | /tea/{id} | 3001 | 3101 | 获取茶叶详情 |
| 39 | updateTea | PUT | /tea/{id} | 3025 | 3125 | 更新茶叶 |
| 40 | deleteTea | DELETE | /tea/{id} | 3024 | 3124 | 删除茶叶 |
| 61 | getRecommendTeas | GET | /tea/recommend | 200 | 3100 | 获取推荐茶叶 |

### 2.2 分类管理 (4个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 41 | getTeaCategories | GET | /tea/categories | 200 | 3130 | 获取分类列表 |
| 42 | createCategory | POST | /tea/categories | 3030 | 3130 | 创建分类 |
| 43 | updateCategory | PUT | /tea/categories/{id} | 3031 | 3130 | 更新分类 |
| 44 | deleteCategory | DELETE | /tea/categories/{id} | 3032 | 3131 | 删除分类 |

### 2.3 评价系统 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 45 | getTeaReviews | GET | /tea/{teaId}/reviews | 200 | 1102 | 获取评价列表 |
| 46 | getReviewStats | GET | /tea/{teaId}/reviews/stats | 200 | 1102 | 获取评价统计 |
| 47 | submitReview | POST | /tea/reviews | 4050 | 4150 | 提交评价 |
| 48 | replyReview | POST | /tea/reviews/{reviewId}/reply | 3013 | 3112 | 回复评价 |
| 49 | likeReview | POST | /tea/reviews/{reviewId}/like | 6010 | 3113 | 点赞评价 |

### 2.4 规格管理 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 50 | getTeaSpecifications | GET | /tea/{teaId}/specifications | 200 | 1102 | 获取规格列表 |
| 51 | addSpecification | POST | /tea/{teaId}/specifications | 1000 | 1100 | 添加规格 |
| 52 | updateSpecification | PUT | /tea/specifications/{specId} | 1004 | 1100 | 更新规格 |
| 53 | deleteSpecification | DELETE | /tea/specifications/{specId} | 1003 | 1100 | 删除规格 |
| 54 | setDefaultSpecification | PUT | /tea/specifications/{specId}/default | 1004 | 1100 | 设置默认规格 |

### 2.5 图片管理 (4个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 55 | uploadTeaImages | POST | /tea/{teaId}/images | 1001 | 1101, 1103, 1104 | 上传图片 |
| 56 | deleteTeaImage | DELETE | /tea/images/{imageId} | 1003 | 1100 | 删除图片 |
| 57 | setMainImage | PUT | /tea/images/{imageId}/main | 1004 | 1100 | 设置主图 |
| 58 | updateImageOrder | PUT | /tea/images/order | 1004 | 1100 | 更新图片顺序 |

### 2.6 状态管理 (2个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 59 | toggleTeaStatus | PUT | /tea/{teaId}/status | 3020, 3021 | 3120, 3121 | 上架/下架 |
| 60 | batchToggleTeaStatus | PUT | /tea/batch-status | 3022, 3023 | 3122, 3123 | 批量上架/下架 |


## 三、店铺模块接口 (shop.js) - 共26个接口

### 3.1 店铺基础操作 (6个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 62 | getShops | GET | /shop/list | 200 | 5101 | 获取店铺列表 |
| 63 | createShop | POST | /shop/list | 1000 | 1100 | 创建店铺 |
| 64 | getShopDetail | GET | /shop/{id} | 200 | 5100, 5105 | 获取店铺详情 |
| 65 | updateShop | PUT | /shop/{id} | 1004 | 1100 | 更新店铺信息 |
| 66 | getMyShop | GET | /shop/my | 200 | 5100 | 获取我的店铺 |
| 72 | getShopStatistics | GET | /shop/{shopId}/statistics | 200 | 1102 | 获取店铺统计 |

### 3.2 店铺茶叶管理 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 67 | getShopTeas | GET | /shop/{shopId}/teas | 200 | 5101 | 获取店铺茶叶 |
| 68 | addShopTea | POST | /shop/{shopId}/teas | 3026 | 3125 | 添加店铺茶叶 |
| 69 | updateShopTea | PUT | /shop/teas/{teaId} | 3025 | 3125 | 更新店铺茶叶 |
| 70 | deleteShopTea | DELETE | /shop/teas/{teaId} | 3024 | 3124 | 删除店铺茶叶 |
| 71 | toggleShopTeaStatus | PUT | /shop/teas/{teaId}/status | 3020, 3021 | 3120, 3121 | 茶叶上下架 |

### 3.3 Logo上传 (1个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 73 | uploadShopLogo | POST | /shop/{shopId}/logo | 5030 | 5130, 1103, 1104 | 上传Logo |

### 3.4 Banner管理 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 74 | getShopBanners | GET | /shop/{shopId}/banners | 200 | 5110 | 获取Banner列表 |
| 75 | uploadBanner | POST | /shop/{shopId}/banners | 5010 | 5111, 1103, 1104 | 上传Banner |
| 76 | updateBanner | PUT | /shop/banners/{bannerId} | 5011 | 5111 | 更新Banner |
| 77 | deleteBanner | DELETE | /shop/banners/{bannerId} | 5012 | 5112 | 删除Banner |
| 78 | updateBannerOrder | PUT | /shop/banners/order | 5013 | 5113 | 更新Banner排序 |

### 3.5 公告管理 (4个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 79 | getShopAnnouncements | GET | /shop/{shopId}/announcements | 200 | 5120 | 获取公告列表 |
| 80 | createAnnouncement | POST | /shop/{shopId}/announcements | 5020 | 5121 | 创建公告 |
| 81 | updateAnnouncement | PUT | /shop/announcements/{announcementId} | 5021 | 5121 | 更新公告 |
| 82 | deleteAnnouncement | DELETE | /shop/announcements/{announcementId} | 5022 | 5122 | 删除公告 |

### 3.6 店铺关注与评价 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 83 | followShop | POST | /shop/{shopId}/follow | 5000 | 5102 | 关注店铺 |
| 84 | unfollowShop | DELETE | /shop/{shopId}/follow | 5001 | 5102 | 取消关注 |
| 85 | checkFollowStatus | GET | /shop/{shopId}/follow-status | 200 | 1102 | 获取关注状态 |
| 86 | getShopReviews | GET | /shop/{shopId}/reviews | 200 | 5103 | 获取店铺评价 |
| 87 | submitShopReview | POST | /shop/{shopId}/reviews | 5002 | 5104 | 提交店铺评价 |


## 四、订单模块接口 (order.js) - 共20个接口

### 4.1 购物车相关 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 85 | getCartItems | GET | /order/cart | 200 | 4110 | 获取购物车 |
| 86 | addToCart | POST | /order/cart/add | 4010 | 4111, 4116, 4117, 4118 | 加入购物车 |
| 87 | updateCartItem | PUT | /order/cart/update | 4011, 4012 | 4112, 4116, 4117 | 更新购物车 |
| 88 | removeFromCart | DELETE | /order/cart/remove | 4013 | 4113 | 移除商品 |
| 89 | clearCart | DELETE | /order/cart/clear | 4015 | 4115 | 清空购物车 |

### 4.2 订单基础操作 (7个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 90 | createOrder | POST | /order/create | 4000 | 4100, 4116, 4118 | 创建订单 |
| 91 | getOrders | GET | /order/list | 200 | 1102 | 获取订单列表 |
| 92 | getOrderDetail | GET | /order/{id} | 200 | 4105, 4106, 4107 | 获取订单详情 |
| 93 | payOrder | POST | /order/pay | 4005, 4020 | 4120, 4121, 4122, 4124 | 支付订单 |
| 94 | cancelOrder | POST | /order/cancel | 4002 | 4102, 4105, 4106 | 取消订单 |
| 95 | confirmOrder | POST | /order/confirm | 4003 | 4103, 4105, 4106 | 确认收货 |
| 96 | reviewOrder | POST | /order/review | 4050 | 4150 | 评价订单 |

### 4.3 退款相关 (3个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 97 | refundOrder | POST | /order/refund | 4030 | 4130, 4105, 4106 | 申请退款 |
| 98 | processRefund | POST | /order/{id}/refund/process | 4031, 4032 | 4131, 4106 | 审批退款 |
| 99 | getRefundDetail | GET | /order/{id}/refund | 200 | 4132, 4105 | 获取退款详情 |

### 4.4 发货与物流 (3个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 100 | shipOrder | POST | /order/{id}/ship | 4004 | 4104, 4105, 4106 | 发货 |
| 101 | batchShipOrders | POST | /order/batch-ship | 4006 | 4108, 4106 | 批量发货 |
| 102 | getOrderLogistics | GET | /order/{id}/logistics | 200 | 4140, 4105 | 获取物流信息 |

### 4.5 统计与导出 (2个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 103 | getOrderStatistics | GET | /order/statistics | 200 | 1102 | 获取订单统计 |
| 104 | exportOrders | GET | /order/export | 200 | 1100 | 导出订单数据 |


## 五、论坛模块接口 (forum.js) - 共36个接口

### 5.1 首页与Banner (7个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 105 | getHomeData | GET | /forum/home | 200 | 6160 | 获取首页数据 |
| 106 | updateHomeData | PUT | /forum/home | 6060 | 6163 | 更新首页数据 |
| 107 | getBanners | GET | /forum/banners | 200 | 6161 | 获取Banner列表 |
| 108 | uploadBanner | POST | /forum/banners | 5010 | 5111, 1103, 1104 | 上传Banner |
| 109 | updateBanner | PUT | /forum/banners/{id} | 5011 | 5111 | 更新Banner |
| 110 | deleteBanner | DELETE | /forum/banners/{id} | 5012 | 5112 | 删除Banner |
| 111 | updateBannerOrder | PUT | /forum/banners/order | 5013 | 5113 | 更新Banner排序 |

### 5.2 文章管理 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 112 | getArticles | GET | /forum/articles | 200 | 6153 | 获取文章列表 |
| 113 | getArticleDetail | GET | /forum/articles/{id} | 200 | 6153 | 获取文章详情 |
| 114 | createArticle | POST | /forum/articles | 6050 | 6150 | 创建文章 |
| 115 | updateArticle | PUT | /forum/articles/{id} | 6051 | 6151 | 更新文章 |
| 116 | deleteArticle | DELETE | /forum/articles/{id} | 6052 | 6152 | 删除文章 |

### 5.3 版块管理 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 117 | getForumTopics | GET | /forum/topics | 200 | 6143 | 获取版块列表 |
| 118 | getTopicDetail | GET | /forum/topics/{id} | 200 | 6143 | 获取版块详情 |
| 119 | createTopic | POST | /forum/topics | 6040 | 6140 | 创建版块 |
| 120 | updateTopic | PUT | /forum/topics/{id} | 6041 | 6141 | 更新版块 |
| 121 | deleteTopic | DELETE | /forum/topics/{id} | 6042 | 6142 | 删除版块 |

### 5.4 帖子操作 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 122 | getForumPosts | GET | /forum/posts | 200 | 6103 | 获取帖子列表 |
| 123 | createPost | POST | /forum/posts | 6000 | 6100 | 创建帖子 |
| 124 | getPendingPosts | GET | /forum/posts/pending | 200 | 6136 | 获取待审核帖子 |
| 125 | getPostDetail | GET | /forum/posts/{id} | 200 | 6104 | 获取帖子详情 |
| 126 | updatePost | PUT | /forum/posts/{id} | 6001 | 6101 | 更新帖子 |
| 127 | deletePost | DELETE | /forum/posts/{id} | 6002 | 6102 | 删除帖子 |

### 5.5 帖子互动 (4个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 128 | likePost | POST | /forum/posts/{id}/like | 6010 | 6110 | 点赞帖子 |
| 129 | unlikePost | DELETE | /forum/posts/{id}/like | 6011 | 6111 | 取消点赞 |
| 130 | favoritePost | POST | /forum/posts/{id}/favorite | 6012 | 6112 | 收藏帖子 |
| 131 | unfavoritePost | DELETE | /forum/posts/{id}/favorite | 6013 | 6113 | 取消收藏 |

### 5.6 回复管理 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 132 | getPostReplies | GET | /forum/posts/{id}/replies | 200 | 6123 | 获取回复列表 |
| 133 | createReply | POST | /forum/posts/{id}/replies | 6022 | 6122 | 创建回复 |
| 134 | deleteReply | DELETE | /forum/replies/{id} | 6021 | 6121 | 删除回复 |
| 135 | likeReply | POST | /forum/replies/{id}/like | 6010 | 6110 | 点赞回复 |
| 136 | unlikeReply | DELETE | /forum/replies/{id}/like | 6011 | 6111 | 取消点赞回复 |

### 5.7 内容审核 (4个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 137 | approvePost | POST | /forum/posts/{id}/approve | 6034 | 6134 | 审核通过 |
| 138 | rejectPost | POST | /forum/posts/{id}/reject | 6035 | 6135 | 审核拒绝 |
| 139 | togglePostSticky | PUT | /forum/posts/{id}/sticky | 6030, 6031 | 6130, 6131 | 置顶/取消置顶 |
| 140 | togglePostEssence | PUT | /forum/posts/{id}/essence | 6032, 6033 | 6132, 6133 | 加精/取消加精 |


## 六、消息模块接口 (message.js) - 共22个接口

### 6.1 消息基础操作 (6个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 141 | getMessages | GET | /message/list | 200 | 7101 | 获取消息列表 |
| 142 | getMessageDetail | GET | /message/{id} | 200 | 7101 | 获取消息详情 |
| 143 | sendMessage | POST | /message/send | 7000 | 7100 | 发送消息 |
| 144 | markAsRead | POST | /message/read | 7010 | 7110 | 标记已读 |
| 145 | deleteMessages | POST | /message/delete | 7012 | 1100 | 删除消息 |
| 146 | getUnreadCount | GET | /message/unread-count | 200 | 7101 | 获取未读数量 |

### 6.2 通知管理 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 147 | getNotifications | GET | /message/notifications | 200 | 7101 | 获取通知列表 |
| 148 | getNotificationDetail | GET | /message/notifications/{id} | 200 | 7101 | 获取通知详情 |
| 149 | deleteNotification | DELETE | /message/notifications/{id} | 7012 | 1100 | 删除通知 |
| 150 | batchMarkAsRead | PUT | /message/notifications/batch-read | 7011 | 7110 | 批量标记已读 |
| 151 | batchDeleteNotifications | DELETE | /message/notifications/batch | 7013 | 1100 | 批量删除通知 |

### 6.3 聊天会话 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 152 | getChatSessions | GET | /message/list/sessions | 200 | 7101 | 获取会话列表 |
| 153 | getChatHistory | GET | /message/list/history | 200 | 7101 | 获取聊天记录 |
| 154 | createChatSession | POST | /message/sessions | 1000 | 1100 | 创建会话 |
| 155 | pinChatSession | PUT | /message/sessions/{sessionId}/pin | 7014 | 1100 | 置顶会话 |
| 156 | deleteChatSession | DELETE | /message/sessions/{sessionId} | 7001 | 1100 | 删除会话 |

### 6.4 图片消息 (1个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 157 | sendImageMessage | POST | /message/messages/image | 7003 | 7103, 1103, 1104 | 发送图片消息 |

### 6.5 用户主页 (5个)

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 158 | getUserProfile | GET | /message/user/{userId} | 200 | 7120, 7121 | 获取用户主页 |
| 159 | getUserDynamic | GET | /message/user/{userId}/dynamic | 200 | 7120 | 获取用户动态 |
| 160 | getUserStatistics | GET | /message/user/{userId}/statistics | 200 | 7120 | 获取用户统计 |
| 161 | getUserPosts | GET | /message/user/posts | 200 | 1102 | 获取用户帖子 |
| 162 | getUserReviews | GET | /message/user/reviews | 200 | 1102 | 获取用户评价 |

## 七、系统通用接口 (upload.js) - 共2个接口

| 序号 | 接口函数 | 请求方式 | 路径 | 成功码 | 失败码 | 备注 |
|------|----------|----------|------|--------|--------|------|
| 163 | uploadFile | POST | /upload | 1001 | 1101, 1103, 1104 | 上传文件 |
| 164 | uploadImage | POST | /upload/image | 1001 | 1101, 1103, 1104 | 上传图片 |

---

## 接口统计汇总

| 模块 | 接口数量 | 状态码范围 |
|------|----------|------------|
| 用户模块 | 35 | 2xxx |
| 茶叶模块 | 26 | 3xxx |
| 店铺模块 | 26 | 5xxx |
| 订单模块 | 20 | 4xxx |
| 论坛模块 | 36 | 6xxx |
| 消息模块 | 22 | 7xxx |
| 系统通用 | 2 | 1xxx |
| **总计** | **167** | - |

---

## 使用建议

1. 后端Controller返回时，根据本文档选择对应的状态码
2. 前端根据状态码自动匹配消息显示
3. 新增接口时，按照分段规则分配状态码
4. 定期检查是否有遗漏的状态码需要补充
