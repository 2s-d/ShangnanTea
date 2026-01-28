package com.shangnantea.model.dto.tea;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 更新茶叶DTO
 */
@Data
public class UpdateTeaDTO {
    
    /**
     * 茶叶名称
     */
    @NotBlank(message = "茶叶名称不能为空")
    private String name;
    
    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Integer categoryId;
    
    /**
     * 价格
     */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;
    
    /**
     * 库存
     */
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;
    
    /**
     * 茶叶描述
     */
    private String description;
    
    /**
     * 详细介绍
     */
    private String detail;
    
    /**
     * 产地
     */
    private String origin;
    
    /**
     * 主图URL
     */
    private String mainImage;
    
    /**
     * 状态(0下架,1上架)
     */
    private Integer status;
}
