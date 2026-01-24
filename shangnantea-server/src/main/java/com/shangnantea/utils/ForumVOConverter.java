package com.shangnantea.utils;

import com.shangnantea.model.entity.forum.ForumPost;
import com.shangnantea.model.entity.forum.ForumReply;
import com.shangnantea.model.entity.forum.ForumTopic;
import com.shangnantea.model.entity.forum.TeaArticle;
import com.shangnantea.model.entity.user.User;
import com.shangnantea.model.vo.forum.ArticleVO;
import com.shangnantea.model.vo.forum.PostVO;
import com.shangnantea.model.vo.forum.TopicVO;

import java.util.Map;

/**
 * 论坛VO转换工具类
 * 统一处理Entity到VO的转换，包括图片URL生成
 */
public class ForumVOConverter {
    
    /**
     * 生成图片访问URL
     * 如果relativePath为空或null，返回null
     * 
     * @param relativePath 相对路径
     * @param baseUrl 基础URL
     * @return 完整访问URL
     */
    public static String generateImageUrl(String relativePath, String baseUrl) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        return FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
    }
    
    /**
     * 转换TeaArticle为ArticleVO
     * 
     * @param article 文章实体
     * @param baseUrl 基础URL
     * @return ArticleVO
     */
    public static ArticleVO convertToArticleVO(TeaArticle article, String baseUrl) {
        if (article == null) {
            return null;
        }
        
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(generateImageUrl(article.getCoverImage(), baseUrl));
        vo.setAuthorName(article.getAuthor());
        vo.setCategory(article.getCategory());
        vo.setViewCount(article.getViewCount() != null ? article.getViewCount() : 0);
        vo.setLikeCount(article.getLikeCount() != null ? article.getLikeCount() : 0);
        vo.setIsTop(article.getIsTop());
        vo.setIsRecommend(article.getIsRecommend());
        vo.setPublishTime(article.getPublishTime());
        vo.setCreateTime(article.getCreateTime());
        
        return vo;
    }
    
    /**
     * 转换ForumTopic为TopicVO
     * 
     * @param topic 版块实体
     * @param baseUrl 基础URL
     * @return TopicVO
     */
    public static TopicVO convertToTopicVO(ForumTopic topic, String baseUrl) {
        if (topic == null) {
            return null;
        }
        
        TopicVO vo = new TopicVO();
        vo.setId(topic.getId());
        vo.setName(topic.getName());
        vo.setDescription(topic.getDescription());
        vo.setIcon(generateImageUrl(topic.getIcon(), baseUrl));
        vo.setCover(generateImageUrl(topic.getCover(), baseUrl));
        vo.setSortOrder(topic.getSortOrder());
        vo.setPostCount(topic.getPostCount() != null ? topic.getPostCount() : 0);
        vo.setCreateTime(topic.getCreateTime());
        
        return vo;
    }
    
    /**
     * 转换ForumPost为PostVO
     * 
     * @param post 帖子实体
     * @param user 用户信息
     * @param topicName 版块名称
     * @param baseUrl 基础URL
     * @return PostVO
     */
    public static PostVO convertToPostVO(ForumPost post, User user, String topicName, String baseUrl) {
        if (post == null) {
            return null;
        }
        
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setUserName(user != null ? user.getUsername() : "未知用户");
        vo.setUserAvatar(user != null ? generateImageUrl(user.getAvatar(), baseUrl) : null);
        vo.setTopicId(post.getTopicId());
        vo.setTopicName(topicName != null ? topicName : "");
        vo.setTitle(post.getTitle());
        vo.setSummary(post.getSummary());
        vo.setCoverImage(generateImageUrl(post.getCoverImage(), baseUrl));
        vo.setViewCount(post.getViewCount());
        vo.setReplyCount(post.getReplyCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setFavoriteCount(post.getFavoriteCount());
        vo.setIsSticky(post.getIsSticky());
        vo.setIsEssence(post.getIsEssence());
        vo.setStatus(post.getStatus());
        vo.setLastReplyTime(post.getLastReplyTime());
        vo.setCreateTime(post.getCreateTime());
        
        return vo;
    }
    
    /**
     * 批量转换ForumPost为PostVO
     * 
     * @param post 帖子实体
     * @param userMap 用户信息Map（userId -> User）
     * @param topicMap 版块信息Map（topicId -> ForumTopic）
     * @param baseUrl 基础URL
     * @return PostVO
     */
    public static PostVO convertToPostVO(ForumPost post, Map<String, User> userMap, 
                                         Map<Integer, ForumTopic> topicMap, String baseUrl) {
        if (post == null) {
            return null;
        }
        
        User user = userMap.get(post.getUserId());
        ForumTopic topic = topicMap.get(post.getTopicId());
        String topicName = topic != null ? topic.getName() : "";
        
        return convertToPostVO(post, user, topicName, baseUrl);
    }
}
