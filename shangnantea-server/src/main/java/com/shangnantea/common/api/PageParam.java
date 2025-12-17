package com.shangnantea.common.api;

import com.shangnantea.common.constants.Constants;
import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageParam {

    /**
     * 当前页码，从1开始
     */
    private Integer pageNum = 1;
    
    /**
     * 每页记录数
     */
    private Integer pageSize = Constants.System.DEFAULT_PAGE_SIZE;
    
    /**
     * 排序字段
     */
    private String orderBy;
    
    /**
     * 排序方向，默认为升序
     * 可选值：asc（升序）, desc（降序）
     */
    private String orderDirection = "asc";
    
    /**
     * 获取分页起始位置，用于MySQL limit查询
     *
     * @return 起始位置
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
    
    /**
     * 验证并修正分页参数
     */
    public void checkAndFix() {
        // 页码最小为1
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        
        // 页大小限制
        if (pageSize == null || pageSize < 1) {
            pageSize = Constants.System.DEFAULT_PAGE_SIZE;
        } else if (pageSize > Constants.System.MAX_PAGE_SIZE) {
            pageSize = Constants.System.MAX_PAGE_SIZE;
        }
        
        // 排序方向只能是asc或desc
        if (!"asc".equalsIgnoreCase(orderDirection) && !"desc".equalsIgnoreCase(orderDirection)) {
            orderDirection = "asc";
        }
    }
    
    /**
     * 获取排序SQL片段
     *
     * @return 排序SQL
     */
    public String getOrderByClause() {
        if (orderBy == null || orderBy.trim().isEmpty()) {
            return "";
        }
        
        // 防止SQL注入
        String cleanOrderBy = orderBy.replaceAll("[^a-zA-Z0-9_,]", "");
        String cleanDirection = "asc".equalsIgnoreCase(orderDirection) ? "ASC" : "DESC";
        
        return cleanOrderBy + " " + cleanDirection;
    }
} 