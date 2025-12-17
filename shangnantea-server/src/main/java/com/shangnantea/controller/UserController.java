package com.shangnantea.controller;

import com.shangnantea.common.api.Result;
import com.shangnantea.common.api.ResultCode;
import com.shangnantea.model.dto.ChangePasswordDTO;
import com.shangnantea.model.dto.LoginDTO;
import com.shangnantea.model.dto.RegisterDTO;
import com.shangnantea.model.entity.user.User;
import com.shangnantea.model.vo.user.TokenVO;
import com.shangnantea.model.vo.user.UserVO;
import com.shangnantea.security.annotation.RequiresLogin;
import com.shangnantea.security.annotation.RequiresRoles;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.security.util.JwtUtil;
import com.shangnantea.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<TokenVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        logger.info("用户登录请求: {}", loginDTO.getUsername());
        
        try {
            // 验证用户名和密码
            User user = userService.checkUserAndPassword(loginDTO.getUsername(), loginDTO.getPassword());
            if (user == null) {
                logger.warn("登录失败: 用户名或密码错误: {}", loginDTO.getUsername());
                return Result.failure(ResultCode.UNAUTHORIZED, "用户名或密码错误");
            }
            
            // 验证用户状态
            if (user.getStatus() != null && user.getStatus() != 1) {
                logger.warn("登录失败: 用户已被禁用: {}", loginDTO.getUsername());
                return Result.failure(ResultCode.FORBIDDEN, "账户已被禁用");
            }
            
            // 验证用户角色 - 只接受三种有效角色
            if (user.getRole() == null || (user.getRole() != 1 && user.getRole() != 2 && user.getRole() != 3)) {
                logger.warn("登录失败: 用户角色无效: {}, 角色: {}", loginDTO.getUsername(), user.getRole());
                return Result.failure(ResultCode.FORBIDDEN, "账户角色无效");
            }
            
            // 生成JWT令牌
            String token = jwtUtil.generateToken(user);
            if (token == null) {
                logger.error("登录失败: 生成令牌失败: {}", loginDTO.getUsername());
                return Result.failure(ResultCode.INTERNAL_SERVER_ERROR, "生成令牌失败");
            }
            
            // 转换为前端用户VO对象
            UserVO userVO = convertToUserVO(user);
            
            // 封装令牌和用户信息
            TokenVO tokenVO = new TokenVO();
            tokenVO.setToken(token);
            tokenVO.setUserInfo(userVO);
            
            logger.info("用户登录成功: {}", loginDTO.getUsername());
            return Result.success(tokenVO);
        } catch (Exception e) {
            logger.error("登录处理异常: {}", e.getMessage(), e);
            return Result.failure(ResultCode.INTERNAL_SERVER_ERROR, "登录处理异常: " + e.getMessage());
        }
    }
    
    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody @Valid RegisterDTO registerDTO) {
        logger.info("用户注册请求: {}", registerDTO.getUsername());
        
        // 验证两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            logger.warn("注册失败: 两次密码不一致: {}", registerDTO.getUsername());
            return Result.failure(ResultCode.VALIDATE_FAILED, "两次密码不一致");
        }
        
        // 检查用户名是否已存在
        if (userService.isUserExist(registerDTO.getUsername())) {
            logger.warn("注册失败: 用户名已存在: {}", registerDTO.getUsername());
            return Result.failure(ResultCode.BAD_REQUEST, "用户名已存在");
        }
        
        // 创建用户对象
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword());
        // RegisterDTO中没有nickname字段，省略设置昵称
        // user.setNickname(registerDTO.getNickname());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        
        // 设置为普通用户角色(2)
        user.setRole(2);
        // 设置为正常状态(1)
        user.setStatus(1);
        
        // 注册用户
        User registeredUser = userService.register(user);
        if (registeredUser == null) {
            logger.error("注册失败: 保存用户失败: {}", registerDTO.getUsername());
            return Result.failure(ResultCode.INTERNAL_SERVER_ERROR, "注册失败");
        }
        
        logger.info("用户注册成功: {}", registerDTO.getUsername());
        return Result.success(convertToUserVO(registeredUser));
    }
    
    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        // 获取当前用户名（如果有）
        String token = getTokenFromRequest(request);
        if (token != null) {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {
                logger.info("用户登出: {}", username);
            }
        }
        
        // 前端负责清除token，后端不做特殊处理
        return Result.success();
    }
    
    /**
     * 获取当前用户信息
     *
     * @param request HTTP请求
     * @return 当前用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> info(HttpServletRequest request) {
        // 获取令牌
        String token = getTokenFromRequest(request);
        if (token == null) {
            logger.warn("获取用户信息失败: 未提供令牌");
            return Result.failure(ResultCode.UNAUTHORIZED, "未登录");
        }
        
        // 验证令牌
        if (!jwtUtil.validateToken(token)) {
            logger.warn("获取用户信息失败: 令牌无效");
            return Result.failure(ResultCode.UNAUTHORIZED, "登录已过期");
        }
        
        // 获取用户信息
        User user = jwtUtil.getUserFromToken(token);
        if (user == null) {
            logger.warn("获取用户信息失败: 用户不存在");
            return Result.failure(ResultCode.UNAUTHORIZED, "用户不存在");
        }
        
        logger.info("获取用户信息成功: {}", user.getUsername());
        return Result.success(convertToUserVO(user));
    }
    
    /**
     * 获取当前用户信息 (需要登录)
     *
     * @return 用户信息
     */
    @GetMapping("/me")
    @RequiresLogin
    public Result<UserVO> getUserInfo() {
        User user = UserContext.getCurrentUser();
        if (user == null) {
            return Result.failure(ResultCode.UNAUTHORIZED, "未登录");
        }
        return Result.success(convertToUserVO(user));
    }
    
    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 修改结果
     */
    @PutMapping("/update")
    @RequiresLogin
    public Result<UserVO> updateUser(@RequestBody User user) {
        // 获取当前用户
        User currentUser = UserContext.getCurrentUser();
        
        // 只能修改自己的信息
        if (!currentUser.getId().equals(user.getId())) {
            return Result.failure(ResultCode.FORBIDDEN, "只能修改自己的信息");
        }
        
        // 不允许修改密码和角色
        user.setPassword(null);
        user.setRole(currentUser.getRole());
        
        // 更新用户信息
        boolean success = userService.updateUser(user);
        if (!success) {
            return Result.failure(ResultCode.FAILURE, "修改失败");
        }
        
        // 获取最新用户信息
        User updatedUser = userService.getUserById(user.getId());
        return Result.success(convertToUserVO(updatedUser));
    }
    
    /**
     * 修改密码
     *
     * @param changePasswordDTO 修改密码信息
     * @return 修改结果
     */
    @PostMapping("/change-password")
    @RequiresLogin
    public Result<String> changePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
        // 验证两次密码是否一致
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmNewPassword())) {
            return Result.failure(ResultCode.VALIDATE_FAILED, "两次密码不一致");
        }
        
        // 获取当前用户
        User currentUser = UserContext.getCurrentUser();
        
        // 修改密码
        boolean success = userService.changePassword(currentUser.getId(), 
                changePasswordDTO.getOldPassword(), changePasswordDTO.getNewPassword());
        if (!success) {
            return Result.failure(ResultCode.VALIDATE_FAILED, "原密码错误或修改失败");
        }
        
        return Result.success("密码修改成功");
    }
    
    /**
     * 获取用户列表（仅管理员）
     *
     * @return 用户列表
     */
    @GetMapping("/list")
    @RequiresRoles({1}) // 管理员角色
    public Result<String> getUserList() {
        // TODO: 实现获取用户列表
        return Result.success("用户列表功能正在开发中");
    }
    
    /**
     * 删除用户（仅管理员）
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @RequiresRoles({1}) // 管理员角色
    public Result<String> deleteUser(@PathVariable String id) {
        // 禁止删除管理员账号
        User userToDelete = userService.getUserById(id);
        if (userToDelete != null && userToDelete.getRole() == 1) {
            return Result.failure(ResultCode.FORBIDDEN, "不能删除管理员账号");
        }
        
        // 删除用户
        boolean success = userService.deleteUser(id);
        if (!success) {
            return Result.failure(ResultCode.FAILURE, "删除失败");
        }
        
        return Result.success("删除成功");
    }
    
    /**
     * 将User转换为UserVO
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    private UserVO convertToUserVO(User user) {
        if (user == null) {
            return null;
        }
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // UserVO中没有password字段，不需要清除
        // userVO.setPassword(null); // 清除密码
        
        return userVO;
    }
    
    /**
     * 从请求中获取令牌
     *
     * @param request HTTP请求
     * @return 令牌字符串，如果没有则返回null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 