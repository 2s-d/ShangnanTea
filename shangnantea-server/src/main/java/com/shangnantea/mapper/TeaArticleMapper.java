package com.shangnantea.mapper;

import com.shangnantea.model.entity.forum.TeaArticle;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 茶叶文章Mapper接口
 */
public interface TeaArticleMapper extends BaseMapper<TeaArticle, Long> {
    
    /**
     * 查询已发布的文章列表（支持分类和关键词过滤）
     * 
     * @param category 分类（可选）
     * @param keyword 关键词（可选）
     * @param status 状态（1=已发布）
     * @return 文章列表
     */
    List<TeaArticle> selectPublishedArticles(@Param("category") String category, 
                                              @Param("keyword") String keyword,
                                              @Param("status") Integer status);
} 