package com.shangnantea.mapper;

import com.shangnantea.model.entity.tea.Tea;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 茶叶Mapper接口
 */
public interface TeaMapper extends BaseMapper<Tea, Long> {
    
    /**
     * 分页查询店铺茶叶列表
     *
     * @param shopId 店铺ID
     * @param offset 分页偏移量
     * @param pageSize 每页记录数
     * @return 茶叶列表
     */
    List<Tea> selectByShopId(@Param("shopId") String shopId,
                             @Param("offset") Integer offset,
                             @Param("pageSize") Integer pageSize);
    
    /**
     * 统计店铺茶叶总数
     *
     * @param shopId 店铺ID
     * @return 茶叶总数
     */
    Long countByShopId(@Param("shopId") String shopId);
    
    /**
     * 逻辑删除茶叶（设置deleted=1）
     *
     * @param id 茶叶ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
} 