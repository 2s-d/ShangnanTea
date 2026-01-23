package com.shangnantea.model.vo.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车响应VO
 */
public class CartResponseVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 购物车商品列表
     */
    private List<CartItemVO> items;
    
    /**
     * 总价格
     */
    private BigDecimal totalPrice;
    
    // Getters and Setters
    
    public List<CartItemVO> getItems() {
        return items;
    }
    
    public void setItems(List<CartItemVO> items) {
        this.items = items;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
