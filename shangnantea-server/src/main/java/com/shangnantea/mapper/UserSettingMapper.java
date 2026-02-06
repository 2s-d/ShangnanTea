package com.shangnantea.mapper;

import com.shangnantea.model.entity.user.UserSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户设置数据访问层
 */
@Mapper
public interface UserSettingMapper extends BaseMapper<UserSetting, Long> {
    
    /**
     * 通过用户ID和键名获取设置
     *
     * @param userId 用户ID
     * @param settingKey 设置键名
     * @return 用户设置对象
     */
    UserSetting selectByUserIdAndKey(@Param("userId") String userId, @Param("settingKey") String settingKey);
    
    /**
     * 获取用户所有设置
     *
     * @param userId 用户ID
     * @return 用户设置列表
     */
    List<UserSetting> selectByUserId(@Param("userId") String userId);
    
    /**
     * 根据用户ID删除所有设置（用于删除用户时清理数据）
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(@Param("userId") String userId);
} 