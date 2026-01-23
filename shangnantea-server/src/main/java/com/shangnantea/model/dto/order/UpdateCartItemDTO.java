package com.shangnantea.model.dto.order;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 更新购物车DTO
 */
@Data
public class UpdateCartItemDTO {
    
    /**
     * 购物车项ID
     */
    @NotNull(message = "购物车项ID不能为空")
    private Integer id;
    
    /**
     * 数量（可选，更新数量时提供）
     */
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
    
    /**
     * 规格ID（可选，更新规格时提供）
     */
    private String specificationId;
}
