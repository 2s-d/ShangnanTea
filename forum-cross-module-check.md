# 论坛模块跨模块数据调用问题检查报告 - Type 2

## 检查目标
查找以下三类问题：
1. **硬编码的数据**（应该从Mapper查询）
2. **TODO待办的跨模块数据获取**
3. **因为"害怕越权"而没有调用公共Mapper的情况**

---

## 问题汇总

### ❌ 发现的问题

| # | 问题类型 | 位置 | 当前实现 | 应该实现 | 严重程度 |
|---|---------|------|---------|---------|---------|
| 1 | 硬编码 | getTopicDetail() line 1149 | `vo.setModeratorName("版主")` | 从UserMapper查询版主用户名 | 🔴 高 |
| 2 | TODO待办 | getTopicDetail() line 1152 | `vo.setTodayPostCount(0)` | 统计今日帖子数 | 🟡 中 |
| 3 | 硬编码 | createArticle() line 918 | `article.setAuthor("管理员")` | 从UserContext获取当前用户名 | 🟡 中 |

### ✅ 已正确实现的跨模块数据调用

| # | 位置 | 实现方式 | 状态 |
|---|------|---------|------|
| 1 | getForumPosts() | 使用UserMapper查询用户信息 | ✅ 正确 |
| 2 | getPendingPosts() | 使用UserMapper查询用户信息 | ✅ 正确 |
| 3 | getPostDetail() | 使用UserMapper查询用户信息 | ✅ 正确 |
| 4 | getPostReplies() | 使用UserMapper查询用户信息和目标用户信息 | ✅ 正确 |

---

## 详细问题分析

### 问题1: getTopicDetail() - 硬编码版主名称 🔴

**代码位置**: ForumServiceImpl.java:1149

**当前实现**:
```java
vo.setModeratorName("版主"); // TODO: 从用户表查询版主名称
```

**问题描述**:
- 硬编码了"版主"字符串
- 有TODO注释但未实现
- 版块表中有userId字段，应该查询该用户的真实用户名

**应该实现**:
```java
// 查询版主信息
if (topic.getUserId() != null) {
    User moderator = userMapper.selectById(topic.getUserId());
    vo.setModeratorName(moderator != null ? moderator.getUsername() : "未设置版主");
} else {
    vo.setModeratorName("未设置版主");
}
```

**影响**:
- 前端显示的版主名称永远是"版主"，无法显示真实版主用户名
- 用户体验差

---

### 问题2: getTopicDetail() - TODO待办统计今日帖子数 🟡

**代码位置**: ForumServiceImpl.java:1152

**当前实现**:
```java
vo.setTodayPostCount(0); // TODO: 统计今日帖子数
```

**问题描述**:
- 硬编码返回0
- 有TODO注释但未实现
- 需要统计该版块今日发布的帖子数量

**应该实现**:
```java
// 统计今日帖子数
LocalDate today = LocalDate.now();
Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

List<ForumPost> allPosts = postMapper.selectAll();
long todayPostCount = allPosts.stream()
    .filter(p -> p.getTopicId().equals(topicId))
    .filter(p -> p.getCreateTime() != null)
    .filter(p -> p.getCreateTime().after(startOfDay) && p.getCreateTime().before(endOfDay))
    .filter(p -> p.getStatus() != null && p.getStatus() == 1)
    .count();

vo.setTodayPostCount((int) todayPostCount);
```

**影响**:
- 前端无法显示版块的今日活跃度
- 功能不完整

---

### 问题3: createArticle() - 硬编码作者名称 🟡

**代码位置**: ForumServiceImpl.java:918

**当前实现**:
```java
article.setAuthor("管理员"); // 默认作者
```

**问题描述**:
- 硬编码了"管理员"字符串
- 应该从UserContext获取当前登录用户的真实用户名

**应该实现**:
```java
// 获取当前用户信息
String userId = UserContext.getCurrentUserId();
User currentUser = userMapper.selectById(userId);
article.setAuthor(currentUser != null ? currentUser.getUsername() : "管理员");
```

**影响**:
- 所有文章的作者都显示为"管理员"，无法区分是哪个管理员创建的
- 无法追溯文章创建者

---

## 已正确实现的示例

### ✅ 示例1: getForumPosts() - 正确查询用户信息

**代码位置**: ForumServiceImpl.java:1376-1379

```java
// 查询用户信息
User user = userMapper.selectById(post.getUserId());
vo.setUserName(user != null ? user.getUsername() : "未知用户");
vo.setUserAvatar(user != null ? user.getAvatar() : null);
```

**优点**:
- 使用UserMapper查询用户信息
- 有空值保护（user != null）
- 提供默认值"未知用户"

---

### ✅ 示例2: getPostReplies() - 正确查询用户和目标用户信息

**代码位置**: ForumServiceImpl.java:2000-2020

```java
// 查询用户信息
User user = userMapper.selectById(reply.getUserId());
if (user != null) {
    vo.setUsername(user.getUsername());
    vo.setAvatar(user.getAvatar());
}

// 查询目标用户信息
if (reply.getToUserId() != null) {
    User toUser = userMapper.selectById(reply.getToUserId());
    if (toUser != null) {
        vo.setToUsername(toUser.getUsername());
    }
}
```

**优点**:
- 查询了回复者和被回复者的用户信息
- 有完整的空值检查
- 跨模块数据调用规范

---

## 检查结果统计

- **总跨模块数据调用点**: 7处
- **已正确实现**: 4处 ✅
- **存在问题**: 3处 ❌
  - 硬编码问题: 2处 🔴
  - TODO待办: 1处 🟡

---

## 修复建议

### 优先级排序

1. **🔴 高优先级** - 问题1: getTopicDetail()硬编码版主名称
   - 影响用户体验
   - 实现简单，只需查询UserMapper

2. **🟡 中优先级** - 问题3: createArticle()硬编码作者名称
   - 影响数据追溯
   - 实现简单，使用UserContext + UserMapper

3. **🟡 中优先级** - 问题2: getTopicDetail()统计今日帖子数
   - 功能不完整
   - 实现稍复杂，需要日期计算和过滤

---

## 总结

论坛模块在跨模块数据调用方面**大部分实现正确**，但仍有3处需要修复：

**优点**:
- ✅ 已经注入了UserMapper，说明理解数据访问层是公共模块
- ✅ 大部分地方正确使用UserMapper查询用户信息
- ✅ 有良好的空值保护

**需要改进**:
- ❌ 仍有2处硬编码的用户信息
- ❌ 有1处TODO待办未实现
- ❌ 需要统一处理跨模块数据调用的规范

**建议**:
1. 立即修复3个已发现的问题
2. 建立跨模块数据调用的代码规范
3. 在代码审查时重点检查硬编码和TODO注释
