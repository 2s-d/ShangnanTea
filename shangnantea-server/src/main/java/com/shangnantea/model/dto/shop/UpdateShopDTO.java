package com.shangnantea.model.dto.shop;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 更新店铺DTO
 */
@Data
public class UpdateShopDTO {
    
    /**
     * 店铺名称
     */
    @Size(max = 50, message = "店铺名称长度不能超过50个字符")
    private String name;
    
    /**
     * 店铺Logo URL
     */
    private String logo;
    
    /**
     * 店铺描述
     */
    @Size(max = 500, message = "店铺描述长度不能超过500个字符")
    private String description;
}
