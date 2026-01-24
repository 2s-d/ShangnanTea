package com.shangnantea.model.dto.forum;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审核拒绝帖子DTO
 */
@Data
public class RejectPostDTO {
    /**
     * 拒绝原因
     */
    @NotBlank(message = "拒绝原因不能为空")
    private String reason;
}
