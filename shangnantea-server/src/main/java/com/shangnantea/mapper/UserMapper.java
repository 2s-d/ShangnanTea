package com.shangnantea.mapper;

import com.shangnantea.model.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper extends BaseMapper<User, String> {
    
    /**
     * 通过ID获取用户
     *
     * @param id 用户ID
     * @return 用户对象
     */
    User selectById(@Param("id") String id);
    
    /**
     * 通过用户名获取用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User selectByUsername(@Param("username") String username);
    
    /**
     * 插入用户
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int insert(User user);
    
    /**
     * 更新用户
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int update(User user);
    
    /**
     * 更新密码
     *
     * @param id       用户ID
     * @param password 密码
     * @return 影响行数
     */
    int updatePassword(@Param("id") String id, @Param("password") String password);
    
    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 影响行数
     */
    int delete(@Param("id") String id);
} 