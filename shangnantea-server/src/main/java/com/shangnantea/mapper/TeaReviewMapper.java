package com.shangnantea.mapper;

import com.shangnantea.model.entity.tea.TeaReview;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 茶叶评价Mapper接口
 */
public interface TeaReviewMapper extends BaseMapper<TeaReview, Long> {
    /**
     * 根据用户ID查询评价列表（分页）
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 评价列表
     */
    List<TeaReview> selectByUserId(@Param("userId") String userId, 
                                    @Param("offset") Integer offset, 
                                    @Param("limit") Integer limit);
    
    /**
     * 统计用户发布的评价数量
     *
     * @param userId 用户ID
     * @return 评价数量
     */
    long countByUserId(@Param("userId") String userId);
} 