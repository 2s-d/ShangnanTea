package com.shangnantea.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 更新用户偏好设置数据传输对象。
 */
@Data
public class UpdateUserPreferencesDTO {

    /**
     * 设置项列表（编号驱动）。
     */
    private List<UserPreferenceItemDTO> items;
}
