package com.shangnantea.model.dto.forum;

import lombok.Data;

/**
 * 审核通过帖子DTO
 */
@Data
public class ApprovePostDTO {
    /**
     * 审核通过原因（可选）
     */
    private String reason;
}
