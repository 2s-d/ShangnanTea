package com.shangnantea.service;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.model.dto.forum.CreateTopicDTO;
import com.shangnantea.model.dto.forum.UpdateTopicDTO;
import com.shangnantea.model.entity.forum.ForumPost;
import com.shangnantea.model.entity.forum.ForumReply;
import com.shangnantea.model.entity.forum.ForumTopic;
import com.shangnantea.model.entity.forum.HomeContent;
import com.shangnantea.model.entity.forum.TeaArticle;
import com.shangnantea.model.vo.forum.ForumHomeVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 论坛服务接口
 */
public interface ForumService {
    
    /**
     * 获取论坛首页数据
     * 路径: GET /forum/home
     * 成功码: 200, 失败码: 6100
     *
     * @return 首页数据，包含Banner、热门帖子、最新文章
     */
    Result<ForumHomeVO> getHomeData();
    
    /**
     * 更新论坛首页数据（管理员）
     * 路径: PUT /forum/home
     * 成功码: 6000, 失败码: 6101
     *
     * @param data 首页数据配置
     * @return 更新结果
     */
    Result<Object> updateHomeData(Map<String, Object> data);
    
    /**
     * 获取Banner列表
     * 路径: GET /forum/banners
     * 成功码: 200, 失败码: 6102
     *
     * @return Banner列表
     */
    Result<List<ForumHomeVO.BannerVO>> getBanners();
    
    /**
     * 获取论坛主题列表
     *
     * @return 主题列表
     */
    List<ForumTopic> listTopics();
    
    /**
     * 获取论坛主题详情
     *
     * @param id 主题ID
     * @return 主题信息
     */
    ForumTopic getTopicById(Integer id);
    
    /**
     * 创建论坛主题(管理员)
     *
     * @param topic 主题信息
     * @return 主题信息
     */
    ForumTopic createTopic(ForumTopic topic);
    
    /**
     * 更新论坛主题(管理员)
     *
     * @param topic 主题信息
     * @return 是否成功
     */
    boolean updateTopic(ForumTopic topic);
    
    /**
     * 删除论坛主题(管理员)
     *
     * @param id 主题ID
     * @return 是否成功
     */
    boolean deleteTopic(Integer id);
    
    /**
     * 获取帖子详情
     *
     * @param id 帖子ID
     * @return 帖子信息
     */
    ForumPost getPostById(Long id);
    
    /**
     * 分页查询主题下的帖子
     *
     * @param topicId 主题ID
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<ForumPost> listPostsByTopic(Integer topicId, PageParam pageParam);
    
    /**
     * 创建帖子
     *
     * @param post 帖子信息
     * @return 帖子信息
     */
    ForumPost createPost(ForumPost post);
    
    /**
     * 更新帖子
     *
     * @param post 帖子信息
     * @return 是否成功
     */
    boolean updatePost(ForumPost post);
    
    /**
     * 删除帖子
     *
     * @param id 帖子ID
     * @return 是否成功
     */
    boolean deletePost(Long id);
    
    /**
     * 设置帖子置顶(管理员)
     *
     * @param id 帖子ID
     * @param isSticky 是否置顶
     * @return 是否成功
     */
    boolean setPostSticky(Long id, Boolean isSticky);
    
    /**
     * 设置帖子精华(管理员)
     *
     * @param id 帖子ID
     * @param isEssence 是否精华
     * @return 是否成功
     */
    boolean setPostEssence(Long id, Boolean isEssence);
    
    /**
     * 获取回复列表
     *
     * @param postId 帖子ID
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<ForumReply> listRepliesByPost(Long postId, PageParam pageParam);
    
    /**
     * 创建回复
     *
     * @param reply 回复信息
     * @return 回复信息
     */
    ForumReply createReply(ForumReply reply);
    
    /**
     * 删除回复
     *
     * @param id 回复ID
     * @return 是否成功
     */
    boolean deleteReply(Long id);
    
    /**
     * 获取茶文化文章详情
     *
     * @param id 文章ID
     * @return 文章信息
     */
    TeaArticle getArticleById(Long id);
    
    /**
     * 分页查询茶文化文章
     *
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<TeaArticle> listArticles(PageParam pageParam);
    
    /**
     * 创建茶文化文章(管理员)
     *
     * @param article 文章信息
     * @return 文章信息
     */
    TeaArticle createArticle(TeaArticle article);
    
    /**
     * 更新茶文化文章(管理员)
     *
     * @param article 文章信息
     * @return 是否成功
     */
    boolean updateArticle(TeaArticle article);
    
    /**
     * 删除茶文化文章(管理员)
     *
     * @param id 文章ID
     * @return 是否成功
     */
    boolean deleteArticle(Long id);
    
    /**
     * 获取首页内容列表
     *
     * @param section 板块
     * @return 内容列表
     */
    List<HomeContent> listHomeContents(String section);
    
    /**
     * 创建首页内容(管理员)
     *
     * @param content 内容信息
     * @return 内容信息
     */
    HomeContent createHomeContent(HomeContent content);
    
    /**
     * 更新首页内容(管理员)
     *
     * @param content 内容信息
     * @return 是否成功
     */
    boolean updateHomeContent(HomeContent content);
    
    /**
     * 删除首页内容(管理员)
     *
     * @param id 内容ID
     * @return 是否成功
     */
    boolean deleteHomeContent(Integer id);
    
    /**
     * 上传帖子图片
     *
     * @param image 图片文件
     * @return 上传结果
     */
    Result<Map<String, Object>> uploadPostImage(MultipartFile image);
    
    /**
     * 上传论坛轮播图
     *
     * @param file 图片文件
     * @param title 标题
     * @param subtitle 副标题
     * @param linkUrl 链接地址
     * @return 上传结果
     */
    Result<Object> uploadBanner(MultipartFile file, String title, String subtitle, String linkUrl);
    
    /**
     * 更新Banner信息（管理员）
     * 路径: PUT /forum/banners/{id}
     * 成功码: 6002, 失败码: 6106
     *
     * @param id Banner ID
     * @param data Banner数据（title, linkUrl, sortOrder）
     * @return 更新结果
     */
    Result<Object> updateBanner(String id, Map<String, Object> data);
    
    /**
     * 删除Banner（管理员）
     * 路径: DELETE /forum/banners/{id}
     * 成功码: 6003, 失败码: 6107
     *
     * @param id Banner ID
     * @return 删除结果
     */
    Result<Boolean> deleteBanner(String id);
    
    /**
     * 更新Banner顺序（管理员）
     * 路径: PUT /forum/banners/order
     * 成功码: 6004, 失败码: 6108
     *
     * @param data 包含bannerIds数组的数据
     * @return 更新结果
     */
    Result<Object> updateBannerOrder(Map<String, Object> data);
    
    /**
     * 获取文章列表
     * 路径: GET /forum/articles
     * 成功码: 200, 失败码: 6109
     *
     * @param params 查询参数（categoryId, keyword, page, pageSize）
     * @return 文章列表
     */
    Result<Object> getArticles(Map<String, Object> params);
    
    /**
     * 获取文章详情
     * 路径: GET /forum/articles/{id}
     * 成功码: 200, 失败码: 6110
     *
     * @param id 文章ID
     * @return 文章详情
     */
    Result<Object> getArticleDetail(String id);
    
    /**
     * 创建文章（管理员）
     * 路径: POST /forum/articles
     * 成功码: 6005, 失败码: 6111
     *
     * @param data 文章数据
     * @return 创建结果
     */
    Result<Object> createArticle(Map<String, Object> data);
    
    /**
     * 更新文章（管理员）
     * 路径: PUT /forum/articles/{id}
     * 成功码: 6006, 失败码: 6112
     *
     * @param id 文章ID
     * @param data 文章数据
     * @return 更新结果
     */
    Result<Object> updateArticle(String id, Map<String, Object> data);
    
    /**
     * 删除文章（管理员）
     * 路径: DELETE /forum/articles/{id}
     * 成功码: 6007, 失败码: 6113
     *
     * @param id 文章ID
     * @return 删除结果
     */
    Result<Boolean> deleteArticle(String id);
    
    /**
     * 获取版块列表
     * 路径: GET /forum/topics
     * 成功码: 200, 失败码: 6114
     *
     * @return 版块列表
     */
    Result<Object> getForumTopics();
    
    /**
     * 获取版块详情
     * 路径: GET /forum/topics/{id}
     * 成功码: 200, 失败码: 6115
     *
     * @param id 版块ID
     * @return 版块详情
     */
    Result<Object> getTopicDetail(String id);
    
    /**
     * 创建版块（管理员）
     * 路径: POST /forum/topics
     * 成功码: 6008, 失败码: 6116
     *
     * @param dto 版块数据
     * @return 创建结果
     */
    Result<Object> createTopic(CreateTopicDTO dto);
    
    /**
     * 更新版块（管理员）
     * 路径: PUT /forum/topics/{id}
     * 成功码: 6009, 失败码: 6117
     *
     * @param id 版块ID
     * @param dto 版块数据
     * @return 更新结果
     */
    Result<Object> updateTopic(String id, UpdateTopicDTO dto);
    
    /**
     * 删除版块（管理员）
     * 路径: DELETE /forum/topics/{id}
     * 成功码: 6010, 失败码: 6118
     *
     * @param id 版块ID
     * @return 删除结果
     */
    Result<Boolean> deleteTopic(String id);
} 