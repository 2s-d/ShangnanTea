package com.shangnantea.model.dto.message;

import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 标记已读DTO
 */
public class MarkAsReadDTO implements Serializable {
    
    /**
     * 消息ID列表
     */
    @NotEmpty(message = "消息ID列表不能为空")
    private List<Long> messageIds;
    
    public MarkAsReadDTO() {
    }
    
    public List<Long> getMessageIds() {
        return messageIds;
    }
    
    public void setMessageIds(List<Long> messageIds) {
        this.messageIds = messageIds;
    }
}
