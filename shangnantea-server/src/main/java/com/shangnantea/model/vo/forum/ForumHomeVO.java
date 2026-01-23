package com.shangnantea.model.vo.forum;

import java.util.List;

/**
 * 论坛首页数据VO
 */
public class ForumHomeVO {
    
    /**
     * Banner列表
     */
    private List<BannerVO> banners;
    
    /**
     * 热门帖子列表
     */
    private List<HotPostVO> hotPosts;
    
    /**
     * 最新文章列表
     */
    private List<LatestArticleVO> latestArticles;
    
    public List<BannerVO> getBanners() {
        return banners;
    }
    
    public void setBanners(List<BannerVO> banners) {
        this.banners = banners;
    }
    
    public List<HotPostVO> getHotPosts() {
        return hotPosts;
    }
    
    public void setHotPosts(List<HotPostVO> hotPosts) {
        this.hotPosts = hotPosts;
    }
    
    public List<LatestArticleVO> getLatestArticles() {
        return latestArticles;
    }
    
    public void setLatestArticles(List<LatestArticleVO> latestArticles) {
        this.latestArticles = latestArticles;
    }
    
    /**
     * Banner VO
     */
    public static class BannerVO {
        private Integer id;
        private String imageUrl;
        private String title;
        private String linkUrl;
        
        public Integer getId() {
            return id;
        }
        
        public void setId(Integer id) {
            this.id = id;
        }
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getLinkUrl() {
            return linkUrl;
        }
        
        public void setLinkUrl(String linkUrl) {
            this.linkUrl = linkUrl;
        }
    }
    
    /**
     * 热门帖子VO
     */
    public static class HotPostVO {
        private Long id;
        private String title;
        private Integer viewCount;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public Integer getViewCount() {
            return viewCount;
        }
        
        public void setViewCount(Integer viewCount) {
            this.viewCount = viewCount;
        }
    }
    
    /**
     * 最新文章VO
     */
    public static class LatestArticleVO {
        private Long id;
        private String title;
        private String summary;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getSummary() {
            return summary;
        }
        
        public void setSummary(String summary) {
            this.summary = summary;
        }
    }
}
