package com.shangnantea.service;

import com.shangnantea.common.api.Result;
import com.shangnantea.model.dto.AddFavoriteDTO;
import com.shangnantea.model.dto.AddFollowDTO;
import com.shangnantea.model.dto.AddLikeDTO;
import com.shangnantea.model.dto.ChangePasswordDTO;
import com.shangnantea.model.dto.CreateAdminDTO;
import com.shangnantea.model.dto.LoginDTO;
import com.shangnantea.model.dto.ProcessCertificationDTO;
import com.shangnantea.model.dto.RegisterDTO;
import com.shangnantea.model.dto.SubmitShopCertificationDTO;
import com.shangnantea.model.dto.UpdateUserDTO;
import com.shangnantea.model.dto.UpdateUserPreferencesDTO;
import com.shangnantea.model.entity.user.User;
import com.shangnantea.model.vo.user.TokenVO;
import com.shangnantea.model.vo.user.UserVO;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {
    
    // ==================== 认证相关 ====================
    
    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果，包含token和用户信息
     */
    Result<TokenVO> login(LoginDTO loginDTO);
    
    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册结果，包含用户信息
     */
    Result<UserVO> register(RegisterDTO registerDTO);
    
    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @return 登出结果
     */
    Result<Void> logout(HttpServletRequest request);
    
    /**
     * 刷新令牌
     *
     * @param request HTTP请求
     * @return 刷新结果
     */
    Result<TokenVO> refreshToken(HttpServletRequest request);
    
    // ==================== 用户信息管理 ====================
    
    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    Result<UserVO> getCurrentUser();
    
    /**
     * 根据用户ID获取用户信息
     * 改造说明：当查询其他用户时，返回的用户信息中包含 isFollowed 字段（当前用户是否已关注该用户）
     *
     * @param userId 用户ID
     * @return 用户信息（查询其他用户时包含 isFollowed 字段）
     */
    Result<UserVO> getUserById(String userId);
    
    /**
     * 更新用户信息
     *
     * @param userData 用户数据
     * @return 更新结果
     */
    Result<UserVO> updateUserInfo(Map<String, Object> userData);
    
    /**
     * 上传头像
     *
     * @param file 头像文件
     * @return 上传结果，包含头像URL
     */
    Result<Map<String, Object>> uploadAvatar(MultipartFile file);
    
    // ==================== 密码管理 ====================
    
    /**
     * 修改密码
     *
     * @param changePasswordDTO 修改密码信息
     * @return 修改结果
     */
    Result<String> changePassword(ChangePasswordDTO changePasswordDTO);
    
    /**
     * 密码找回/重置
     *
     * @param resetData 重置信息
     * @return 重置结果
     */
    Result<String> resetPassword(Map<String, Object> resetData);
    
    // ==================== 收货地址管理 ====================
    
    /**
     * 获取用户地址列表
     *
     * @return 地址列表
     */
    Result<Object> getAddressList();
    
    /**
     * 新增收货地址
     *
     * @param addressData 地址数据
     * @return 创建结果
     */
    Result<Object> addAddress(Map<String, Object> addressData);
    
    /**
     * 更新收货地址
     *
     * @param id 地址ID
     * @param addressData 地址数据
     * @return 更新结果
     */
    Result<Boolean> updateAddress(String id, Map<String, Object> addressData);
    
    /**
     * 删除收货地址
     *
     * @param id 地址ID
     * @return 删除结果
     */
    Result<Boolean> deleteAddress(String id);
    
    /**
     * 设置默认地址
     *
     * @param id 地址ID
     * @return 设置结果
     */
    Result<Boolean> setDefaultAddress(String id);
    
    // ==================== 商家认证 ====================
    
    /**
     * 获取商家认证状态
     *
     * @return 认证状态
     */
    Result<Object> getShopCertificationStatus();
    
    /**
     * 提交商家认证申请
     *
     * @param certificationDTO 认证数据
     * @return 提交结果
     */
    Result<Boolean> submitShopCertification(SubmitShopCertificationDTO certificationDTO);
    
    /**
     * 上传商家认证图片
     *
     * @param file 图片文件
     * @param type 图片类型
     * @return 上传结果
     */
    Result<Map<String, Object>> uploadCertificationImage(MultipartFile file, String type);
    
    // ==================== 用户互动功能 ====================
    
    /**
     * 获取关注列表
     *
     * @param type 关注类型
     * @return 关注列表
     */
    Result<Object> getFollowList(String type);
    
    /**
     * 添加关注
     *
     * @param followDTO 关注信息
     * @return 关注结果
     */
    Result<Boolean> addFollow(AddFollowDTO followDTO);
    
    /**
     * 取消关注
     *
     * @param id 关注ID
     * @return 取消结果
     */
    Result<Boolean> removeFollow(String id);
    
    /**
     * 获取收藏列表
     *
     * @param type 收藏类型
     * @return 收藏列表
     */
    Result<Object> getFavoriteList(String type);
    
    /**
     * 添加收藏
     *
     * @param favoriteDTO 收藏信息
     * @return 收藏结果
     */
    Result<Boolean> addFavorite(AddFavoriteDTO favoriteDTO);
    
    /**
     * 取消收藏
     *
     * @param id 收藏ID
     * @return 取消结果
     */
    Result<Boolean> removeFavorite(String id);
    
    /**
     * 点赞
     *
     * @param likeDTO 点赞信息
     * @return 点赞结果
     */
    Result<Boolean> addLike(AddLikeDTO likeDTO);
    
    /**
     * 取消点赞
     *
     * @param id 点赞ID
     * @return 取消结果
     */
    Result<Boolean> removeLike(String id);
    
    // ==================== 用户偏好设置 ====================
    
    /**
     * 获取用户偏好设置
     *
     * @return 偏好设置
     */
    Result<Object> getUserPreferences();
    
    /**
     * 更新用户偏好设置
     *
     * @param preferencesDTO 偏好设置
     * @return 更新结果
     */
    Result<Object> updateUserPreferences(UpdateUserPreferencesDTO preferencesDTO);
    
    // ==================== 管理员功能 ====================
    
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
    Result<Object> getAdminUserList(String keyword, Integer role, Integer status, Integer page, Integer pageSize);
    
    /**
     * 创建管理员账号（管理员）
     *
     * @param adminDTO 管理员数据
     * @return 创建结果
     */
    Result<Boolean> createAdmin(CreateAdminDTO adminDTO);
    
    /**
     * 更新用户信息（管理员）
     *
     * @param userId 用户ID
     * @param updateUserDTO 用户数据
     * @return 更新结果
     */
    Result<Boolean> updateUser(String userId, UpdateUserDTO updateUserDTO);
    
    /**
     * 删除用户（管理员）
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    Result<Boolean> deleteUser(String userId);
    
    /**
     * 启用/禁用用户（管理员）
     *
     * @param userId 用户ID
     * @param statusData 状态数据
     * @return 更新结果
     */
    Result<Boolean> toggleUserStatus(String userId, Map<String, Object> statusData);
    
    /**
     * 获取商家认证申请列表（管理员）
     *
     * @param status 状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 认证申请列表
     */
    Result<Object> getCertificationList(Integer status, Integer page, Integer pageSize);
    
    /**
     * 审核认证申请（管理员）
     *
     * @param id 认证ID
     * @param processCertificationDTO 审核数据
     * @return 审核结果
     */
    Result<Boolean> processCertification(Integer id, ProcessCertificationDTO processCertificationDTO);
    
    // ==================== 基础方法（内部使用） ====================
    
    /**
     * 根据用户ID获取用户
     *
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回null
     */
    User getUserEntityById(String id);
    
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
     * 更新用户信息
     *
     * @param user 用户对象
     * @return 是否更新成功
     */
    boolean updateUserEntity(User user);
    
    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changeUserPassword(String userId, String oldPassword, String newPassword);
    
    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    boolean deleteUserEntity(String id);
} 