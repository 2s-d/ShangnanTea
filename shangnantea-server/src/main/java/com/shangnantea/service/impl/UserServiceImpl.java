package com.shangnantea.service.impl;

import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.UserMapper;
import com.shangnantea.model.dto.LoginDTO;
import com.shangnantea.model.entity.user.User;
import com.shangnantea.model.vo.user.TokenVO;
import com.shangnantea.model.vo.user.UserVO;
import com.shangnantea.security.util.JwtUtil;
import com.shangnantea.security.util.PasswordEncoder;
import com.shangnantea.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Random;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final Random RANDOM = new Random();
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 用户登录
     * 成功码：2000，失败码：2100, 2105
     */
    @Override
    public Result<TokenVO> login(LoginDTO loginDTO) {
        logger.info("用户登录请求: {}", loginDTO.getUsername());
        
        // 验证用户名和密码
        User user = checkUserAndPassword(loginDTO.getUsername(), loginDTO.getPassword());
        if (user == null) {
            logger.warn("登录失败: 用户名或密码错误, username: {}", loginDTO.getUsername());
            return Result.failure(2100); // 登录失败，请检查用户名和密码
        }
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            logger.warn("登录失败: 用户已被禁用, username: {}", loginDTO.getUsername());
            return Result.failure(2100); // 登录失败
        }
        
        // 生成JWT token
        String token = jwtUtil.generateToken(user);
        if (token == null) {
            logger.error("登录失败: Token生成失败, username: {}", loginDTO.getUsername());
            return Result.failure(2105); // 登录失败，服务器返回的Token无效
        }
        
        // 构建TokenVO
        TokenVO tokenVO = new TokenVO();
        tokenVO.setToken(token);
        tokenVO.setUserInfo(convertToUserVO(user));
        
        logger.info("登录成功: username: {}, userId: {}", loginDTO.getUsername(), user.getId());
        return Result.success(2000, tokenVO); // 登录成功
    }
    
    /**
     * 将User实体转换为UserVO
     */
    private UserVO convertToUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        userVO.setAvatar(user.getAvatar());
        userVO.setRole(user.getRole());
        userVO.setStatus(user.getStatus());
        userVO.setCreateTime(user.getCreateTime());
        userVO.setUpdateTime(user.getUpdateTime());
        return userVO;
    }
    
    @Override
    public User getUserById(String id) {
        return userMapper.selectById(id);
    }
    
    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
    
    @Override
    public User checkUserAndPassword(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }
    
    @Override
    public boolean isUserExist(String username) {
        return getUserByUsername(username) != null;
    }
    
    /**
     * 生成用户ID，格式为 'cy' + 6位数字
     * 同时确保ID不重复
     * 
     * @return 新生成的用户ID
     */
    private String generateUserId() {
        StringBuilder sb = new StringBuilder("cy");
        for (int i = 0; i < 6; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        
        String userId = sb.toString();
        // 检查ID是否已存在，存在则重新生成
        if (getUserById(userId) != null) {
            return generateUserId(); // 递归调用直到生成唯一ID
        }
        
        return userId;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(User user) {
        // 检查用户名是否已存在
        if (isUserExist(user.getUsername())) {
            return null;
        }
        
        // 生成用户ID，格式为 'cy' + 6位数字
        String userId = generateUserId();
        user.setId(userId);
        
        // 加密密码
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // 设置默认值
        if (user.getRole() == null) {
            user.setRole(2); // 默认为普通用户
        }
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认为正常状态
        }
        if (user.getIsDeleted() == null) {
            user.setIsDeleted(0); // 默认为未删除
        }
        if (user.getCreateTime() == null) {
            user.setCreateTime(new Date());
        }
        user.setUpdateTime(new Date());
        
        // 保存用户到数据库
        userMapper.insert(user);
        
        logger.info("用户注册成功: {}, ID: {}", user.getUsername(), userId);
        return user;
    }
    
    @Override
    public boolean updateUser(User user) {
        if (user.getId() == null) {
            return false;
        }
        
        User existingUser = getUserById(user.getId());
        if (existingUser == null) {
            return false;
        }
        
        // 不允许修改用户名
        if (user.getUsername() != null && !user.getUsername().equals(existingUser.getUsername())) {
            return false;
        }
        
        // 设置更新时间
        user.setUpdateTime(new Date());
        
        // 更新用户信息到数据库
        int result = userMapper.update(user);
        
        logger.info("用户信息更新结果: {}, 用户名: {}", result > 0, existingUser.getUsername());
        return result > 0;
    }
    
    @Override
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        // 更新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        int result = userMapper.updatePassword(userId, encodedPassword);
        
        logger.info("用户密码修改结果: {}, 用户名: {}", result > 0, user.getUsername());
        return result > 0;
    }
    
    @Override
    public boolean deleteUser(String id) {
        User user = getUserById(id);
        if (user == null) {
            return false;
        }
        
        // 删除用户（软删除）
        int result = userMapper.delete(id);
        
        logger.info("用户删除结果: {}, 用户名: {}", result > 0, user.getUsername());
        return result > 0;
    }
} 