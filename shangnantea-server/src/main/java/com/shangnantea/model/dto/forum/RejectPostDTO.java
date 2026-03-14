package com.shangnantea.model.dto.forum;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审核拒绝帖子DTO
 */
@Data
public class RejectPostDTO {
    /**
     * 选择的拒绝原因（预设选项，必填）
     */
    @NotBlank(message = "选择的拒绝原因不能为空")
    private String selectedReason;
    
    /**
     * 管理员自定义拒绝原因（可选）
     */
    private String customReason;
}
