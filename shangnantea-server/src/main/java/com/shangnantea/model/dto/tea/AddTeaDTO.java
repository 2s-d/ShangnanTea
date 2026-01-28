package com.shangnantea.model.dto.tea;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 添加茶叶DTO
 */
@Data
public class AddTeaDTO {
    
    /**
     * 茶叶名称
     */
    @NotBlank(message = "茶叶名称不能为空")
    @Size(max = 100, message = "茶叶名称长度不能超过100个字符")
    private String name;
    
    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Integer categoryId;
    
    /**
     * 商家ID
     */
    @NotBlank(message = "商家ID不能为空")
    private String shopId;
    
    /**
     * 价格
     */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;
    
    /**
     * 原价（可选）
     */
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;
    
    /**
     * 库存
     */
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;
    
    /**
     * 茶叶描述
     */
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
    
    /**
     * 详细介绍
     */
    private String detail;
    
    /**
     * 产地
     */
    @Size(max = 100, message = "产地长度不能超过100个字符")
    private String origin;
    
    /**
     * 图片列表
     */
    private List<String> images;
    
    /**
     * 状态(0下架,1上架)
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值必须为0或1")
    @Max(value = 1, message = "状态值必须为0或1")
    private Integer status;
}
