package com.shangnantea.model.vo.order;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车商品项VO
 */
public class CartItemVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 购物车项ID
     */
    private String id;
    
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
     * 规格ID
     */
    private String specId;
    
    /**
     * 规格名称
     */
    private String specName;
    
    /**
     * 单价
     */
    private BigDecimal price;
    
    /**
     * 数量
     */
    private Integer quantity;
    
    /**
     * 是否选中
     */
    private Boolean selected;
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTeaId() {
        return teaId;
    }
    
    public void setTeaId(String teaId) {
        this.teaId = teaId;
    }
    
    public String getTeaName() {
        return teaName;
    }
    
    public void setTeaName(String teaName) {
        this.teaName = teaName;
    }
    
    public String getTeaImage() {
        return teaImage;
    }
    
    public void setTeaImage(String teaImage) {
        this.teaImage = teaImage;
    }
    
    public String getSpecId() {
        return specId;
    }
    
    public void setSpecId(String specId) {
        this.specId = specId;
    }
    
    public String getSpecName() {
        return specName;
    }
    
    public void setSpecName(String specName) {
        this.specName = specName;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Boolean getSelected() {
        return selected;
    }
    
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
