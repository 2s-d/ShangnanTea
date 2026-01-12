package com.shangnantea.controller;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.model.entity.forum.ForumPost;
import com.shangnantea.model.entity.forum.ForumReply;
import com.shangnantea.model.entity.forum.ForumTopic;
import com.shangnantea.model.entity.forum.HomeContent;
import com.shangnantea.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
    public Result<List<Map<String, Object>>> listTopics() {
        List<Map<String, Object>> result = forumService.listTopics();
        return Result.success(result);
    }
    
    /**
     * 获取论坛主题详情
     *
     * @param id 主题ID
     * @return 结果
     */
    @GetMapping("/topics/{id}")
    public Result<ForumTopic> getTopicById(@PathVariable Integer id) {
        ForumTopic result = forumService.getTopicById(id);
        return Result.success(result);
    }
    
    /**
     * 创建论坛主题(管理员)
     *
     * @param topic 主题信息
     * @return 结果
     */
    @PostMapping("/topics")
    public Result<ForumTopic> createTopic(@RequestBody ForumTopic topic) {
        ForumTopic result = forumService.createTopic(topic);
        return Result.success(result);
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
        Boolean result = forumService.updateTopic(topic);
        return Result.success(result);
    }
    
    /**
     * 删除论坛主题(管理员)
     *
     * @param id 主题ID
     * @return 结果
     */
    @DeleteMapping("/topics/{id}")
    public Result<Boolean> deleteTopic(@PathVariable Integer id) {
        Boolean result = forumService.deleteTopic(id);
        return Result.success(result);
    }
    
    /**
     * 获取帖子详情
     *
     * @param id 帖子ID
     * @return 结果
     */
    @GetMapping("/posts/{id}")
    public Result<Map<String, Object>> getPostById(@PathVariable Long id) {
        Map<String, Object> result = forumService.getPostById(id);
        return Result.success(result);
    }

    /**
     * 获取帖子列表（支持筛选、排序、分页）
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
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        Map<String, Object> result = forumService.listPosts(topicId, keyword, sortBy, pageParam);
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
        PageResult<ForumPost> result = forumService.listPostsByTopic(topicId, pageParam);
        return Result.success(result);
    }
    
    /**
     * 创建帖子
     *
     * @param postData 帖子信息
     * @return 结果
     */
    @PostMapping("/posts")
    public Result<Map<String, Object>> createPost(@RequestBody Map<String, Object> postData) {
        Map<String, Object> result = forumService.createPost(postData);
        return Result.success(result);
    }
    
    /**
     * 更新帖子
     *
     * @param id 帖子ID
     * @param postData 帖子信息
     * @return 结果
     */
    @PutMapping("/posts/{id}")
    public Result<Map<String, Object>> updatePost(@PathVariable Long id, @RequestBody Map<String, Object> postData) {
        Map<String, Object> result = forumService.updatePost(id, postData);
        return Result.success(result);
    }
    
    /**
     * 删除帖子
     *
     * @param id 帖子ID
     * @return 结果
     */
    @DeleteMapping("/posts/{id}")
    public Result<Boolean> deletePost(@PathVariable Long id) {
        Boolean result = forumService.deletePost(id);
        return Result.success(result);
    }
    
    /**
     * 设置帖子置顶(管理员)
     *
     * @param id 帖子ID
     * @param isSticky 是否置顶
     * @return 结果
     */
    @PutMapping("/posts/{id}/sticky")
    public Result<Map<String, Object>> setPostSticky(
            @PathVariable Long id,
            @RequestParam Boolean isSticky) {
        Map<String, Object> result = forumService.setPostSticky(id, isSticky);
        return Result.success(result);
    }
    
    /**
     * 设置帖子精华(管理员)
     *
     * @param id 帖子ID
     * @param isEssence 是否精华
     * @return 结果
     */
    @PutMapping("/posts/{id}/essence")
    public Result<Map<String, Object>> setPostEssence(
            @PathVariable Long id,
            @RequestParam Boolean isEssence) {
        Map<String, Object> result = forumService.setPostEssence(id, isEssence);
        return Result.success(result);
    }
    
    /**
     * 获取待审核帖子列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 待审核帖子列表
     */
    @GetMapping("/posts/pending")
    public Result<Map<String, Object>> getPendingPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        Map<String, Object> result = forumService.getPendingPosts(pageParam);
        return Result.success(result);
    }
    
    /**
     * 审核通过帖子
     *
     * @param id 帖子ID
     * @param auditData 审核数据
     * @return 审核结果
     */
    @PostMapping("/posts/{id}/approve")
    public Result<Map<String, Object>> approvePost(
            @PathVariable Long id,
            @RequestBody Map<String, Object> auditData) {
        Map<String, Object> result = forumService.approvePost(id, auditData);
        return Result.success(result);
    }
    
    /**
     * 审核拒绝帖子
     *
     * @param id 帖子ID
     * @param auditData 审核数据（包含拒绝原因）
     * @return 审核结果
     */
    @PostMapping("/posts/{id}/reject")
    public Result<Map<String, Object>> rejectPost(
            @PathVariable Long id,
            @RequestBody Map<String, Object> auditData) {
        Map<String, Object> result = forumService.rejectPost(id, auditData);
        return Result.success(result);
    }
    
    /**
     * 获取帖子回复列表
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
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        Map<String, Object> result = forumService.listRepliesByPost(postId, sortBy, pageParam);
        return Result.success(result);
    }
    
    /**
     * 创建回复
     *
     * @param replyData 回复信息
     * @return 结果
     */
    @PostMapping("/posts/{postId}/replies")
    public Result<Map<String, Object>> createReply(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> replyData) {
        Map<String, Object> result = forumService.createReply(postId, replyData);
        return Result.success(result);
    }
    
    /**
     * 删除回复
     *
     * @param id 回复ID
     * @return 结果
     */
    @DeleteMapping("/replies/{id}")
    public Result<Boolean> deleteReply(@PathVariable Long id) {
        Boolean result = forumService.deleteReply(id);
        return Result.success(result);
    }
    
    /**
     * 点赞回复
     *
     * @param id 回复ID
     * @return 结果
     */
    @PostMapping("/replies/{id}/like")
    public Result<Map<String, Object>> likeReply(@PathVariable Long id) {
        Map<String, Object> result = forumService.likeReply(id);
        return Result.success(result);
    }
    
    /**
     * 取消点赞回复
     *
     * @param id 回复ID
     * @return 结果
     */
    @DeleteMapping("/replies/{id}/like")
    public Result<Map<String, Object>> unlikeReply(@PathVariable Long id) {
        Map<String, Object> result = forumService.unlikeReply(id);
        return Result.success(result);
    }
    
    /**
     * 获取茶文化文章详情
     *
     * @param id 文章ID
     * @return 结果
     */
    @GetMapping("/articles/{id}")
    public Result<Map<String, Object>> getArticleById(@PathVariable Long id) {
        Map<String, Object> result = forumService.getArticleById(id);
        return Result.success(result);
    }
    
    /**
     * 获取茶文化文章列表
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
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(page);
        pageParam.setPageSize(size);
        Map<String, Object> result = forumService.listArticles(category, pageParam);
        return Result.success(result);
    }
    
    /**
     * 创建茶文化文章(管理员)
     *
     * @param articleData 文章信息
     * @return 结果
     */
    @PostMapping("/articles")
    public Result<Map<String, Object>> createArticle(@RequestBody Map<String, Object> articleData) {
        Map<String, Object> result = forumService.createArticle(articleData);
        return Result.success(result);
    }
    
    /**
     * 更新茶文化文章(管理员)
     *
     * @param id 文章ID
     * @param articleData 文章信息
     * @return 结果
     */
    @PutMapping("/articles/{id}")
    public Result<Map<String, Object>> updateArticle(@PathVariable Long id, @RequestBody Map<String, Object> articleData) {
        Map<String, Object> result = forumService.updateArticle(id, articleData);
        return Result.success(result);
    }
    
    /**
     * 删除茶文化文章(管理员)
     *
     * @param id 文章ID
     * @return 结果
     */
    @DeleteMapping("/articles/{id}")
    public Result<Boolean> deleteArticle(@PathVariable Long id) {
        Boolean result = forumService.deleteArticle(id);
        return Result.success(result);
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
        List<HomeContent> result = forumService.listHomeContents(section);
        return Result.success(result);
    }
    
    /**
     * 创建首页内容(管理员)
     *
     * @param content 内容信息
     * @return 结果
     */
    @PostMapping("/home-contents")
    public Result<HomeContent> createHomeContent(@RequestBody HomeContent content) {
        HomeContent result = forumService.createHomeContent(content);
        return Result.success(result);
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
        Boolean result = forumService.updateHomeContent(content);
        return Result.success(result);
    }
    
    /**
     * 删除首页内容(管理员)
     *
     * @param id 内容ID
     * @return 结果
     */
    @DeleteMapping("/home-contents/{id}")
    public Result<Boolean> deleteHomeContent(@PathVariable Integer id) {
        Boolean result = forumService.deleteHomeContent(id);
        return Result.success(result);
    }
    
    /**
     * 获取Banner列表
     *
     * @return Banner列表
     */
    @GetMapping("/banners")
    public Result<List<Map<String, Object>>> getBanners() {
        List<Map<String, Object>> result = forumService.getBanners();
        return Result.success(result);
    }
    
    /**
     * 上传Banner
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
        Map<String, Object> result = forumService.uploadBanner(file, title, subtitle, linkUrl);
        return Result.success(result);
    }
    
    /**
     * 更新Banner
     *
     * @param id Banner ID
     * @param bannerData Banner数据
     * @return 更新结果
     */
    @PutMapping("/banners/{id}")
    public Result<Map<String, Object>> updateBanner(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> bannerData) {
        Map<String, Object> result = forumService.updateBanner(id, bannerData);
        return Result.success(result);
    }
    
    /**
     * 删除Banner
     *
     * @param id Banner ID
     * @return 删除结果
     */
    @DeleteMapping("/banners/{id}")
    public Result<Boolean> deleteBanner(@PathVariable Long id) {
        Boolean result = forumService.deleteBanner(id);
        return Result.success(result);
    }
    
    /**
     * 更新Banner顺序
     *
     * @param orderData Banner顺序数据
     * @return 更新结果
     */
    @PutMapping("/banners/order")
    public Result<List<Map<String, Object>>> updateBannerOrder(@RequestBody Map<String, Object> orderData) {
        List<Map<String, Object>> result = forumService.updateBannerOrder(orderData);
        return Result.success(result);
    }
    
    /**
     * 获取首页完整数据
     *
     * @return 首页数据
     */
    @GetMapping("/home-contents/full")
    public Result<Map<String, Object>> getHomeContentsFull() {
        Map<String, Object> result = forumService.getHomeContentsFull();
        return Result.success(result);
    }
    
    /**
     * 更新首页数据
     *
     * @param homeData 首页数据
     * @return 更新结果
     */
    @PutMapping("/home-contents")
    public Result<Map<String, Object>> updateHomeContents(@RequestBody Map<String, Object> homeData) {
        Map<String, Object> result = forumService.updateHomeContents(homeData);
        return Result.success(result);
    }
    
    /**
     * 点赞帖子
     *
     * @param id 帖子ID
     * @return 点赞结果
     */
    @PostMapping("/posts/{id}/like")
    public Result<Map<String, Object>> likePost(@PathVariable Long id) {
        Map<String, Object> result = forumService.likePost(id);
        return Result.success(result);
    }
    
    /**
     * 取消点赞帖子
     *
     * @param id 帖子ID
     * @return 取消点赞结果
     */
    @DeleteMapping("/posts/{id}/like")
    public Result<Map<String, Object>> unlikePost(@PathVariable Long id) {
        Map<String, Object> result = forumService.unlikePost(id);
        return Result.success(result);
    }
    
    /**
     * 收藏帖子
     *
     * @param id 帖子ID
     * @return 收藏结果
     */
    @PostMapping("/posts/{id}/favorite")
    public Result<Map<String, Object>> favoritePost(@PathVariable Long id) {
        Map<String, Object> result = forumService.favoritePost(id);
        return Result.success(result);
    }
    
    /**
     * 取消收藏帖子
     *
     * @param id 帖子ID
     * @return 取消收藏结果
     */
    @DeleteMapping("/posts/{id}/favorite")
    public Result<Map<String, Object>> unfavoritePost(@PathVariable Long id) {
        Map<String, Object> result = forumService.unfavoritePost(id);
        return Result.success(result);
    }
}
