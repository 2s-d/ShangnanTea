package com.shangnantea.mapper;

import java.util.List;

/**
 * 基础Mapper接口，定义通用CRUD方法
 *
 * @param <T> 实体类型
 * @param <K> 主键类型
 */
public interface BaseMapper<T, K> {

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int insert(T entity);
    
    /**
     * 根据主键删除记录
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(K id);
    
    /**
     * 根据主键批量删除记录
     *
     * @param ids 主键列表
     * @return 影响行数
     */
    int deleteByIds(List<K> ids);
    
    /**
     * 根据主键更新记录
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int updateById(T entity);
    
    /**
     * 根据主键查询记录
     *
     * @param id 主键
     * @return 实体对象
     */
    T selectById(K id);
    
    /**
     * 根据主键批量查询记录
     *
     * @param ids 主键列表
     * @return 实体对象列表
     */
    List<T> selectByIds(List<K> ids);
    
    /**
     * 查询所有记录
     *
     * @return 实体对象列表
     */
    List<T> selectAll();
    
    /**
     * 统计记录总数
     *
     * @return 记录总数
     */
    long count();
} 