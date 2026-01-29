package com.shangnantea.model.vo.forum;

import lombok.Data;

import java.util.Date;

/**
 * 文章列表VO
 */
@Data
public class ArticleVO {
    /**
     * 文章ID
     */
    private Long id;
    
    /**
     * 文章标题
     */
    private String title;
    
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
    private String authorName;
    
    /**
     * 文章分类
     */
    private String category;
    
    /**
     * 阅读量
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
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
