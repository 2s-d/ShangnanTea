package com.shangnantea.model.entity.forum;

import lombok.Data;

import java.util.Date;

/**
 * 茶文化文章实体类
 */
@Data
public class TeaArticle {
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
     * 作者名称
     */
    private String author;
    
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
     * 图片JSON数组
     */
    private String images;
    
    /**
     * 视频链接
     */
    private String videoUrl;
    
    /**
     * 文章分类
     */
    private String category;
    
    /**
     * 标签，英文逗号分隔
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
     * 是否置顶
     */
    private Integer isTop;
    
    /**
     * 是否推荐
     */
    private Integer isRecommend;
    
    /**
     * 状态(1已发布,0草稿,2已删除)
     */
    private Integer status;
    
    /**
     * 发布时间
     */
    private Date publishTime;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 