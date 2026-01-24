package com.shangnantea.model.vo.forum;

import lombok.Data;

import java.util.Date;

/**
 * 文章详情VO
 */
@Data
public class ArticleDetailVO {
    /**
     * 文章ID
     */
    private Long id;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 副标题
     */
    private String subtitle;
    
    /**
     * 文章内容
     */
    private String content;
    
    /**
     * 文章摘要
     */
    private String summary;
    
    /**
     * 封面图片
     */
    private String coverImage;
    
    /**
     * 作者名称
     */
    private String author;
    
    /**
     * 文章分类
     */
    private String category;
    
    /**
     * 标签
     */
    private String tags;
    
    /**
     * 文章来源
     */
    private String source;
    
    /**
     * 阅读量
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 收藏数
     */
    private Integer favoriteCount;
    
    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;
    
    /**
     * 当前用户是否已收藏
     */
    private Boolean isFavorited;
    
    /**
     * 是否置顶
     */
    private Integer isTop;
    
    /**
     * 是否推荐
     */
    private Integer isRecommend;
    
    /**
     * 发布时间
     */
    private Date publishTime;
    
    /**
     * 创建时间
     */
    private Date createTime;
}
