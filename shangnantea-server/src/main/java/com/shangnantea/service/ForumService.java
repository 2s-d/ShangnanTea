package com.shangnantea.service;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.model.entity.forum.ForumPost;
import com.shangnantea.model.entity.forum.ForumReply;
import com.shangnantea.model.entity.forum.ForumTopic;
import com.shangnantea.model.entity.forum.HomeContent;
import com.shangnantea.model.entity.forum.TeaArticle;

import java.util.List;

/**
 * 论坛服务接口
 */
public interface ForumService {
    
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
} 