# 文件上传系统全流程指南

> **文档定位**：这是一份完整的文件上传系统指导文档，记录了项目中所有图片上传接口的设计思路、实现方案和技术细节。
> 
> **使用说明**：
> - 🔴 **必须掌握** - 核心流程，必须清晰理解
> - 🟡 **需要了解** - 重要概念，了解存在即可
> - 🟢 **参考资料** - 辅助信息，需要时查阅
>
> **⚠️ 重要：文档同步维护原则**
> 
> 本文档记录了我们的文件上传系统的完整设计方案，包括：
> - 9个图片上传接口的详细信息
> - 统一的文件处理工具类
> - 分类存储策略
> - 数据库存储方案
> - 完整的业务流程
> 
> **给 AI 的关键提示**：当你在工作中发现以下情况时，**必须主动更新本文档**：
> - ✅ 新增了图片上传接口
> - ✅ 修改了文件存储路径规则
> - ✅ 更新了FileUploadUtils工具类
> - ✅ 改变了数据库存储方案
> - ✅ 发现文档内容与实际代码不符
> 
> 这样才能确保后续丢失上下文的 AI 能够准确理解我们的设计思路，直接继续开发。

## 目录

- [🔴 图片上传接口统计（必读）](#-图片上传接口统计必读)
- [🔴 文件上传全流程](#-文件上传全流程)
- [🔴 工具类使用指南](#-工具类使用指南)
- [🟡 数据库存储方案](#-数据库存储方案)
- [🟡 文件存储策略](#-文件存储策略)
- [🟢 接口实现模板](#-接口实现模板)
- [🟢 常见问题与优化](#-常见问题与优化)

---

## 🔴 图片上传接口统计（必读）

### 接口总览

**项目共有 9 个图片上传接口**，采用统一的技术方案：

| 序号 | 接口名称 | Type分类 | 数据库表 | 存储字段 | 业务场景 |
|------|----------|----------|----------|----------|----------|
| 1 | uploadAvatar | avatars | users | avatar | 用户头像上传 |
| 2 | uploadCertificationImage | certifications | shop_certifications | id_card_front, id_card_back, business_license | 商家认证图片 |
| 3 | uploadTeaImages | teas | tea_images | url | 茶叶图片上传 |
| 4 | uploadShopLogo | logos | shops | logo | 店铺Logo上传 |
| 5 | uploadShopBanner | shop-banners | shop_banners | image_url | 店铺轮播图 |
| 6 | uploadReviewImage | reviews | tea_reviews | images | 订单评价图片 |
| 7 | uploadBanner | forum-banners | home_contents | content | 论坛首页轮播图 |
| 8 | sendImageMessage | messages | chat_messages | content | 聊天图片消息 |
| 9 | uploadPostImage | posts | forum_posts | images | 论坛帖子图片 |

### 状态码统计

每个接口都有对应的成功和失败状态码：

| 接口 | 成功码 | 失败码 | 说明 |
|------|--------|--------|------|
| uploadAvatar | 2004 | 2109, 2110, 2111 | 头像更新成功/失败 |
| uploadCertificationImage | 2024 | 2146, 2147, 2148 | 认证图片上传成功/失败 |
| uploadTeaImages | 3014 | 3120, 3121, 3122 | 茶叶图片上传成功/失败 |
| uploadShopLogo | 4007 | 4113, 4114, 4115 | Logo上传成功/失败 |
| uploadShopBanner | 4008 | 4117, 4118, 4119 | Banner上传成功/失败 |
| uploadReviewImage | 5016 | 5144, 5145, 5146 | 评价图片上传成功/失败 |
| uploadBanner | 6001 | 6103, 6104, 6105 | 论坛Banner上传成功/失败 |
| sendImageMessage | 7009 | 7116, 7117, 7118 | 图片消息发送成功/失败 |
| uploadPostImage | 6028 | 6140, 6141, 6142 | 帖子图片上传成功/失败 |

### 技术方案统一性

**所有接口采用相同的技术方案**：
- **前端**：使用FormData + multipart/form-data上传
- **后端**：Controller接收 → Service处理 → 调用FileUploadUtils工具类
- **存储**：统一的文件命名和目录结构
- **返回**：统一的响应格式 {url, path}

---

## 🔴 文件上传全流程

### 流程概览

```
前端上传文件
    ↓
Controller接收 (@RequestParam("file") MultipartFile file)
    ├─ @RequiresLogin 权限验证
    ├─ 日志记录：logger.info("上传[业务]图片请求, 文件名: {}", file.getOriginalFilename())
    └─ 调用Service层方法
    ↓
Service层处理
    ├─ 获取当前用户ID：UserContext.getCurrentUserId()
    ├─ 用户身份验证：getUserEntityById(userId)
    ├─ 硬编码type（如 "avatars", "posts", "messages"）
    ├─ 调用工具类：FileUploadUtils.uploadImage(file, type)
    │   └─ 工具类处理：
    │       ├─ 验证文件（类型、大小、扩展名）
    │       ├─ 生成文件名（时间戳_UUID.扩展名）
    │       ├─ 构建路径（files/images/{type}/{year}/{month}/{day}/{filename}）
    │       ├─ 确保目录存在
    │       ├─ 保存文件（>1MB自动压缩到1200px宽度）
    │       └─ 返回相对路径
    ├─ 生成访问URL：FileUploadUtils.generateAccessUrl(relativePath, baseUrl)
    └─ 业务逻辑决策：
        ├─ 场景1：直接存数据库（如头像）
        │   ├─ 更新数据库：mapper.updateAvatar(userId, relativePath)
        │   └─ 返回 {url, path}
        ├─ 场景2：只返回URL（如帖子图片，等发帖时再存）
        │   └─ 直接返回 {url, path}
        └─ 场景3：上传+业务操作（如聊天图片，上传并创建消息）
            ├─ 创建业务记录
            └─ 返回业务结果
    ↓
返回Result给前端（统一格式：Result.success(状态码, {url, path})）
```

### 三种业务场景

**场景1：直接存数据库（如头像上传）**
- 上传文件 → 调用工具类 → 更新数据库 → 返回结果
- 适用接口：uploadAvatar, uploadShopLogo
- **实际实现要点**：
  - 需要在Mapper中定义专门的更新方法（如updateAvatar）
  - 需要在XML中编写对应的SQL语句
  - 数据库存储相对路径，前端使用完整URL
  - 成功后需要返回 {url, path} 格式的数据

**场景2：先返回URL，稍后存储（如帖子图片）**
- 上传文件 → 调用工具类 → 返回URL → 用户发帖时再存数据库
- 适用接口：uploadPostImage, uploadReviewImage
- **实际实现要点**：
  - 不需要立即操作数据库
  - 直接返回文件URL供前端预览
  - 在后续业务操作中将路径存入相关表
  - **实用经验**：Service层实现最简单，无需用户身份验证和数据库操作，只需调用工具类和返回结果

**场景3：上传+业务操作（如聊天图片）**
- 上传文件 → 调用工具类 → 创建消息记录 → 返回结果
- 适用接口：sendImageMessage
- **实际实现要点**：
  - 需要同时处理文件上传和业务逻辑
  - 通常需要事务控制确保数据一致性
  - 返回的是业务操作结果，不仅仅是文件信息
  - **重要经验**：数据类型匹配问题 - 确保实体类ID类型与Mapper接口泛型一致（如ChatSession使用String ID，Mapper应为BaseMapper<ChatSession, String>）

---

## 🔴 工具类使用指南

### FileUploadUtils核心方法

**主要上传方法**：
```java
// 基础上传（默认5MB限制）
String relativePath = FileUploadUtils.uploadImage(file, "avatars");

// 指定大小限制
String relativePath = FileUploadUtils.uploadImage(file, "posts", 10 * 1024 * 1024);
```

**辅助方法**：
```java
// 生成访问URL（必须配置baseUrl）
String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);

// 删除文件
boolean success = FileUploadUtils.deleteFile(relativePath);
```

### 重要配置要求

**application.yml配置**：
```yaml
# 应用配置
app:
  base-url: http://localhost:8080  # 必须配置，用于生成访问URL
```

**WebMvcConfig静态资源映射**：
```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 配置静态资源映射，用于访问上传的文件
    registry.addResourceHandler("/files/**")
            .addResourceLocations("file:" + System.getProperty("user.dir") + "/files/");
}

@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(jwtInterceptor)
            .excludePathPatterns("/files/**");  // 排除文件访问路径
}
```

### 文件命名规则

**格式**：`时间戳_UUID.扩展名`
**示例**：`20240118120530_abc123def456.jpg`

**优点**：
- 避免文件名冲突
- 包含时间信息，便于管理
- 不依赖原文件名，安全性高

### 目录结构规则

**路径格式**：`files/images/{type}/{year}/{month}/{day}/{filename}`

**示例**：
```
files/
  images/
    avatars/2024/01/18/20240118120530_abc123.jpg
    posts/2024/01/18/20240118120530_def456.jpg
    messages/2024/01/18/20240118120530_ghi789.jpg
```

**优点**：
- 按业务类型分类存储
- 按日期分层，便于管理和清理
- 支持大量文件存储

### 文件验证规则

**支持的图片类型**：
- MIME类型：image/jpeg, image/jpg, image/png, image/gif, image/webp
- 扩展名：.jpg, .jpeg, .png, .gif, .webp

**文件大小限制**：
- 默认：5MB
- 可自定义：通过参数指定

**图片压缩**：
- 触发条件：文件大小 > 1MB
- 压缩规则：宽度压缩到1200px，保持比例
- 压缩质量：85%
- **注意**：压缩只针对过大图片，小图片直接保存

**安全验证**：
- 文件类型双重验证（MIME类型 + 扩展名）
- 路径遍历攻击防护（不允许包含..和\）
- 文件名安全化（使用UUID避免冲突）

---

## 🟡 数据库存储方案

### 存储字段类型

**单个图片字段**：
```sql
-- 用户头像
users.avatar VARCHAR(255) -- 存储相对路径

-- 店铺Logo
shops.logo VARCHAR(255) -- 存储相对路径

-- 茶叶主图
teas.main_image VARCHAR(255) -- 存储相对路径
```

**多图片JSON字段**：
```sql
-- 论坛帖子图片
forum_posts.images TEXT -- JSON数组格式

-- 茶叶评价图片  
tea_reviews.images TEXT -- JSON数组格式

-- 茶文化文章图片
tea_articles.images TEXT -- JSON数组格式
```

**专用图片表**：
```sql
-- 茶叶图片表
tea_images.image_url VARCHAR(255) -- 存储相对路径

-- 店铺Banner表
shop_banners.image_url VARCHAR(255) -- 存储相对路径
```

**特殊存储方式**：
```sql
-- 聊天消息（图片URL存在content字段）
chat_messages.content TEXT -- 存储完整访问URL
chat_messages.content_type VARCHAR(20) -- 'image'标识

-- 首页内容（轮播图等）
home_contents.content TEXT -- 存储图片相关数据
```

### 存储路径规则

**数据库存储**：相对路径（如：`files/images/avatars/2024/01/18/xxx.jpg`）
**前端访问**：完整URL（如：`http://localhost:8080/files/images/avatars/2024/01/18/xxx.jpg`）

**优点**：
- 数据库存储空间小
- 域名变更时无需修改数据库
- 便于文件迁移

---

## 🟡 文件存储策略

### 存储位置

**开发环境**：项目根目录下的 `files/` 文件夹
**生产环境**：可配置到独立的文件服务器或CDN

### 清理策略

**定时清理任务**（FileCleanupTask）：
- **执行时间**：每天凌晨3点
- **清理规则**：删除7天前的孤儿文件
- **安全模式**：当前只记录日志，不自动删除

**手动删除**：
- 业务删除时调用 `FileUploadUtils.deleteFile()`
- 立即删除对应的物理文件

### 访问配置

**静态资源映射**（WebMvcConfig）：
```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/files/**")
            .addResourceLocations("file:" + System.getProperty("user.dir") + "/files/");
}
```

**访问URL格式**：`http://域名/files/images/{type}/{year}/{month}/{day}/{filename}`

---

## 🟢 接口实现模板

### Controller层模板

```java
/**
 * 上传[业务]图片
 * 路径: POST /[模块]/[功能]/image
 * 成功码: [xxxx], 失败码: [xxxx, xxxx, xxxx]
 */
@PostMapping("/[功能]/image")
@RequiresLogin
public Result<Map<String, Object>> upload[业务]Image(@RequestParam("file") MultipartFile file) {
    logger.info("上传[业务]图片请求, 文件名: {}", file.getOriginalFilename());
    return [模块]Service.upload[业务]Image(file);
}
```

### Service层模板

**场景1：直接存数据库（如头像上传）**
```java
@Override
@Transactional(rollbackFor = Exception.class)  // 重要：添加事务注解
public Result<Map<String, Object>> upload[业务]Image(MultipartFile file) {
    try {
        // 1. 获取当前用户ID
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            logger.warn("[业务]图片上传失败: 用户未登录");
            return Result.failure([失败码1]); // 用户未登录
        }
        
        // 2. 验证用户是否存在（根据业务需要）
        User user = getUserEntityById(userId);
        if (user == null) {
            logger.warn("[业务]图片上传失败: 用户不存在, userId: {}", userId);
            return Result.failure([失败码1]); // 用户不存在
        }
        
        // 3. 调用工具类上传（硬编码type）
        String relativePath = FileUploadUtils.uploadImage(file, "[type]");
        
        // 4. 生成访问URL
        String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
        
        // 5. 更新数据库
        int result = [mapper].update[字段](userId, relativePath);
        if (result <= 0) {
            logger.error("[业务]图片上传失败: 数据库更新失败, userId: {}", userId);
            return Result.failure([失败码2]); // 数据库更新失败
        }
        
        // 6. 返回结果
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("url", accessUrl);
        responseData.put("path", relativePath);
        
        logger.info("[业务]图片上传成功: userId: {}, path: {}", userId, relativePath);
        return Result.success([成功码], responseData);
        
    } catch (BusinessException e) {
        logger.error("[业务]图片上传失败: 业务异常", e);
        return Result.failure([失败码3]); // 业务异常
    } catch (Exception e) {
        logger.error("[业务]图片上传失败: 系统异常", e);
        return Result.failure([失败码3]); // 系统异常
    }
}
```

**场景2：只返回URL**
```java
@Override
public Result<Map<String, Object>> upload[业务]Image(MultipartFile file) {
    try {
        // 1. 调用工具类上传
        String relativePath = FileUploadUtils.uploadImage(file, "[type]");
        
        // 2. 生成访问URL
        String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
        
        // 3. 直接返回，不存数据库
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("url", accessUrl);
        responseData.put("path", relativePath);
        
        logger.info("[业务]图片上传成功: path: {}", relativePath);
        return Result.success([成功码], responseData);
        
    } catch (BusinessException e) {
        logger.error("[业务]图片上传失败: 业务异常", e);
        return Result.failure([失败码]);
    } catch (Exception e) {
        logger.error("[业务]图片上传失败: 系统异常", e);
        return Result.failure([失败码]);
    }
}
```

**场景3：上传+业务操作**
```java
@Override
@Transactional(rollbackFor = Exception.class)  // 重要：事务控制
public Result<Object> send[业务]Message(String sessionId, String receiverId, MultipartFile file) {
    try {
        // 1. 获取当前用户ID
        String senderId = UserContext.getCurrentUserId();
        if (senderId == null) {
            logger.warn("[业务]消息发送失败: 用户未登录");
            return Result.failure([失败码1]);
        }
        
        // 2. 验证业务对象是否存在（如会话）
        [业务对象] object = [mapper].selectById(sessionId);
        if (object == null) {
            logger.warn("[业务]消息发送失败: 对象不存在, id: {}", sessionId);
            return Result.failure([失败码2]);
        }
        
        // 3. 调用工具类上传（硬编码type）
        String relativePath = FileUploadUtils.uploadImage(file, "[type]");
        
        // 4. 生成访问URL
        String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
        
        // 5. 创建业务记录
        [业务记录] record = new [业务记录]();
        record.setContent(accessUrl); // 存储完整URL
        record.setContentType("image"); // 标识类型
        record.setSenderId(senderId);
        record.setReceiverId(receiverId);
        record.setCreateTime(new Date());
        
        // 6. 保存到数据库
        int result = [mapper].insert(record);
        if (result <= 0) {
            logger.error("[业务]消息发送失败: 数据库插入失败");
            return Result.failure([失败码3]);
        }
        
        // 7. 更新相关业务对象（如会话最后消息）
        object.setLastMessage("[图片]");
        object.setLastMessageTime(new Date());
        [mapper].updateById(object);
        
        // 8. 构造业务返回数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("messageId", record.getId());
        responseData.put("content", accessUrl);
        responseData.put("contentType", "image");
        responseData.put("createTime", record.getCreateTime());
        
        logger.info("[业务]消息发送成功: id: {}, path: {}", record.getId(), relativePath);
        return Result.success([成功码], responseData);
        
    } catch (Exception e) {
        logger.error("[业务]消息发送失败: 系统异常", e);
        return Result.failure([失败码3]);
    }
}
```

### Mapper层模板

**接口定义**：
```java
/**
 * 更新[业务]图片
 *
 * @param id     记录ID
 * @param [字段] 图片路径
 * @return 影响行数
 */
int update[字段](@Param("id") String id, @Param("[字段]") String [字段]);
```

**XML实现**：
```xml
<!-- 更新[业务]图片 -->
<update id="update[字段]">
    UPDATE [表名]
    SET [字段] = #{[字段],jdbcType=VARCHAR},
    update_time = now()
    WHERE id = #{id,jdbcType=VARCHAR}
</update>
```

### 前端调用模板

**API层**：
```javascript
export function upload[业务]Image(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: API.[模块].[接口常量],
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
```

**Store层**：
```javascript
async upload[业务]Image({ commit }, file) {
  try {
    const res = await upload[业务]Image(file)
    
    // 根据业务场景处理返回结果
    if (res.code === [成功码]) {
      // 场景1：更新本地状态（如头像）
      if (res.data && res.data.url) {
        const updatedInfo = {
          ...state.userInfo,
          [字段]: res.data.url
        }
        commit('SET_USER_INFO', updatedInfo)
      }
    }
    
    return res // 返回 {code, data: {url, path}}
  } catch (error) {
    commit('SET_ERROR', error.message || '上传图片失败')
    throw error
  }
}
```

**组件层**：
```javascript
// 上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    // 使用统一的状态码消息系统
    showByCode([失败码1]) // 文件类型错误
    return false
  }
  if (!isLt2M) {
    showByCode([失败码2]) // 文件大小超限
    return false
  }
  return true
}

// 上传处理
const handleUpload = async (options) => {
  const file = options.file
  if (!file) return
  
  try {
    loading.value = true
    
    const res = await store.dispatch('[模块]/upload[业务]Image', file)
    
    // 使用统一的状态码消息系统
    showByCode(res.code)
    
    if (isSuccess(res.code)) {
      // 上传成功，使用 res.data.url 显示图片
      imageUrl.value = res.data.url
      
      // 根据业务需要刷新相关数据
      await store.dispatch('[模块]/getUserInfo')
    }
  } catch (error) {
    console.error('上传失败:', error)
  } finally {
    loading.value = false
  }
}
```

---

## 🟢 常见问题与优化

### 常见问题

**Q1：文件上传失败，提示"不支持的文件类型"**
- 检查文件MIME类型是否在允许列表中
- 检查文件扩展名是否正确
- 确认FileUploadUtils中的类型验证逻辑

**Q2：文件过大无法上传**
- 默认限制5MB，可通过参数调整
- 检查服务器上传大小限制
- 确认Spring Boot的文件上传配置

**Q3：图片显示不出来**
- 检查静态资源映射配置（WebMvcConfig）
- 确认文件路径是否正确
- 检查文件是否真实存在
- 验证JWT拦截器是否排除了/files/**路径

**Q4：数据库路径字段长度不够**
- 建议VARCHAR(255)，足够存储完整路径
- 检查字段定义和实际存储内容

**Q5：baseUrl配置问题**
- 确保application.yml中配置了app.base-url
- 检查Service层是否正确注入@Value("${app.base-url}")
- 开发环境通常使用http://localhost:8080

**Q6：权限验证失败**
- 确认Controller方法添加了@RequiresLogin注解
- 检查JWT token是否有效
- 验证UserContext.getCurrentUserId()是否返回正确值

**Q7：实体类ID类型与Mapper泛型不匹配**
- 确保实体类的ID类型与Mapper接口的泛型参数一致
- 例如：ChatSession使用String ID，则ChatSessionMapper应为BaseMapper<ChatSession, String>
- 同时检查XML文件中的parameterType是否匹配
- 这是场景3实现中容易遇到的问题

### 实际开发经验

**文件存储最佳实践**：
- 数据库存储相对路径，便于域名变更
- 前端使用完整URL进行访问
- 文件名使用时间戳+UUID避免冲突
- 按日期分层存储，便于管理

**错误处理最佳实践**：
- 使用统一的Result返回格式
- 记录详细的日志信息
- 区分业务异常和系统异常
- 前端使用统一的状态码消息系统

**性能优化经验**：
- 大图片自动压缩，提升用户体验
- 使用事务确保数据一致性
- 合理的文件大小限制
- 考虑CDN集成提升访问速度

### 性能优化

**已实现的优化**：
- 自动图片压缩（>1MB触发）
- 按日期分层存储
- 定时清理孤儿文件

**可考虑的优化**：
- CDN加速
- 图片缩略图生成
- 异步上传处理
- 文件去重

### 安全考虑

**已实现的安全措施**：
- 文件类型验证
- 文件大小限制
- 路径遍历攻击防护
- UUID文件名避免冲突

**建议的安全措施**：
- 图片内容检测
- 病毒扫描
- 访问权限控制
- 防盗链设置

---

## 注意事项

### 关键原则

1. **type在Service层硬编码**：每个接口的type是固定的，不由前端传递
2. **统一使用FileUploadUtils**：所有图片上传都通过工具类处理
3. **相对路径存数据库**：便于域名变更和文件迁移
4. **完整URL返回前端**：便于前端直接使用

### 开发规范

1. **新增接口时**：
   - 确定type分类
   - 确定数据库存储方案
   - 按照模板实现代码
   - 更新本文档

2. **修改工具类时**：
   - 考虑向后兼容性
   - 更新相关文档
   - 测试所有接口

3. **数据库设计时**：
   - 图片字段使用VARCHAR(255)
   - 多图片使用TEXT存JSON
   - 考虑是否需要专用图片表

### 给 AI 的提示

如果你是 AI 模型，在处理文件上传相关任务时：

1. **理解业务场景**：区分三种不同的业务场景
2. **遵循统一方案**：所有接口都使用相同的技术方案
3. **硬编码type**：在Service层硬编码，不由前端传递
4. **使用工具类**：必须通过FileUploadUtils处理文件
5. **更新文档**：修改代码后及时更新本文档

---

## 参考资料

- **工具类实现**：`src/main/java/com/shangnantea/utils/FileUploadUtils.java`
- **定时任务**：`src/main/java/com/shangnantea/task/FileCleanupTask.java`
- **数据库表结构**：`teasystem.sql`
- **API接口定义**：`../../../openapi_new.yaml`
- **状态码映射**：`../../../shangnantea-web/docs/tasks/code-message-mapping.md`

---

**最后更新**：2026-01-21  
**维护者**：项目团队  
**基于实现**：用户头像上传、图片消息发送、评价图片上传、帖子图片上传功能完整开发经验