package com.shangnantea.controller;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.model.entity.forum.ForumPost;
import com.shangnantea.model.entity.forum.ForumReply;
import com.shangnantea.model.entity.forum.ForumTopic;
import com.shangnantea.model.entity.forum.HomeContent;
import com.shangnantea.model.entity.forum.TeaArticle;
import com.shangnantea.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.time.LocalDateTime;

/**
 * 论坛控制器
 */
@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;
    
    /**
     * 获取论坛主题列表
     * 任务组A：返回测试数据，不走Service/DB
     *
     * @return 结果
     */
    @GetMapping("/topics")
    public Result<List<Map<String, Object>>> listTopics() {
        // 返回测试版块数据
        List<Map<String, Object>> topics = new ArrayList<>();
        
        Map<String, Object> topic1 = new HashMap<>();
        topic1.put("id", 1);
        topic1.put("name", "茶文化讨论");
        topic1.put("description", "探讨茶文化的历史传承与现代发展");
        topic1.put("icon", "tea-culture");
        topic1.put("postCount", 156);
        topic1.put("sortOrder", 1);
        topic1.put("status", 1);
        topics.add(topic1);
        
        Map<String, Object> topic2 = new HashMap<>();
        topic2.put("id", 2);
        topic2.put("name", "品茶心得");
        topic2.put("description", "分享品茶体验和心得感悟");
        topic2.put("icon", "tea-tasting");
        topic2.put("postCount", 89);
        topic2.put("sortOrder", 2);
        topic2.put("status", 1);
        topics.add(topic2);
        
        Map<String, Object> topic3 = new HashMap<>();
        topic3.put("id", 3);
        topic3.put("name", "茶艺交流");
        topic3.put("description", "茶艺技法交流与学习");
        topic3.put("icon", "tea-art");
        topic3.put("postCount", 67);
        topic3.put("sortOrder", 3);
        topic3.put("status", 1);
        topics.add(topic3);
        
        return Result.success(topics);
    }
    
    /**
     * 获取论坛主题详情
     *
     * @param id 主题ID
     * @return 结果
     */
    @GetMapping("/topics/{id}")
    public Result<ForumTopic> getTopicById(@PathVariable Integer id) {
        ForumTopic topic = forumService.getTopicById(id);
        return Result.success(topic);
    }
    
    /**
     * 创建论坛主题(管理员)
     *
     * @param topic 主题信息
     * @return 结果
     */
    @PostMapping("/topics")
    public Result<ForumTopic> createTopic(@RequestBody ForumTopic topic) {
        // TODO: 获取当前管理员ID
        String userId = "currentAdminId";
        topic.setUserId(userId);
        
        ForumTopic createdTopic = forumService.createTopic(topic);
        return Result.success(createdTopic);
    }
    
    /**
     * 更新论坛主题(管理员)
     *
     * @param id 主题ID
     * @param topic 主题信息
     * @return 结果
     */
    @PutMapping("/topics/{id}")
    public Result<Boolean> updateTopic(@PathVariable Integer id, @RequestBody ForumTopic topic) {
        topic.setId(id);
        boolean success = forumService.updateTopic(topic);
        return Result.success(success);
    }
    
    /**
     * 删除论坛主题(管理员)
     *
     * @param id 主题ID
     * @return 结果
     */
    @DeleteMapping("/topics/{id}")
    public Result<Boolean> deleteTopic(@PathVariable Integer id) {
        boolean success = forumService.deleteTopic(id);
        return Result.success(success);
    }
    
    /**
     * 获取帖子详情
     * 任务组A：返回测试数据，不走Service/DB
     *
     * @param id 帖子ID
     * @return 结果
     */
    @GetMapping("/posts/{id}")
    public Result<Map<String, Object>> getPostById(@PathVariable Long id) {
        // 返回测试帖子详情数据
        Map<String, Object> post = new HashMap<>();
        post.put("id", id);
        post.put("title", "帖子详情标题 " + id + " - 深度探讨商南茶文化");
        post.put("content", "这是帖子" + id + "的详细内容。\n\n" +
                "商南茶文化博大精深，有着悠久的历史传承。从古代的茶马古道到现代的茶艺表演，" +
                "每一个环节都体现着深厚的文化底蕴。\n\n" +
                "在这里，我想和大家分享一些关于商南茶的心得体会：\n" +
                "1. 茶叶的选择非常重要\n" +
                "2. 冲泡的水温要控制好\n" +
                "3. 品茶的环境也很关键\n\n" +
                "希望大家能够一起交流讨论，共同传承这份珍贵的文化遗产。");
        
        post.put("topicId", ((id.intValue() % 3) + 1));
        post.put("topicName", getTopicName(((id.intValue() % 3) + 1)));
        post.put("userId", "user" + ((id.intValue() % 5) + 1));
        post.put("userName", "茶友" + ((id.intValue() % 5) + 1));
        post.put("userAvatar", "https://via.placeholder.com/60x60/4CAF50/FFFFFF?text=U" + ((id.intValue() % 5) + 1));
        post.put("createTime", LocalDateTime.now().minusHours(id.intValue()));
        post.put("updateTime", LocalDateTime.now().minusHours(id.intValue()));
        post.put("viewCount", (int)(Math.random() * 1000) + 100);
        post.put("likeCount", (int)(Math.random() * 50) + 10);
        post.put("replyCount", (int)(Math.random() * 30) + 5);
        post.put("favoriteCount", (int)(Math.random() * 20) + 3);
        post.put("isSticky", id <= 2);
        post.put("isEssence", id <= 3);
        post.put("status", 1);
        
        // 帖子图片
        List<String> images = new ArrayList<>();
        if (id % 3 == 0) {
            images.add("https://via.placeholder.com/600x400/4CAF50/FFFFFF?text=PostImage1");
            images.add("https://via.placeholder.com/600x400/2196F3/FFFFFF?text=PostImage2");
        }
        post.put("images", images);
        
        // 当前用户状态
        post.put("isLiked", id % 7 == 0);
        post.put("isFavorited", id % 9 == 0);
        post.put("canEdit", id % 5 == 1); // 模拟用户权限
        post.put("canDelete", id % 5 == 1);
        
        // 标签
        post.put("tags", Arrays.asList("茶文化", "商南茶", "传统文化", "品茶心得"));
        
        return Result.success(post);
    }

    /**
     * 获取帖子列表（支持筛选、排序、分页）
     * 任务组A：返回测试数据，不走Service/DB
     *
     * @param topicId 主题ID（可选）
     * @param keyword 关键词搜索（可选）
     * @param sortBy 排序方式（latest/hot/replies）
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    @GetMapping("/posts")
    public Result<Map<String, Object>> listPosts(
            @RequestParam(required = false) Integer topicId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // 创建测试帖子数据
        List<Map<String, Object>> posts = new ArrayList<>();
        
        // 生成测试帖子数据
        for (int i = 1; i <= 25; i++) {
            Map<String, Object> post = new HashMap<>();
            post.put("id", (long) i);
            post.put("title", "论坛帖子标题 " + i + " - 分享茶文化心得");
            post.put("content", "这是帖子" + i + "的内容摘要。分享一些关于茶文化的心得体会，欢迎大家一起讨论交流...");
            post.put("topicId", (i % 3) + 1); // 轮换分配到不同版块
            post.put("topicName", getTopicName((i % 3) + 1));
            post.put("userId", "user" + ((i % 5) + 1));
            post.put("userName", "茶友" + ((i % 5) + 1));
            post.put("userAvatar", "https://via.placeholder.com/40x40/4CAF50/FFFFFF?text=U" + ((i % 5) + 1));
            post.put("createTime", LocalDateTime.now().minusHours(i));
            post.put("updateTime", LocalDateTime.now().minusHours(i));
            post.put("viewCount", (int)(Math.random() * 500) + 50);
            post.put("likeCount", (int)(Math.random() * 20) + 5);
            post.put("replyCount", (int)(Math.random() * 15) + 2);
            post.put("favoriteCount", (int)(Math.random() * 10) + 1);
            post.put("isSticky", i <= 2); // 前两个帖子置顶
            post.put("isEssence", i <= 3); // 前三个帖子精华
            post.put("status", 1); // 已发布
            post.put("images", i % 4 == 0 ? Arrays.asList(
                "https://via.placeholder.com/300x200/4CAF50/FFFFFF?text=Image1",
                "https://via.placeholder.com/300x200/2196F3/FFFFFF?text=Image2"
            ) : new ArrayList<>());
            
            // 当前用户的点赞收藏状态（模拟）
            post.put("isLiked", i % 7 == 0);
            post.put("isFavorited", i % 9 == 0);
            
            posts.add(post);
        }
        
        // 根据版块筛选
        if (topicId != null) {
            posts = posts.stream()
                    .filter(post -> topicId.equals(post.get("topicId")))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 根据关键词筛选
        if (keyword != null && !keyword.trim().isEmpty()) {
            String finalKeyword = keyword.toLowerCase();
            posts = posts.stream()
                    .filter(post -> {
                        String title = (String) post.get("title");
                        String content = (String) post.get("content");
                        return title.toLowerCase().contains(finalKeyword) || 
                               content.toLowerCase().contains(finalKeyword);
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 排序
        switch (sortBy) {
            case "hot":
                posts.sort((a, b) -> Integer.compare((Integer) b.get("likeCount"), (Integer) a.get("likeCount")));
                break;
            case "replies":
                posts.sort((a, b) -> Integer.compare((Integer) b.get("replyCount"), (Integer) a.get("replyCount")));
                break;
            case "latest":
            default:
                posts.sort((a, b) -> ((LocalDateTime) b.get("createTime")).compareTo((LocalDateTime) a.get("createTime")));
                break;
        }
        
        // 置顶帖子排在前面
        posts.sort((a, b) -> {
            boolean aSticky = (Boolean) a.get("isSticky");
            boolean bSticky = (Boolean) b.get("isSticky");
            if (aSticky && !bSticky) return -1;
            if (!aSticky && bSticky) return 1;
            return 0;
        });
        
        // 分页处理
        int total = posts.size();
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, total);
        List<Map<String, Object>> pagePosts = posts.subList(startIndex, endIndex);
        
        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("posts", pagePosts);
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current", page);
        pagination.put("pageSize", size);
        pagination.put("total", total);
        pagination.put("pages", (int) Math.ceil((double) total / size));
        result.put("pagination", pagination);
        
        return Result.success(result);
    }
    
    /**
     * 获取主题下的帖子列表
     *
     * @param topicId 主题ID
     * @param page 页码
     * @param size 每页数量
     * @return 结果
     */
    @GetMapping("/topics/{topicId}/posts")
    public Result<PageResult<ForumPost>> listPostsByTopic(
            @PathVariable Integer topicId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<ForumPost> pageResult = forumService.listPostsByTopic(topicId, pageParam);
        return Result.success(pageResult);
    }
    
    /**
     * 创建帖子
     * 任务组A：返回测试数据，不走Service/DB
     *
     * @param postData 帖子信息
     * @return 结果
     */
    @PostMapping("/posts")
    public Result<Map<String, Object>> createPost(@RequestBody Map<String, Object> postData) {
        // 模拟创建帖子，返回新帖子数据
        Map<String, Object> newPost = new HashMap<>();
        newPost.put("id", System.currentTimeMillis()); // 使用时间戳作为ID
        newPost.put("title", postData.get("title"));
        newPost.put("content", postData.get("content"));
        newPost.put("topicId", postData.get("topicId"));
        newPost.put("topicName", getTopicName((Integer) postData.get("topicId")));
        newPost.put("userId", "currentUser");
        newPost.put("userName", "当前用户");
        newPost.put("userAvatar", "https://via.placeholder.com/60x60/4CAF50/FFFFFF?text=ME");
        newPost.put("createTime", LocalDateTime.now());
        newPost.put("updateTime", LocalDateTime.now());
        newPost.put("viewCount", 1);
        newPost.put("likeCount", 0);
        newPost.put("replyCount", 0);
        newPost.put("favoriteCount", 0);
        newPost.put("isSticky", false);
        newPost.put("isEssence", false);
        newPost.put("status", 1);
        newPost.put("images", postData.get("images") != null ? postData.get("images") : new ArrayList<>());
        newPost.put("isLiked", false);
        newPost.put("isFavorited", false);
        newPost.put("canEdit", true);
        newPost.put("canDelete", true);
        newPost.put("tags", postData.get("tags") != null ? postData.get("tags") : new ArrayList<>());
        
        return Result.success(newPost);
    }
    
    /**
     * 更新帖子
     * 任务组A：返回测试数据，不走Service/DB
     *
     * @param id 帖子ID
     * @param postData 帖子信息
     * @return 结果
     */
    @PutMapping("/posts/{id}")
    public Result<Map<String, Object>> updatePost(@PathVariable Long id, @RequestBody Map<String, Object> postData) {
        // 模拟更新帖子，返回更新后的帖子数据
        Map<String, Object> updatedPost = new HashMap<>();
        updatedPost.put("id", id);
        updatedPost.put("title", postData.get("title"));
        updatedPost.put("content", postData.get("content"));
        updatedPost.put("topicId", postData.get("topicId"));
        updatedPost.put("topicName", getTopicName((Integer) postData.get("topicId")));
        updatedPost.put("userId", "currentUser");
        updatedPost.put("userName", "当前用户");
        updatedPost.put("userAvatar", "https://via.placeholder.com/60x60/4CAF50/FFFFFF?text=ME");
        updatedPost.put("createTime", LocalDateTime.now().minusHours(id.intValue()));
        updatedPost.put("updateTime", LocalDateTime.now()); // 更新时间为当前时间
        updatedPost.put("viewCount", (int)(Math.random() * 1000) + 100);
        updatedPost.put("likeCount", (int)(Math.random() * 50) + 10);
        updatedPost.put("replyCount", (int)(Math.random() * 30) + 5);
        updatedPost.put("favoriteCount", (int)(Math.random() * 20) + 3);
        updatedPost.put("isSticky", id <= 2);
        updatedPost.put("isEssence", id <= 3);
        updatedPost.put("status", 1);
        updatedPost.put("images", postData.get("images") != null ? postData.get("images") : new ArrayList<>());
        updatedPost.put("isLiked", id % 7 == 0);
        updatedPost.put("isFavorited", id % 9 == 0);
        updatedPost.put("canEdit", true);
        updatedPost.put("canDelete", true);
        updatedPost.put("tags", postData.get("tags") != null ? postData.get("tags") : new ArrayList<>());
        
        return Result.success(updatedPost);
    }
    
    /**
     * 删除帖子
     * 任务组A：返回测试数据，不走Service/DB
     *
     * @param id 帖子ID
     * @return 结果
     */
    @DeleteMapping("/posts/{id}")
    public Result<Boolean> deletePost(@PathVariable Long id) {
        // 模拟删除操作
        return Result.success(true);
    }
    
    /**
     * 设置帖子置顶(管理员)
     * 任务组F：返回测试数据，不走Service/DB
     *
     * @param id 帖子ID
     * @param isSticky 是否置顶
     * @return 结果
     */
    @PutMapping("/posts/{id}/sticky")
    public Result<Map<String, Object>> setPostSticky(
            @PathVariable Long id,
            @RequestParam Boolean isSticky) {
        // 模拟置顶操作，返回更新后的帖子状态
        Map<String, Object> result = new HashMap<>();
        result.put("postId", id);
        result.put("isSticky", isSticky);
        result.put("message", isSticky ? "帖子已置顶" : "已取消置顶");
        return Result.success(result);
    }
    
    /**
     * 设置帖子精华(管理员)
     * 任务组F：返回测试数据，不走Service/DB
     *
     * @param id 帖子ID
     * @param isEssence 是否精华
     * @return 结果
     */
    @PutMapping("/posts/{id}/essence")
    public Result<Map<String, Object>> setPostEssence(
            @PathVariable Long id,
            @RequestParam Boolean isEssence) {
        // 模拟精华操作，返回更新后的帖子状态
        Map<String, Object> result = new HashMap<>();
        result.put("postId", id);
        result.put("isEssence", isEssence);
        result.put("message", isEssence ? "帖子已设为精华" : "已取消精华");
        return Result.success(result);
    }
    
    // ===== 任务组F：内容审核相关接口（返回测试数据，不走Service/DB） =====
    
    /**
     * 获取待审核帖子列表
     * 任务组F：返回测试数据，不走Service/DB
     *
     * @param page 页码
     * @param size 每页数量
     * @return 待审核帖子列表
     */
    @GetMapping("/posts/pending")
    public Result<Map<String, Object>> getPendingPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // 创建测试待审核帖子数据
        List<Map<String, Object>> pendingPosts = new ArrayList<>();
        
        // 生成测试待审核帖子数据
        for (int i = 1; i <= 15; i++) {
            Map<String, Object> post = new HashMap<>();
            post.put("id", (long) (100 + i));
            post.put("title", "待审核帖子标题 " + i + " - 需要管理员审核");
            post.put("content", "这是待审核帖子" + i + "的内容。用户发布的内容需要管理员审核后才能公开显示...");
            post.put("topicId", (i % 3) + 1);
            post.put("topicName", getTopicName((i % 3) + 1));
            post.put("userId", "user" + ((i % 5) + 1));
            post.put("userName", "用户" + ((i % 5) + 1));
            post.put("userAvatar", "https://via.placeholder.com/40x40/FF5722/FFFFFF?text=U" + ((i % 5) + 1));
            post.put("createTime", LocalDateTime.now().minusHours(i));
            post.put("status", 0); // 0-待审核
            post.put("auditStatus", "pending"); // pending-待审核, approved-已通过, rejected-已拒绝
            post.put("submitTime", LocalDateTime.now().minusHours(i));
            post.put("priority", i <= 5 ? "high" : "normal"); // 优先级
            
            // 模拟一些帖子有图片
            if (i % 4 == 0) {
                post.put("images", Arrays.asList(
                    "https://via.placeholder.com/300x200/FF5722/FFFFFF?text=Image1",
                    "https://via.placeholder.com/300x200/9C27B0/FFFFFF?text=Image2"
                ));
            } else {
                post.put("images", new ArrayList<>());
            }
            
            pendingPosts.add(post);
        }
        
        // 分页处理
        int total = pendingPosts.size();
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, total);
        List<Map<String, Object>> pagePosts = pendingPosts.subList(startIndex, endIndex);
        
        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("posts", pagePosts);
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current", page);
        pagination.put("pageSize", size);
        pagination.put("total", total);
        pagination.put("pages", (int) Math.ceil((double) total / size));
        result.put("pagination", pagination);
        
        return Result.success(result);
    }
    
    /**
     * 审核通过帖子
     * 任务组F：返回测试数据，不走Service/DB
     *
     * @param id 帖子ID
     * @param auditData 审核数据
     * @return 审核结果
     */
    @PostMapping("/posts/{id}/approve")
    public Result<Map<String, Object>> approvePost(
            @PathVariable Long id,
            @RequestBody Map<String, Object> auditData) {
        
        // 模拟审核通过操作
        Map<String, Object> result = new HashMap<>();
        result.put("postId", id);
        result.put("auditStatus", "approved");
        result.put("auditTime", LocalDateTime.now());
        result.put("auditorId", "admin001");
        result.put("auditorName", "管理员");
        result.put("auditComment", auditData.get("comment"));
        result.put("message", "帖子审核通过");
        
        return Result.success(result);
    }
    
    /**
     * 审核拒绝帖子
     * 任务组F：返回测试数据，不走Service/DB
     *
     * @param id 帖子ID
     * @param auditData 审核数据（包含拒绝原因）
     * @return 审核结果
     */
    @PostMapping("/posts/{id}/reject")
    public Result<Map<String, Object>> rejectPost(
            @PathVariable Long id,
            @RequestBody Map<String, Object> auditData) {
        
        // 模拟审核拒绝操作
        Map<String, Object> result = new HashMap<>();
        result.put("postId", id);
        result.put("auditStatus", "rejected");
        result.put("auditTime", LocalDateTime.now());
        result.put("auditorId", "admin001");
        result.put("auditorName", "管理员");
        result.put("rejectReason", auditData.get("reason"));
        result.put("auditComment", auditData.get("comment"));
        result.put("message", "帖子审核拒绝");
        
        return Result.success(result);
    }
    
    /**
     * 获取帖子回复列表
     * 任务组B：返回测试数据，不走Service/DB
     *
     * @param postId 帖子ID
     * @param page 页码
     * @param size 每页数量
     * @param sortBy 排序方式（time/timeDesc/hot）
     * @return 结果
     */
    @GetMapping("/posts/{postId}/replies")
    public Result<Map<String, Object>> listRepliesByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "time") String sortBy) {
        
        // 创建测试回复数据
        List<Map<String, Object>> replies = new ArrayList<>();
        
        // 生成测试回复数据（包含多级回复）
        for (int i = 1; i <= 20; i++) {
            Map<String, Object> reply = new HashMap<>();
            reply.put("id", (long) i);
            reply.put("postId", postId);
            reply.put("content", "这是回复" + i + "的内容。感谢楼主的分享，我也有一些心得想要交流。" +
                    "茶文化确实博大精深，需要我们不断学习和体验。");
            reply.put("userId", "user" + ((i % 8) + 1));
            reply.put("userName", "茶友" + ((i % 8) + 1));
            reply.put("userAvatar", "https://via.placeholder.com/50x50/4CAF50/FFFFFF?text=U" + ((i % 8) + 1));
            reply.put("createTime", LocalDateTime.now().minusHours(i));
            reply.put("likeCount", (int)(Math.random() * 10) + 1);
            reply.put("isLiked", i % 6 == 0); // 模拟当前用户点赞状态
            
            // 设置多级回复关系
            if (i > 5 && i % 4 == 0) {
                // 部分回复是二级回复
                int parentId = i - (int)(Math.random() * 3) - 1;
                reply.put("parentId", (long) parentId);
                reply.put("parentUserId", "user" + ((parentId % 8) + 1));
                reply.put("parentUserName", "茶友" + ((parentId % 8) + 1));
                reply.put("parentContent", "这是被回复的内容摘要...");
            } else {
                // 一级回复
                reply.put("parentId", null);
                reply.put("parentUserId", null);
                reply.put("parentUserName", null);
                reply.put("parentContent", null);
            }
            
            // 当前用户权限（模拟）
            reply.put("canDelete", i % 5 == 1); // 模拟用户只能删除自己的回复
            
            replies.add(reply);
        }
        
        // 排序处理
        switch (sortBy) {
            case "timeDesc":
                replies.sort((a, b) -> ((LocalDateTime) b.get("createTime")).compareTo((LocalDateTime) a.get("createTime")));
                break;
            case "hot":
                replies.sort((a, b) -> Integer.compare((Integer) b.get("likeCount"), (Integer) a.get("likeCount")));
                break;
            case "time":
            default:
                replies.sort((a, b) -> ((LocalDateTime) a.get("createTime")).compareTo((LocalDateTime) b.get("createTime")));
                break;
        }
        
        // 分页处理
        int total = replies.size();
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, total);
        List<Map<String, Object>> pageReplies = replies.subList(startIndex, endIndex);
        
        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("replies", pageReplies);
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current", page);
        pagination.put("pageSize", size);
        pagination.put("total", total);
        pagination.put("pages", (int) Math.ceil((double) total / size));
        result.put("pagination", pagination);
        
        return Result.success(result);
    }
    
    /**
     * 创建回复
     * 任务组B：返回测试数据，不走Service/DB
     *
     * @param replyData 回复信息
     * @return 结果
     */
    @PostMapping("/posts/{postId}/replies")
    public Result<Map<String, Object>> createReply(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> replyData) {
        
        // 模拟创建回复，返回新回复数据
        Map<String, Object> newReply = new HashMap<>();
        newReply.put("id", System.currentTimeMillis()); // 使用时间戳作为ID
        newReply.put("postId", postId);
        newReply.put("content", replyData.get("content"));
        newReply.put("userId", "currentUser");
        newReply.put("userName", "当前用户");
        newReply.put("userAvatar", "https://via.placeholder.com/50x50/4CAF50/FFFFFF?text=ME");
        newReply.put("createTime", LocalDateTime.now());
        newReply.put("likeCount", 0);
        newReply.put("isLiked", false);
        newReply.put("canDelete", true);
        
        // 处理多级回复
        if (replyData.get("parentId") != null) {
            newReply.put("parentId", replyData.get("parentId"));
            newReply.put("parentUserId", replyData.get("parentUserId"));
            newReply.put("parentUserName", replyData.get("parentUserName"));
            newReply.put("parentContent", replyData.get("parentContent"));
        } else {
            newReply.put("parentId", null);
            newReply.put("parentUserId", null);
            newReply.put("parentUserName", null);
            newReply.put("parentContent", null);
        }
        
        return Result.success(newReply);
    }
    
    /**
     * 删除回复
     * 任务组B：返回测试数据，不走Service/DB
     *
     * @param id 回复ID
     * @return 结果
     */
    @DeleteMapping("/replies/{id}")
    public Result<Boolean> deleteReply(@PathVariable Long id) {
        // 模拟删除操作
        return Result.success(true);
    }
    
    /**
     * 点赞回复
     * 任务组B：返回测试数据，不走Service/DB
     *
     * @param id 回复ID
     * @return 结果
     */
    @PostMapping("/replies/{id}/like")
    public Result<Map<String, Object>> likeReply(@PathVariable Long id) {
        // 模拟点赞操作，返回更新后的点赞状态
        Map<String, Object> result = new HashMap<>();
        result.put("replyId", id);
        result.put("isLiked", true);
        result.put("likeCount", (int)(Math.random() * 20) + 5); // 模拟点赞数
        return Result.success(result);
    }
    
    /**
     * 取消点赞回复
     * 任务组B：返回测试数据，不走Service/DB
     *
     * @param id 回复ID
     * @return 结果
     */
    @DeleteMapping("/replies/{id}/like")
    public Result<Map<String, Object>> unlikeReply(@PathVariable Long id) {
        // 模拟取消点赞操作，返回更新后的点赞状态
        Map<String, Object> result = new HashMap<>();
        result.put("replyId", id);
        result.put("isLiked", false);
        result.put("likeCount", (int)(Math.random() * 20) + 3); // 模拟点赞数
        return Result.success(result);
    }
    
    /**
     * 获取茶文化文章详情
     * 任务组0：返回测试数据，不走Service/DB
     *
     * @param id 文章ID
     * @return 结果
     */
    @GetMapping("/articles/{id}")
    public Result<Map<String, Object>> getArticleById(@PathVariable Long id) {
        // 根据ID返回对应的测试文章详情
        Map<String, Object> article = new HashMap<>();
        article.put("id", id);
        
        // 根据ID设置不同的文章内容
        switch (id.intValue()) {
            case 1:
                article.put("title", "商南茶叶的千年传承");
                article.put("category", "history");
                article.put("summary", "探索商南茶叶从唐代至今的悠久历史");
                article.put("content", "商南茶叶历史悠久，可追溯至唐代。据史料记载，唐代商南地区就有茶叶种植的记录...");
                break;
            case 2:
                article.put("title", "古代茶马古道与商南茶");
                article.put("category", "history");
                article.put("summary", "茶马古道上的商南茶叶贸易历史");
                article.put("content", "茶马古道是中国古代重要的贸易通道，商南茶叶通过这条古道走向全国...");
                break;
            default:
                article.put("title", "茶文化文章 " + id);
                article.put("category", "general");
                article.put("summary", "这是一篇关于茶文化的文章");
                article.put("content", "这是文章ID为" + id + "的详细内容。文章内容丰富，涵盖了茶文化的各个方面...");
        }
        
        article.put("author", "茶文化专家");
        article.put("coverImage", "https://via.placeholder.com/800x400/4CAF50/FFFFFF?text=Article" + id);
        article.put("publishTime", LocalDateTime.now().minusDays(id.intValue()));
        article.put("readCount", (int)(Math.random() * 1000) + 100);
        article.put("likeCount", (int)(Math.random() * 50) + 10);
        article.put("status", 1);
        article.put("tags", Arrays.asList("茶文化", "商南茶", "传统文化"));
        
        return Result.success(article);
    }
    
    /**
     * 获取茶文化文章列表
     * 任务组0：返回测试数据，不走Service/DB
     *
     * @param page 页码
     * @param size 每页数量
     * @param category 文章分类（可选）
     * @return 结果
     */
    @GetMapping("/articles")
    public Result<Map<String, Object>> listArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category) {
        
        // 创建测试文章数据
        List<Map<String, Object>> articles = new ArrayList<>();
        
        // 茶叶历史分类文章
        if (category == null || "history".equals(category)) {
            articles.add(createTestArticle(1L, "商南茶叶的千年传承", "history", "探索商南茶叶从唐代至今的悠久历史", "2024-12-15"));
            articles.add(createTestArticle(2L, "古代茶马古道与商南茶", "history", "茶马古道上的商南茶叶贸易历史", "2024-12-14"));
            articles.add(createTestArticle(3L, "明清时期的商南茶业", "history", "明清两朝商南茶业的兴盛与发展", "2024-12-13"));
            articles.add(createTestArticle(4L, "商南茶叶的历史名人", "history", "历史上与商南茶叶相关的名人轶事", "2024-12-12"));
            articles.add(createTestArticle(5L, "古法制茶工艺传承", "history", "传统制茶技艺的历史传承与发展", "2024-12-11"));
        }
        
        // 茶艺茶道分类文章
        if (category == null || "art".equals(category)) {
            articles.add(createTestArticle(6L, "商南茶艺的独特魅力", "art", "领略商南茶艺的精妙技法与文化内涵", "2024-12-15"));
            articles.add(createTestArticle(7L, "茶道礼仪与修身养性", "art", "通过茶道修炼内心，提升个人修养", "2024-12-14"));
            articles.add(createTestArticle(8L, "四季品茶的艺术", "art", "不同季节品茶的方法与意境", "2024-12-13"));
            articles.add(createTestArticle(9L, "茶具选择与搭配", "art", "如何选择合适的茶具提升品茶体验", "2024-12-12"));
            articles.add(createTestArticle(10L, "茶席布置的美学", "art", "营造优雅茶席，享受品茶时光", "2024-12-11"));
        }
        
        // 茶叶百科分类文章
        if (category == null || "encyclopedia".equals(category)) {
            articles.add(createTestArticle(11L, "商南绿茶品种大全", "encyclopedia", "详细介绍商南地区各种绿茶品种特色", "2024-12-15"));
            articles.add(createTestArticle(12L, "茶叶的营养价值", "encyclopedia", "科学解析茶叶中的营养成分与健康功效", "2024-12-14"));
            articles.add(createTestArticle(13L, "茶叶储存保鲜技巧", "encyclopedia", "正确储存茶叶，保持最佳品质", "2024-12-13"));
            articles.add(createTestArticle(14L, "茶叶冲泡水温指南", "encyclopedia", "不同茶类的最佳冲泡水温", "2024-12-12"));
            articles.add(createTestArticle(15L, "茶叶等级划分标准", "encyclopedia", "了解茶叶品质等级的划分依据", "2024-12-11"));
        }
        
        // 茶文化传承分类文章
        if (category == null || "heritage".equals(category)) {
            articles.add(createTestArticle(16L, "商南茶文化的传承与创新", "heritage", "在传承中创新，让茶文化焕发新活力", "2024-12-15"));
            articles.add(createTestArticle(17L, "茶文化在现代生活中的意义", "heritage", "茶文化如何融入现代人的生活方式", "2024-12-14"));
            articles.add(createTestArticle(18L, "青年一代与茶文化传承", "heritage", "年轻人如何传承和发扬茶文化", "2024-12-13"));
            articles.add(createTestArticle(19L, "茶文化的国际传播", "heritage", "中国茶文化走向世界的历程", "2024-12-12"));
            articles.add(createTestArticle(20L, "非遗茶艺的保护与传承", "heritage", "保护和传承非物质文化遗产茶艺", "2024-12-11"));
        }
        
        // 根据分页参数返回对应数据
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, articles.size());
        List<Map<String, Object>> pageArticles = articles.subList(startIndex, endIndex);
        
        // 构造分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("articles", pageArticles);
        
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current", page);
        pagination.put("pageSize", size);
        pagination.put("total", articles.size());
        pagination.put("pages", (int) Math.ceil((double) articles.size() / size));
        result.put("pagination", pagination);
        
        return Result.success(result);
    }
    
    /**
     * 创建测试文章数据
     */
    private Map<String, Object> createTestArticle(Long id, String title, String category, String summary, String date) {
        Map<String, Object> article = new HashMap<>();
        article.put("id", id);
        article.put("title", title);
        article.put("category", category);
        article.put("summary", summary);
        article.put("content", "这是" + title + "的详细内容。" + summary + "。文章内容丰富，图文并茂，为读者提供了深入的了解。");
        article.put("author", "茶文化专家");
        article.put("coverImage", "https://via.placeholder.com/400x300/4CAF50/FFFFFF?text=" + title.substring(0, Math.min(title.length(), 4)));
        article.put("publishTime", date);
        article.put("readCount", (int)(Math.random() * 1000) + 100);
        article.put("likeCount", (int)(Math.random() * 50) + 10);
        article.put("status", 1);
        return article;
    }
    
    /**
     * 获取版块名称（辅助方法）
     */
    private String getTopicName(Integer topicId) {
        switch (topicId) {
            case 1:
                return "茶文化讨论";
            case 2:
                return "品茶心得";
            case 3:
                return "茶艺交流";
            default:
                return "综合讨论";
        }
    }
    
    /**
     * 创建茶文化文章(管理员)
     * 任务组E：返回测试数据，不走Service/DB
     *
     * @param articleData 文章信息
     * @return 结果
     */
    @PostMapping("/articles")
    public Result<Map<String, Object>> createArticle(@RequestBody Map<String, Object> articleData) {
        // 模拟创建文章，返回新文章数据
        Map<String, Object> newArticle = new HashMap<>();
        newArticle.put("id", System.currentTimeMillis()); // 使用时间戳作为ID
        newArticle.put("title", articleData.get("title"));
        newArticle.put("subtitle", articleData.get("subtitle"));
        newArticle.put("category", articleData.get("category"));
        newArticle.put("content", articleData.get("content"));
        newArticle.put("summary", articleData.get("summary"));
        newArticle.put("author", articleData.get("author"));
        newArticle.put("coverImage", articleData.get("coverImage"));
        newArticle.put("videoUrl", articleData.get("videoUrl"));
        newArticle.put("tags", articleData.get("tags"));
        newArticle.put("source", articleData.get("source"));
        newArticle.put("status", articleData.get("status"));
        newArticle.put("isTop", articleData.get("isTop"));
        newArticle.put("isRecommend", articleData.get("isRecommend"));
        newArticle.put("publishTime", LocalDateTime.now());
        newArticle.put("createTime", LocalDateTime.now());
        newArticle.put("updateTime", LocalDateTime.now());
        newArticle.put("readCount", 0);
        newArticle.put("likeCount", 0);
        
        return Result.success(newArticle);
    }
    
    /**
     * 更新茶文化文章(管理员)
     * 任务组E：返回测试数据，不走Service/DB
     *
     * @param id 文章ID
     * @param articleData 文章信息
     * @return 结果
     */
    @PutMapping("/articles/{id}")
    public Result<Map<String, Object>> updateArticle(@PathVariable Long id, @RequestBody Map<String, Object> articleData) {
        // 模拟更新文章，返回更新后的文章数据
        Map<String, Object> updatedArticle = new HashMap<>();
        updatedArticle.put("id", id);
        updatedArticle.put("title", articleData.get("title"));
        updatedArticle.put("subtitle", articleData.get("subtitle"));
        updatedArticle.put("category", articleData.get("category"));
        updatedArticle.put("content", articleData.get("content"));
        updatedArticle.put("summary", articleData.get("summary"));
        updatedArticle.put("author", articleData.get("author"));
        updatedArticle.put("coverImage", articleData.get("coverImage"));
        updatedArticle.put("videoUrl", articleData.get("videoUrl"));
        updatedArticle.put("tags", articleData.get("tags"));
        updatedArticle.put("source", articleData.get("source"));
        updatedArticle.put("status", articleData.get("status"));
        updatedArticle.put("isTop", articleData.get("isTop"));
        updatedArticle.put("isRecommend", articleData.get("isRecommend"));
        updatedArticle.put("publishTime", LocalDateTime.now().minusDays(id.intValue()));
        updatedArticle.put("createTime", LocalDateTime.now().minusDays(id.intValue()));
        updatedArticle.put("updateTime", LocalDateTime.now()); // 更新时间为当前时间
        updatedArticle.put("readCount", (int)(Math.random() * 1000) + 100);
        updatedArticle.put("likeCount", (int)(Math.random() * 50) + 10);
        
        return Result.success(updatedArticle);
    }
    
    /**
     * 删除茶文化文章(管理员)
     * 任务组E：返回测试数据，不走Service/DB
     *
     * @param id 文章ID
     * @return 结果
     */
    @DeleteMapping("/articles/{id}")
    public Result<Boolean> deleteArticle(@PathVariable Long id) {
        // 模拟删除操作
        return Result.success(true);
    }
    
    /**
     * 获取首页内容
     *
     * @param section 板块
     * @return 结果
     */
    @GetMapping("/home-contents")
    public Result<List<HomeContent>> listHomeContents(
            @RequestParam(required = false) String section) {
        List<HomeContent> contents = forumService.listHomeContents(section);
        return Result.success(contents);
    }
    
    /**
     * 创建首页内容(管理员)
     *
     * @param content 内容信息
     * @return 结果
     */
    @PostMapping("/home-contents")
    public Result<HomeContent> createHomeContent(@RequestBody HomeContent content) {
        HomeContent createdContent = forumService.createHomeContent(content);
        return Result.success(createdContent);
    }
    
    /**
     * 更新首页内容(管理员)
     *
     * @param id 内容ID
     * @param content 内容信息
     * @return 结果
     */
    @PutMapping("/home-contents/{id}")
    public Result<Boolean> updateHomeContent(@PathVariable Integer id, @RequestBody HomeContent content) {
        content.setId(id);
        boolean success = forumService.updateHomeContent(content);
        return Result.success(success);
    }
    
    /**
     * 删除首页内容(管理员)
     *
     * @param id 内容ID
     * @return 结果
     */
    @DeleteMapping("/home-contents/{id}")
    public Result<Boolean> deleteHomeContent(@PathVariable Integer id) {
        boolean success = forumService.deleteHomeContent(id);
        return Result.success(success);
    }
    
    // ===== 任务组0：Banner管理相关接口（返回测试数据，不走Service/DB） =====
    
    /**
     * 获取Banner列表
     * 任务组0：返回测试数据，不走Service/DB
     *
     * @return Banner列表
     */
    @GetMapping("/banners")
    public Result<List<Map<String, Object>>> getBanners() {
        // 返回测试Banner数据
        List<Map<String, Object>> banners = new ArrayList<>();
        
        Map<String, Object> banner1 = new HashMap<>();
        banner1.put("id", 1);
        banner1.put("title", "商南茶文化传承千年");
        banner1.put("subtitle", "探索茶叶的历史与文化底蕴");
        banner1.put("imageUrl", "https://via.placeholder.com/800x400/4CAF50/FFFFFF?text=Banner1");
        banner1.put("linkUrl", "/culture/history");
        banner1.put("sortOrder", 1);
        banner1.put("status", 1);
        banner1.put("createTime", LocalDateTime.now().minusDays(5));
        banners.add(banner1);
        
        Map<String, Object> banner2 = new HashMap<>();
        banner2.put("id", 2);
        banner2.put("title", "品味商南好茶");
        banner2.put("subtitle", "精选优质茶叶，传承古法工艺");
        banner2.put("imageUrl", "https://via.placeholder.com/800x400/2196F3/FFFFFF?text=Banner2");
        banner2.put("linkUrl", "/tea/recommend");
        banner2.put("sortOrder", 2);
        banner2.put("status", 1);
        banner2.put("createTime", LocalDateTime.now().minusDays(3));
        banners.add(banner2);
        
        Map<String, Object> banner3 = new HashMap<>();
        banner3.put("id", 3);
        banner3.put("title", "茶艺体验课程");
        banner3.put("subtitle", "专业茶艺师指导，感受茶道魅力");
        banner3.put("imageUrl", "https://via.placeholder.com/800x400/FF9800/FFFFFF?text=Banner3");
        banner3.put("linkUrl", "/course/tea-art");
        banner3.put("sortOrder", 3);
        banner3.put("status", 1);
        banner3.put("createTime", LocalDateTime.now().minusDays(1));
        banners.add(banner3);
        
        return Result.success(banners);
    }
    
    /**
     * 上传Banner
     * 任务组0：返回测试数据，不走Service/DB
     *
     * @param file Banner图片文件
     * @param title Banner标题
     * @param subtitle Banner副标题
     * @param linkUrl 链接地址
     * @return 上传结果
     */
    @PostMapping("/banners")
    public Result<Map<String, Object>> uploadBanner(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "subtitle", required = false) String subtitle,
            @RequestParam(value = "linkUrl", required = false) String linkUrl) {
        
        // 模拟文件上传处理
        if (file.isEmpty()) {
            return Result.failure("请选择要上传的图片文件");
        }
        
        // 返回测试Banner数据
        Map<String, Object> banner = new HashMap<>();
        banner.put("id", System.currentTimeMillis()); // 使用时间戳作为ID
        banner.put("title", title);
        banner.put("subtitle", subtitle != null ? subtitle : "");
        banner.put("imageUrl", "https://via.placeholder.com/800x400/E91E63/FFFFFF?text=" + title);
        banner.put("linkUrl", linkUrl != null ? linkUrl : "");
        banner.put("sortOrder", 99); // 新上传的排在最后
        banner.put("status", 1);
        banner.put("createTime", LocalDateTime.now());
        
        return Result.success(banner);
    }
    
    /**
     * 更新Banner
     * 任务组0：返回测试数据，不走Service/DB
     *
     * @param id Banner ID
     * @param bannerData Banner数据
     * @return 更新结果
     */
    @PutMapping("/banners/{id}")
    public Result<Map<String, Object>> updateBanner(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> bannerData) {
        
        // 返回更新后的测试数据
        Map<String, Object> banner = new HashMap<>();
        banner.put("id", id);
        banner.put("title", bannerData.get("title"));
        banner.put("subtitle", bannerData.get("subtitle"));
        banner.put("imageUrl", bannerData.get("imageUrl"));
        banner.put("linkUrl", bannerData.get("linkUrl"));
        banner.put("sortOrder", bannerData.get("sortOrder"));
        banner.put("status", bannerData.get("status"));
        banner.put("updateTime", LocalDateTime.now());
        
        return Result.success(banner);
    }
    
    /**
     * 删除Banner
     * 任务组0：返回测试数据，不走Service/DB
     *
     * @param id Banner ID
     * @return 删除结果
     */
    @DeleteMapping("/banners/{id}")
    public Result<Boolean> deleteBanner(@PathVariable Long id) {
        // 模拟删除操作
        return Result.success(true);
    }
    
    /**
     * 更新Banner顺序
     * 任务组0：返回测试数据，不走Service/DB
     *
     * @param orderData Banner顺序数据
     * @return 更新结果
     */
    @PutMapping("/banners/order")
    public Result<List<Map<String, Object>>> updateBannerOrder(@RequestBody Map<String, Object> orderData) {
        @SuppressWarnings("unchecked")
        List<Long> bannerIds = (List<Long>) orderData.get("bannerIds");
        
        // 返回重新排序后的测试数据
        List<Map<String, Object>> banners = new ArrayList<>();
        for (int i = 0; i < bannerIds.size(); i++) {
            Map<String, Object> banner = new HashMap<>();
            banner.put("id", bannerIds.get(i));
            banner.put("title", "Banner " + bannerIds.get(i));
            banner.put("subtitle", "重新排序后的Banner");
            banner.put("imageUrl", "https://via.placeholder.com/800x400/9C27B0/FFFFFF?text=Banner" + bannerIds.get(i));
            banner.put("linkUrl", "/banner/" + bannerIds.get(i));
            banner.put("sortOrder", i + 1);
            banner.put("status", 1);
            banner.put("updateTime", LocalDateTime.now());
            banners.add(banner);
        }
        
        return Result.success(banners);
    }
    
    // ===== 任务组0：首页数据接口增强（返回完整结构化数据） =====
    
    /**
     * 获取首页完整数据
     * 任务组0：返回测试数据，不走Service/DB
     * 覆盖原有的 /home-contents 接口，返回完整的首页数据结构
     *
     * @return 首页数据
     */
    @GetMapping("/home-contents")
    public Result<Map<String, Object>> getHomeContents() {
        Map<String, Object> homeData = new HashMap<>();
        
        // Banner数据
        List<Map<String, Object>> banners = new ArrayList<>();
        Map<String, Object> banner1 = new HashMap<>();
        banner1.put("id", 1);
        banner1.put("title", "商南茶文化传承千年");
        banner1.put("subtitle", "探索茶叶的历史与文化底蕴");
        banner1.put("imageUrl", "https://via.placeholder.com/800x400/4CAF50/FFFFFF?text=Banner1");
        banners.add(banner1);
        
        Map<String, Object> banner2 = new HashMap<>();
        banner2.put("id", 2);
        banner2.put("title", "品味商南好茶");
        banner2.put("subtitle", "精选优质茶叶，传承古法工艺");
        banner2.put("imageUrl", "https://via.placeholder.com/800x400/2196F3/FFFFFF?text=Banner2");
        banners.add(banner2);
        
        homeData.put("banners", banners);
        
        // 文化特色数据
        List<Map<String, Object>> cultureFeatures = new ArrayList<>();
        Map<String, Object> feature1 = new HashMap<>();
        feature1.put("id", 1);
        feature1.put("title", "千年茶乡");
        feature1.put("description", "商南茶文化历史悠久，传承千年");
        feature1.put("icon", "tea-leaf");
        cultureFeatures.add(feature1);
        
        Map<String, Object> feature2 = new HashMap<>();
        feature2.put("id", 2);
        feature2.put("title", "古法工艺");
        feature2.put("description", "传统制茶工艺，匠心传承");
        feature2.put("icon", "craft");
        cultureFeatures.add(feature2);
        
        homeData.put("cultureFeatures", cultureFeatures);
        
        // 茶叶分类数据
        List<Map<String, Object>> teaCategories = new ArrayList<>();
        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 1);
        category1.put("name", "绿茶");
        category1.put("description", "清香淡雅，回甘悠长");
        category1.put("image", "https://via.placeholder.com/200x200/4CAF50/FFFFFF?text=绿茶");
        teaCategories.add(category1);
        
        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", 2);
        category2.put("name", "红茶");
        category2.put("description", "香醇浓郁，温润甘甜");
        category2.put("image", "https://via.placeholder.com/200x200/F44336/FFFFFF?text=红茶");
        teaCategories.add(category2);
        
        homeData.put("teaCategories", teaCategories);
        
        // 最新资讯数据
        List<Map<String, Object>> latestNews = new ArrayList<>();
        Map<String, Object> news1 = new HashMap<>();
        news1.put("id", 1);
        news1.put("title", "商南茶叶荣获国际金奖");
        news1.put("summary", "在国际茶叶博览会上，商南茶叶凭借优异品质荣获金奖");
        news1.put("publishTime", LocalDateTime.now().minusDays(2));
        latestNews.add(news1);
        
        Map<String, Object> news2 = new HashMap<>();
        news2.put("id", 2);
        news2.put("title", "春茶采摘季正式开始");
        news2.put("summary", "随着气温回升，商南各大茶园春茶采摘工作全面展开");
        news2.put("publishTime", LocalDateTime.now().minusDays(1));
        latestNews.add(news2);
        
        homeData.put("latestNews", latestNews);
        
        // 合作伙伴数据
        List<Map<String, Object>> partners = new ArrayList<>();
        Map<String, Object> partner1 = new HashMap<>();
        partner1.put("id", 1);
        partner1.put("name", "中国茶叶协会");
        partner1.put("logo", "https://via.placeholder.com/150x80/607D8B/FFFFFF?text=协会");
        partners.add(partner1);
        
        Map<String, Object> partner2 = new HashMap<>();
        partner2.put("id", 2);
        partner2.put("name", "陕西省茶业集团");
        partner2.put("logo", "https://via.placeholder.com/150x80/795548/FFFFFF?text=集团");
        partners.add(partner2);
        
        homeData.put("partners", partners);
        
        return Result.success(homeData);
    }
    
    /**
     * 更新首页数据
     * 任务组0：返回测试数据，不走Service/DB
     *
     * @param homeData 首页数据
     * @return 更新结果
     */
    @PutMapping("/home-contents")
    public Result<Map<String, Object>> updateHomeContents(@RequestBody Map<String, Object> homeData) {
        // 返回更新后的数据（实际上就是传入的数据加上更新时间）
        homeData.put("updateTime", LocalDateTime.now());
        return Result.success(homeData);
    }
    
    // ===== 任务组C：帖子点赞收藏相关接口（返回测试数据，不走Service/DB） =====
    
    /**
     * 点赞帖子
     * 任务组C：返回测试数据，不走Service/DB
     *
     * @param id 帖子ID
     * @return 点赞结果
     */
    @PostMapping("/posts/{id}/like")
    public Result<Map<String, Object>> likePost(@PathVariable Long id) {
        // 模拟点赞操作，返回更新后的点赞状态
        Map<String, Object> result = new HashMap<>();
        result.put("postId", id);
        result.put("isLiked", true);
        result.put("likeCount", (int)(Math.random() * 100) + 50); // 模拟点赞数
        result.put("message", "点赞成功");
        return Result.success(result);
    }
    
    /**
     * 取消点赞帖子
     * 任务组C：返回测试数据，不走Service/DB
     *
     * @param id 帖子ID
     * @return 取消点赞结果
     */
    @DeleteMapping("/posts/{id}/like")
    public Result<Map<String, Object>> unlikePost(@PathVariable Long id) {
        // 模拟取消点赞操作，返回更新后的点赞状态
        Map<String, Object> result = new HashMap<>();
        result.put("postId", id);
        result.put("isLiked", false);
        result.put("likeCount", (int)(Math.random() * 100) + 30); // 模拟点赞数
        result.put("message", "已取消点赞");
        return Result.success(result);
    }
    
    /**
     * 收藏帖子
     * 任务组C：返回测试数据，不走Service/DB
     *
     * @param id 帖子ID
     * @return 收藏结果
     */
    @PostMapping("/posts/{id}/favorite")
    public Result<Map<String, Object>> favoritePost(@PathVariable Long id) {
        // 模拟收藏操作，返回更新后的收藏状态
        Map<String, Object> result = new HashMap<>();
        result.put("postId", id);
        result.put("isFavorited", true);
        result.put("favoriteCount", (int)(Math.random() * 50) + 20); // 模拟收藏数
        result.put("message", "收藏成功");
        return Result.success(result);
    }
    
    /**
     * 取消收藏帖子
     * 任务组C：返回测试数据，不走Service/DB
     *
     * @param id 帖子ID
     * @return 取消收藏结果
     */
    @DeleteMapping("/posts/{id}/favorite")
    public Result<Map<String, Object>> unfavoritePost(@PathVariable Long id) {
        // 模拟取消收藏操作，返回更新后的收藏状态
        Map<String, Object> result = new HashMap<>();
        result.put("postId", id);
        result.put("isFavorited", false);
        result.put("favoriteCount", (int)(Math.random() * 50) + 10); // 模拟收藏数
        result.put("message", "已取消收藏");
        return Result.success(result);
    }
} 