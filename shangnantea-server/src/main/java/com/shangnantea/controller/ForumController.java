package com.shangnantea.controller;

import com.shangnantea.common.api.Result;
import com.shangnantea.security.annotation.RequiresLogin;
import com.shangnantea.security.annotation.RequiresRoles;
import com.shangnantea.service.ForumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 论坛控制器
 * 参考：前端 forum.js 和 code-message-mapping.md
 */
@RestController
@RequestMapping({"/forum", "/api/forum"})
@Validated
public class ForumController {

    private static final Logger logger = LoggerFactory.getLogger(ForumController.class);

    @Autowired
    private ForumService forumService;

    // ==================== 首页与Banner ====================

    /**
     * 获取首页数据
     * 路径: GET /forum/home
     * 成功码: 200, 失败码: 6100
     *
     * @return 首页数据
     */
    @GetMapping("/home")
    public Result<Object> getHomeData() {
        logger.info("获取首页数据请求");
        return forumService.getHomeData();
    }

    /**
     * 更新首页数据（管理员）
     * 路径: PUT /forum/home
     * 成功码: 6000, 失败码: 6101
     *
     * @param data 首页数据
     * @return 更新结果
     */
    @PutMapping("/home")
    @RequiresRoles({1})
    public Result<Object> updateHomeData(@RequestBody Map<String, Object> data) {
        logger.info("更新首页数据请求");
        return forumService.updateHomeData(data);
    }

    /**
     * 获取Banner列表
     * 路径: GET /forum/banners
     * 成功码: 200, 失败码: 6102
     *
     * @return Banner列表
     */
    @GetMapping("/banners")
    public Result<Object> getBanners() {
        logger.info("获取Banner列表请求");
        return forumService.getBanners();
    }

    /**
     * 上传Banner（管理员）
     * 路径: POST /forum/banners
     * 成功码: 6001, 失败码: 6103, 6104, 6105
     *
     * @param file Banner图片文件
     * @param title Banner标题
     * @param subtitle Banner副标题
     * @param linkUrl 链接地址
     * @return 上传结果
     */
    @PostMapping("/banners")
    @RequiresRoles({1})
    public Result<Object> uploadBanner(@RequestParam("file") MultipartFile file,
                                       @RequestParam("title") String title,
                                       @RequestParam(value = "subtitle", required = false) String subtitle,
                                       @RequestParam(value = "linkUrl", required = false) String linkUrl) {
        logger.info("上传Banner请求, title: {}, 文件名: {}", title, file.getOriginalFilename());
        return forumService.uploadBanner(file, title, subtitle, linkUrl);
    }

    /**
     * 更新Banner顺序（管理员）
     * 路径: PUT /forum/banners/order
     * 成功码: 6004, 失败码: 6108
     *
     * @param data Banner顺序数据 {bannerIds}
     * @return 更新结果
     */
    @PutMapping("/banners/order")
    @RequiresRoles({1})
    public Result<Object> updateBannerOrder(@RequestBody Map<String, Object> data) {
        logger.info("更新Banner顺序请求");
        return forumService.updateBannerOrder(data);
    }

    /**
     * 更新Banner（管理员）
     * 路径: PUT /forum/banners/{id}
     * 成功码: 6002, 失败码: 6106
     *
     * @param id Banner ID
     * @param data Banner数据
     * @return 更新结果
     */
    @PutMapping("/banners/{id}")
    @RequiresRoles({1})
    public Result<Object> updateBanner(@PathVariable String id, @RequestBody Map<String, Object> data) {
        logger.info("更新Banner请求: {}", id);
        return forumService.updateBanner(id, data);
    }

    /**
     * 删除Banner（管理员）
     * 路径: DELETE /forum/banners/{id}
     * 成功码: 6003, 失败码: 6107
     *
     * @param id Banner ID
     * @return 删除结果
     */
    @DeleteMapping("/banners/{id}")
    @RequiresRoles({1})
    public Result<Boolean> deleteBanner(@PathVariable String id) {
        logger.info("删除Banner请求: {}", id);
        return forumService.deleteBanner(id);
    }

    // ==================== 文章管理 ====================

    /**
     * 获取文章列表
     * 路径: GET /forum/articles
     * 成功码: 200, 失败码: 6109
     *
     * @param params 查询参数 {page, size, category}
     * @return 文章列表
     */
    @GetMapping("/articles")
    public Result<Object> getArticles(@RequestParam Map<String, Object> params) {
        logger.info("获取文章列表请求, params: {}", params);
        return forumService.getArticles(params);
    }

    /**
     * 创建文章（管理员）
     * 路径: POST /forum/articles
     * 成功码: 6005, 失败码: 6111
     *
     * @param data 文章数据
     * @return 创建结果
     */
    @PostMapping("/articles")
    @RequiresRoles({1})
    public Result<Object> createArticle(@RequestBody Map<String, Object> data) {
        logger.info("创建文章请求");
        return forumService.createArticle(data);
    }

    /**
     * 获取文章详情
     * 路径: GET /forum/articles/{id}
     * 成功码: 200, 失败码: 6110
     *
     * @param id 文章ID
     * @return 文章详情
     */
    @GetMapping("/articles/{id}")
    public Result<Object> getArticleDetail(@PathVariable String id) {
        logger.info("获取文章详情请求: {}", id);
        return forumService.getArticleDetail(id);
    }

    /**
     * 更新文章（管理员）
     * 路径: PUT /forum/articles/{id}
     * 成功码: 6006, 失败码: 6112
     *
     * @param id 文章ID
     * @param data 文章数据
     * @return 更新结果
     */
    @PutMapping("/articles/{id}")
    @RequiresRoles({1})
    public Result<Object> updateArticle(@PathVariable String id, @RequestBody Map<String, Object> data) {
        logger.info("更新文章请求: {}", id);
        return forumService.updateArticle(id, data);
    }

    /**
     * 删除文章（管理员）
     * 路径: DELETE /forum/articles/{id}
     * 成功码: 6007, 失败码: 6113
     *
     * @param id 文章ID
     * @return 删除结果
     */
    @DeleteMapping("/articles/{id}")
    @RequiresRoles({1})
    public Result<Boolean> deleteArticle(@PathVariable String id) {
        logger.info("删除文章请求: {}", id);
        return forumService.deleteArticle(id);
    }

    // ==================== 版块管理 ====================

    /**
     * 获取版块列表
     * 路径: GET /forum/topics
     * 成功码: 200, 失败码: 6114
     *
     * @return 版块列表
     */
    @GetMapping("/topics")
    public Result<Object> getForumTopics() {
        logger.info("获取版块列表请求");
        return forumService.getForumTopics();
    }

    /**
     * 创建版块（管理员）
     * 路径: POST /forum/topics
     * 成功码: 6008, 失败码: 6116
     *
     * @param data 版块数据
     * @return 创建结果
     */
    @PostMapping("/topics")
    @RequiresRoles({1})
    public Result<Object> createTopic(@RequestBody Map<String, Object> data) {
        logger.info("创建版块请求");
        return forumService.createTopic(data);
    }

    /**
     * 获取版块详情
     * 路径: GET /forum/topics/{id}
     * 成功码: 200, 失败码: 6115
     *
     * @param id 版块ID
     * @return 版块详情
     */
    @GetMapping("/topics/{id}")
    public Result<Object> getTopicDetail(@PathVariable String id) {
        logger.info("获取版块详情请求: {}", id);
        return forumService.getTopicDetail(id);
    }

    /**
     * 更新版块（管理员）
     * 路径: PUT /forum/topics/{id}
     * 成功码: 6009, 失败码: 6117
     *
     * @param id 版块ID
     * @param data 版块数据
     * @return 更新结果
     */
    @PutMapping("/topics/{id}")
    @RequiresRoles({1})
    public Result<Object> updateTopic(@PathVariable String id, @RequestBody Map<String, Object> data) {
        logger.info("更新版块请求: {}", id);
        return forumService.updateTopic(id, data);
    }

    /**
     * 删除版块（管理员）
     * 路径: DELETE /forum/topics/{id}
     * 成功码: 6010, 失败码: 6118
     *
     * @param id 版块ID
     * @return 删除结果
     */
    @DeleteMapping("/topics/{id}")
    @RequiresRoles({1})
    public Result<Boolean> deleteTopic(@PathVariable String id) {
        logger.info("删除版块请求: {}", id);
        return forumService.deleteTopic(id);
    }

    // ==================== 帖子操作 ====================

    /**
     * 获取帖子列表
     * 路径: GET /forum/posts
     * 成功码: 200, 失败码: 6119
     *
     * @param params 查询参数 {topicId, keyword, sortBy, page, size}
     * @return 帖子列表
     */
    @GetMapping("/posts")
    public Result<Object> getForumPosts(@RequestParam Map<String, Object> params) {
        logger.info("获取帖子列表请求, params: {}", params);
        return forumService.getForumPosts(params);
    }

    /**
     * 获取待审核帖子列表（管理员）
     * 路径: GET /forum/posts/pending
     * 成功码: 200, 失败码: 6121
     *
     * @param params 查询参数 {page, size}
     * @return 待审核帖子列表
     */
    @GetMapping("/posts/pending")
    @RequiresRoles({1})
    public Result<Object> getPendingPosts(@RequestParam Map<String, Object> params) {
        logger.info("获取待审核帖子列表请求, params: {}", params);
        return forumService.getPendingPosts(params);
    }

    /**
     * 创建帖子
     * 路径: POST /forum/posts
     * 成功码: 6011, 失败码: 6120
     *
     * @param data 帖子数据
     * @return 创建结果
     */
    @PostMapping("/posts")
    @RequiresLogin
    public Result<Object> createPost(@RequestBody Map<String, Object> data) {
        logger.info("创建帖子请求");
        return forumService.createPost(data);
    }

    /**
     * 上传帖子图片
     * 路径: POST /forum/posts/image
     * 成功码: 6028, 失败码: 6140, 6141, 6142
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/posts/image")
    @RequiresLogin
    public Result<Map<String, Object>> uploadPostImage(@RequestParam("file") MultipartFile file) {
        logger.info("上传帖子图片请求, 文件名: {}", file.getOriginalFilename());
        return forumService.uploadPostImage(file);
    }

    /**
     * 获取帖子回复列表
     * 路径: GET /forum/posts/{id}/replies
     * 成功码: 200, 失败码: 6123
     *
     * @param id 帖子ID
     * @param params 查询参数 {page, size, sortBy}
     * @return 回复列表
     */
    @GetMapping("/posts/{id}/replies")
    public Result<Object> getPostReplies(@PathVariable String id, @RequestParam Map<String, Object> params) {
        logger.info("获取帖子回复列表请求: {}, params: {}", id, params);
        return forumService.getPostReplies(id, params);
    }

    /**
     * 创建回复
     * 路径: POST /forum/posts/{id}/replies
     * 成功码: 6022, 失败码: 6122
     *
     * @param id 帖子ID
     * @param data 回复数据
     * @return 创建结果
     */
    @PostMapping("/posts/{id}/replies")
    @RequiresLogin
    public Result<Object> createReply(@PathVariable String id, @RequestBody Map<String, Object> data) {
        logger.info("创建回复请求, postId: {}", id);
        return forumService.createReply(id, data);
    }

    /**
     * 点赞帖子
     * 路径: POST /forum/posts/{id}/like
     * 成功码: 6010, 失败码: 6110
     *
     * @param id 帖子ID
     * @return 点赞结果
     */
    @PostMapping("/posts/{id}/like")
    @RequiresLogin
    public Result<Object> likePost(@PathVariable String id) {
        logger.info("点赞帖子请求: {}", id);
        return forumService.likePost(id);
    }

    /**
     * 取消点赞帖子
     * 路径: DELETE /forum/posts/{id}/like
     * 成功码: 6011, 失败码: 6111
     *
     * @param id 帖子ID
     * @return 取消点赞结果
     */
    @DeleteMapping("/posts/{id}/like")
    @RequiresLogin
    public Result<Object> unlikePost(@PathVariable String id) {
        logger.info("取消点赞帖子请求: {}", id);
        return forumService.unlikePost(id);
    }

    /**
     * 收藏帖子
     * 路径: POST /forum/posts/{id}/favorite
     * 成功码: 6012, 失败码: 6112
     *
     * @param id 帖子ID
     * @return 收藏结果
     */
    @PostMapping("/posts/{id}/favorite")
    @RequiresLogin
    public Result<Object> favoritePost(@PathVariable String id) {
        logger.info("收藏帖子请求: {}", id);
        return forumService.favoritePost(id);
    }

    /**
     * 取消收藏帖子
     * 路径: DELETE /forum/posts/{id}/favorite
     * 成功码: 6013, 失败码: 6113
     *
     * @param id 帖子ID
     * @return 取消收藏结果
     */
    @DeleteMapping("/posts/{id}/favorite")
    @RequiresLogin
    public Result<Object> unfavoritePost(@PathVariable String id) {
        logger.info("取消收藏帖子请求: {}", id);
        return forumService.unfavoritePost(id);
    }

    /**
     * 审核通过帖子（管理员）
     * 路径: POST /forum/posts/{id}/approve
     * 成功码: 6034, 失败码: 6134
     *
     * @param id 帖子ID
     * @param data 审核数据
     * @return 审核结果
     */
    @PostMapping("/posts/{id}/approve")
    @RequiresRoles({1})
    public Result<Object> approvePost(@PathVariable String id, @RequestBody Map<String, Object> data) {
        logger.info("审核通过帖子请求: {}", id);
        return forumService.approvePost(id, data);
    }

    /**
     * 审核拒绝帖子（管理员）
     * 路径: POST /forum/posts/{id}/reject
     * 成功码: 6035, 失败码: 6135
     *
     * @param id 帖子ID
     * @param data 审核数据（包含拒绝原因）
     * @return 审核结果
     */
    @PostMapping("/posts/{id}/reject")
    @RequiresRoles({1})
    public Result<Object> rejectPost(@PathVariable String id, @RequestBody Map<String, Object> data) {
        logger.info("审核拒绝帖子请求: {}", id);
        return forumService.rejectPost(id, data);
    }

    /**
     * 设置帖子置顶/取消置顶（管理员）
     * 路径: PUT /forum/posts/{id}/sticky
     * 成功码: 6030, 6031, 失败码: 6130, 6131
     *
     * @param id 帖子ID
     * @param isSticky 是否置顶
     * @return 操作结果
     */
    @PutMapping("/posts/{id}/sticky")
    @RequiresRoles({1})
    public Result<Object> togglePostSticky(@PathVariable String id, @RequestParam Boolean isSticky) {
        logger.info("设置帖子置顶请求: {}, isSticky: {}", id, isSticky);
        return forumService.togglePostSticky(id, isSticky);
    }

    /**
     * 设置帖子精华/取消精华（管理员）
     * 路径: PUT /forum/posts/{id}/essence
     * 成功码: 6032, 6033, 失败码: 6132, 6133
     *
     * @param id 帖子ID
     * @param isEssence 是否精华
     * @return 操作结果
     */
    @PutMapping("/posts/{id}/essence")
    @RequiresRoles({1})
    public Result<Object> togglePostEssence(@PathVariable String id, @RequestParam Boolean isEssence) {
        logger.info("设置帖子精华请求: {}, isEssence: {}", id, isEssence);
        return forumService.togglePostEssence(id, isEssence);
    }

    /**
     * 获取帖子详情
     * 路径: GET /forum/posts/{id}
     * 成功码: 200, 失败码: 6104
     * 注意：此路径应放在最后，避免与更具体的路径冲突
     *
     * @param id 帖子ID
     * @return 帖子详情
     */
    @GetMapping("/posts/{id}")
    public Result<Object> getPostDetail(@PathVariable String id) {
        logger.info("获取帖子详情请求: {}", id);
        return forumService.getPostDetail(id);
    }

    /**
     * 更新帖子
     * 路径: PUT /forum/posts/{id}
     * 成功码: 6001, 失败码: 6101
     *
     * @param id 帖子ID
     * @param data 帖子数据
     * @return 更新结果
     */
    @PutMapping("/posts/{id}")
    @RequiresLogin
    public Result<Object> updatePost(@PathVariable String id, @RequestBody Map<String, Object> data) {
        logger.info("更新帖子请求: {}", id);
        return forumService.updatePost(id, data);
    }

    /**
     * 删除帖子
     * 路径: DELETE /forum/posts/{id}
     * 成功码: 6002, 失败码: 6102
     *
     * @param id 帖子ID
     * @return 删除结果
     */
    @DeleteMapping("/posts/{id}")
    @RequiresLogin
    public Result<Boolean> deletePost(@PathVariable String id) {
        logger.info("删除帖子请求: {}", id);
        return forumService.deletePost(id);
    }

    // ==================== 回复管理 ====================

    /**
     * 点赞回复
     * 路径: POST /forum/replies/{id}/like
     * 成功码: 6020, 失败码: 6132
     *
     * @param id 回复ID
     * @return 点赞结果
     */
    @PostMapping("/replies/{id}/like")
    @RequiresLogin
    public Result<Object> likeReply(@PathVariable String id) {
        logger.info("点赞回复请求: {}", id);
        return forumService.likeReply(id);
    }

    /**
     * 取消点赞回复
     * 路径: DELETE /forum/replies/{id}/like
     * 成功码: 6021, 失败码: 6133
     *
     * @param id 回复ID
     * @return 取消点赞结果
     */
    @DeleteMapping("/replies/{id}/like")
    @RequiresLogin
    public Result<Object> unlikeReply(@PathVariable String id) {
        logger.info("取消点赞回复请求: {}", id);
        return forumService.unlikeReply(id);
    }

    /**
     * 删除回复
     * 路径: DELETE /forum/replies/{id}
     * 成功码: 6019, 失败码: 6131
     *
     * @param id 回复ID
     * @return 删除结果
     */
    @DeleteMapping("/replies/{id}")
    @RequiresLogin
    public Result<Boolean> deleteReply(@PathVariable String id) {
        logger.info("删除回复请求: {}", id);
        return forumService.deleteReply(id);
    }
}
