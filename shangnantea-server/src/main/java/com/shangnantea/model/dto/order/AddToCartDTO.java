package com.shangnantea.model.dto.order;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 添加购物车DTO
 */
@Data
public class AddToCartDTO {
    
    /**
     * 茶叶ID
     */
    @NotBlank(message = "茶叶ID不能为空")
    private String teaId;
    
    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
    
    /**
     * 规格ID（可选）
     */
    private String specificationId;
}
