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

import java.util.List;

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
     *
     * @return 结果
     */
    @GetMapping("/topics")
    public Result<List<ForumTopic>> listTopics() {
        List<ForumTopic> topics = forumService.listTopics();
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
     *
     * @param id 帖子ID
     * @return 结果
     */
    @GetMapping("/posts/{id}")
    public Result<ForumPost> getPostById(@PathVariable Long id) {
        ForumPost post = forumService.getPostById(id);
        return Result.success(post);
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
     *
     * @param post 帖子信息
     * @return 结果
     */
    @PostMapping("/posts")
    public Result<ForumPost> createPost(@RequestBody ForumPost post) {
        // TODO: 获取当前登录用户ID
        String userId = "currentUserId";
        post.setUserId(userId);
        
        ForumPost createdPost = forumService.createPost(post);
        return Result.success(createdPost);
    }
    
    /**
     * 更新帖子
     *
     * @param id 帖子ID
     * @param post 帖子信息
     * @return 结果
     */
    @PutMapping("/posts/{id}")
    public Result<Boolean> updatePost(@PathVariable Long id, @RequestBody ForumPost post) {
        post.setId(id);
        boolean success = forumService.updatePost(post);
        return Result.success(success);
    }
    
    /**
     * 删除帖子
     *
     * @param id 帖子ID
     * @return 结果
     */
    @DeleteMapping("/posts/{id}")
    public Result<Boolean> deletePost(@PathVariable Long id) {
        boolean success = forumService.deletePost(id);
        return Result.success(success);
    }
    
    /**
     * 设置帖子置顶(管理员)
     *
     * @param id 帖子ID
     * @param isSticky 是否置顶
     * @return 结果
     */
    @PutMapping("/posts/{id}/sticky")
    public Result<Boolean> setPostSticky(
            @PathVariable Long id,
            @RequestParam Boolean isSticky) {
        boolean success = forumService.setPostSticky(id, isSticky);
        return Result.success(success);
    }
    
    /**
     * 设置帖子精华(管理员)
     *
     * @param id 帖子ID
     * @param isEssence 是否精华
     * @return 结果
     */
    @PutMapping("/posts/{id}/essence")
    public Result<Boolean> setPostEssence(
            @PathVariable Long id,
            @RequestParam Boolean isEssence) {
        boolean success = forumService.setPostEssence(id, isEssence);
        return Result.success(success);
    }
    
    /**
     * 获取帖子回复列表
     *
     * @param postId 帖子ID
     * @param page 页码
     * @param size 每页数量
     * @return 结果
     */
    @GetMapping("/posts/{postId}/replies")
    public Result<PageResult<ForumReply>> listRepliesByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<ForumReply> pageResult = forumService.listRepliesByPost(postId, pageParam);
        return Result.success(pageResult);
    }
    
    /**
     * 创建回复
     *
     * @param reply 回复信息
     * @return 结果
     */
    @PostMapping("/replies")
    public Result<ForumReply> createReply(@RequestBody ForumReply reply) {
        // TODO: 获取当前登录用户ID
        String userId = "currentUserId";
        reply.setUserId(userId);
        
        ForumReply createdReply = forumService.createReply(reply);
        return Result.success(createdReply);
    }
    
    /**
     * 删除回复
     *
     * @param id 回复ID
     * @return 结果
     */
    @DeleteMapping("/replies/{id}")
    public Result<Boolean> deleteReply(@PathVariable Long id) {
        boolean success = forumService.deleteReply(id);
        return Result.success(success);
    }
    
    /**
     * 获取茶文化文章详情
     *
     * @param id 文章ID
     * @return 结果
     */
    @GetMapping("/articles/{id}")
    public Result<TeaArticle> getArticleById(@PathVariable Long id) {
        TeaArticle article = forumService.getArticleById(id);
        return Result.success(article);
    }
    
    /**
     * 获取茶文化文章列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 结果
     */
    @GetMapping("/articles")
    public Result<PageResult<TeaArticle>> listArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        PageResult<TeaArticle> pageResult = forumService.listArticles(pageParam);
        return Result.success(pageResult);
    }
    
    /**
     * 创建茶文化文章(管理员)
     *
     * @param article 文章信息
     * @return 结果
     */
    @PostMapping("/articles")
    public Result<TeaArticle> createArticle(@RequestBody TeaArticle article) {
        TeaArticle createdArticle = forumService.createArticle(article);
        return Result.success(createdArticle);
    }
    
    /**
     * 更新茶文化文章(管理员)
     *
     * @param id 文章ID
     * @param article 文章信息
     * @return 结果
     */
    @PutMapping("/articles/{id}")
    public Result<Boolean> updateArticle(@PathVariable Long id, @RequestBody TeaArticle article) {
        article.setId(id);
        boolean success = forumService.updateArticle(article);
        return Result.success(success);
    }
    
    /**
     * 删除茶文化文章(管理员)
     *
     * @param id 文章ID
     * @return 结果
     */
    @DeleteMapping("/articles/{id}")
    public Result<Boolean> deleteArticle(@PathVariable Long id) {
        boolean success = forumService.deleteArticle(id);
        return Result.success(success);
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
} 