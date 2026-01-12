package com.shangnantea.controller;

import com.shangnantea.common.api.Result;
import com.shangnantea.model.dto.ChangePasswordDTO;
import com.shangnantea.model.dto.LoginDTO;
import com.shangnantea.model.dto.RegisterDTO;
import com.shangnantea.model.vo.user.TokenVO;
import com.shangnantea.model.vo.user.UserVO;
import com.shangnantea.security.annotation.RequiresLogin;
import com.shangnantea.security.annotation.RequiresRoles;
import com.shangnantea.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping({"/user", "/api/user"})
@Validated
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<TokenVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        logger.info("用户登录请求: {}", loginDTO.getUsername());
        TokenVO result = userService.login(loginDTO);
        return Result.success(result);
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
        UserVO result = userService.register(registerDTO);
        return Result.success(result);
    }
    
    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        logger.info("用户登出请求");
        userService.logout(request);
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
        logger.info("获取用户信息请求");
        UserVO result = userService.getCurrentUserInfo(request);
        return Result.success(result);
    }
    
    /**
     * 获取当前用户信息 (需要登录)
     *
     * @return 用户信息
     */
    @GetMapping("/me")
    @RequiresLogin
    public Result<UserVO> getUserInfo() {
        UserVO result = userService.getUserInfo();
        return Result.success(result);
    }

    /**
     * 获取用户地址列表
     *
     * @return 地址列表
     */
    @GetMapping("/addresses")
    public Result<Object> listAddresses() {
        Object result = userService.listAddresses();
        return Result.success(result);
    }

    /**
     * 新增收货地址
     *
     * @param body 地址数据
     * @return 创建结果
     */
    @PostMapping("/addresses")
    public Result<Object> addAddress(@RequestBody(required = false) Map<String, Object> body) {
        Object result = userService.addAddress(body);
        return Result.success(result);
    }

    /**
     * 更新收货地址
     *
     * @param id 地址ID
     * @param body 地址数据
     * @return 更新结果
     */
    @PutMapping("/addresses/{id}")
    public Result<Boolean> updateAddress(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        Boolean result = userService.updateAddress(id, body);
        return Result.success(result);
    }

    /**
     * 删除收货地址
     *
     * @param id 地址ID
     * @return 删除结果
     */
    @DeleteMapping("/addresses/{id}")
    public Result<Boolean> deleteAddress(@PathVariable String id) {
        Boolean result = userService.deleteAddress(id);
        return Result.success(result);
    }

    /**
     * 设置默认地址
     *
     * @param id 地址ID
     * @return 设置结果
     */
    @PutMapping("/addresses/{id}/default")
    public Result<Boolean> setDefaultAddress(@PathVariable String id) {
        Boolean result = userService.setDefaultAddress(id);
        return Result.success(result);
    }

    /**
     * 获取用户偏好设置
     *
     * @return 偏好设置
     */
    @GetMapping("/preferences")
    public Result<Object> getPreferences() {
        Object result = userService.getPreferences();
        return Result.success(result);
    }

    /**
     * 更新用户偏好设置
     *
     * @param body 偏好设置
     * @return 更新结果
     */
    @PutMapping("/preferences")
    public Result<Object> updatePreferences(@RequestBody(required = false) Map<String, Object> body) {
        Object result = userService.updatePreferences(body);
        return Result.success(result);
    }

    /**
     * 商家认证提交
     */
    @PostMapping("/shop-certification")
    public Result<Boolean> submitShopCertification(@RequestBody(required = false) Map<String, Object> body) {
        Boolean result = userService.submitShopCertification(body);
        return Result.success(result);
    }

    /**
     * 获取商家认证状态
     */
    @GetMapping("/shop-certification")
    public Result<Object> getShopCertification() {
        Object result = userService.getShopCertification();
        return Result.success(result);
    }
    
    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 修改结果
     */
    @PutMapping("/update")
    @RequiresLogin
    public Result<UserVO> updateUser(@RequestBody Map<String, Object> user) {
        UserVO result = userService.updateUser(user);
        return Result.success(result);
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
        String result = userService.changePassword(changePasswordDTO);
        return Result.success(result);
    }
    
    /**
     * 获取用户列表（仅管理员）
     *
     * @return 用户列表
     */
    @GetMapping("/list")
    @RequiresRoles({1}) // 管理员角色
    public Result<Object> getUserList() {
        Object result = userService.getUserList();
        return Result.success(result);
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
        String result = userService.deleteUser(id);
        return Result.success(result);
    }
    
    /**
     * 获取指定用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable String userId) {
        UserVO result = userService.getUserById(userId);
        return Result.success(result);
    }
    
    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/{userId}")
    public Result<UserVO> updateUserInfo(@PathVariable String userId, @RequestBody Map<String, Object> user) {
        UserVO result = userService.updateUserInfo(userId, user);
        return Result.success(result);
    }
    
    /**
     * 上传头像
     *
     * @param request HTTP请求
     * @return 上传结果
     */
    @PostMapping("/avatar")
    public Result<Object> uploadAvatar(HttpServletRequest request) {
        Object result = userService.uploadAvatar(request);
        return Result.success(result);
    }
    
    /**
     * 修改密码
     *
     * @param body 密码信息
     * @return 修改结果
     */
    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody(required = false) Map<String, Object> body) {
        String result = userService.updatePassword(body);
        return Result.success(result);
    }
    
    /**
     * 密码找回/重置
     *
     * @param body 重置信息
     * @return 重置结果
     */
    @PostMapping("/password/reset")
    public Result<String> resetPassword(@RequestBody(required = false) Map<String, Object> body) {
        String result = userService.resetPassword(body);
        return Result.success(result);
    }
    
    /**
     * 刷新令牌
     *
     * @param request HTTP请求
     * @return 刷新结果
     */
    @PostMapping("/refresh")
    public Result<TokenVO> refreshToken(HttpServletRequest request) {
        TokenVO result = userService.refreshToken(request);
        return Result.success(result);
    }
    
    /**
     * 获取关注列表
     *
     * @param type 关注类型
     * @return 关注列表
     */
    @GetMapping("/follows")
    public Result<Object> listFollows(@RequestParam(required = false) String type) {
        Object result = userService.listFollows(type);
        return Result.success(result);
    }
    
    /**
     * 添加关注
     *
     * @param body 关注信息
     * @return 关注结果
     */
    @PostMapping("/follows")
    public Result<Boolean> addFollow(@RequestBody(required = false) Map<String, Object> body) {
        Boolean result = userService.addFollow(body);
        return Result.success(result);
    }
    
    /**
     * 取消关注
     *
     * @param id 关注ID
     * @return 取消结果
     */
    @DeleteMapping("/follows/{id}")
    public Result<Boolean> deleteFollow(@PathVariable String id) {
        Boolean result = userService.deleteFollow(id);
        return Result.success(result);
    }
    
    /**
     * 获取收藏列表
     *
     * @param type 收藏类型
     * @return 收藏列表
     */
    @GetMapping("/favorites")
    public Result<Object> listFavorites(@RequestParam(required = false) String type) {
        Object result = userService.listFavorites(type);
        return Result.success(result);
    }
    
    /**
     * 添加收藏
     *
     * @param body 收藏信息
     * @return 收藏结果
     */
    @PostMapping("/favorites")
    public Result<Boolean> addFavorite(@RequestBody(required = false) Map<String, Object> body) {
        Boolean result = userService.addFavorite(body);
        return Result.success(result);
    }
    
    /**
     * 取消收藏
     *
     * @param id 收藏ID
     * @return 取消结果
     */
    @DeleteMapping("/favorites/{id}")
    public Result<Boolean> deleteFavorite(@PathVariable String id) {
        Boolean result = userService.deleteFavorite(id);
        return Result.success(result);
    }
    
    /**
     * 点赞
     *
     * @param body 点赞信息
     * @return 点赞结果
     */
    @PostMapping("/likes")
    public Result<Boolean> addLike(@RequestBody(required = false) Map<String, Object> body) {
        Boolean result = userService.addLike(body);
        return Result.success(result);
    }
    
    /**
     * 取消点赞
     *
     * @param id 点赞ID
     * @return 取消结果
     */
    @DeleteMapping("/likes/{id}")
    public Result<Boolean> deleteLike(@PathVariable String id) {
        Boolean result = userService.deleteLike(id);
        return Result.success(result);
    }
    
    /**
     * 获取用户列表（管理员）
     *
     * @param keyword 关键词
     * @param role 角色
     * @param status 状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 用户列表
     */
    @GetMapping("/admin/users")
    public Result<Object> getAdminUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Object result = userService.getAdminUsers(keyword, role, status, page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 创建管理员账号（管理员）
     *
     * @param body 用户信息
     * @return 创建结果
     */
    @PostMapping("/admin/users")
    public Result<Boolean> createAdminUser(@RequestBody(required = false) Map<String, Object> body) {
        Boolean result = userService.createAdminUser(body);
        return Result.success(result);
    }
    
    /**
     * 更新用户信息（管理员）
     *
     * @param userId 用户ID
     * @param body 用户信息
     * @return 更新结果
     */
    @PutMapping("/admin/users/{userId}")
    public Result<Boolean> updateAdminUser(@PathVariable String userId, @RequestBody(required = false) Map<String, Object> body) {
        Boolean result = userService.updateAdminUser(userId, body);
        return Result.success(result);
    }
    
    /**
     * 删除用户（管理员）
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/admin/users/{userId}")
    public Result<Boolean> deleteAdminUser(@PathVariable String userId) {
        Boolean result = userService.deleteAdminUser(userId);
        return Result.success(result);
    }
    
    /**
     * 更新用户角色（管理员，已废弃）
     *
     * @param userId 用户ID
     * @param body 角色信息
     * @return 更新结果
     */
    @PutMapping("/admin/users/{userId}/role")
    @Deprecated
    public Result<Boolean> updateUserRole(@PathVariable String userId, @RequestBody(required = false) Map<String, Object> body) {
        Boolean result = userService.updateUserRole(userId, body);
        return Result.success(result);
    }
    
    /**
     * 启用/禁用用户（管理员）
     *
     * @param userId 用户ID
     * @param body 状态信息
     * @return 更新结果
     */
    @PutMapping("/admin/users/{userId}/status")
    public Result<Boolean> updateUserStatus(@PathVariable String userId, @RequestBody(required = false) Map<String, Object> body) {
        Boolean result = userService.updateUserStatus(userId, body);
        return Result.success(result);
    }
    
    /**
     * 获取商家认证申请列表（管理员）
     *
     * @param status 状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 认证申请列表
     */
    @GetMapping("/admin/certifications")
    public Result<Object> getAdminCertifications(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Object result = userService.getAdminCertifications(status, page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 审核认证申请（管理员）
     *
     * @param id 认证ID
     * @param body 审核信息
     * @return 审核结果
     */
    @PutMapping("/admin/certifications/{id}")
    public Result<Boolean> auditCertification(@PathVariable Integer id, @RequestBody(required = false) Map<String, Object> body) {
        Boolean result = userService.auditCertification(id, body);
        return Result.success(result);
    }
}
