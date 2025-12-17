package com.shangnantea.service;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;

import java.util.List;

/**
 * 基础Service接口，提供通用的CRUD方法
 *
 * @param <T> 实体类型
 * @param <K> 主键类型
 */
public interface BaseService<T, K> {

    /**
     * 保存实体
     *
     * @param entity 实体对象
     * @return 保存后的实体
     */
    T save(T entity);
    
    /**
     * 根据ID删除实体
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean removeById(K id);
    
    /**
     * 批量删除实体
     *
     * @param ids 主键列表
     * @return 是否成功
     */
    boolean removeByIds(List<K> ids);
    
    /**
     * 更新实体
     *
     * @param entity 实体对象
     * @return 是否成功
     */
    boolean update(T entity);
    
    /**
     * 根据ID查询实体
     *
     * @param id 主键
     * @return 实体对象
     */
    T getById(K id);
    
    /**
     * 批量查询实体
     *
     * @param ids 主键列表
     * @return 实体对象列表
     */
    List<T> listByIds(List<K> ids);
    
    /**
     * 查询所有实体
     *
     * @return 实体对象列表
     */
    List<T> list();
    
    /**
     * 分页查询实体
     *
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<T> page(PageParam pageParam);
} 