package com.shangnantea.mapper;

import com.shangnantea.model.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
     * 更新头像
     *
     * @param id     用户ID
     * @param avatar 头像路径
     * @return 影响行数
     */
    int updateAvatar(@Param("id") String id, @Param("avatar") String avatar);
    
    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 影响行数
     */
    int delete(@Param("id") String id);
    
    /**
     * 分页查询用户列表
     *
     * @param keyword 关键词（用户名/昵称/手机号）
     * @param role 角色
     * @param status 状态
     * @param offset 偏移量
     * @param pageSize 每页数量
     * @return 用户列表
     */
    List<User> selectByPage(@Param("keyword") String keyword, 
                            @Param("role") Integer role, 
                            @Param("status") Integer status, 
                            @Param("offset") Integer offset, 
                            @Param("pageSize") Integer pageSize);
    
    /**
     * 统计用户数量
     *
     * @param keyword 关键词（用户名/昵称/手机号）
     * @param role 角色
     * @param status 状态
     * @return 用户数量
     */
    int countByCondition(@Param("keyword") String keyword, 
                        @Param("role") Integer role, 
                        @Param("status") Integer status);
    
    /**
     * 批量查询用户（通过ID列表）
     *
     * @param ids 用户ID列表
     * @return 用户列表
     */
    List<User> selectByIds(@Param("ids") List<String> ids);
} 