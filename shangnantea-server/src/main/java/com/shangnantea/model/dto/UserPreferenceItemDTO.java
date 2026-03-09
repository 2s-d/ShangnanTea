package com.shangnantea.model.dto;

import lombok.Data;

/**
 * 单个个人设置项更新请求。
 */
@Data
public class UserPreferenceItemDTO {

    /**
     * 设置编号。
     */
    private Integer code;

    /**
     * 设置值。
     */
    private Object value;
}
