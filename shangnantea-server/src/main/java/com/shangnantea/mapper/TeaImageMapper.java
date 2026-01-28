package com.shangnantea.mapper;

import com.shangnantea.model.entity.tea.TeaImage;

import java.util.List;

/**
 * 茶叶图片Mapper接口
 */
public interface TeaImageMapper extends BaseMapper<TeaImage, Long> {
    
    /**
     * 根据茶叶ID查询图片列表
     *
     * @param teaId 茶叶ID
     * @return 图片列表
     */
    List<TeaImage> selectByTeaId(String teaId);
    
    /**
     * 设置主图
     *
     * @param id 图片ID
     * @return 影响行数
     */
    int setMainImage(Integer id);
    
    /**
     * 更新图片排序
     *
     * @param id 图片ID
     * @param sortOrder 排序值
     * @return 影响行数
     */
    int updateSortOrder(Integer id, Integer sortOrder);
} 