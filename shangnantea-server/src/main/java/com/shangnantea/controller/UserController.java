package com.shangnantea.controller;

import com.shangnantea.common.api.Result;
import com.shangnantea.model.dto.AddFavoriteDTO;
import com.shangnantea.model.dto.AddFollowDTO;
import com.shangnantea.model.dto.AddLikeDTO;
import com.shangnantea.model.dto.ChangePasswordDTO;
import com.shangnantea.model.dto.CreateAdminDTO;
import com.shangnantea.model.dto.LoginDTO;
import com.shangnantea.model.dto.RegisterDTO;
import com.shangnantea.model.dto.SubmitShopCertificationDTO;
import com.shangnantea.model.dto.UpdateUserPreferencesDTO;
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
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * 用户控制器
 * 参考：前端 user.js 和 code-message-mapping.md
 */
@RestController
@RequestMapping({"/user", "/api/user"})
@Validated
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    // ==================== 认证相关 ====================
    
    /**
     * 用户登录
     * 路径: POST /user/login
     * 成功码: 2000, 失败码: 2100, 2101
     *
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<TokenVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        logger.info("用户登录请求: {}", loginDTO.getUsername());
        return userService.login(loginDTO);
    }
    
    /**
     * 用户注册
     * 路径: POST /user/register
     * 成功码: 2001, 失败码: 2102
     *
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody @Valid RegisterDTO registerDTO) {
        logger.info("用户注册请求: {}", registerDTO.getUsername());
        return userService.register(registerDTO);
    }
    
    /**
     * 用户登出
     * 路径: POST /user/logout
     * 成功码: 2002, 失败码: 2103
     *
     * @param request HTTP请求
     * @return 登出结果
     */
    @PostMapping("/logout")
    @RequiresLogin
    public Result<Void> logout(HttpServletRequest request) {
        logger.info("用户登出请求");
        return userService.logout(request);
    }
    
    /**
     * 刷新令牌
     * 路径: POST /user/refresh
     * 成功码: 200, 失败码: 2105, 2106
     *
     * @param request HTTP请求
     * @return 刷新结果
     */
    @PostMapping("/refresh")
    @RequiresLogin
    public Result<TokenVO> refreshToken(HttpServletRequest request) {
        logger.info("刷新令牌请求");
        return userService.refreshToken(request);
    }
    
    // ==================== 用户信息管理 ====================
    
    /**
     * 获取当前用户信息
     * 路径: GET /user/me
     * 成功码: 200, 失败码: 2104
     *
     * @return 用户信息
     */
    @GetMapping("/me")
    @RequiresLogin
    public Result<UserVO> getCurrentUser() {
        logger.info("获取当前用户信息请求");
        return userService.getCurrentUser();
    }
    
    /**
     * 更新用户信息
     * 路径: PUT /user
     * 成功码: 2003, 失败码: 2108
     *
     * @param userData 用户信息
     * @return 修改结果
     */
    @PutMapping
    @RequiresLogin
    public Result<UserVO> updateUserInfo(@RequestBody Map<String, Object> userData) {
        logger.info("更新用户信息请求");
        return userService.updateUserInfo(userData);
    }
    
    /**
     * 上传头像
     * 路径: POST /user/avatar
     * 成功码: 2004, 失败码: 2109, 2110, 2111
     *
     * @param file 头像文件
     * @return 上传结果
     */
    @PostMapping("/avatar")
    @RequiresLogin
    public Result<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        logger.info("上传头像请求, 文件名: {}", file.getOriginalFilename());
        return userService.uploadAvatar(file);
    }
    
    // ==================== 密码管理 ====================
    
    /**
     * 修改密码
     * 路径: PUT /user/password
     * 成功码: 2005, 失败码: 2112, 2113
     *
     * @param changePasswordDTO 修改密码信息
     * @return 修改结果
     */
    @PutMapping("/password")
    @RequiresLogin
    public Result<String> changePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
        logger.info("修改密码请求");
        return userService.changePassword(changePasswordDTO);
    }
    
    /**
     * 密码找回/重置
     * 路径: POST /user/password/reset
     * 成功码: 2006, 失败码: 2114
     *
     * @param resetData 重置信息
     * @return 重置结果
     */
    @PostMapping("/password/reset")
    public Result<String> resetPassword(@RequestBody Map<String, Object> resetData) {
        logger.info("密码重置请求");
        return userService.resetPassword(resetData);
    }
    
    // ==================== 收货地址管理 ====================
    
    /**
     * 获取用户地址列表
     * 路径: GET /user/addresses
     * 成功码: 200, 失败码: 2115
     *
     * @return 地址列表
     */
    @GetMapping("/addresses")
    @RequiresLogin
    public Result<Object> getAddressList() {
        logger.info("获取地址列表请求");
        return userService.getAddressList();
    }

    /**
     * 新增收货地址
     * 路径: POST /user/addresses
     * 成功码: 2007, 失败码: 2116
     *
     * @param addressData 地址数据
     * @return 创建结果
     */
    @PostMapping("/addresses")
    @RequiresLogin
    public Result<Object> addAddress(@RequestBody Map<String, Object> addressData) {
        logger.info("添加地址请求");
        return userService.addAddress(addressData);
    }

    /**
     * 更新收货地址
     * 路径: PUT /user/addresses/{id}
     * 成功码: 2008, 失败码: 2117
     *
     * @param id 地址ID
     * @param addressData 地址数据
     * @return 更新结果
     */
    @PutMapping("/addresses/{id}")
    @RequiresLogin
    public Result<Boolean> updateAddress(@PathVariable String id, @RequestBody Map<String, Object> addressData) {
        logger.info("更新地址请求: {}", id);
        return userService.updateAddress(id, addressData);
    }

    /**
     * 删除收货地址
     * 路径: DELETE /user/addresses/{id}
     * 成功码: 2009, 失败码: 2118
     *
     * @param id 地址ID
     * @return 删除结果
     */
    @DeleteMapping("/addresses/{id}")
    @RequiresLogin
    public Result<Boolean> deleteAddress(@PathVariable String id) {
        logger.info("删除地址请求: {}", id);
        return userService.deleteAddress(id);
    }

    /**
     * 设置默认地址
     * 路径: PUT /user/addresses/{id}/default
     * 成功码: 2010, 失败码: 2119
     *
     * @param id 地址ID
     * @return 设置结果
     */
    @PutMapping("/addresses/{id}/default")
    @RequiresLogin
    public Result<Boolean> setDefaultAddress(@PathVariable String id) {
        logger.info("设置默认地址请求: {}", id);
        return userService.setDefaultAddress(id);
    }

    // ==================== 商家认证 ====================

    /**
     * 获取商家认证状态
     * 路径: GET /user/shop-certification
     * 成功码: 200, 失败码: 2121
     *
     * @return 认证状态
     */
    @GetMapping("/shop-certification")
    @RequiresLogin
    public Result<Object> getShopCertificationStatus() {
        logger.info("获取商家认证状态请求");
        return userService.getShopCertificationStatus();
    }

    /**
     * 提交商家认证申请
     * 路径: POST /user/shop-certification
     * 成功码: 2011, 失败码: 2120
     *
     * @param certificationDTO 认证数据
     * @return 提交结果
     */
    @PostMapping("/shop-certification")
    @RequiresLogin
    public Result<Boolean> submitShopCertification(@Valid @RequestBody SubmitShopCertificationDTO certificationDTO) {
        logger.info("提交商家认证申请请求");
        return userService.submitShopCertification(certificationDTO);
    }

    // ==================== 用户互动功能 ====================

    /**
     * 获取关注列表
     * 路径: GET /user/follows
     * 成功码: 200, 失败码: 2122
     *
     * @param type 关注类型（user/shop），可选
     * @return 关注列表
     */
    @GetMapping("/follows")
    @RequiresLogin
    public Result<Object> getFollowList(@RequestParam(required = false) String type) {
        logger.info("获取关注列表请求, type: {}", type);
        return userService.getFollowList(type);
    }

    /**
     * 添加关注
     * 路径: POST /user/follows
     * 成功码: 2012, 失败码: 2123
     *
     * @param followDTO 关注信息
     * @return 关注结果
     */
    @PostMapping("/follows")
    @RequiresLogin
    public Result<Boolean> addFollow(@Valid @RequestBody AddFollowDTO followDTO) {
        logger.info("添加关注请求");
        return userService.addFollow(followDTO);
    }

    /**
     * 取消关注
     * 路径: DELETE /user/follows/{id}
     * 成功码: 2013, 失败码: 2124
     *
     * @param id 关注ID
     * @return 取消结果
     */
    @DeleteMapping("/follows/{id}")
    @RequiresLogin
    public Result<Boolean> removeFollow(@PathVariable String id) {
        logger.info("取消关注请求: {}", id);
        return userService.removeFollow(id);
    }

    /**
     * 获取收藏列表
     * 路径: GET /user/favorites
     * 成功码: 200, 失败码: 2125
     *
     * @param type 收藏类型（tea/post/article），可选
     * @return 收藏列表
     */
    @GetMapping("/favorites")
    @RequiresLogin
    public Result<Object> getFavoriteList(@RequestParam(required = false) String type) {
        logger.info("获取收藏列表请求, type: {}", type);
        return userService.getFavoriteList(type);
    }

    /**
     * 添加收藏
     * 路径: POST /user/favorites
     * 成功码: 2014, 失败码: 2126
     *
     * @param favoriteDTO 收藏信息
     * @return 收藏结果
     */
    @PostMapping("/favorites")
    @RequiresLogin
    public Result<Boolean> addFavorite(@Valid @RequestBody AddFavoriteDTO favoriteDTO) {
        logger.info("添加收藏请求");
        return userService.addFavorite(favoriteDTO);
    }

    /**
     * 取消收藏
     * 路径: DELETE /user/favorites/{id}
     * 成功码: 2015, 失败码: 2127
     *
     * @param id 收藏ID
     * @return 取消结果
     */
    @DeleteMapping("/favorites/{id}")
    @RequiresLogin
    public Result<Boolean> removeFavorite(@PathVariable String id) {
        logger.info("取消收藏请求: {}", id);
        return userService.removeFavorite(id);
    }

    /**
     * 点赞
     * 路径: POST /user/likes
     * 成功码: 2016, 失败码: 2128
     *
     * @param likeDTO 点赞信息
     * @return 点赞结果
     */
    @PostMapping("/likes")
    @RequiresLogin
    public Result<Boolean> addLike(@Valid @RequestBody AddLikeDTO likeDTO) {
        logger.info("点赞请求");
        return userService.addLike(likeDTO);
    }

    /**
     * 取消点赞
     * 路径: DELETE /user/likes/{id}
     * 成功码: 2017, 失败码: 2129
     *
     * @param id 点赞ID
     * @return 取消结果
     */
    @DeleteMapping("/likes/{id}")
    @RequiresLogin
    public Result<Boolean> removeLike(@PathVariable String id) {
        logger.info("取消点赞请求: {}", id);
        return userService.removeLike(id);
    }

    // ==================== 用户偏好设置 ====================

    /**
     * 获取用户偏好设置
     * 路径: GET /user/preferences
     * 成功码: 200, 失败码: 2130
     *
     * @return 偏好设置
     */
    @GetMapping("/preferences")
    @RequiresLogin
    public Result<Object> getUserPreferences() {
        logger.info("获取用户偏好设置请求");
        return userService.getUserPreferences();
    }

    /**
     * 更新用户偏好设置
     * 路径: PUT /user/preferences
     * 成功码: 2018, 失败码: 2131
     *
     * @param preferencesDTO 偏好设置
     * @return 更新结果
     */
    @PutMapping("/preferences")
    @RequiresLogin
    public Result<Object> updateUserPreferences(@Valid @RequestBody UpdateUserPreferencesDTO preferencesDTO) {
        logger.info("更新用户偏好设置请求");
        return userService.updateUserPreferences(preferencesDTO);
    }

    /**
     * 获取指定用户信息
     * 路径: GET /user/{userId}
     * 成功码: 200, 失败码: 2107
     * 注意：此路径应放在最后，避免与更具体的路径冲突
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    public Result<UserVO> getUserInfo(@PathVariable String userId) {
        logger.info("获取用户信息请求: {}", userId);
        return userService.getUserById(userId);
    }

    // ==================== 管理员功能 ====================

    /**
     * 获取用户列表（管理员）
     * 路径: GET /user/admin/users
     * 成功码: 200, 失败码: 2132, 2133
     *
     * @param keyword 关键词
     * @param role 角色
     * @param status 状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 用户列表
     */
    @GetMapping("/admin/users")
    @RequiresRoles({1}) // 管理员角色
    public Result<Object> getAdminUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        logger.info("获取用户列表请求(管理员), keyword: {}, role: {}, status: {}, page: {}, pageSize: {}", 
                keyword, role, status, page, pageSize);
        return userService.getAdminUserList(keyword, role, status, page, pageSize);
    }

    /**
     * 创建管理员账号（管理员）
     * 路径: POST /user/admin/users
     * 成功码: 2019, 失败码: 2134, 2135
     *
     * @param adminDTO 管理员数据
     * @return 创建结果
     */
    @PostMapping("/admin/users")
    @RequiresRoles({1}) // 管理员角色
    public Result<Boolean> createAdmin(@Valid @RequestBody CreateAdminDTO adminDTO) {
        logger.info("创建管理员账号请求");
        return userService.createAdmin(adminDTO);
    }

    /**
     * 更新用户信息（管理员）
     * 路径: PUT /user/admin/users/{userId}
     * 成功码: 2020, 失败码: 2136, 2137
     *
     * @param userId 用户ID
     * @param userData 用户数据
     * @return 更新结果
     */
    @PutMapping("/admin/users/{userId}")
    @RequiresRoles({1}) // 管理员角色
    public Result<Boolean> updateUser(@PathVariable String userId, @RequestBody Map<String, Object> userData) {
        logger.info("更新用户信息请求(管理员): {}", userId);
        return userService.updateUser(userId, userData);
    }

    /**
     * 删除用户（管理员）
     * 路径: DELETE /user/admin/users/{userId}
     * 成功码: 2021, 失败码: 2138, 2139
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/admin/users/{userId}")
    @RequiresRoles({1}) // 管理员角色
    public Result<Boolean> deleteUser(@PathVariable String userId) {
        logger.info("删除用户请求(管理员): {}", userId);
        return userService.deleteUser(userId);
    }

    /**
     * 启用/禁用用户（管理员）
     * 路径: PUT /user/admin/users/{userId}/status
     * 成功码: 2022, 失败码: 2140, 2141
     *
     * @param userId 用户ID
     * @param statusData 状态数据 {status}
     * @return 更新结果
     */
    @PutMapping("/admin/users/{userId}/status")
    @RequiresRoles({1}) // 管理员角色
    public Result<Boolean> toggleUserStatus(@PathVariable String userId, @RequestBody Map<String, Object> statusData) {
        logger.info("切换用户状态请求(管理员): {}, status: {}", userId, statusData.get("status"));
        return userService.toggleUserStatus(userId, statusData);
    }

    /**
     * 获取商家认证申请列表（管理员）
     * 路径: GET /user/admin/certifications
     * 成功码: 200, 失败码: 2142, 2143
     *
     * @param status 状态，可选
     * @param page 页码
     * @param pageSize 每页数量
     * @return 认证申请列表
     */
    @GetMapping("/admin/certifications")
    @RequiresRoles({1}) // 管理员角色
    public Result<Object> getCertificationList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        logger.info("获取商家认证申请列表请求(管理员), status: {}, page: {}, pageSize: {}", status, page, pageSize);
        return userService.getCertificationList(status, page, pageSize);
    }

    /**
     * 审核认证申请（管理员）
     * 路径: PUT /user/admin/certifications/{id}
     * 成功码: 2023, 失败码: 2144, 2145
     *
     * @param id 认证ID
     * @param auditData 审核数据 {status, message}
     * @return 审核结果
     */
    @PutMapping("/admin/certifications/{id}")
    @RequiresRoles({1}) // 管理员角色
    public Result<Boolean> processCertification(@PathVariable Integer id, @RequestBody Map<String, Object> auditData) {
        logger.info("审核认证申请请求(管理员): {}, status: {}", id, auditData.get("status"));
        return userService.processCertification(id, auditData);
    }

    /**
     * 上传商家认证图片
     * 路径: POST /user/merchant/certification/image
     * 成功码: 2024, 失败码: 2146, 2147, 2148
     *
     * @param file 图片文件
     * @param type 图片类型(id_front, id_back, business_license)
     * @return 上传结果
     */
    @PostMapping("/merchant/certification/image")
    @RequiresLogin
    public Result<Map<String, Object>> uploadCertificationImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        logger.info("上传商家认证图片请求, type: {}, 文件名: {}", type, file.getOriginalFilename());
        return userService.uploadCertificationImage(file, type);
    }
}
