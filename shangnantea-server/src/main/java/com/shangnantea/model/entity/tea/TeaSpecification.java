package com.shangnantea.model.entity.tea;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 茶叶规格实体类
 */
@Data
public class TeaSpecification {
    /**
     * 规格ID
     */
    private Integer id;
    
    /**
     * 茶叶ID
     */
    private String teaId;
    
    /**
     * 规格名称
     */
    private String specName;
    
    /**
     * 规格价格
     */
    private BigDecimal price;
    
    /**
     * 规格库存
     */
    private Integer stock;
    
    /**
     * 是否默认规格
     */
    private Integer isDefault;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 