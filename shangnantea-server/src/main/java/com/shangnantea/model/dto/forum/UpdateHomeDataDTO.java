package com.shangnantea.model.dto.forum;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新首页数据DTO
 */
@Data
public class UpdateHomeDataDTO {
    
    /**
     * Banner配置列表
     */
    private List<BannerConfig> banners;
    
    /**
     * 推荐帖子ID列表
     */
    private List<Long> hotPostIds;
    
    /**
     * 推荐文章ID列表
     */
    private List<Long> latestArticleIds;
    
    /**
     * Banner配置
     */
    @Data
    public static class BannerConfig {
        /**
         * Banner ID
         */
        private Integer id;
        
        /**
         * 图片URL
         */
        @NotNull(message = "图片URL不能为空")
        private String imageUrl;
        
        /**
         * 标题
         */
        private String title;
        
        /**
         * 链接地址
         */
        private String linkUrl;
        
        /**
         * 排序值
         */
        private Integer sortOrder;
    }
}
