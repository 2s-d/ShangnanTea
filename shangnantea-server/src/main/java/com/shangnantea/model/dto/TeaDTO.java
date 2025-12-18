package com.shangnantea.model.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * 茶叶数据传输对象，用于接收前端提交的茶叶数据
 */
@Data
public class TeaDTO {

    /**
     * 茶叶ID，创建时可为空
     */
    private String id;
    
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
     * 基础价格
     */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;
    
    /**
     * 茶叶描述
     */
    @Size(max = 500, message = "茶叶描述长度不能超过500个字符")
    private String description;
    
    /**
     * 详细介绍，支持富文本
     */
    private String detail;
    
    /**
     * 产地
     */
    @Size(max = 100, message = "产地长度不能超过100个字符")
    private String origin;
    
    /**
     * 总库存
     */
    @NotNull(message = "库存不能为空")
    @DecimalMin(value = "0", message = "库存不能小于0")
    private Integer stock;
    
    /**
     * 规格列表
     */
    private List<TeaSpecificationDTO> specifications;
    
    /**
     * 图片URL列表
     */
    private List<String> images;
    
    /**
     * 主图URL
     */
    private String mainImage;
    
    /**
     * 状态(0下架,1上架)
     */
    private Integer status;
    
    /**
     * 茶叶规格DTO
     */
    @Data
    public static class TeaSpecificationDTO {
        /**
         * 规格ID，创建时可为空
         */
        private Integer id;
        
        /**
         * 规格名称
         */
        @NotBlank(message = "规格名称不能为空")
        @Size(max = 50, message = "规格名称长度不能超过50个字符")
        private String specName;
        
        /**
         * 规格价格
         */
        @NotNull(message = "规格价格不能为空")
        @DecimalMin(value = "0.01", message = "规格价格必须大于0")
        private BigDecimal price;
        
        /**
         * 规格库存
         */
        @NotNull(message = "规格库存不能为空")
        @DecimalMin(value = "0", message = "规格库存不能小于0")
        private Integer stock;
        
        /**
         * 是否默认规格
         */
        private Integer isDefault;
    }
} 