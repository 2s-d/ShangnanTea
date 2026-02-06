package com.shangnantea.controller;

import com.shangnantea.common.api.Result;
import com.shangnantea.model.dto.forum.CreatePostDTO;
import com.shangnantea.model.dto.forum.CreateReplyDTO;
import com.shangnantea.model.dto.forum.CreateTopicDTO;
import com.shangnantea.model.dto.forum.UpdatePostDTO;
import com.shangnantea.model.dto.forum.UpdateTopicDTO;
import com.shangnantea.security.annotation.RequiresLogin;
import com.shangnantea.security.annotation.RequiresRoles;
import com.shangnantea.service.ForumService;
import jakarta.validation.Valid;
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
    public Result<com.shangnantea.model.vo.forum.ForumHomeVO> getHomeData() {
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
    public Result<java.util.List<com.shangnantea.model.vo.forum.ForumHomeVO.BannerVO>> getBanners() {
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
     * @param file 新的Banner图片文件（可选）
     * @param title Banner标题（可选）
     * @param linkUrl 链接地址（可选）
     * @param sortOrder 排序顺序（可选）
     * @return 更新结果
     */
    @PutMapping("/banners/{id}")
    @RequiresRoles({1})
    public Result<Object> updateBanner(@PathVariable String id,
                                       @RequestParam(value = "file", required = false) MultipartFile file,
                                       @RequestParam(value = "title", required = false) String title,
                                       @RequestParam(value = "linkUrl", required = false) String linkUrl,
                                       @RequestParam(value = "sortOrder", required = false) Integer sortOrder) {
        logger.info("更新Banner请求: id={}, hasFile={}", id, file != null && !file.isEmpty());
        return forumService.updateBanner(id, file, title, linkUrl, sortOrder);
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
     * 改造说明：返回的文章详情中包含 isLiked 和 isFavorited 字段（当前用户是否已点赞/收藏该文章）
     *
     * @param id 文章ID
     * @return 文章详情（包含 isLiked 和 isFavorited 字段）
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
     * @param dto 版块数据
     * @return 创建结果
     */
    @PostMapping("/topics")
    @RequiresRoles({1})
    public Result<Object> createTopic(@Valid @RequestBody CreateTopicDTO dto) {
        logger.info("创建版块请求");
        return forumService.createTopic(dto);
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
     * 更新版块（管理员或版主）
     * 路径: PUT /forum/topics/{id}
     * 成功码: 6009, 失败码: 6117
     *
     * @param id 版块ID
     * @param dto 版块数据
     * @return 更新结果
     */
    @PutMapping("/topics/{id}")
    @RequiresLogin
    public Result<Object> updateTopic(@PathVariable String id, @Valid @RequestBody UpdateTopicDTO dto) {
        logger.info("更新版块请求: {}", id);
        return forumService.updateTopic(id, dto);
    }

    /**
     * 删除版块（管理员或版主）
     * 路径: DELETE /forum/topics/{id}
     * 成功码: 6010, 失败码: 6118
     *
     * @param id 版块ID
     * @return 删除结果
     */
    @DeleteMapping("/topics/{id}")
    @RequiresLogin
    public Result<Boolean> deleteTopic(@PathVariable String id) {
        logger.info("删除版块请求: {}", id);
        return forumService.deleteTopic(id);
    }

    // ==================== 帖子操作 ====================

    /**
     * 获取帖子列表
     * 路径: GET /forum/posts
     * 成功码: 200, 失败码: 6119
     * 改造说明：返回的帖子列表中，每个帖子对象包含 isLiked 和 isFavorited 字段（当前用户是否已点赞/收藏该帖子）
     *
     * @param params 查询参数 {topicId, keyword, sortBy, page, size}
     * @return 帖子列表（每个帖子对象包含 isLiked 和 isFavorited 字段）
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
     * @param dto 帖子数据
     * @return 创建结果
     */
    @PostMapping("/posts")
    @RequiresLogin
    public Result<Object> createPost(@Valid @RequestBody CreatePostDTO dto) {
        logger.info("创建帖子请求");
        return forumService.createPost(dto);
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
     * 成功码: 200, 失败码: 6129
     * 改造说明：返回的回复列表中，每个回复对象包含 isLiked 字段（当前用户是否已点赞该回复）
     *
     * @param id 帖子ID
     * @param params 查询参数 {page, pageSize}
     * @return 回复列表（每个回复对象包含 isLiked 字段）
     */
    @GetMapping("/posts/{id}/replies")
    public Result<Object> getPostReplies(@PathVariable String id, @RequestParam Map<String, Object> params) {
        logger.info("获取帖子回复列表请求: {}, params: {}", id, params);
        return forumService.getPostReplies(id, params);
    }

    /**
     * 创建回复
     * 路径: POST /forum/posts/{id}/replies
     * 成功码: 6018, 失败码: 6130
     *
     * @param id 帖子ID
     * @param dto 回复数据
     * @return 创建结果
     */
    @PostMapping("/posts/{id}/replies")
    @RequiresLogin
    public Result<Object> createReply(@PathVariable String id, @Valid @RequestBody CreateReplyDTO dto) {
        logger.info("创建回复请求, postId: {}", id);
        return forumService.createReply(id, dto);
    }

    // ⚠️ 已删除：帖子点赞/收藏相关接口（likePost, unlikePost, favoritePost, unfavoritePost）
    // 说明：帖子点赞和收藏功能已统一使用用户模块的通用接口（UserController 中的 addLike/removeLike, addFavorite/removeFavorite）
    // 帖子详情接口（getPostDetail）已包含 isLiked 和 isFavorited 字段，无需单独调用点赞/收藏接口

    /**
     * 审核通过帖子（管理员）
     * 路径: POST /forum/posts/{id}/approve
     * 成功码: 6022, 失败码: 6134
     *
     * @param id 帖子ID
     * @return 审核结果
     */
    @PostMapping("/posts/{id}/approve")
    @RequiresRoles({1})
    public Result<Object> approvePost(@PathVariable String id) {
        logger.info("审核通过帖子请求: {}", id);
        return forumService.approvePost(id);
    }

    /**
     * 审核拒绝帖子（管理员）
     * 路径: POST /forum/posts/{id}/reject
     * 成功码: 6023, 失败码: 6135
     *
     * @param id 帖子ID
     * @param dto 审核数据（包含拒绝原因）
     * @return 审核结果
     */
    @PostMapping("/posts/{id}/reject")
    @RequiresRoles({1})
    public Result<Object> rejectPost(@PathVariable String id, @Valid @RequestBody com.shangnantea.model.dto.forum.RejectPostDTO dto) {
        logger.info("审核拒绝帖子请求: {}", id);
        return forumService.rejectPost(id, dto);
    }

    /**
     * 设置帖子置顶/取消置顶（管理员）
     * 路径: PUT /forum/posts/{id}/sticky
     * 成功码: 6024, 6025, 失败码: 6136, 6137
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
     * 成功码: 6026, 6027, 失败码: 6138, 6139
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
     * 改造说明：返回的帖子详情中包含 isLiked 和 isFavorited 字段（当前用户是否已点赞/收藏该帖子）
     *
     * @param id 帖子ID
     * @return 帖子详情（包含 isLiked 和 isFavorited 字段）
     */
    @GetMapping("/posts/{id}")
    public Result<Object> getPostDetail(@PathVariable String id) {
        logger.info("获取帖子详情请求: {}", id);
        return forumService.getPostDetail(id);
    }

    /**
     * 更新帖子
     * 路径: PUT /forum/posts/{id}
     * 成功码: 6012, 失败码: 6123
     *
     * @param id 帖子ID
     * @param dto 帖子数据
     * @return 更新结果
     */
    @PutMapping("/posts/{id}")
    @RequiresLogin
    public Result<Object> updatePost(@PathVariable String id, @Valid @RequestBody UpdatePostDTO dto) {
        logger.info("更新帖子请求: {}", id);
        return forumService.updatePost(id, dto);
    }

    /**
     * 删除帖子
     * 路径: DELETE /forum/posts/{id}
     * 成功码: 6013, 失败码: 6124
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
    // ⚠️ 已删除：回复点赞相关接口（likeReply, unlikeReply）
    // 说明：回复点赞功能已统一使用用户模块的通用接口（UserController 中的 addLike/removeLike）
    // 回复列表接口（getPostReplies）已包含每个回复的 isLiked 字段，无需单独调用点赞接口

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
