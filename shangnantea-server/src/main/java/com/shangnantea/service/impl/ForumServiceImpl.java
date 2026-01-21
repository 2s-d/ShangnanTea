package com.shangnantea.service.impl;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.ForumPostMapper;
import com.shangnantea.mapper.ForumReplyMapper;
import com.shangnantea.mapper.ForumTopicMapper;
import com.shangnantea.mapper.HomeContentMapper;
import com.shangnantea.mapper.TeaArticleMapper;
import com.shangnantea.model.entity.forum.ForumPost;
import com.shangnantea.model.entity.forum.ForumReply;
import com.shangnantea.model.entity.forum.ForumTopic;
import com.shangnantea.model.entity.forum.HomeContent;
import com.shangnantea.model.entity.forum.TeaArticle;
import com.shangnantea.service.ForumService;
import com.shangnantea.utils.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 论坛服务实现类
 */
@Service
public class ForumServiceImpl implements ForumService {

    private static final Logger logger = LoggerFactory.getLogger(ForumServiceImpl.class);

    @Autowired
    private ForumTopicMapper topicMapper;
    
    @Autowired
    private ForumPostMapper postMapper;
    
    @Autowired
    private ForumReplyMapper replyMapper;
    
    @Autowired
    private TeaArticleMapper articleMapper;
    
    @Autowired
    private HomeContentMapper homeContentMapper;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Override
    public List<ForumTopic> listTopics() {
        // TODO: 实现获取论坛主题列表的逻辑
        return topicMapper.selectAll();
    }
    
    @Override
    public ForumTopic getTopicById(Integer id) {
        // TODO: 实现获取论坛主题详情的逻辑
        return topicMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public ForumTopic createTopic(ForumTopic topic) {
        // TODO: 实现创建论坛主题的逻辑
        Date now = new Date();
        topic.setCreateTime(now);
        topic.setUpdateTime(now);
        topicMapper.insert(topic);
        return topic;
    }
    
    @Override
    @Transactional
    public boolean updateTopic(ForumTopic topic) {
        // TODO: 实现更新论坛主题的逻辑
        topic.setUpdateTime(new Date());
        return topicMapper.updateById(topic) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteTopic(Integer id) {
        // TODO: 实现删除论坛主题的逻辑
        return topicMapper.deleteById(id) > 0;
    }
    
    @Override
    public ForumPost getPostById(Long id) {
        // TODO: 实现获取帖子详情的逻辑
        return postMapper.selectById(id);
    }
    
    @Override
    public PageResult<ForumPost> listPostsByTopic(Integer topicId, PageParam pageParam) {
        // TODO: 实现分页查询主题下的帖子的逻辑
        return new PageResult<>();
    }
    
    @Override
    @Transactional
    public ForumPost createPost(ForumPost post) {
        // TODO: 实现创建帖子的逻辑
        Date now = new Date();
        post.setCreateTime(now);
        post.setUpdateTime(now);
        post.setViewCount(0);
        post.setReplyCount(0);
        post.setLikeCount(0);
        post.setFavoriteCount(0);
        post.setLastReplyTime(now);
        postMapper.insert(post);
        
        // 更新主题的帖子数
        ForumTopic topic = topicMapper.selectById(post.getTopicId());
        if (topic != null) {
            topic.setPostCount(topic.getPostCount() + 1);
            topic.setUpdateTime(now);
            topicMapper.updateById(topic);
        }
        
        return post;
    }
    
    @Override
    @Transactional
    public boolean updatePost(ForumPost post) {
        // TODO: 实现更新帖子的逻辑
        post.setUpdateTime(new Date());
        return postMapper.updateById(post) > 0;
    }
    
    @Override
    @Transactional
    public boolean deletePost(Long id) {
        // TODO: 实现删除帖子的逻辑
        ForumPost post = postMapper.selectById(id);
        if (post == null) {
            return false;
        }
        
        // 更新主题的帖子数
        ForumTopic topic = topicMapper.selectById(post.getTopicId());
        if (topic != null) {
            topic.setPostCount(topic.getPostCount() - 1);
            topic.setUpdateTime(new Date());
            topicMapper.updateById(topic);
        }
        
        return postMapper.deleteById(id) > 0;
    }
    
    @Override
    @Transactional
    public boolean setPostSticky(Long id, Boolean isSticky) {
        ForumPost post = postMapper.selectById(id);
        if (post == null) {
            return false;
        }
        
        post.setIsSticky(isSticky ? 1 : 0);
        post.setUpdateTime(new Date());
        
        return postMapper.updateById(post) > 0;
    }
    
    @Override
    @Transactional
    public boolean setPostEssence(Long id, Boolean isEssence) {
        ForumPost post = postMapper.selectById(id);
        if (post == null) {
            return false;
        }
        
        post.setIsEssence(isEssence ? 1 : 0);
        post.setUpdateTime(new Date());
        
        return postMapper.updateById(post) > 0;
    }
    
    @Override
    public PageResult<ForumReply> listRepliesByPost(Long postId, PageParam pageParam) {
        // TODO: 实现获取回复列表的逻辑
        return new PageResult<>();
    }
    
    @Override
    @Transactional
    public ForumReply createReply(ForumReply reply) {
        // TODO: 实现创建回复的逻辑
        Date now = new Date();
        reply.setCreateTime(now);
        reply.setUpdateTime(now);
        reply.setLikeCount(0);
        replyMapper.insert(reply);
        
        // 更新帖子的回复数和最后回复时间
        ForumPost post = postMapper.selectById(reply.getPostId());
        if (post != null) {
            post.setReplyCount(post.getReplyCount() + 1);
            post.setLastReplyTime(now);
            post.setUpdateTime(now);
            postMapper.updateById(post);
        }
        
        return reply;
    }
    
    @Override
    @Transactional
    public boolean deleteReply(Long id) {
        // TODO: 实现删除回复的逻辑
        ForumReply reply = replyMapper.selectById(id);
        if (reply == null) {
            return false;
        }
        
        // 更新帖子的回复数
        ForumPost post = postMapper.selectById(reply.getPostId());
        if (post != null) {
            post.setReplyCount(post.getReplyCount() - 1);
            post.setUpdateTime(new Date());
            postMapper.updateById(post);
        }
        
        return replyMapper.deleteById(id) > 0;
    }
    
    @Override
    public TeaArticle getArticleById(Long id) {
        // TODO: 实现获取茶文化文章详情的逻辑
        return articleMapper.selectById(id);
    }
    
    @Override
    public PageResult<TeaArticle> listArticles(PageParam pageParam) {
        // TODO: 实现分页查询茶文化文章的逻辑
        return new PageResult<>();
    }
    
    @Override
    @Transactional
    public TeaArticle createArticle(TeaArticle article) {
        // TODO: 实现创建茶文化文章的逻辑
        Date now = new Date();
        article.setCreateTime(now);
        article.setUpdateTime(now);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setFavoriteCount(0);
        if (article.getPublishTime() == null) {
            article.setPublishTime(now);
        }
        articleMapper.insert(article);
        return article;
    }
    
    @Override
    @Transactional
    public boolean updateArticle(TeaArticle article) {
        // TODO: 实现更新茶文化文章的逻辑
        article.setUpdateTime(new Date());
        return articleMapper.updateById(article) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteArticle(Long id) {
        // TODO: 实现删除茶文化文章的逻辑
        return articleMapper.deleteById(id) > 0;
    }
    
    @Override
    public List<HomeContent> listHomeContents(String section) {
        // TODO: 实现获取首页内容列表的逻辑
        return null; // 待实现
    }
    
    @Override
    @Transactional
    public HomeContent createHomeContent(HomeContent content) {
        // TODO: 实现创建首页内容的逻辑
        Date now = new Date();
        content.setCreateTime(now);
        content.setUpdateTime(now);
        homeContentMapper.insert(content);
        return content;
    }
    
    @Override
    @Transactional
    public boolean updateHomeContent(HomeContent content) {
        // TODO: 实现更新首页内容的逻辑
        content.setUpdateTime(new Date());
        return homeContentMapper.updateById(content) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteHomeContent(Integer id) {
        // TODO: 实现删除首页内容的逻辑
        return homeContentMapper.deleteById(id) > 0;
    }
    
    @Override
    public Result<Map<String, Object>> uploadPostImage(MultipartFile image) {
        try {
            logger.info("上传帖子图片请求, 文件名: {}", image.getOriginalFilename());
            
            // 1. 调用工具类上传（硬编码type为"posts"）
            String relativePath = FileUploadUtils.uploadImage(image, "posts");
            
            // 2. 生成访问URL
            String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
            
            // 3. 直接返回，不存数据库（场景2：先返回URL，稍后存储）
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", accessUrl);
            responseData.put("path", relativePath);
            
            logger.info("帖子图片上传成功: path: {}", relativePath);
            return Result.success(6028, responseData); // 帖子图片上传成功
            
        } catch (Exception e) {
            logger.error("帖子图片上传失败: 系统异常", e);
            return Result.failure(6140); // 帖子图片上传失败
        }
    }
} 