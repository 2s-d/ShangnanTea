package com.shangnantea.mapper;

import com.shangnantea.model.entity.tea.TeaSpecification;
import org.apache.ibatis.annotations.Param;

/**
 * 茶叶规格Mapper接口
 */
public interface TeaSpecificationMapper extends BaseMapper<TeaSpecification, Long> {
    
    /**
     * 更新规格库存（扣减库存）
     * @param id 规格ID
     * @param quantity 数量
     * @return 影响行数（0表示库存不足或规格不存在）
     */
    int updateStock(@Param("id") Integer id, @Param("quantity") Integer quantity);
    
    /**
     * 恢复规格库存（增加库存）
     * 用于取消订单或退款
     * @param id 规格ID
     * @param quantity 数量
     * @return 影响行数
     */
    int restoreStock(@Param("id") Integer id, @Param("quantity") Integer quantity);
} 