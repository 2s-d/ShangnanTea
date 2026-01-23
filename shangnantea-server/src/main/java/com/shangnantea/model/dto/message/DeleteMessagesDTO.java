package com.shangnantea.model.dto.message;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 删除消息DTO
 */
public class DeleteMessagesDTO implements Serializable {
    
    /**
     * 消息ID列表
     */
    @NotEmpty(message = "消息ID列表不能为空")
    private List<Long> messageIds;
    
    public DeleteMessagesDTO() {
    }
    
    public List<Long> getMessageIds() {
        return messageIds;
    }
    
    public void setMessageIds(List<Long> messageIds) {
        this.messageIds = messageIds;
    }
}
