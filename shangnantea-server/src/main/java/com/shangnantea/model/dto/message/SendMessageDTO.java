package com.shangnantea.model.dto.message;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 发送消息DTO
 */
public class SendMessageDTO implements Serializable {
    
    /**
     * 接收者ID
     */
    @NotBlank(message = "接收者ID不能为空")
    private String receiverId;
    
    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容不能超过1000个字符")
    private String content;
    
    /**
     * 消息类型（text文本, image图片）
     */
    private String type;
    
    public SendMessageDTO() {
    }
    
    public String getReceiverId() {
        return receiverId;
    }
    
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}
