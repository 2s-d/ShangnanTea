package com.shangnantea.model.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单列表VO
 */
@Data
public class OrderVO {
    /**
     * 订单ID
     */
    private String id;
    
    /**
     * 订单状态(0待付款,1待发货,2待收货,3已完成,4已取消)
     */
    private Integer status;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalPrice;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 茶叶ID
     */
    private String teaId;
    
    /**
     * 茶叶名称
     */
    private String teaName;
    
    /**
     * 茶叶图片
     */
    private String teaImage;
    
    /**
     * 规格名称
     */
    private String specName;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 单价
     */
    private BigDecimal price;
}
