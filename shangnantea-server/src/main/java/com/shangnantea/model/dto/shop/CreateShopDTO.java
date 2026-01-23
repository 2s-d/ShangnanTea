package com.shangnantea.model.dto.shop;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 创建店铺DTO
 */
@Data
public class CreateShopDTO {
    
    /**
     * 店铺名称
     */
    @NotBlank(message = "店铺名称不能为空")
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
