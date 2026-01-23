package com.shangnantea.service.impl;

import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.ShopCertificationMapper;
import com.shangnantea.mapper.UserAddressMapper;
import com.shangnantea.mapper.UserFavoriteMapper;
import com.shangnantea.mapper.UserFollowMapper;
import com.shangnantea.mapper.UserLikeMapper;
import com.shangnantea.mapper.UserMapper;
import com.shangnantea.model.dto.AddFavoriteDTO;
import com.shangnantea.model.dto.AddFollowDTO;
import com.shangnantea.model.dto.AddLikeDTO;
import com.shangnantea.model.dto.ChangePasswordDTO;
import com.shangnantea.model.dto.LoginDTO;
import com.shangnantea.model.dto.RegisterDTO;
import com.shangnantea.model.dto.SubmitShopCertificationDTO;
import com.shangnantea.model.entity.shop.ShopCertification;
import com.shangnantea.model.entity.user.User;
import com.shangnantea.model.entity.user.UserAddress;
import com.shangnantea.model.entity.user.UserFavorite;
import com.shangnantea.model.entity.user.UserFollow;
import com.shangnantea.model.entity.user.UserLike;
import com.shangnantea.model.vo.user.AddressVO;
import com.shangnantea.model.vo.user.CertificationStatusVO;
import com.shangnantea.model.vo.user.FavoriteVO;
import com.shangnantea.model.vo.user.FollowVO;
import com.shangnantea.model.vo.user.LikeVO;
import com.shangnantea.model.vo.user.TokenVO;
import com.shangnantea.model.vo.user.UserVO;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.security.util.JwtUtil;
import com.shangnantea.security.util.PasswordEncoder;
import com.shangnantea.service.UserService;
import com.shangnantea.utils.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private UserAddressMapper userAddressMapper;
    
    @Autowired
    private ShopCertificationMapper shopCertificationMapper;
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    @Autowired
    private UserFavoriteMapper userFavoriteMapper;
    
    @Autowired
    private UserLikeMapper userLikeMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    // ==================== 认证相关 ====================
    
    /**
     * 用户登录
     * 成功码：2000，失败码：2100, 2101
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
            return Result.failure(2100); // 登录失败，请检查用户名和密码
        }
        
        // 生成JWT token
        String token = jwtUtil.generateToken(user);
        if (token == null) {
            logger.error("登录失败: Token生成失败, username: {}", loginDTO.getUsername());
            return Result.failure(2101); // 登录失败，服务器Token生成失败
        }
        
        // 构建TokenVO
        TokenVO tokenVO = new TokenVO();
        tokenVO.setToken(token);
        tokenVO.setUserInfo(convertToUserVO(user));
        
        logger.info("登录成功: username: {}, userId: {}", loginDTO.getUsername(), user.getId());
        return Result.success(2000, tokenVO); // 登录成功
    }
    
    /**
     * 用户注册
     * 成功码：2001，失败码：2102
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<UserVO> register(RegisterDTO registerDTO) {
        logger.info("用户注册请求: {}", registerDTO.getUsername());
        
        // 检查用户名是否已存在
        if (isUserExist(registerDTO.getUsername())) {
            logger.warn("注册失败: 用户名已存在, username: {}", registerDTO.getUsername());
            return Result.failure(2102); // 注册失败，用户名已存在或数据格式错误
        }
        
        // 验证密码和确认密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            logger.warn("注册失败: 两次输入的密码不一致, username: {}", registerDTO.getUsername());
            return Result.failure(2102); // 注册失败，用户名已存在或数据格式错误
        }
        
        // 将RegisterDTO转换为User实体
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        
        // 生成用户ID，格式为 'cy' + 6位数字
        String userId = generateUserId();
        user.setId(userId);
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认值
        user.setRole(2); // 默认为普通用户
        user.setStatus(1); // 默认为正常状态
        user.setIsDeleted(0); // 默认为未删除
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        
        // 保存用户到数据库
        int result = userMapper.insert(user);
        if (result <= 0) {
            logger.error("注册失败: 数据库插入失败, username: {}", registerDTO.getUsername());
            return Result.failure(2102); // 注册失败，用户名已存在或数据格式错误
        }
        
        // 查询刚注册的用户（获取完整信息）
        User savedUser = getUserEntityById(userId);
        if (savedUser == null) {
            logger.error("注册失败: 无法获取注册后的用户信息, userId: {}", userId);
            return Result.failure(2102); // 注册失败，用户名已存在或数据格式错误
        }
        
        logger.info("用户注册成功: username: {}, userId: {}", registerDTO.getUsername(), userId);
        return Result.success(2001, convertToUserVO(savedUser)); // 注册成功，请登录
    }
    
    /**
     * 用户登出
     * 成功码：2002，失败码：2103
     */
    @Override
    public Result<Void> logout(HttpServletRequest request) {
        try {
            // 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            String username = UserContext.getCurrentUsername();
            
            if (userId == null) {
                logger.warn("登出失败: 用户未登录");
                return Result.failure(2103); // 退出登录失败
            }
            
            // 记录登出日志
            logger.info("用户登出: userId: {}, username: {}", userId, username);
            
            // 清除用户上下文（虽然拦截器会自动清除，但显式清除更安全）
            UserContext.clear();
            
            logger.info("用户登出成功: userId: {}, username: {}", userId, username);
            return Result.success(2002); // 已安全退出系统
            
        } catch (Exception e) {
            logger.error("登出失败: 系统异常", e);
            // 即使发生异常，也清除用户上下文
            UserContext.clear();
            return Result.failure(2103); // 退出登录失败
        }
    }
    
    /**
     * 刷新令牌
     * 成功码：200，失败码：2105, 2106
     */
    @Override
    public Result<TokenVO> refreshToken(HttpServletRequest request) {
        try {
            // 从UserContext获取当前用户ID（@RequiresLogin注解已验证token）
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("刷新令牌失败: 用户未登录");
                return Result.failure(2106); // 您的登录已过期，请重新登录
            }
            
            // 查询用户信息
            User user = getUserEntityById(userId);
            if (user == null) {
                logger.warn("刷新令牌失败: 用户不存在, userId: {}", userId);
                return Result.failure(2105); // 刷新令牌失败
            }
            
            // 检查用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                logger.warn("刷新令牌失败: 用户已被禁用, userId: {}", userId);
                return Result.failure(2105); // 刷新令牌失败
            }
            
            // 生成新的JWT token
            String newToken = jwtUtil.generateToken(user);
            if (newToken == null) {
                logger.error("刷新令牌失败: Token生成失败, userId: {}", userId);
                return Result.failure(2105); // 刷新令牌失败
            }
            
            // 构建TokenVO
            TokenVO tokenVO = new TokenVO();
            tokenVO.setToken(newToken);
            tokenVO.setUserInfo(convertToUserVO(user));
            
            logger.info("刷新令牌成功: userId: {}, username: {}", userId, user.getUsername());
            return Result.success(200, tokenVO); // 刷新成功（静默）
            
        } catch (Exception e) {
            logger.error("刷新令牌失败: 系统异常", e);
            return Result.failure(2105); // 刷新令牌失败
        }
    }
    
    // ==================== 用户信息管理 ====================
    
    /**
     * 获取当前用户信息
     * 成功码：200，失败码：2104
     */
    @Override
    public Result<UserVO> getCurrentUser() {
        try {
            // 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取当前用户信息失败: 用户未登录");
                return Result.failure(2104); // 获取当前用户信息失败
            }
            
            // 查询用户信息
            User user = getUserEntityById(userId);
            if (user == null) {
                logger.warn("获取当前用户信息失败: 用户不存在, userId: {}", userId);
                return Result.failure(2104); // 获取当前用户信息失败
            }
            
            // 转换为VO并返回
            logger.info("获取当前用户信息成功: userId: {}, username: {}", userId, user.getUsername());
            return Result.success(200, convertToUserVO(user)); // 操作成功（静默）
            
        } catch (Exception e) {
            logger.error("获取当前用户信息失败: 系统异常", e);
            return Result.failure(2104); // 获取当前用户信息失败
        }
    }
    
    /**
     * 根据用户ID获取用户信息
     * 成功码：200，失败码：2107
     */
    @Override
    public Result<UserVO> getUserById(String userId) {
        try {
            // 验证参数
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("获取用户信息失败: 用户ID为空");
                return Result.failure(2107); // 获取用户信息失败
            }
            
            // 查询用户信息
            User user = getUserEntityById(userId);
            if (user == null) {
                logger.warn("获取用户信息失败: 用户不存在, userId: {}", userId);
                return Result.failure(2107); // 获取用户信息失败
            }
            
            // 转换为VO并返回（只返回公开信息，不包含敏感数据）
            logger.info("获取用户信息成功: userId: {}, username: {}", userId, user.getUsername());
            return Result.success(200, convertToUserVO(user)); // 操作成功（静默）
            
        } catch (Exception e) {
            logger.error("获取用户信息失败: 系统异常, userId: {}", userId, e);
            return Result.failure(2107); // 获取用户信息失败
        }
    }
    
    /**
     * 更新用户信息
     * 成功码：2003，失败码：2108
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<UserVO> updateUserInfo(Map<String, Object> userData) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("更新用户信息失败: 用户未登录");
                return Result.failure(2108); // 个人资料更新失败
            }
            
            // 2. 查询用户信息
            User user = getUserEntityById(userId);
            if (user == null) {
                logger.warn("更新用户信息失败: 用户不存在, userId: {}", userId);
                return Result.failure(2108); // 个人资料更新失败
            }
            
            // 3. 更新允许修改的字段
            boolean hasUpdate = false;
            
            if (userData.containsKey("nickname")) {
                user.setNickname((String) userData.get("nickname"));
                hasUpdate = true;
            }
            
            if (userData.containsKey("email")) {
                user.setEmail((String) userData.get("email"));
                hasUpdate = true;
            }
            
            if (userData.containsKey("phone")) {
                user.setPhone((String) userData.get("phone"));
                hasUpdate = true;
            }
            
            // 4. 如果没有任何更新，直接返回成功
            if (!hasUpdate) {
                logger.info("更新用户信息: 无需更新, userId: {}", userId);
                return Result.success(2003, convertToUserVO(user)); // 个人资料更新成功
            }
            
            // 5. 执行更新
            int rows = userMapper.update(user);
            if (rows <= 0) {
                logger.error("更新用户信息失败: 数据库更新失败, userId: {}", userId);
                return Result.failure(2108); // 个人资料更新失败
            }
            
            // 6. 查询更新后的用户信息
            User updatedUser = getUserEntityById(userId);
            
            logger.info("更新用户信息成功: userId: {}, username: {}", userId, user.getUsername());
            return Result.success(2003, convertToUserVO(updatedUser)); // 个人资料更新成功
            
        } catch (Exception e) {
            logger.error("更新用户信息失败: 系统异常", e);
            return Result.failure(2108); // 个人资料更新失败
        }
    }
    
    /**
     * 上传头像
     * 成功码：2004，失败码：2109, 2110, 2111
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> uploadAvatar(MultipartFile file) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("头像上传失败: 用户未登录");
                return Result.failure(2109); // 头像更新失败
            }
            
            // 2. 验证用户是否存在
            User user = getUserEntityById(userId);
            if (user == null) {
                logger.warn("头像上传失败: 用户不存在, userId: {}", userId);
                return Result.failure(2109); // 头像更新失败
            }
            
            // 3. 调用工具类上传文件（硬编码type为"avatars"）
            String relativePath = FileUploadUtils.uploadImage(file, "avatars");
            
            // 4. 生成访问URL
            String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
            
            // 5. 更新数据库中的头像字段
            int result = userMapper.updateAvatar(userId, relativePath);
            if (result <= 0) {
                logger.error("头像上传失败: 数据库更新失败, userId: {}", userId);
                return Result.failure(2110); // 头像更新失败
            }
            
            // 6. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", accessUrl);
            responseData.put("path", relativePath);
            
            logger.info("头像上传成功: userId: {}, path: {}", userId, relativePath);
            return Result.success(2004, responseData); // 头像更新成功
            
        } catch (Exception e) {
            logger.error("头像上传失败: 系统异常", e);
            return Result.failure(2111); // 头像更新失败
        }
    }
    
    // ==================== 密码管理 ====================
    
    /**
     * 修改密码
     * 成功码：2005，失败码：2112, 2113
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> changePassword(ChangePasswordDTO changePasswordDTO) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("修改密码失败: 用户未登录");
                return Result.failure(2112); // 密码修改失败，请检查原密码是否正确
            }
            
            // 2. 验证新密码和确认密码是否一致
            if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmNewPassword())) {
                logger.warn("修改密码失败: 两次输入的密码不一致, userId: {}", userId);
                return Result.failure(2113); // 两次输入的密码不一致
            }
            
            // 3. 调用changeUserPassword方法修改密码
            boolean success = changeUserPassword(
                userId, 
                changePasswordDTO.getOldPassword(), 
                changePasswordDTO.getNewPassword()
            );
            
            if (!success) {
                logger.warn("修改密码失败: 旧密码错误, userId: {}", userId);
                return Result.failure(2112); // 密码修改失败，请检查原密码是否正确
            }
            
            logger.info("修改密码成功: userId: {}", userId);
            return Result.success(2005, "密码修改成功，请使用新密码登录"); // 密码修改成功，请使用新密码登录
            
        } catch (Exception e) {
            logger.error("修改密码失败: 系统异常", e);
            return Result.failure(2112); // 密码修改失败，请检查原密码是否正确
        }
    }
    
    /**
     * 密码找回/重置
     * 成功码：2006，失败码：2114
     * 注意：简化实现，通过用户名+手机号验证身份（实际项目应使用验证码）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> resetPassword(Map<String, Object> resetData) {
        try {
            // 1. 获取参数
            String username = (String) resetData.get("username");
            String phone = (String) resetData.get("phone");
            String newPassword = (String) resetData.get("newPassword");
            
            // 2. 参数验证
            if (username == null || username.trim().isEmpty()) {
                logger.warn("密码重置失败: 用户名不能为空");
                return Result.failure(2114); // 密码重置失败
            }
            
            if (phone == null || phone.trim().isEmpty()) {
                logger.warn("密码重置失败: 手机号不能为空");
                return Result.failure(2114); // 密码重置失败
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                logger.warn("密码重置失败: 新密码不能为空");
                return Result.failure(2114); // 密码重置失败
            }
            
            // 3. 验证用户名和手机号是否匹配
            User user = getUserByUsername(username);
            if (user == null) {
                logger.warn("密码重置失败: 用户不存在, username: {}", username);
                return Result.failure(2114); // 密码重置失败
            }
            
            if (!phone.equals(user.getPhone())) {
                logger.warn("密码重置失败: 手机号不匹配, username: {}", username);
                return Result.failure(2114); // 密码重置失败
            }
            
            // 4. 加密新密码并更新
            String encodedPassword = passwordEncoder.encode(newPassword);
            int result = userMapper.updatePassword(user.getId(), encodedPassword);
            
            if (result <= 0) {
                logger.error("密码重置失败: 数据库更新失败, username: {}", username);
                return Result.failure(2114); // 密码重置失败
            }
            
            logger.info("密码重置成功: username: {}, userId: {}", username, user.getId());
            return Result.success(2006, "密码重置成功"); // 密码重置成功
            
        } catch (Exception e) {
            logger.error("密码重置失败: 系统异常", e);
            return Result.failure(2114); // 密码重置失败
        }
    }
    
    // ==================== 收货地址管理 ====================
    
    /**
     * 获取当前用户的地址列表
     * 成功码：200，失败码：2115
     */
    @Override
    public Result<Object> getAddressList() {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取地址列表失败: 用户未登录");
                return Result.failure(2115); // 获取地址列表失败
            }
            
            // 2. 查询用户的所有地址（已按默认地址排序）
            List<UserAddress> addressList = userAddressMapper.selectByUserId(userId);
            
            // 3. 转换为AddressVO列表
            List<AddressVO> addressVOList = convertToAddressVOList(addressList);
            
            logger.info("获取地址列表成功: userId: {}, count: {}", userId, addressVOList.size());
            return Result.success(200, addressVOList); // 操作成功（静默）
            
        } catch (Exception e) {
            logger.error("获取地址列表失败: 系统异常", e);
            return Result.failure(2115); // 获取地址列表失败
        }
    }
    
    /**
     * 添加收货地址
     * 成功码：2007，失败码：2116
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> addAddress(Map<String, Object> addressData) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("添加地址失败: 用户未登录");
                return Result.failure(2116); // 保存地址失败
            }
            
            // 2. 构建UserAddress实体
            UserAddress address = new UserAddress();
            address.setUserId(userId);
            address.setReceiverName((String) addressData.get("receiverName"));
            address.setReceiverPhone((String) addressData.get("receiverPhone"));
            address.setProvince((String) addressData.get("province"));
            address.setCity((String) addressData.get("city"));
            address.setDistrict((String) addressData.get("district"));
            address.setDetailAddress((String) addressData.get("detailAddress"));
            
            // 3. 处理默认地址逻辑
            Integer isDefault = addressData.get("isDefault") != null ? 
                (Integer) addressData.get("isDefault") : 0;
            
            if (isDefault == 1) {
                // 如果设置为默认地址，先将该用户的其他地址设为非默认
                userAddressMapper.resetDefaultByUserId(userId);
            }
            address.setIsDefault(isDefault);
            
            // 4. 设置时间戳
            Date now = new Date();
            address.setCreateTime(now);
            address.setUpdateTime(now);
            
            // 5. 插入数据库
            int result = userAddressMapper.insert(address);
            if (result <= 0) {
                logger.error("添加地址失败: 数据库插入失败, userId: {}", userId);
                return Result.failure(2116); // 保存地址失败
            }
            
            // 6. 转换为VO并返回
            AddressVO addressVO = convertToAddressVO(address);
            
            logger.info("添加地址成功: userId: {}, addressId: {}", userId, address.getId());
            return Result.success(2007, addressVO); // 地址添加成功
            
        } catch (Exception e) {
            logger.error("添加地址失败: 系统异常", e);
            return Result.failure(2116); // 保存地址失败
        }
    }
    
    /**
     * 更新收货地址
     * 成功码：2008，失败码：2117
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateAddress(String id, Map<String, Object> addressData) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("更新地址失败: 用户未登录");
                return Result.failure(2117); // 保存地址失败
            }
            
            // 2. 验证地址ID
            Integer addressId;
            try {
                addressId = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                logger.warn("更新地址失败: 地址ID格式错误, id: {}", id);
                return Result.failure(2117); // 保存地址失败
            }
            
            // 3. 查询地址是否存在
            UserAddress existingAddress = userAddressMapper.selectById(addressId);
            if (existingAddress == null) {
                logger.warn("更新地址失败: 地址不存在, addressId: {}", addressId);
                return Result.failure(2117); // 保存地址失败
            }
            
            // 4. 验证用户是否有权限修改该地址
            if (!userId.equals(existingAddress.getUserId())) {
                logger.warn("更新地址失败: 无权限修改该地址, userId: {}, addressUserId: {}", 
                    userId, existingAddress.getUserId());
                return Result.failure(2117); // 保存地址失败
            }
            
            // 5. 更新地址信息
            if (addressData.containsKey("receiverName")) {
                existingAddress.setReceiverName((String) addressData.get("receiverName"));
            }
            if (addressData.containsKey("receiverPhone")) {
                existingAddress.setReceiverPhone((String) addressData.get("receiverPhone"));
            }
            if (addressData.containsKey("province")) {
                existingAddress.setProvince((String) addressData.get("province"));
            }
            if (addressData.containsKey("city")) {
                existingAddress.setCity((String) addressData.get("city"));
            }
            if (addressData.containsKey("district")) {
                existingAddress.setDistrict((String) addressData.get("district"));
            }
            if (addressData.containsKey("detailAddress")) {
                existingAddress.setDetailAddress((String) addressData.get("detailAddress"));
            }
            
            // 6. 处理默认地址逻辑
            if (addressData.containsKey("isDefault")) {
                Integer isDefault = (Integer) addressData.get("isDefault");
                if (isDefault == 1 && existingAddress.getIsDefault() != 1) {
                    // 如果要设置为默认地址，先将该用户的其他地址设为非默认
                    userAddressMapper.resetDefaultByUserId(userId);
                }
                existingAddress.setIsDefault(isDefault);
            }
            
            // 7. 更新时间戳
            existingAddress.setUpdateTime(new Date());
            
            // 8. 执行更新
            int result = userAddressMapper.updateById(existingAddress);
            if (result <= 0) {
                logger.error("更新地址失败: 数据库更新失败, addressId: {}", addressId);
                return Result.failure(2117); // 保存地址失败
            }
            
            logger.info("更新地址成功: userId: {}, addressId: {}", userId, addressId);
            return Result.success(2008, true); // 更新成功
            
        } catch (Exception e) {
            logger.error("更新地址失败: 系统异常", e);
            return Result.failure(2117); // 保存地址失败
        }
    }
    
    /**
     * 删除收货地址
     * 成功码：2009，失败码：2118
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteAddress(String id) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("删除地址失败: 用户未登录");
                return Result.failure(2118); // 操作失败
            }
            
            // 2. 验证地址ID
            Integer addressId;
            try {
                addressId = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                logger.warn("删除地址失败: 地址ID格式错误, id: {}", id);
                return Result.failure(2118); // 操作失败
            }
            
            // 3. 查询地址是否存在
            UserAddress existingAddress = userAddressMapper.selectById(addressId);
            if (existingAddress == null) {
                logger.warn("删除地址失败: 地址不存在, addressId: {}", addressId);
                return Result.failure(2118); // 操作失败
            }
            
            // 4. 验证用户是否有权限删除该地址
            if (!userId.equals(existingAddress.getUserId())) {
                logger.warn("删除地址失败: 无权限删除该地址, userId: {}, addressUserId: {}", 
                    userId, existingAddress.getUserId());
                return Result.failure(2118); // 操作失败
            }
            
            // 5. 执行删除
            int result = userAddressMapper.deleteById(addressId);
            if (result <= 0) {
                logger.error("删除地址失败: 数据库删除失败, addressId: {}", addressId);
                return Result.failure(2118); // 操作失败
            }
            
            logger.info("删除地址成功: userId: {}, addressId: {}", userId, addressId);
            return Result.success(2009, true); // 删除成功
            
        } catch (Exception e) {
            logger.error("删除地址失败: 系统异常", e);
            return Result.failure(2118); // 操作失败
        }
    }
    
    /**
     * 设置默认地址
     * 成功码：2010，失败码：2119
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> setDefaultAddress(String id) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("设置默认地址失败: 用户未登录");
                return Result.failure(2119); // 操作失败
            }
            
            // 2. 验证地址ID
            Integer addressId;
            try {
                addressId = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                logger.warn("设置默认地址失败: 地址ID格式错误, id: {}", id);
                return Result.failure(2119); // 操作失败
            }
            
            // 3. 查询地址是否存在
            UserAddress existingAddress = userAddressMapper.selectById(addressId);
            if (existingAddress == null) {
                logger.warn("设置默认地址失败: 地址不存在, addressId: {}", addressId);
                return Result.failure(2119); // 操作失败
            }
            
            // 4. 验证用户是否有权限操作该地址
            if (!userId.equals(existingAddress.getUserId())) {
                logger.warn("设置默认地址失败: 无权限操作该地址, userId: {}, addressUserId: {}", 
                    userId, existingAddress.getUserId());
                return Result.failure(2119); // 操作失败
            }
            
            // 5. 如果该地址已经是默认地址，直接返回成功
            if (existingAddress.getIsDefault() != null && existingAddress.getIsDefault() == 1) {
                logger.info("地址已是默认地址: userId: {}, addressId: {}", userId, addressId);
                return Result.success(2010, true); // 更新成功
            }
            
            // 6. 先将该用户的所有地址设为非默认
            userAddressMapper.resetDefaultByUserId(userId);
            
            // 7. 设置当前地址为默认
            existingAddress.setIsDefault(1);
            existingAddress.setUpdateTime(new Date());
            int result = userAddressMapper.updateById(existingAddress);
            
            if (result <= 0) {
                logger.error("设置默认地址失败: 数据库更新失败, addressId: {}", addressId);
                return Result.failure(2119); // 操作失败
            }
            
            logger.info("设置默认地址成功: userId: {}, addressId: {}", userId, addressId);
            return Result.success(2010, true); // 更新成功
            
        } catch (Exception e) {
            logger.error("设置默认地址失败: 系统异常", e);
            return Result.failure(2119); // 操作失败
        }
    }
    
    // ==================== 商家认证 ====================
    
    /**
     * 获取商家认证状态
     * 成功码：200，失败码：2121
     */
    @Override
    public Result<Object> getShopCertificationStatus() {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取认证状态失败: 用户未登录");
                return Result.failure(2121); // 加载失败
            }
            
            // 2. 查询用户的认证信息
            ShopCertification certification = shopCertificationMapper.selectByUserId(userId);
            
            // 3. 如果没有认证记录，返回null
            if (certification == null) {
                logger.info("用户暂无认证记录: userId: {}", userId);
                return Result.success(200, null); // 操作成功（静默）
            }
            
            // 4. 转换为VO并返回
            CertificationStatusVO certificationVO = convertToCertificationStatusVO(certification);
            
            logger.info("获取认证状态成功: userId: {}, status: {}", userId, certification.getStatus());
            return Result.success(200, certificationVO); // 操作成功（静默）
            
        } catch (Exception e) {
            logger.error("获取认证状态失败: 系统异常", e);
            return Result.failure(2121); // 加载失败
        }
    }
    
    /**
     * 提交商家认证申请
     * 成功码：2011，失败码：2120
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> submitShopCertification(SubmitShopCertificationDTO certificationDTO) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("提交认证失败: 用户未登录");
                return Result.failure(2120); // 操作失败
            }
            
            // 2. 检查是否已有待审核或已通过的认证
            ShopCertification existingCert = shopCertificationMapper.selectByUserId(userId);
            if (existingCert != null) {
                if (existingCert.getStatus() == 0) {
                    logger.warn("提交认证失败: 已有待审核的认证申请, userId: {}", userId);
                    return Result.failure(2120); // 操作失败
                }
                if (existingCert.getStatus() == 1) {
                    logger.warn("提交认证失败: 已通过认证, userId: {}", userId);
                    return Result.failure(2120); // 操作失败
                }
            }
            
            // 3. 构建认证实体
            ShopCertification certification = new ShopCertification();
            certification.setUserId(userId);
            certification.setShopName(certificationDTO.getShopName());
            certification.setBusinessLicense(certificationDTO.getBusinessLicense());
            certification.setIdCardFront(certificationDTO.getIdCardFront());
            certification.setIdCardBack(certificationDTO.getIdCardBack());
            certification.setRealName(certificationDTO.getRealName());
            certification.setIdCard(certificationDTO.getIdCard());
            certification.setContactPhone(certificationDTO.getContactPhone());
            certification.setProvince(certificationDTO.getProvince());
            certification.setCity(certificationDTO.getCity());
            certification.setDistrict(certificationDTO.getDistrict());
            certification.setAddress(certificationDTO.getAddress());
            certification.setApplyReason(certificationDTO.getApplyReason());
            
            // 4. 设置状态为待审核
            certification.setStatus(0);
            certification.setCreateTime(new Date());
            certification.setUpdateTime(new Date());
            
            // 5. 插入数据库
            int result = shopCertificationMapper.insert(certification);
            if (result <= 0) {
                logger.error("提交认证失败: 数据库插入失败, userId: {}", userId);
                return Result.failure(2120); // 操作失败
            }
            
            logger.info("提交认证成功: userId: {}, certificationId: {}", userId, certification.getId());
            return Result.success(2011, true); // 操作成功
            
        } catch (Exception e) {
            logger.error("提交认证失败: 系统异常", e);
            return Result.failure(2120); // 操作失败
        }
    }
    
    @Override
    public Result<Map<String, Object>> uploadCertificationImage(MultipartFile file, String type) {
        // TODO: 实现上传认证图片逻辑
        return Result.success(2024);
    }
    
    // ==================== 用户互动功能 ====================
    
    /**
     * 获取关注列表
     * 成功码：200，失败码：2122
     */
    @Override
    public Result<Object> getFollowList(String type) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取关注列表失败: 用户未登录");
                return Result.failure(2122); // 加载失败
            }
            
            // 2. 查询关注列表
            List<UserFollow> followList = userFollowMapper.selectByUserId(userId, type);
            
            // 3. 转换为VO列表
            List<FollowVO> followVOList = convertToFollowVOList(followList);
            
            logger.info("获取关注列表成功: userId: {}, type: {}, count: {}", userId, type, followVOList.size());
            return Result.success(200, followVOList); // 操作成功（静默）
            
        } catch (Exception e) {
            logger.error("获取关注列表失败: 系统异常", e);
            return Result.failure(2122); // 加载失败
        }
    }
    
    /**
     * 添加关注
     * 成功码：2012，失败码：2123
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> addFollow(AddFollowDTO followDTO) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("添加关注失败: 用户未登录");
                return Result.failure(2123); // 操作失败
            }
            
            // 2. 获取参数
            String targetId = followDTO.getTargetId();
            String targetType = followDTO.getTargetType();
            
            // 3. 检查是否已关注
            UserFollow existingFollow = userFollowMapper.selectByUserIdAndFollowId(userId, targetId, targetType);
            if (existingFollow != null) {
                logger.warn("添加关注失败: 已关注该对象, userId: {}, targetId: {}", userId, targetId);
                return Result.failure(2123); // 操作失败
            }
            
            // 4. 构建关注实体
            UserFollow follow = new UserFollow();
            follow.setUserId(userId);
            follow.setFollowId(targetId);
            follow.setFollowType(targetType);
            follow.setTargetName(followDTO.getTargetName());
            follow.setTargetAvatar(followDTO.getTargetAvatar());
            follow.setCreateTime(new Date());
            
            // 5. 插入数据库
            int result = userFollowMapper.insert(follow);
            if (result <= 0) {
                logger.error("添加关注失败: 数据库插入失败, userId: {}", userId);
                return Result.failure(2123); // 操作失败
            }
            
            logger.info("添加关注成功: userId: {}, targetId: {}, targetType: {}", userId, targetId, targetType);
            return Result.success(2012, true); // 已关注店铺
            
        } catch (Exception e) {
            logger.error("添加关注失败: 系统异常", e);
            return Result.failure(2123); // 操作失败
        }
    }
    
    /**
     * 取消关注
     * 成功码：2013，失败码：2124
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> removeFollow(String id) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("取消关注失败: 用户未登录");
                return Result.failure(2124); // 操作失败
            }
            
            // 2. 验证关注ID
            Integer followId;
            try {
                followId = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                logger.warn("取消关注失败: 关注ID格式错误, id: {}", id);
                return Result.failure(2124); // 操作失败
            }
            
            // 3. 查询关注记录是否存在
            UserFollow existingFollow = userFollowMapper.selectById(followId);
            if (existingFollow == null) {
                logger.warn("取消关注失败: 关注记录不存在, followId: {}", followId);
                return Result.failure(2124); // 操作失败
            }
            
            // 4. 验证用户是否有权限删除该关注
            if (!userId.equals(existingFollow.getUserId())) {
                logger.warn("取消关注失败: 无权限删除该关注, userId: {}, followUserId: {}", 
                    userId, existingFollow.getUserId());
                return Result.failure(2124); // 操作失败
            }
            
            // 5. 执行删除
            int result = userFollowMapper.deleteById(followId);
            if (result <= 0) {
                logger.error("取消关注失败: 数据库删除失败, followId: {}", followId);
                return Result.failure(2124); // 操作失败
            }
            
            logger.info("取消关注成功: userId: {}, followId: {}", userId, followId);
            return Result.success(2013, true); // 已取消关注
            
        } catch (Exception e) {
            logger.error("取消关注失败: 系统异常", e);
            return Result.failure(2124); // 操作失败
        }
    }
    
    @Override
    public Result<Object> getFavoriteList(String type) {
        // TODO: 实现获取收藏列表逻辑
        return Result.success(200);
    }
    
    @Override
    public Result<Boolean> addFavorite(Map<String, Object> favoriteData) {
        // TODO: 实现添加收藏逻辑
        return Result.success(3010, true);
    }
    
    @Override
    public Result<Boolean> removeFavorite(String id) {
        // TODO: 实现取消收藏逻辑
        return Result.success(3011, true);
    }
    
    @Override
    public Result<Boolean> addLike(Map<String, Object> likeData) {
        // TODO: 实现点赞逻辑
        return Result.success(6010, true);
    }
    
    @Override
    public Result<Boolean> removeLike(String id) {
        // TODO: 实现取消点赞逻辑
        return Result.success(6011, true);
    }
    
    // ==================== 用户偏好设置 ====================
    
    @Override
    public Result<Object> getUserPreferences() {
        // TODO: 实现获取偏好设置逻辑
        return Result.success(200);
    }
    
    @Override
    public Result<Object> updateUserPreferences(Map<String, Object> preferences) {
        // TODO: 实现更新偏好设置逻辑
        return Result.success(2013);
    }
    
    // ==================== 管理员功能 ====================
    
    @Override
    public Result<Object> getAdminUserList(String keyword, Integer role, Integer status, Integer page, Integer pageSize) {
        // TODO: 实现获取用户列表逻辑
        return Result.success(200);
    }
    
    @Override
    public Result<Boolean> createAdmin(Map<String, Object> adminData) {
        // TODO: 实现创建管理员逻辑
        return Result.success(2023, true);
    }
    
    @Override
    public Result<Boolean> updateUser(String userId, Map<String, Object> userData) {
        // TODO: 实现更新用户逻辑
        return Result.success(2022, true);
    }
    
    @Override
    public Result<Boolean> deleteUser(String userId) {
        // TODO: 实现删除用户逻辑
        return Result.success(2020, true);
    }
    
    @Override
    public Result<Boolean> toggleUserStatus(String userId, Map<String, Object> statusData) {
        // TODO: 实现切换用户状态逻辑
        return Result.success(2021, true);
    }
    
    @Override
    public Result<Object> getCertificationList(Integer status, Integer page, Integer pageSize) {
        // TODO: 实现获取认证列表逻辑
        return Result.success(200);
    }
    
    @Override
    public Result<Boolean> processCertification(Integer id, Map<String, Object> auditData) {
        // TODO: 实现审核认证逻辑
        return Result.success(1000, true);
    }
    
    // ==================== 基础方法（内部使用） ====================
    
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
    
    /**
     * 将UserAddress实体列表转换为AddressVO列表
     */
    private List<AddressVO> convertToAddressVOList(List<UserAddress> addressList) {
        List<AddressVO> addressVOList = new ArrayList<>();
        if (addressList == null || addressList.isEmpty()) {
            return addressVOList;
        }
        
        for (UserAddress address : addressList) {
            addressVOList.add(convertToAddressVO(address));
        }
        
        return addressVOList;
    }
    
    /**
     * 将UserAddress实体转换为AddressVO
     */
    private AddressVO convertToAddressVO(UserAddress address) {
        if (address == null) {
            return null;
        }
        
        AddressVO addressVO = new AddressVO();
        addressVO.setId(address.getId());
        addressVO.setReceiverName(address.getReceiverName());
        addressVO.setReceiverPhone(address.getReceiverPhone());
        addressVO.setProvince(address.getProvince());
        addressVO.setCity(address.getCity());
        addressVO.setDistrict(address.getDistrict());
        addressVO.setDetailAddress(address.getDetailAddress());
        addressVO.setIsDefault(address.getIsDefault());
        
        return addressVO;
    }
    
    /**
     * 将ShopCertification实体转换为CertificationStatusVO
     */
    private CertificationStatusVO convertToCertificationStatusVO(ShopCertification certification) {
        if (certification == null) {
            return null;
        }
        
        CertificationStatusVO certificationVO = new CertificationStatusVO();
        certificationVO.setId(certification.getId());
        certificationVO.setUserId(certification.getUserId());
        certificationVO.setShopName(certification.getShopName());
        certificationVO.setBusinessLicense(certification.getBusinessLicense());
        certificationVO.setIdCardFront(certification.getIdCardFront());
        certificationVO.setIdCardBack(certification.getIdCardBack());
        certificationVO.setStatus(certification.getStatus());
        certificationVO.setRejectReason(certification.getRejectReason());
        certificationVO.setCreateTime(certification.getCreateTime());
        certificationVO.setUpdateTime(certification.getUpdateTime());
        
        return certificationVO;
    }
    
    /**
     * 将UserFollow实体列表转换为FollowVO列表
     */
    private List<FollowVO> convertToFollowVOList(List<UserFollow> followList) {
        List<FollowVO> followVOList = new ArrayList<>();
        if (followList == null || followList.isEmpty()) {
            return followVOList;
        }
        
        for (UserFollow follow : followList) {
            FollowVO followVO = new FollowVO();
            followVO.setId(follow.getId());
            followVO.setTargetId(follow.getFollowId());
            followVO.setTargetType(follow.getFollowType());
            followVO.setTargetName(follow.getTargetName());
            followVO.setTargetAvatar(follow.getTargetAvatar());
            followVO.setCreateTime(follow.getCreateTime());
            followVOList.add(followVO);
        }
        
        return followVOList;
    }
    
    @Override
    public User getUserEntityById(String id) {
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
        if (getUserEntityById(userId) != null) {
            return generateUserId(); // 递归调用直到生成唯一ID
        }
        
        return userId;
    }
    
    @Override
    public boolean updateUserEntity(User user) {
        if (user.getId() == null) {
            return false;
        }
        
        User existingUser = getUserEntityById(user.getId());
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
    public boolean changeUserPassword(String userId, String oldPassword, String newPassword) {
        User user = getUserEntityById(userId);
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
    public boolean deleteUserEntity(String id) {
        User user = getUserEntityById(id);
        if (user == null) {
            return false;
        }
        
        // 删除用户（软删除）
        int result = userMapper.delete(id);
        
        logger.info("用户删除结果: {}, 用户名: {}", result > 0, user.getUsername());
        return result > 0;
    }
} 