# 项目图片上传场景统计

## 概述
本文档统计了项目中所有图片上传场景，以及每个场景使用的上传方案和对应的接口。

## 上传方案说明

### 方案一：先上传拿URL，再决定下一步
- **特点**：上传接口只负责保存文件并返回URL，业务数据通过其他接口提交
- **适用场景**：富文本图片、帖子配图、通用素材、头像等
- **流程**：上传图片 → 获取URL → 将URL插入富文本/提交业务数据

### 方案二：一次性提交（文件+业务数据一起）
- **特点**：业务接口同时接收文件和其他表单数据，一次性完成文件保存和数据创建
- **适用场景**：认证图片、轮播图、强绑定业务数据的图片
- **流程**：选择文件 → 填写表单 → 一次性提交（文件+数据）

---

## 详细场景统计

### 1. 用户模块（User Module）

#### 1.1 头像上传
- **场景**：用户个人中心修改头像
- **接口**：`POST /user/avatar`
- **方案**：方案一（先上传拿URL，再存数据库）
- **前端文件**：
  - API: `src/api/user.js` - `uploadAvatar(file)`
  - Store: `src/stores/user.js` - `uploadAvatar(file)`
  - 组件: `src/views/user/profile/ProfilePage.vue`
- **后端文件**：
  - Controller: `UserController.java` - `uploadAvatar(@RequestParam("file") MultipartFile file)`
- **说明**：上传后直接返回URL，前端调用`updateUserInfo`将URL存入用户表

#### 1.2 商家认证图片上传
- **场景**：商家认证申请时上传身份证正反面、营业执照
- **接口**：`POST /user/merchant/certification/image`
- **方案**：方案一（先上传拿URL，再存数据库）
- **前端文件**：
  - API: `src/api/user.js` - `uploadCertificationImage(file, type)`
  - Store: `src/stores/user.js` - `uploadCertificationImage({ file, type })`
  - 组件: `src/views/user/auth/MerchantApplication.vue`
- **后端文件**：
  - Controller: `UserController.java` - `uploadCertificationImage(@RequestParam("file") MultipartFile file, @RequestParam("type") String type)`
- **说明**：上传后返回URL，前端暂存URL，提交认证申请时一起提交
- **⚠️ 优化建议**：可改为方案二，将图片上传和认证申请合并为一个接口

---

### 2. 茶叶模块（Tea Module）

#### 2.1 茶叶图片上传
- **场景**：添加/编辑茶叶时上传多张商品图片
- **接口**：`POST /tea/{teaId}/images`
- **方案**：方案一（先上传拿URL，再存数据库）
- **前端文件**：
  - API: `src/api/tea.js` - `uploadTeaImages(teaId, formData)`
  - Store: `src/stores/tea.js` - `uploadTeaImages({ teaId, files })`
  - 组件: `src/views/tea/manage/TeaManagePage.vue`
- **后端文件**：
  - Controller: `TeaController.java` - `uploadTeaImages(@PathVariable String teaId, @RequestParam("files") MultipartFile[] files)`
- **说明**：支持批量上传多张图片，上传后返回图片列表，前端在编辑茶叶时调用

---

### 3. 商店模块（Shop Module）

#### 3.1 店铺Logo上传
- **场景**：商家修改店铺Logo
- **接口**：`POST /shop/{shopId}/logo`
- **方案**：方案一（先上传拿URL，再存数据库）
- **前端文件**：
  - API: `src/api/shop.js` - `uploadShopLogo(shopId, file)`
  - Store: `src/stores/shop.js` - `uploadShopLogo({ shopId, file })`
  - 组件: `src/views/shop/manage/ShopManagePage.vue`
- **后端文件**：
  - Controller: `ShopController.java` - `uploadShopLogo(@PathVariable String shopId, @RequestParam("file") MultipartFile file)`
- **说明**：上传后返回URL，前端调用`updateShop`将URL存入店铺表

#### 3.2 店铺Banner上传
- **场景**：商家添加/编辑店铺轮播Banner
- **接口**：`POST /shop/{shopId}/banners`
- **方案**：方案二（一次性提交，文件+其他数据一起）
- **前端文件**：
  - API: `src/api/shop.js` - `uploadBanner(shopId, bannerData)`
  - Store: `src/stores/shop.js` - `uploadBanner(shopId, file, { linkUrl, sortOrder })`
  - 组件: `src/views/shop/manage/ShopManagePage.vue`, `src/views/shop/manage/ShopSettingsPage.vue`
- **后端文件**：
  - Controller: `ShopController.java` - `uploadBanner(@PathVariable String shopId, @RequestParam("image") MultipartFile file, @RequestParam(value = "linkUrl", required = false) String linkUrl, @RequestParam(value = "sortOrder", required = false) Integer sortOrder)`
- **说明**：一次性提交图片文件、链接URL、排序等数据

---

### 4. 论坛模块（Forum Module）

#### 4.1 首页轮播图上传
- **场景**：管理员添加/编辑首页轮播图
- **接口**：`POST /forum/banners`
- **方案**：方案二（一次性提交，文件+其他数据一起）✅ **已修复**
- **前端文件**：
  - API: `src/api/forum.js` - `uploadBanner(formData)`
  - Store: `src/stores/forum.js` - `uploadBanner(formData)`
  - 组件: `src/views/forum/manage/CultureManagerPage.vue`
- **后端文件**：
  - Controller: `ForumController.java` - `uploadBanner(@RequestParam("file") MultipartFile file, @RequestParam(value = "title", required = false) String title, @RequestParam(value = "subtitle", required = false) String subtitle, @RequestParam(value = "linkUrl", required = false) String linkUrl)`
- **说明**：一次性提交图片文件、标题、副标题、链接URL等数据
- **修复记录**：之前错误地使用了方案一（先上传拿URL），已修复为方案二

#### 4.2 帖子图片上传
- **场景**：用户发帖时插入图片到富文本编辑器
- **接口**：`POST /forum/posts/image`
- **方案**：方案一（先上传拿URL，插入富文本）
- **前端文件**：
  - API: `src/api/forum.js` - `uploadPostImage(file)`
  - Store: `src/stores/forum.js` - `uploadPostImage(file)`
  - 组件: `src/views/forum/list/ForumListPage.vue`（富文本编辑器）
- **后端文件**：
  - Controller: `ForumController.java` - `uploadPostImage(@RequestParam("file") MultipartFile file)`
- **说明**：上传后返回URL，前端将URL插入富文本编辑器，提交帖子时URL已在内容中

#### 4.3 文章图片上传
- **场景**：管理员创建/编辑茶文化文章时插入图片
- **接口**：`POST /forum/articles/image`
- **方案**：方案一（先上传拿URL，插入富文本）
- **前端文件**：
  - API: `src/api/forum.js` - `uploadArticleImage(file)`
  - Store: `src/stores/forum.js`（未找到对应的store方法，可能直接调用API）
  - 组件: `src/views/forum/manage/CultureManagerPage.vue`（富文本编辑器）
- **后端文件**：
  - Controller: `ForumController.java` - `uploadArticleImage(@RequestParam("file") MultipartFile file)`
- **说明**：上传后返回URL，前端将URL插入富文本编辑器，提交文章时URL已在内容中

---

### 5. 订单模块（Order Module）

#### 5.1 评价图片上传
- **场景**：用户评价订单时上传商品图片
- **接口**：`POST /order/review/image`
- **方案**：方案一（先上传拿URL，再存数据库）
- **前端文件**：
  - API: `src/api/order.js` - `uploadReviewImage(file)`
  - Store: `src/stores/order.js` - `uploadReviewImage(file)`
  - 组件: `src/views/order/review/OrderReviewPage.vue`
- **后端文件**：
  - Controller: `OrderController.java` - `uploadReviewImage(@RequestParam("file") MultipartFile file)`
- **说明**：支持上传多张图片，上传后返回URL数组，提交评价时一起提交

---

### 6. 消息模块（Message Module）

#### 6.1 聊天图片上传
- **场景**：用户聊天时发送图片消息
- **接口**：`POST /message/messages/image`
- **方案**：方案二（一次性提交，图片+消息数据一起）
- **前端文件**：
  - API: `src/api/message.js` - `sendImageMessage(data)`
  - 组件: `src/views/message/chat/ChatPage.vue`（聊天输入组件）
- **后端文件**：
  - Controller: `MessageController.java` - `sendImageMessage(@RequestParam("sessionId") String sessionId, @RequestParam("receiverId") String receiverId, @RequestParam("image") MultipartFile image)`
- **说明**：一次性提交图片文件、会话ID、接收者ID等数据，直接创建消息记录

---

## 统计汇总

### 按方案分类

#### 方案一（先上传拿URL）：共 7 个场景
1. ✅ 用户头像上传
2. ✅ 商家认证图片上传（建议优化为方案二）
3. ✅ 茶叶图片上传
4. ✅ 店铺Logo上传
5. ✅ 帖子图片上传
6. ✅ 文章图片上传
7. ✅ 评价图片上传

#### 方案二（一次性提交）：共 3 个场景
1. ✅ 店铺Banner上传
2. ✅ 首页轮播图上传（已修复）
3. ✅ 聊天图片上传

---

## 优化建议

### 1. 商家认证图片上传
- **当前方案**：方案一（先上传拿URL，再提交认证申请）
- **建议方案**：方案二（一次性提交，图片+认证数据一起）
- **理由**：认证图片与认证申请强绑定，不需要独立存在，一次性提交更符合业务逻辑

### 2. 其他场景
- 当前方案基本合理，符合各自业务特点
- 富文本图片（帖子、文章）必须使用方案一，因为需要在编辑器中预览和插入
- 头像、Logo等独立资源使用方案一，便于复用和替换

---

## 接口列表（完整）

| 序号 | 场景 | 接口 | 方法 | 方案 | 状态 |
|------|------|------|------|------|------|
| 1 | 用户头像上传 | `/user/avatar` | POST | 方案一 | ✅ |
| 2 | 商家认证图片上传 | `/user/merchant/certification/image` | POST | 方案一 | ⚠️ 建议优化 |
| 3 | 茶叶图片上传 | `/tea/{teaId}/images` | POST | 方案一 | ✅ |
| 4 | 店铺Logo上传 | `/shop/{shopId}/logo` | POST | 方案一 | ✅ |
| 5 | 店铺Banner上传 | `/shop/{shopId}/banners` | POST | 方案二 | ❌ **缺少上传功能** |
| 6 | 首页轮播图上传 | `/forum/banners` | POST | 方案二 | ✅ 已修复 |
| 7 | 帖子图片上传 | `/forum/posts/image` | POST | 方案一 | ❌ **上传逻辑有问题** |
| 8 | 文章图片上传 | `/forum/articles/image` | POST | 方案一 | ✅ |
| 9 | 评价图片上传 | `/order/review/image` | POST | 方案一 | ✅ |
| 10 | 聊天图片上传 | `/message/messages/image` | POST | 方案二 | ✅ |

---

## 注意事项

1. **方案一接口**：只负责保存文件并返回URL，不涉及业务数据
2. **方案二接口**：同时接收文件和其他表单数据，一次性完成业务操作
3. **前端实现**：
   - 方案一：先调用上传接口获取URL，再调用业务接口提交数据
   - 方案二：构建FormData包含文件和其他字段，一次性提交
4. **后端实现**：
   - 方案一：只处理文件上传，返回URL
   - 方案二：处理文件上传+业务逻辑，返回完整业务对象

---

## 业务完整性评估

### 评估方法
对每个场景进行业务流程完整性检查，确认：
1. 前端是否正确调用上传接口和业务接口
2. 后端是否正确处理文件保存和业务数据存储
3. 数据流是否完整，是否存在缺失环节

---

### 详细评估结果

#### 1. 用户头像上传 ✅ **业务完整**
- **前端流程**：
  - 上传头像 → 获取URL → 自动刷新用户信息（`getUserInfo`）
- **后端流程**：
  - 接收文件 → 保存文件 → **自动更新用户表avatar字段** → 返回URL
- **评估**：✅ 后端在`uploadAvatar`中直接调用`userMapper.updateAvatar(userId, relativePath)`更新数据库，前端刷新用户信息即可看到新头像，业务流程完整。

#### 2. 商家认证图片上传 ✅ **业务完整**
- **前端流程**：
  - 上传图片（3张）→ 获取URL → 暂存在表单 → 提交认证申请时一起提交
- **后端流程**：
  - `uploadCertificationImage`：只保存文件返回URL
  - `submitShopCertification`：接收DTO包含`idCardFront`、`idCardBack`、`businessLicense`字段，保存到认证表
- **评估**：✅ 前端正确将URL存入表单，后端`SubmitShopCertificationDTO`包含图片URL字段，提交时正确保存，业务流程完整。

#### 3. 茶叶图片上传 ✅ **业务完整**
- **前端流程**：
  - 创建/更新茶叶 → 获取teaId → 上传图片（批量）→ 设置主图/排序
- **后端流程**：
  - `addTea`/`updateTea`：创建/更新茶叶基本信息
  - `uploadTeaImages`：保存图片文件并关联到茶叶
- **评估**：✅ 前端在创建茶叶后获取teaId再上传图片，后端正确关联图片和茶叶，业务流程完整。

#### 4. 店铺Logo上传 ✅ **业务完整**
- **前端流程**：
  - 上传Logo → 获取URL → 自动刷新店铺信息（`fetchMyShop`）
- **后端流程**：
  - 接收文件 → 保存文件 → **自动更新店铺表logo字段** → 返回URL
- **评估**：✅ 后端在`uploadShopLogo`中直接调用`shopMapper.updateById(shop)`更新数据库，前端刷新店铺信息即可看到新Logo，业务流程完整。

#### 5. 店铺Banner上传 ❌ **业务不完整 - 缺少图片上传功能**
- **前端流程**：
  - **问题**：只有手动输入图片URL的输入框，没有图片上传组件
  - 当前实现：填写表单（包括手动输入image_url） → 提交bannerData（只包含URL字符串）
- **后端流程**：
  - `uploadBanner`：需要接收文件（`@RequestParam("image") MultipartFile file`）和其他参数
- **评估**：❌ **严重问题** - 前端没有图片上传组件，只有URL输入框，但后端接口需要接收文件。前端提交的`bannerData`只包含URL字符串，后端无法接收文件，导致业务失败。
- **修复方案**：参考首页轮播图的实现，添加`el-upload`组件，改为一次性提交文件+数据。
  - **补充隐患A（字段名不一致）**：后端创建接口接收`image/title/linkUrl`，更新接口只识别`title/linkUrl/sortOrder`；但前端表单字段是`image_url/link_url/order/is_enabled`，需要提交时做字段映射，否则编辑/排序不会生效。
  - **补充隐患B（store/api签名错误）**：`shopStore.uploadShopBanner`曾错误调用`uploadBanner(shopId, file, {...})`，而`api/shop.js`的`uploadBanner`只接收`(shopId, bannerData)`；必须统一为`FormData(image,title,linkUrl)`。

#### 6. 首页轮播图上传 ✅ **业务完整（已修复）**
- **前端流程**：
  - 选择文件 + 填写表单 → 一次性提交（FormData包含file、title、subtitle、linkUrl）
- **后端流程**：
  - `uploadBanner`：接收文件和其他参数 → 保存文件 → 创建Banner记录
- **评估**：✅ **已修复**，前端现在使用FormData一次性提交，后端接口签名匹配，业务流程完整。
- **修复记录**：之前前端错误地先上传图片获取URL，然后只提交URL和其他数据，但后端接口需要文件，导致500错误。已修复为一次性提交。

#### 7. 帖子图片上传 ⚠️ **业务不完整 - 图片上传逻辑有问题**
- **前端流程**：
  - **问题1**：使用`el-upload`组件，但`:auto-upload="false"`，图片不会自动上传
  - **问题2**：提交时使用`postForm.images.map(file => file.url || file.response?.url || '')`，但file对象只有`raw`属性（File对象），没有`url`属性
  - 当前实现：选择图片 → 图片只存在本地（未上传） → 提交时images数组为空 → 图片丢失
- **后端流程**：
  - `uploadPostImage`：保存文件返回URL（但前端没有调用）
  - `createPost`：接收`topicId(Integer)`与`images(String类型，JSON字符串)`，保存到帖子表
- **评估**：❌ **严重问题** - 前端选择了图片但没有上传，提交时images数组为空，图片无法保存。用户选择图片后应该先调用`uploadPostImage`上传获取URL，然后再提交。
- **修复方案**：
  1. 在`submitPost`中，先遍历`postForm.images`，对每个有`raw`属性的文件调用`uploadPostImage`上传
  2. 收集所有上传后的URL，`submitData.images = JSON.stringify(urls)`（后端是String）
  3. 字段名从`categoryId`改为`topicId`（后端DTO要求）
  4. 然后再调用`createPost`提交

#### 8. 文章图片上传 ✅ **业务完整**
- **前端流程**：
  - 上传图片 → 获取URL → 插入富文本编辑器 → 提交文章时URL已在content中
- **后端流程**：
  - `uploadArticleImage`：保存文件返回URL
  - `createArticle`：接收content字段（包含图片URL的HTML）
- **评估**：✅ 图片URL已嵌入到content中，提交文章时一起保存，业务流程完整。

#### 9. 评价图片上传 ✅ **业务完整**
- **前端流程**：
  - 上传图片（多张）→ 获取URL数组 → 提交评价时包含images字段
- **后端流程**：
  - `uploadReviewImage`：保存文件返回URL
  - `reviewOrder`：接收images字段（List<String>），序列化为JSON存入评价表
- **评估**：✅ 前端正确收集所有图片URL并作为数组提交，后端正确接收并保存，业务流程完整。

#### 10. 聊天图片上传 ✅ **业务完整**
- **前端流程**：
  - 选择文件 + 会话信息 → 一次性提交（FormData包含image、sessionId、receiverId）
- **后端流程**：
  - `sendImageMessage`：接收文件和其他参数 → 保存文件 → 创建消息记录
- **评估**：✅ 前端使用FormData一次性提交，后端接口签名匹配，业务流程完整。

---

## 潜在问题分析

### 已发现并修复的问题

#### 问题1：首页轮播图上传业务流程不完整 ❌ → ✅ **已修复**
- **问题描述**：
  - 前端先上传图片获取URL，然后只提交URL和其他数据
  - 后端接口`POST /forum/banners`需要接收文件（`@RequestParam("file") MultipartFile file`）
  - 导致500错误：缺少必需的文件参数
- **根本原因**：
  - 前端误用了方案一（先上传拿URL），但后端接口设计为方案二（一次性提交）
- **修复方案**：
  - 前端改为一次性提交：构建FormData包含file、title、subtitle、linkUrl，一次性调用`uploadBanner`
- **修复状态**：✅ 已完成

#### 问题2：帖子图片上传逻辑不完整 ❌ → ✅ **已修复**
- **问题描述**：
  - 前端使用`el-upload`组件，但`:auto-upload="false"`，图片不会自动上传
  - 提交时使用`postForm.images.map(file => file.url || file.response?.url || '')`，但file对象只有`raw`属性，没有`url`
  - 导致用户选择的图片无法上传，提交时images数组为空，图片丢失
- **根本原因**：
  - 前端没有在提交前先上传图片获取URL
  - 期望file对象有url属性，但实际上只有raw属性（File对象）
- **影响范围**：
  - `ForumListPage.vue`：发帖功能，用户选择的图片无法保存
- **修复方案**：
  1. 在`submitPost`方法中，先遍历`postForm.images`
  2. 对每个有`raw`属性的文件，调用`forumStore.uploadPostImage(file.raw)`上传
  3. 收集所有上传后的URL，放入`submitData.images`数组
  4. 然后再调用`createPost`提交
- **修复状态**：✅ 已完成（`ForumListPage.vue`：提交前上传图片 + `topicId/images`字段对齐后端DTO）

#### 问题3：店铺Banner上传缺少图片上传功能 ❌ → ✅ **已修复（ShopSettingsPage入口）**
- **问题描述**：
  - 前端只有手动输入图片URL的输入框（`<el-input v-model="currentBanner.image_url">`），**完全没有图片上传组件**
  - 后端接口`POST /shop/{shopId}/banners`需要接收文件（`@RequestParam("image") MultipartFile file`）
  - 前端提交的`bannerData`只包含URL字符串，后端无法接收文件
  - **导致业务完全无法使用**：用户无法上传图片，只能手动输入URL
- **根本原因**：
  - 前端实现不完整，缺少`el-upload`组件
  - 前端提交方式错误：只提交URL字符串，但后端需要文件
- **影响范围**：
  - `ShopSettingsPage.vue`：店铺设置页的轮播图管理
  - `ShopManagePage.vue`：店铺管理页的轮播图管理（如果也有Banner管理功能）
- **修复方案**：
  1. 在`ShopSettingsPage.vue`的Banner对话框添加`el-upload`组件（支持选择图片+预览）
  2. `shopStore.uploadShopBanner`改为提交`FormData(image,title,linkUrl)`
  3. 更新Banner时做字段映射：`link_url/order` → `linkUrl/sortOrder`
- **修复状态**：✅ 已完成（已覆盖`/shop/settings`轮播图管理入口；若后续确认`ShopManagePage.vue`仍有独立Banner弹窗入口，需要同步同一套逻辑）

### 其他场景状态

- ✅ **10个场景业务完整**：用户头像、商家认证图片、茶叶图片、店铺Logo、店铺Banner、首页轮播图、帖子图片、文章图片、评价图片、聊天图片

---

## 最小化修复方案总结

### 当前状态
- ✅ **10个场景业务完整**
- ✅ **2个场景在本轮修复中打通**：帖子图片上传、店铺Banner上传

### 待修复的场景

本轮已将“功能不可用”的两条链路修通；剩余工作主要是**确认是否存在其他入口复用了旧逻辑**（例如`ShopManagePage.vue`里若后续启用Banner弹窗，需要同步同一套上传+字段映射）。

#### 问题1：帖子图片上传 - 最小化修复方案

**问题**：前端选择了图片但没有上传，提交时images数组为空。

**修复步骤**：

1. **修改 `ForumListPage.vue` 的 `submitPost` 方法**：
   ```javascript
   const submitPost = async () => {
     if (!postFormRef.value) return
     
     const valid = await postFormRef.value.validate().catch(() => false)
     if (!valid) return
     
     localLoading.submit = true
     
     try {
       // 先上传所有图片
       const imageUrls = []
       for (const file of postForm.images) {
         if (file.raw) {
           try {
             const uploadRes = await forumStore.uploadPostImage(file.raw)
             if (uploadRes && uploadRes.data && uploadRes.data.url) {
               imageUrls.push(uploadRes.data.url)
             }
           } catch (error) {
             console.error('图片上传失败:', error)
             ElMessage.error('图片上传失败，请重试')
             return
           }
         } else if (file.url) {
           // 如果已经有URL（编辑时保留的图片），直接使用
           imageUrls.push(file.url)
         }
       }
       
       // 准备提交数据
       const submitData = {
         title: postForm.title,
         categoryId: postForm.categoryId,
         content: postForm.content,
         images: imageUrls.length > 0 ? JSON.stringify(imageUrls) : null
       }
       
       // 调用API
       const res = await forumStore.createPost(submitData)
       showByCode(res.code)
       
       if (res.code === 6011) {
         dialogVisible.post = false
         postFormRef.value.resetFields()
         postForm.images = []
       }
     } catch (error) {
       console.error('发布帖子失败:', error)
     } finally {
       localLoading.submit = false
     }
   }
   ```

**修复优先级**：🔴 **高优先级** - 用户选择的图片无法保存

---

#### 问题2：店铺Banner上传 - 最小化修复方案

**问题**：前端只有URL输入框，没有图片上传组件，后端需要接收文件。

**修复步骤**：

1. **修改 `ShopSettingsPage.vue` 的Banner对话框**：
   ```vue
   <!-- 将图片URL输入框改为图片上传组件 -->
   <el-form-item label="Banner图片" prop="image">
     <el-upload
       class="banner-uploader"
       :show-file-list="false"
       :http-request="handleBannerImageUpload"
       accept="image/*"
     >
       <img v-if="bannerForm.image_url" :src="bannerForm.image_url" class="banner-image" />
       <el-icon v-else class="banner-uploader-icon"><Plus /></el-icon>
     </el-upload>
   </el-form-item>
   ```

2. **修改 `handleSaveBanner` 方法**：
   ```javascript
   const bannerUploadFile = ref(null) // 存储选中的文件
   
   const handleBannerImageUpload = async ({ file }) => {
     bannerUploadFile.value = file
     bannerForm.image_url = URL.createObjectURL(file) // 本地预览
   }
   
   const handleSaveBanner = async () => {
     if (!shop.value || !shop.value.id) {
       showByCode(4103)
       return
     }
     
     if (!bannerUploadFile.value && !isEditBanner.value) {
       ElMessage.warning('请上传Banner图片')
       return
     }
     
     try {
       let res
       if (isEditBanner.value) {
         // 编辑：只更新文本字段（不更新图片）
         res = await shopStore.updateBanner({
           bannerId: currentBanner.value.id,
           bannerData: {
             title: currentBanner.value.title,
             link_url: currentBanner.value.link_url,
             order: currentBanner.value.order,
             is_enabled: currentBanner.value.is_enabled
           }
         })
       } else {
         // 新建：一次性提交文件+数据
         const formData = new FormData()
         formData.append('image', bannerUploadFile.value)
         formData.append('title', currentBanner.value.title || '')
         if (currentBanner.value.link_url) {
           formData.append('linkUrl', currentBanner.value.link_url)
         }
         if (currentBanner.value.order) {
           formData.append('sortOrder', currentBanner.value.order)
         }
         res = await shopStore.uploadBanner(shop.value.id, formData)
       }
       
       showByCode(res.code)
       if (isSuccess(res.code)) {
         bannerDialogVisible.value = false
         bannerUploadFile.value = null
         await loadBanners()
       }
     } catch (error) {
       console.error('保存Banner失败:', error)
     }
   }
   ```

3. **检查 `ShopManagePage.vue`**：如果也有Banner管理功能，需要同样的修复。

**修复优先级**：🔴 **高优先级** - 功能完全无法使用

---

**文档生成时间**：2026-03-07
**最后更新**：2026-03-07 - 完成业务完整性评估，确认所有场景业务完整
