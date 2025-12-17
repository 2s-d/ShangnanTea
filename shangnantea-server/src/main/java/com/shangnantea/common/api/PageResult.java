package com.shangnantea.common.api;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页查询结果
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {

    /**
     * 当前页码
     */
    private Integer pageNum;
    
    /**
     * 每页记录数
     */
    private Integer pageSize;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Integer pages;
    
    /**
     * 当前页数据列表
     */
    private List<T> list;
    
    /**
     * 创建分页结果
     *
     * @param <T>      数据类型
     * @param list     数据列表
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页记录数
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> list, long total, int pageNum, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        
        // 计算总页数
        if (pageSize > 0) {
            long pages = (total + pageSize - 1) / pageSize;
            result.setPages((int) pages);
        } else {
            result.setPages(0);
        }
        
        return result;
    }
    
    /**
     * 创建空的分页结果
     *
     * @param <T> 数据类型
     * @return 空的分页结果
     */
    public static <T> PageResult<T> empty() {
        return of(new ArrayList<>(), 0, 1, 10);
    }
    
    /**
     * 是否有下一页
     *
     * @return 是否有下一页
     */
    public boolean hasNext() {
        return pageNum < pages;
    }
    
    /**
     * 是否有上一页
     *
     * @return 是否有上一页
     */
    public boolean hasPrev() {
        return pageNum > 1;
    }
} 