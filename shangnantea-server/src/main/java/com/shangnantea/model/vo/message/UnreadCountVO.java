package com.shangnantea.model.vo.message;

import java.io.Serializable;

/**
 * 未读消息数量VO
 */
public class UnreadCountVO implements Serializable {
    
    /**
     * 未读消息总数
     */
    private Integer total;
    
    /**
     * 系统通知未读数
     */
    private Integer system;
    
    /**
     * 聊天消息未读数
     */
    private Integer chat;
    
    public UnreadCountVO() {
    }
    
    public UnreadCountVO(Integer total, Integer system, Integer chat) {
        this.total = total;
        this.system = system;
        this.chat = chat;
    }
    
    public Integer getTotal() {
        return total;
    }
    
    public void setTotal(Integer total) {
        this.total = total;
    }
    
    public Integer getSystem() {
        return system;
    }
    
    public void setSystem(Integer system) {
        this.system = system;
    }
    
    public Integer getChat() {
        return chat;
    }
    
    public void setChat(Integer chat) {
        this.chat = chat;
    }
}
