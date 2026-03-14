package com.shangnantea.mapper;

import com.shangnantea.model.entity.tea.TeaSpecification;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 茶叶规格Mapper接口
 */
public interface TeaSpecificationMapper extends BaseMapper<TeaSpecification, Long> {
    
    /**
     * 根据茶叶ID查询规格列表
     *
     * @param teaId 茶叶ID
     * @return 规格列表
     */
    List<TeaSpecification> selectByTeaId(String teaId);
    
    /**
     * 插入规格
     *
     * @param specification 规格对象
     * @return 影响行数
     */
    int insert(TeaSpecification specification);
    
    /**
     * 更新规格
     *
     * @param specification 规格对象
     * @return 影响行数
     */
    int update(TeaSpecification specification);
    
    /**
     * 删除规格
     *
     * @param id 规格ID
     * @return 影响行数
     */
    int deleteById(Integer id);
    
    /**
     * 根据ID查询规格
     *
     * @param id 规格ID
     * @return 规格对象
     */
    TeaSpecification selectById(Integer id);
    
    /**
     * 取消茶叶的所有默认规格
     *
     * @param teaId 茶叶ID
     * @return 影响行数
     */
    int clearDefaultByTeaId(String teaId);
    
    /**
     * 设置默认规格
     *
     * @param id 规格ID
     * @return 影响行数
     */
    int setDefault(Integer id);
    
    /**
     * 更新规格库存（扣减库存）
     *
     * @param id 规格ID
     * @param quantity 数量
     * @return 影响行数（0表示库存不足或规格不存在）
     */
    int updateStock(@Param("id") Integer id, @Param("quantity") Integer quantity);
    
    /**
     * 恢复规格库存（增加库存）
     * 用于取消订单或退款
     *
     * @param id 规格ID
     * @param quantity 数量
     * @return 影响行数
     */
    int restoreStock(@Param("id") Integer id, @Param("quantity") Integer quantity);
} 