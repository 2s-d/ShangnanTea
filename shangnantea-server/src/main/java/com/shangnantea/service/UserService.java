package com.shangnantea.service;

import com.shangnantea.common.api.Result;
import com.shangnantea.model.dto.LoginDTO;
import com.shangnantea.model.entity.user.User;
import com.shangnantea.model.vo.user.TokenVO;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果，包含token和用户信息
     */
    Result<TokenVO> login(LoginDTO loginDTO);
    
    /**
     * 根据用户ID获取用户
     *
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回null
     */
    User getUserById(String id);
    
    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户对象，如果不存在则返回null
     */
    User getUserByUsername(String username);
    
    /**
     * 验证用户名和密码
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户对象，如果验证失败则返回null
     */
    User checkUserAndPassword(String username, String password);
    
    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return 是否已存在
     */
    boolean isUserExist(String username);
    
    /**
     * 注册用户
     *
     * @param user 用户对象
     * @return 注册成功的用户对象，失败则返回null
     */
    User register(User user);
    
    /**
     * 更新用户信息
     *
     * @param user 用户对象
     * @return 是否更新成功
     */
    boolean updateUser(User user);
    
    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(String userId, String oldPassword, String newPassword);
    
    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(String id);
} 