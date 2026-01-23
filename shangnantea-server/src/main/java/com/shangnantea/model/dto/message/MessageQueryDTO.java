package com.shangnantea.model.dto.message;

import java.io.Serializable;

/**
 * 消息查询DTO
 */
public class MessageQueryDTO implements Serializable {
    
    /**
     * 页码
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 消息类型（system, chat, notification）
     */
    private String type;
    
    public MessageQueryDTO() {
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}
