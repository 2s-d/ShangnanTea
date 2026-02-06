package com.shangnantea.service.impl;

import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.ShopCertificationMapper;
import com.shangnantea.mapper.ShopMapper;
import com.shangnantea.mapper.TeaMapper;
import com.shangnantea.mapper.ForumPostMapper;
import com.shangnantea.mapper.ForumReplyMapper;
import com.shangnantea.mapper.TeaArticleMapper;
import com.shangnantea.mapper.UserAddressMapper;
import com.shangnantea.mapper.UserFavoriteMapper;
import com.shangnantea.mapper.UserFollowMapper;
import com.shangnantea.mapper.UserLikeMapper;
import com.shangnantea.mapper.UserMapper;
import com.shangnantea.mapper.UserSettingMapper;
import com.shangnantea.model.dto.AddFavoriteDTO;
import com.shangnantea.model.dto.AddFollowDTO;
import com.shangnantea.model.dto.AddLikeDTO;
import com.shangnantea.model.dto.ChangePasswordDTO;
import com.shangnantea.model.dto.CreateAdminDTO;
import com.shangnantea.model.dto.LoginDTO;
import com.shangnantea.model.dto.RegisterDTO;
import com.shangnantea.model.dto.SendVerificationCodeDTO;
import com.shangnantea.model.dto.SubmitShopCertificationDTO;
import com.shangnantea.model.dto.UpdateUserPreferencesDTO;
import com.shangnantea.model.dto.UpdateUserDTO;
import com.shangnantea.model.dto.ProcessCertificationDTO;
import com.shangnantea.model.entity.shop.ShopCertification;
import com.shangnantea.model.entity.user.User;
import com.shangnantea.model.entity.user.UserAddress;
import com.shangnantea.model.entity.user.UserFavorite;
import com.shangnantea.model.entity.user.UserFollow;
import com.shangnantea.model.entity.user.UserLike;
import com.shangnantea.model.entity.user.UserSetting;
import com.shangnantea.model.vo.user.AddressVO;
import com.shangnantea.model.vo.user.CertificationStatusVO;
import com.shangnantea.model.vo.user.FavoriteVO;
import com.shangnantea.model.vo.user.FollowVO;
import com.shangnantea.model.vo.user.LikeVO;
import com.shangnantea.model.vo.user.TokenVO;
import com.shangnantea.model.vo.user.UserPreferencesVO;
import com.shangnantea.model.vo.user.UserVO;
import com.shangnantea.model.vo.user.VerificationCodeVO;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.security.util.JwtUtil;
import com.shangnantea.security.util.PasswordEncoder;
import com.shangnantea.service.UserService;
import com.shangnantea.utils.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
    private UserSettingMapper userSettingMapper;
    
    @Autowired
    private ShopMapper shopMapper;
    
    @Autowired
    private TeaMapper teaMapper;
    
    @Autowired
    private ForumPostMapper forumPostMapper;
    
    @Autowired
    private ForumReplyMapper forumReplyMapper;
    
    @Autowired
    private TeaArticleMapper teaArticleMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String mailFrom;
    
    @Value("${aliyun.sms.enabled:false}")
    private boolean smsEnabled;  // 是否启用真实短信发送
    
    @Value("${aliyun.sms.access-key-id:}")
    private String aliyunAccessKeyId;  // 阿里云 AccessKeyId
    
    @Value("${aliyun.sms.access-key-secret:}")
    private String aliyunAccessKeySecret;  // 阿里云 AccessKeySecret
    
    @Value("${aliyun.sms.sign-name:}")
    private String aliyunSignName;  // 阿里云短信签名
    
    @Value("${aliyun.sms.template-code:}")
    private String aliyunTemplateCode;  // 阿里云短信模板代码
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * JWT 工具类
     * 用于在登录等场景下生成和解析 Token。
     * 注意：此处仅注入 JwtUtil，本身不再被 JwtUtil 反向依赖，已避免循环依赖问题。
     */
    @Autowired
    private JwtUtil jwtUtil;
    
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
        
        // 1. 验证验证码
        String contact = "phone".equals(registerDTO.getContactType()) ? 
            registerDTO.getPhone() : registerDTO.getEmail();
        
        boolean codeValid = verifyCode(contact, "register", registerDTO.getVerificationCode());
        if (!codeValid) {
            logger.warn("注册失败: 验证码错误或已过期, contact: {}", contact);
            return Result.failure(2102); // 注册失败，用户名已存在或数据格式错误
        }
        
        // 2. 检查用户名是否已存在
        if (isUserExist(registerDTO.getUsername())) {
            logger.warn("注册失败: 用户名已存在, username: {}", registerDTO.getUsername());
            return Result.failure(2102); // 注册失败，用户名已存在或数据格式错误
        }
        
        // 3. 验证密码和确认密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            logger.warn("注册失败: 两次输入的密码不一致, username: {}", registerDTO.getUsername());
            return Result.failure(2102); // 注册失败，用户名已存在或数据格式错误
        }
        
        // 4. 将RegisterDTO转换为User实体
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        // 设置昵称（如果提供）
        if (registerDTO.getNickname() != null && !registerDTO.getNickname().trim().isEmpty()) {
            user.setNickname(registerDTO.getNickname().trim());
        } else {
            // 如果没有提供昵称，默认使用用户名
            user.setNickname(registerDTO.getUsername());
        }
        
        // 5. 生成用户ID，格式为 'cy' + 6位数字
        String userId = generateUserId();
        user.setId(userId);
        
        // 6. 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 7. 设置默认值
        user.setRole(2); // 默认为普通用户
        user.setStatus(1); // 默认为正常状态
        user.setIsDeleted(0); // 默认为未删除
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        
        // 8. 保存用户到数据库
        int result = userMapper.insert(user);
        if (result <= 0) {
            logger.error("注册失败: 数据库插入失败, username: {}", registerDTO.getUsername());
            return Result.failure(2102); // 注册失败，用户名已存在或数据格式错误
        }
        
        // 9. 查询刚注册的用户（获取完整信息）
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
            return Result.success(2002, null); // 已安全退出系统
            
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
            
            // 转换为VO
            UserVO userVO = convertToUserVO(user);
            
            // 查询当前用户是否已关注该用户（仅当查询其他用户时）
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId != null && !currentUserId.equals(userId)) {
                try {
                    UserFollow follow = userFollowMapper.selectByFollowerAndFollowed(currentUserId, userId);
                    userVO.setIsFollowed(follow != null);
                } catch (Exception e) {
                    logger.warn("查询用户关注状态失败, targetUserId: {}, currentUserId: {}, 默认设置为未关注", userId, currentUserId, e);
                    userVO.setIsFollowed(false);
                }
            } else {
                // 查询自己或未登录时，不设置 isFollowed（或设置为 null）
                userVO.setIsFollowed(null);
            }
            
            // 返回（只返回公开信息，不包含敏感数据）
            logger.info("获取用户信息成功: userId: {}, username: {}", userId, user.getUsername());
            return Result.success(200, userVO); // 操作成功（静默）
            
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
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> resetPassword(Map<String, Object> resetData) {
        try {
            // 1. 获取参数
            String username = (String) resetData.get("username");
            String phone = (String) resetData.get("phone");
            String newPassword = (String) resetData.get("newPassword");
            String verificationCode = (String) resetData.get("verificationCode");
            
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
            
            if (verificationCode == null || verificationCode.trim().isEmpty()) {
                logger.warn("密码重置失败: 验证码不能为空");
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
            
            // 4. 验证验证码是否正确
            boolean codeValid = verifyCode(phone, "reset_password", verificationCode);
            if (!codeValid) {
                logger.warn("密码重置失败: 验证码错误或已过期, phone: {}", phone);
                return Result.failure(2114); // 密码重置失败
            }
            
            // 5. 加密新密码并更新
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
            
            // 2. 检查是否已有认证记录
            ShopCertification existingCert = shopCertificationMapper.selectByUserId(userId);
            
            if (existingCert != null) {
                // 2.1 如果有待审核的认证申请，不允许重复提交
                if (existingCert.getStatus() == 0) {
                    logger.warn("提交认证失败: 已有待审核的认证申请, userId: {}", userId);
                    return Result.failure(2120); // 操作失败
                }
                
                // 2.2 如果已通过认证，不允许再次提交
                if (existingCert.getStatus() == 1) {
                    logger.warn("提交认证失败: 已通过认证, userId: {}", userId);
                    return Result.failure(2120); // 操作失败
                }
                
                // 2.3 如果之前被拒绝（status=2），允许重新提交，更新现有记录
                if (existingCert.getStatus() == 2) {
                    logger.info("重新提交认证申请: userId: {}, 之前状态: 已拒绝", userId);
                    
                    // 更新现有认证记录
                    existingCert.setShopName(certificationDTO.getShopName());
                    existingCert.setBusinessLicense(certificationDTO.getBusinessLicense());
                    existingCert.setIdCardFront(certificationDTO.getIdCardFront());
                    existingCert.setIdCardBack(certificationDTO.getIdCardBack());
                    existingCert.setRealName(certificationDTO.getRealName());
                    existingCert.setIdCard(certificationDTO.getIdCard());
                    existingCert.setContactPhone(certificationDTO.getContactPhone());
                    existingCert.setProvince(certificationDTO.getProvince());
                    existingCert.setCity(certificationDTO.getCity());
                    existingCert.setDistrict(certificationDTO.getDistrict());
                    existingCert.setAddress(certificationDTO.getAddress());
                    existingCert.setApplyReason(certificationDTO.getApplyReason());
                    
                    // 重置状态为待审核
                    existingCert.setStatus(0);
                    // 清空之前的拒绝原因和审核管理员
                    existingCert.setRejectReason(null);
                    existingCert.setAdminId(null);
                    existingCert.setUpdateTime(new Date());
                    
                    // 更新数据库
                    int result = shopCertificationMapper.update(existingCert);
                    if (result <= 0) {
                        logger.error("重新提交认证失败: 数据库更新失败, userId: {}", userId);
                        return Result.failure(2120); // 操作失败
                    }
                    
                    logger.info("重新提交认证成功: userId: {}, certificationId: {}", userId, existingCert.getId());
                    return Result.success(2011, true); // 操作成功
                }
            }
            
            // 3. 首次提交：构建新的认证实体
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
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("上传认证图片失败: 用户未登录");
                return Result.failure(2146); // 操作失败
            }
            
            // 2. 验证文件类型参数
            if (type == null || type.trim().isEmpty()) {
                logger.warn("上传认证图片失败: 图片类型参数为空, userId: {}", userId);
                return Result.failure(2147); // 操作失败
            }
            
            // 3. 验证type参数值
            if (!type.equals("id_front") && !type.equals("id_back") && !type.equals("business_license")) {
                logger.warn("上传认证图片失败: 图片类型参数不正确, userId: {}, type: {}", userId, type);
                return Result.failure(2147); // 操作失败
            }
            
            // 4. 调用工具类上传文件
            String relativePath = FileUploadUtils.uploadImage(file, "certifications");
            
            // 5. 生成访问URL
            String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
            
            // 6. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", accessUrl);
            responseData.put("path", relativePath);
            
            logger.info("上传认证图片成功: userId: {}, type: {}, path: {}", userId, type, relativePath);
            return Result.success(2024, responseData); // 图片上传成功
            
        } catch (Exception e) {
            logger.error("上传认证图片失败: 系统异常", e);
            return Result.failure(2148); // 操作失败
        }
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
            
            // 3. 验证目标对象是否存在
            if ("shop".equals(targetType)) {
                // 验证店铺是否存在
                if (shopMapper.selectById(targetId) == null) {
                    logger.warn("添加关注失败: 店铺不存在, shopId: {}", targetId);
                    return Result.failure(2123); // 操作失败
                }
            } else if ("user".equals(targetType)) {
                // 验证用户是否存在
                if (getUserEntityById(targetId) == null) {
                    logger.warn("添加关注失败: 用户不存在, userId: {}", targetId);
                    return Result.failure(2123); // 操作失败
                }
            } else {
                logger.warn("添加关注失败: 关注类型不正确, targetType: {}", targetType);
                return Result.failure(2123); // 操作失败
            }
            
            // 4. 检查是否已关注
            UserFollow existingFollow = userFollowMapper.selectByUserIdAndFollowId(userId, targetId, targetType);
            if (existingFollow != null) {
                logger.warn("添加关注失败: 已关注该对象, userId: {}, targetId: {}", userId, targetId);
                return Result.failure(2123); // 操作失败
            }
            
            // 5. 构建关注实体
            UserFollow follow = new UserFollow();
            follow.setUserId(userId);
            follow.setFollowId(targetId);
            follow.setFollowType(targetType);
            follow.setTargetName(followDTO.getTargetName());
            follow.setTargetAvatar(followDTO.getTargetAvatar());
            follow.setCreateTime(new Date());
            
            // 6. 插入数据库
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
    public Result<Boolean> removeFollow(AddFollowDTO followDTO) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("取消关注失败: 用户未登录");
                return Result.failure(2124); // 操作失败
            }
            
            // 2. 验证参数
            if (followDTO == null || followDTO.getTargetId() == null || followDTO.getTargetType() == null) {
                logger.warn("取消关注失败: 参数不完整");
                return Result.failure(2124); // 操作失败
            }
            
            String targetId = followDTO.getTargetId();
            String targetType = followDTO.getTargetType();
            
            // 3. 执行删除（根据 userId, targetType, targetId 删除）
            int result = userFollowMapper.deleteByUserIdAndFollow(userId, targetType, targetId);
            if (result <= 0) {
                logger.warn("取消关注失败: 关注记录不存在或已删除, userId: {}, targetType: {}, targetId: {}", 
                    userId, targetType, targetId);
                return Result.failure(2124); // 操作失败
            }
            
            logger.info("取消关注成功: userId: {}, targetType: {}, targetId: {}", userId, targetType, targetId);
            return Result.success(2013, true); // 已取消关注
            
        } catch (Exception e) {
            logger.error("取消关注失败: 系统异常", e);
            return Result.failure(2124); // 操作失败
        }
    }
    
    /**
     * 获取收藏列表
     * 成功码：200，失败码：2125
     */
    @Override
    public Result<Object> getFavoriteList(String type) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取收藏列表失败: 用户未登录");
                return Result.failure(2125); // 加载失败
            }
            
            // 2. 查询收藏列表
            List<UserFavorite> favoriteList;
            if (type != null && !type.trim().isEmpty()) {
                favoriteList = userFavoriteMapper.selectByUserIdAndType(userId, type);
            } else {
                favoriteList = userFavoriteMapper.selectByUserId(userId);
            }
            
            // 3. 转换为VO列表
            List<FavoriteVO> favoriteVOList = convertToFavoriteVOList(favoriteList);
            
            logger.info("获取收藏列表成功: userId: {}, type: {}, count: {}", userId, type, favoriteVOList.size());
            return Result.success(200, favoriteVOList); // 操作成功（静默）
            
        } catch (Exception e) {
            logger.error("获取收藏列表失败: 系统异常", e);
            return Result.failure(2125); // 加载失败
        }
    }
    
    /**
     * 添加收藏
     * 成功码：2014，失败码：2126
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> addFavorite(AddFavoriteDTO favoriteDTO) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("添加收藏失败: 用户未登录");
                return Result.failure(2126); // 操作失败
            }
            
            // 2. 获取参数
            String itemType = favoriteDTO.getItemType();
            String itemId = favoriteDTO.getItemId();
            
            // 3. 验证目标对象是否存在
            if ("tea".equals(itemType)) {
                // 验证茶叶是否存在
                if (teaMapper.selectById(itemId) == null) {
                    logger.warn("添加收藏失败: 茶叶不存在, teaId: {}", itemId);
                    return Result.failure(2126); // 操作失败
                }
            } else if ("post".equals(itemType)) {
                // 验证帖子是否存在
                if (forumPostMapper.selectById(Long.parseLong(itemId)) == null) {
                    logger.warn("添加收藏失败: 帖子不存在, postId: {}", itemId);
                    return Result.failure(2126); // 操作失败
                }
            } else if ("tea_article".equals(itemType)) {
                // 验证文章是否存在
                if (teaArticleMapper.selectById(Long.parseLong(itemId)) == null) {
                    logger.warn("添加收藏失败: 文章不存在, articleId: {}", itemId);
                    return Result.failure(2126); // 操作失败
                }
            } else {
                logger.warn("添加收藏失败: 收藏类型不正确, itemType: {}", itemType);
                return Result.failure(2126); // 操作失败
            }
            
            // 4. 检查是否已收藏
            UserFavorite existingFavorite = userFavoriteMapper.selectByUserIdAndItem(userId, itemType, itemId);
            if (existingFavorite != null) {
                logger.warn("添加收藏失败: 已收藏该项, userId: {}, itemId: {}", userId, itemId);
                return Result.failure(2126); // 操作失败
            }
            
            // 5. 构建收藏实体
            UserFavorite favorite = new UserFavorite();
            favorite.setUserId(userId);
            favorite.setItemType(itemType);
            favorite.setItemId(itemId);
            favorite.setTargetName(favoriteDTO.getTargetName());
            favorite.setTargetImage(favoriteDTO.getTargetImage());
            favorite.setCreateTime(new Date());
            
            // 6. 插入数据库
            int result = userFavoriteMapper.insert(favorite);
            if (result <= 0) {
                logger.error("添加收藏失败: 数据库插入失败, userId: {}", userId);
                return Result.failure(2126); // 操作失败
            }
            
            logger.info("添加收藏成功: userId: {}, itemId: {}, itemType: {}", userId, itemId, itemType);
            return Result.success(2014, true); // 已收藏
            
        } catch (Exception e) {
            logger.error("添加收藏失败: 系统异常", e);
            return Result.failure(2126); // 操作失败
        }
    }
    
    /**
     * 取消收藏
     * 成功码：2015，失败码：2127
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> removeFavorite(AddFavoriteDTO favoriteDTO) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("取消收藏失败: 用户未登录");
                return Result.failure(2127); // 操作失败
            }
            
            // 2. 验证参数
            if (favoriteDTO == null || favoriteDTO.getItemId() == null || favoriteDTO.getItemType() == null) {
                logger.warn("取消收藏失败: 参数不完整");
                return Result.failure(2127); // 操作失败
            }
            
            String itemId = favoriteDTO.getItemId();
            String itemType = favoriteDTO.getItemType();
            
            // 3. 执行删除（根据 userId, itemType, itemId 删除）
            int result = userFavoriteMapper.deleteByUserIdAndItem(userId, itemType, itemId);
            if (result <= 0) {
                logger.warn("取消收藏失败: 收藏记录不存在或已删除, userId: {}, itemType: {}, itemId: {}", 
                    userId, itemType, itemId);
                return Result.failure(2127); // 操作失败
            }
            
            logger.info("取消收藏成功: userId: {}, itemType: {}, itemId: {}", userId, itemType, itemId);
            return Result.success(2015, true); // 已取消收藏
            
        } catch (Exception e) {
            logger.error("取消收藏失败: 系统异常", e);
            return Result.failure(2127); // 操作失败
        }
    }
    
    /**
     * 点赞
     * 成功码：2016，失败码：2128
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> addLike(AddLikeDTO likeDTO) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("点赞失败: 用户未登录");
                return Result.failure(2128); // 操作失败
            }
            
            // 2. 获取参数
            String targetType = likeDTO.getTargetType();
            String targetId = likeDTO.getTargetId();
            
            // 3. 验证目标对象是否存在
            if ("post".equals(targetType)) {
                // 验证帖子是否存在
                if (forumPostMapper.selectById(Long.parseLong(targetId)) == null) {
                    logger.warn("点赞失败: 帖子不存在, postId: {}", targetId);
                    return Result.failure(2128); // 操作失败
                }
            } else if ("reply".equals(targetType)) {
                // 验证回复是否存在
                if (forumReplyMapper.selectById(Long.parseLong(targetId)) == null) {
                    logger.warn("点赞失败: 回复不存在, replyId: {}", targetId);
                    return Result.failure(2128); // 操作失败
                }
            } else if ("article".equals(targetType)) {
                // 验证文章是否存在
                if (teaArticleMapper.selectById(Long.parseLong(targetId)) == null) {
                    logger.warn("点赞失败: 文章不存在, articleId: {}", targetId);
                    return Result.failure(2128); // 操作失败
                }
            } else {
                logger.warn("点赞失败: 点赞类型不正确, targetType: {}", targetType);
                return Result.failure(2128); // 操作失败
            }
            
            // 4. 检查是否已点赞
            UserLike existingLike = userLikeMapper.selectByUserIdAndTarget(userId, targetType, targetId);
            if (existingLike != null) {
                logger.warn("点赞失败: 已点赞该对象, userId: {}, targetId: {}", userId, targetId);
                return Result.failure(2128); // 操作失败
            }
            
            // 5. 构建点赞实体
            UserLike like = new UserLike();
            like.setUserId(userId);
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            like.setCreateTime(new Date());
            
            // 6. 插入数据库
            int result = userLikeMapper.insert(like);
            if (result <= 0) {
                logger.error("点赞失败: 数据库插入失败, userId: {}", userId);
                return Result.failure(2128); // 操作失败
            }
            
            logger.info("点赞成功: userId: {}, targetId: {}, targetType: {}", userId, targetId, targetType);
            return Result.success(2016, true); // 已点赞
            
        } catch (Exception e) {
            logger.error("点赞失败: 系统异常", e);
            return Result.failure(2128); // 操作失败
        }
    }
    
    /**
     * 取消点赞
     * 成功码：2017，失败码：2129
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> removeLike(AddLikeDTO likeDTO) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("取消点赞失败: 用户未登录");
                return Result.failure(2129); // 操作失败
            }
            
            // 2. 获取参数
            String targetType = likeDTO.getTargetType();
            String targetId = likeDTO.getTargetId();
            
            // 3. 验证点赞记录是否存在（可选，用于日志记录）
            UserLike existingLike = userLikeMapper.selectByUserIdAndTarget(userId, targetType, targetId);
            if (existingLike == null) {
                logger.warn("取消点赞失败: 点赞记录不存在, userId: {}, targetType: {}, targetId: {}", 
                    userId, targetType, targetId);
                return Result.failure(2129); // 操作失败
            }
            
            // 4. 执行删除（根据userId + targetType + targetId删除）
            int result = userLikeMapper.deleteByUserIdAndTarget(userId, targetType, targetId);
            if (result <= 0) {
                logger.error("取消点赞失败: 数据库删除失败, userId: {}, targetType: {}, targetId: {}", 
                    userId, targetType, targetId);
                return Result.failure(2129); // 操作失败
            }
            
            logger.info("取消点赞成功: userId: {}, targetType: {}, targetId: {}", userId, targetType, targetId);
            return Result.success(2017, true); // 已取消点赞
            
        } catch (Exception e) {
            logger.error("取消点赞失败: 系统异常", e);
            return Result.failure(2129); // 操作失败
        }
    }
    
    // ==================== 用户偏好设置 ====================
    
    /**
     * 获取用户偏好设置
     * 成功码：200，失败码：2130
     */
    @Override
    public Result<Object> getUserPreferences() {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("获取偏好设置失败: 用户未登录");
                return Result.failure(2130); // 加载失败
            }
            
            // 2. 查询用户所有设置
            List<UserSetting> settings = userSettingMapper.selectByUserId(userId);
            
            // 3. 转换为VO
            UserPreferencesVO preferencesVO = convertToUserPreferencesVO(settings);
            
            logger.info("获取偏好设置成功: userId: {}", userId);
            return Result.success(200, preferencesVO); // 操作成功（静默）
            
        } catch (Exception e) {
            logger.error("获取偏好设置失败: 系统异常", e);
            return Result.failure(2130); // 加载失败
        }
    }
    
    /**
     * 更新用户偏好设置
     * 成功码：2018，失败码：2131
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateUserPreferences(UpdateUserPreferencesDTO preferencesDTO) {
        try {
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("更新偏好设置失败: 用户未登录");
                return Result.failure(2131); // 操作失败
            }
            
            // 2. 更新各项设置
            Date now = new Date();
            
            if (preferencesDTO.getLanguage() != null) {
                upsertSetting(userId, "language", preferencesDTO.getLanguage(), "string", now);
            }
            
            if (preferencesDTO.getTheme() != null) {
                upsertSetting(userId, "theme", preferencesDTO.getTheme(), "string", now);
            }
            
            if (preferencesDTO.getSystemNotification() != null) {
                upsertSetting(userId, "systemNotification", preferencesDTO.getSystemNotification().toString(), "boolean", now);
            }
            
            if (preferencesDTO.getOrderNotification() != null) {
                upsertSetting(userId, "orderNotification", preferencesDTO.getOrderNotification().toString(), "boolean", now);
            }
            
            if (preferencesDTO.getCommentNotification() != null) {
                upsertSetting(userId, "commentNotification", preferencesDTO.getCommentNotification().toString(), "boolean", now);
            }
            
            if (preferencesDTO.getLikeNotification() != null) {
                upsertSetting(userId, "likeNotification", preferencesDTO.getLikeNotification().toString(), "boolean", now);
            }
            
            logger.info("更新偏好设置成功: userId: {}", userId);
            return Result.success(2018); // 偏好设置已更新
            
        } catch (Exception e) {
            logger.error("更新偏好设置失败: 系统异常", e);
            return Result.failure(2131); // 操作失败
        }
    }
    
    // ==================== 管理员功能 ====================
    
    @Override
    public Result<Object> getAdminUserList(String keyword, Integer role, Integer status, Integer page, Integer pageSize) {
        try {
            // 1. 参数验证
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }
            
            // 2. 计算偏移量
            int offset = (page - 1) * pageSize;
            
            // 3. 查询用户列表
            List<User> userList = userMapper.selectByPage(keyword, role, status, offset, pageSize);
            
            // 4. 查询总数
            int total = userMapper.countByCondition(keyword, role, status);
            
            // 5. 转换为VO列表
            List<UserVO> userVOList = new ArrayList<>();
            for (User user : userList) {
                userVOList.add(convertToUserVO(user));
            }
            
            // 6. 构建分页结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", userVOList);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) total / pageSize));
            
            logger.info("获取用户列表成功(管理员): keyword: {}, role: {}, status: {}, page: {}, total: {}", 
                keyword, role, status, page, total);
            return Result.success(200, result); // 操作成功（静默）
            
        } catch (Exception e) {
            logger.error("获取用户列表失败(管理员): 系统异常", e);
            return Result.failure(2132); // 加载失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> createAdmin(CreateAdminDTO adminDTO) {
        try {
            // 1. 检查用户名是否已存在
            if (isUserExist(adminDTO.getUsername())) {
                logger.warn("创建管理员失败: 用户名已存在, username: {}", adminDTO.getUsername());
                return Result.failure(2134); // 操作失败，用户名已存在
            }
            
            // 2. 创建User实体
            User user = new User();
            user.setUsername(adminDTO.getUsername());
            user.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
            user.setNickname(adminDTO.getNickname());
            user.setPhone(adminDTO.getPhone());
            user.setEmail(adminDTO.getEmail());
            
            // 3. 生成用户ID
            String userId = generateUserId();
            user.setId(userId);
            
            // 4. 设置角色为管理员
            user.setRole(adminDTO.getRole() != null ? adminDTO.getRole() : 1);
            user.setStatus(1); // 默认为正常状态
            user.setIsDeleted(0); // 默认为未删除
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            
            // 5. 保存到数据库
            int result = userMapper.insert(user);
            if (result <= 0) {
                logger.error("创建管理员失败: 数据库插入失败, username: {}", adminDTO.getUsername());
                return Result.failure(2135); // 操作失败
            }
            
            logger.info("创建管理员成功: username: {}, userId: {}, role: {}", 
                adminDTO.getUsername(), userId, user.getRole());
            return Result.success(2019, true); // 管理员账号创建成功
            
        } catch (Exception e) {
            logger.error("创建管理员失败: 系统异常", e);
            return Result.failure(2135); // 操作失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateUser(String userId, UpdateUserDTO updateUserDTO) {
        try {
            // 1. 验证参数
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("更新用户失败(管理员): 用户ID为空");
                return Result.failure(2136); // 操作失败
            }
            
            // 2. 查询用户是否存在
            User user = getUserEntityById(userId);
            if (user == null) {
                logger.warn("更新用户失败(管理员): 用户不存在, userId: {}", userId);
                return Result.failure(2137); // 用户不存在
            }
            
            // 3. 更新允许修改的字段
            boolean hasUpdate = false;
            
            if (updateUserDTO.getNickname() != null) {
                user.setNickname(updateUserDTO.getNickname());
                hasUpdate = true;
            }
            
            if (updateUserDTO.getEmail() != null) {
                user.setEmail(updateUserDTO.getEmail());
                hasUpdate = true;
            }
            
            if (updateUserDTO.getPhone() != null) {
                user.setPhone(updateUserDTO.getPhone());
                hasUpdate = true;
            }
            
            // 4. 如果没有任何更新，直接返回成功
            if (!hasUpdate) {
                logger.info("更新用户(管理员): 无需更新, userId: {}", userId);
                return Result.success(2020, true); // 用户信息更新成功
            }
            
            // 5. 执行更新
            int rows = userMapper.update(user);
            if (rows <= 0) {
                logger.error("更新用户失败(管理员): 数据库更新失败, userId: {}", userId);
                return Result.failure(2136); // 操作失败
            }
            
            logger.info("更新用户成功(管理员): userId: {}", userId);
            return Result.success(2020, true); // 用户信息更新成功
            
        } catch (Exception e) {
            logger.error("更新用户失败(管理员): 系统异常", e);
            return Result.failure(2136); // 操作失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteUser(String userId) {
        try {
            // 1. 获取当前管理员ID
            String adminId = UserContext.getCurrentUserId();
            if (adminId == null) {
                logger.warn("删除用户失败(管理员): 管理员未登录");
                return Result.failure(2138); // 操作失败
            }
            
            // 2. 验证参数
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("删除用户失败(管理员): 用户ID为空");
                return Result.failure(2138); // 操作失败
            }
            
            // 3. 不能删除自己
            if (adminId.equals(userId)) {
                logger.warn("删除用户失败(管理员): 不能删除自己, adminId: {}", adminId);
                return Result.failure(2139); // 不能删除自己
            }
            
            // 4. 查询用户是否存在
            User user = getUserEntityById(userId);
            if (user == null) {
                logger.warn("删除用户失败(管理员): 用户不存在, userId: {}", userId);
                return Result.failure(2138); // 操作失败
            }
            
            // 5. 清理关联数据
            try {
                // 5.1 删除用户地址
                userAddressMapper.deleteByUserId(userId);
                logger.info("已清理用户地址数据: userId: {}", userId);
                
                // 5.2 删除用户关注
                userFollowMapper.deleteByUserId(userId);
                logger.info("已清理用户关注数据: userId: {}", userId);
                
                // 5.3 删除用户收藏
                userFavoriteMapper.deleteByUserId(userId);
                logger.info("已清理用户收藏数据: userId: {}", userId);
                
                // 5.4 删除用户点赞
                userLikeMapper.deleteByUserId(userId);
                logger.info("已清理用户点赞数据: userId: {}", userId);
                
                // 5.5 删除用户设置
                userSettingMapper.deleteByUserId(userId);
                logger.info("已清理用户设置数据: userId: {}", userId);
                
                // 5.6 如果是商家，需要处理店铺相关数据
                if (user.getRole() != null && user.getRole() == 3) {
                    // 查询是否有店铺
                    if (shopMapper.selectByOwnerId(userId) != null) {
                        logger.warn("删除商家用户: 该用户拥有店铺，需要先处理店铺数据, userId: {}", userId);
                        // 注意：这里不直接删除店铺，因为店铺可能有订单等重要数据
                        // 建议：先关闭店铺，或者提示管理员需要先处理店铺
                    }
                }
                
                // 注意：以下数据不删除，保留历史记录
                // - orders: 订单数据（保留交易记录）
                // - chat_sessions, chat_messages: 聊天记录（保留沟通记录）
                // - forum_posts, forum_replies: 论坛内容（保留社区内容）
                // - tea_reviews: 评价数据（保留评价记录）
                // - user_notifications: 通知数据（可保留）
                // - shop_certifications: 认证记录（保留审核记录）
                
            } catch (Exception e) {
                logger.error("清理用户关联数据失败: userId: {}", userId, e);
                // 继续执行删除用户操作
            }
            
            // 6. 执行删除用户（软删除）
            int result = userMapper.delete(userId);
            if (result <= 0) {
                logger.error("删除用户失败(管理员): 数据库删除失败, userId: {}", userId);
                return Result.failure(2138); // 操作失败
            }
            
            logger.info("删除用户成功(管理员): userId: {}, adminId: {}", userId, adminId);
            return Result.success(2021, true); // 用户删除成功
            
        } catch (Exception e) {
            logger.error("删除用户失败(管理员): 系统异常", e);
            return Result.failure(2138); // 操作失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> toggleUserStatus(String userId, Map<String, Object> statusData) {
        try {
            // 1. 验证参数
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("切换用户状态失败(管理员): 用户ID为空");
                return Result.failure(2140); // 操作失败
            }
            
            if (statusData == null || !statusData.containsKey("status")) {
                logger.warn("切换用户状态失败(管理员): 状态参数为空, userId: {}", userId);
                return Result.failure(2140); // 操作失败
            }
            
            // 2. 查询用户是否存在
            User user = getUserEntityById(userId);
            if (user == null) {
                logger.warn("切换用户状态失败(管理员): 用户不存在, userId: {}", userId);
                return Result.failure(2141); // 用户不存在
            }
            
            // 3. 获取新状态
            Integer newStatus = (Integer) statusData.get("status");
            if (newStatus == null || (newStatus != 0 && newStatus != 1)) {
                logger.warn("切换用户状态失败(管理员): 状态值不正确, userId: {}, status: {}", userId, newStatus);
                return Result.failure(2140); // 操作失败
            }
            
            // 4. 更新状态
            user.setStatus(newStatus);
            int result = userMapper.update(user);
            if (result <= 0) {
                logger.error("切换用户状态失败(管理员): 数据库更新失败, userId: {}", userId);
                return Result.failure(2140); // 操作失败
            }
            
            logger.info("切换用户状态成功(管理员): userId: {}, newStatus: {}", userId, newStatus);
            return Result.success(2022, true); // 用户状态更新成功
            
        } catch (Exception e) {
            logger.error("切换用户状态失败(管理员): 系统异常", e);
            return Result.failure(2140); // 操作失败
        }
    }
    
    @Override
    public Result<Object> getCertificationList(Integer status, Integer page, Integer pageSize) {
        try {
            // 1. 参数验证
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }
            
            // 2. 计算偏移量
            int offset = (page - 1) * pageSize;
            
            // 3. 查询认证列表
            List<ShopCertification> certificationList = shopCertificationMapper.selectByPage(status, offset, pageSize);
            
            // 4. 查询总数
            int total = shopCertificationMapper.countByCondition(status);
            
            // 5. 转换为VO列表
            List<CertificationStatusVO> certificationVOList = new ArrayList<>();
            for (ShopCertification certification : certificationList) {
                certificationVOList.add(convertToCertificationStatusVO(certification));
            }
            
            // 6. 构建分页结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", certificationVOList);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (int) Math.ceil((double) total / pageSize));
            
            logger.info("获取认证列表成功(管理员): status: {}, page: {}, total: {}", status, page, total);
            return Result.success(200, result); // 操作成功（静默）
            
        } catch (Exception e) {
            logger.error("获取认证列表失败(管理员): 系统异常", e);
            return Result.failure(2142); // 加载失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> processCertification(Integer id, ProcessCertificationDTO processCertificationDTO) {
        try {
            // 1. 获取当前管理员ID
            String adminId = UserContext.getCurrentUserId();
            if (adminId == null) {
                logger.warn("审核认证失败(管理员): 管理员未登录");
                return Result.failure(2144); // 操作失败
            }
            
            // 2. 验证参数
            if (id == null) {
                logger.warn("审核认证失败(管理员): 认证ID为空");
                return Result.failure(2144); // 操作失败
            }
            
            // 3. 查询认证记录是否存在
            ShopCertification certification = shopCertificationMapper.selectById(id);
            if (certification == null) {
                logger.warn("审核认证失败(管理员): 认证记录不存在, certificationId: {}", id);
                return Result.failure(2145); // 认证记录不存在
            }
            
            // 4. 检查认证状态（只能审核待审核的记录）
            if (certification.getStatus() != 0) {
                logger.warn("审核认证失败(管理员): 认证记录已审核, certificationId: {}, status: {}", id, certification.getStatus());
                return Result.failure(2144); // 操作失败
            }
            
            // 5. 获取审核状态和意见
            Integer auditStatus = processCertificationDTO.getStatus();
            String message = processCertificationDTO.getMessage();
            
            if (auditStatus == null || (auditStatus != 1 && auditStatus != 2)) {
                logger.warn("审核认证失败(管理员): 审核状态值不正确, certificationId: {}, status: {}", id, auditStatus);
                return Result.failure(2144); // 操作失败
            }
            
            // 6. 更新认证记录
            certification.setStatus(auditStatus);
            certification.setAdminId(adminId);
            if (auditStatus == 2 && message != null) {
                certification.setRejectReason(message);
            }
            certification.setUpdateTime(new Date());
            
            int result = shopCertificationMapper.update(certification);
            if (result <= 0) {
                logger.error("审核认证失败(管理员): 数据库更新失败, certificationId: {}", id);
                return Result.failure(2144); // 操作失败
            }
            
            // 7. 如果审核通过，调用存储过程创建店铺
            if (auditStatus == 1) {
                try {
                    // 调用存储过程：confirm_shop_certification
                    // 该存储过程会：
                    // 1. 更新用户角色为商家（role=3）
                    // 2. 生成店铺ID并创建店铺记录
                    // 3. 标记认证通知已确认
                    String shopId = shopCertificationMapper.confirmCertification(id);
                    logger.info("审核认证通过，店铺创建成功: certificationId: {}, shopId: {}", id, shopId);
                } catch (Exception e) {
                    logger.error("调用存储过程创建店铺失败: certificationId: {}", id, e);
                    // 如果存储过程调用失败，手动更新用户角色
                    User user = getUserEntityById(certification.getUserId());
                    if (user != null) {
                        user.setRole(3); // 3-商家角色
                        userMapper.update(user);
                        logger.warn("存储过程失败，已手动更新用户角色为商家: userId: {}", user.getId());
                    }
                }
            }
            
            logger.info("审核认证成功(管理员): certificationId: {}, status: {}, adminId: {}", id, auditStatus, adminId);
            return Result.success(2023, true); // 审核操作成功
            
        } catch (Exception e) {
            logger.error("审核认证失败(管理员): 系统异常", e);
            return Result.failure(2144); // 操作失败
        }
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
    
    // ==================== 验证码相关 ====================
    
    /**
     * 发送验证码
     * 成功码：2025，失败码：2149, 2150, 2151
     */
    @Override
    public Result<VerificationCodeVO> sendVerificationCode(SendVerificationCodeDTO sendCodeDTO) {
        String contact = sendCodeDTO.getContact();
        String contactType = sendCodeDTO.getContactType();
        String sceneType = sendCodeDTO.getSceneType();
        
        logger.info("发送验证码: contact={}, contactType={}, sceneType={}", contact, contactType, sceneType);
        
        // 1. 检查发送频率限制（60秒内只能发送一次）
        String limitKey = "verification_code_limit:" + contact;
        Boolean hasLimit = redisTemplate.hasKey(limitKey);
        if (Boolean.TRUE.equals(hasLimit)) {
            logger.warn("发送验证码过于频繁: {}", contact);
            return Result.failure(2150); // 发送过于频繁，请稍后再试
        }
        
        // 2. 检查每日发送次数限制（每天最多10次）
        String dailyKey = "verification_code_daily:" + contact;
        String dailyCountStr = redisTemplate.opsForValue().get(dailyKey);
        int dailyCount = dailyCountStr == null ? 0 : Integer.parseInt(dailyCountStr);
        if (dailyCount >= 10) {
            logger.warn("验证码每日发送次数已达上限: {}", contact);
            return Result.failure(2150); // 发送过于频繁，请稍后再试
        }
        
        // 3. 生成6位随机验证码
        String code = String.format("%06d", RANDOM.nextInt(1000000));
        logger.info("生成验证码: contact={}, code={}", contact, code);
        
        // 4. 存储验证码到Redis（5分钟有效期）
        String codeKey = "verification_code:" + sceneType + ":" + contact;
        redisTemplate.opsForValue().set(codeKey, code, 5, TimeUnit.MINUTES);
        
        // 5. 设置发送频率限制（60秒）
        redisTemplate.opsForValue().set(limitKey, "1", 60, TimeUnit.SECONDS);
        
        // 6. 更新每日发送次数（到当天23:59:59过期）
        long secondsUntilMidnight = getSecondsUntilMidnight();
        redisTemplate.opsForValue().set(dailyKey, String.valueOf(dailyCount + 1), secondsUntilMidnight, TimeUnit.SECONDS);
        
        // 7. 发送验证码
        boolean sendSuccess;
        if ("email".equals(contactType)) {
            // 发送邮件验证码
            sendSuccess = sendEmailCode(contact, code, sceneType);
        } else {
            // 发送短信验证码（必须启用真实短信发送）
            if (!smsEnabled || aliyunAccessKeyId == null || aliyunAccessKeyId.isEmpty()) {
                logger.error("短信服务未配置: smsEnabled={}, accessKeyId={}", smsEnabled, aliyunAccessKeyId != null);
                return Result.failure(2149); // 发送验证码失败
            }
            sendSuccess = sendAliyunSms(contact, code);
        }
        
        if (!sendSuccess) {
            logger.error("验证码发送失败: contact={}", contact);
            return Result.failure(2149); // 发送验证码失败
        }
        
        logger.info("验证码发送成功: contact={}", contact);
        return Result.success(2025, null);
    }
    
    /**
     * 验证验证码
     */
    @Override
    public boolean verifyCode(String contact, String sceneType, String code) {
        String codeKey = "verification_code:" + sceneType + ":" + contact;
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        
        if (storedCode == null) {
            logger.warn("验证码不存在或已过期: contact={}, sceneType={}", contact, sceneType);
            return false;
        }
        
        boolean isValid = storedCode.equals(code);
        if (isValid) {
            // 验证成功后删除验证码
            redisTemplate.delete(codeKey);
            logger.info("验证码验证成功: contact={}", contact);
        } else {
            logger.warn("验证码错误: contact={}, input={}, stored={}", contact, code, storedCode);
        }
        
        return isValid;
    }
    
    /**
     * 发送邮件验证码（真实发送）
     */
    private boolean sendEmailCode(String email, String code, String sceneType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("【商南茶城】验证码");
            
            String sceneName = getSceneName(sceneType);
            message.setText(String.format("您正在进行%s操作，验证码是：%s，5分钟内有效，请勿泄露。", sceneName, code));
            
            mailSender.send(message);
            logger.info("邮件验证码发送成功: email={}, code={}", email, code);
            return true;
        } catch (Exception e) {
            logger.error("邮件验证码发送失败: email={}, error={}", email, e.getMessage());
            return false;
        }
    }
    
    /**
     * 阿里云短信认证服务真实发送
     */
    private boolean sendAliyunSms(String phone, String code) {
        try {
            // 配置阿里云SDK
            com.aliyun.dysmsapi20170525.Client client = createAliyunSmsClient();
            
            // 检查模板代码配置
            if (aliyunTemplateCode == null || aliyunTemplateCode.isEmpty()) {
                logger.error("阿里云短信模板代码未配置");
                return false;
            }
            
            // 构建请求
            com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(aliyunSignName)
                .setTemplateCode(aliyunTemplateCode)  // 使用配置的模板代码
                .setTemplateParam("{\"code\":\"" + code + "\"}");
            
            // 发送短信
            com.aliyun.dysmsapi20170525.models.SendSmsResponse response = client.sendSms(sendSmsRequest);
            
            // 判断是否成功
            if ("OK".equals(response.getBody().getCode())) {
                logger.info("阿里云短信发送成功: phone={}, code={}", phone, code);
                return true;
            } else {
                logger.error("阿里云短信发送失败: phone={}, code={}, message={}", 
                    phone, response.getBody().getCode(), response.getBody().getMessage());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("阿里云短信发送异常: phone={}, error={}", phone, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 创建阿里云短信客户端
     */
    private com.aliyun.dysmsapi20170525.Client createAliyunSmsClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
            .setAccessKeyId(aliyunAccessKeyId)
            .setAccessKeySecret(aliyunAccessKeySecret)
            .setEndpoint("dysmsapi.aliyuncs.com");
        return new com.aliyun.dysmsapi20170525.Client(config);
    }
    
    /**
     * 获取场景名称
     */
    private String getSceneName(String sceneType) {
        switch (sceneType) {
            case "register":
                return "注册";
            case "reset_password":
                return "重置密码";
            case "change_phone":
                return "更换手机号";
            default:
                return "验证";
        }
    }
    
    /**
     * 计算到当天午夜的秒数
     */
    private long getSecondsUntilMidnight() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return java.time.Duration.between(now, midnight).getSeconds();
    }

    /**
     * 将UserFavorite实体列表转换为FavoriteVO列表
     */
    private List<FavoriteVO> convertToFavoriteVOList(List<UserFavorite> favoriteList) {
        List<FavoriteVO> favoriteVOList = new ArrayList<>();
        if (favoriteList == null || favoriteList.isEmpty()) {
            return favoriteVOList;
        }
        
        for (UserFavorite favorite : favoriteList) {
            FavoriteVO favoriteVO = new FavoriteVO();
            favoriteVO.setId(favorite.getId());
            favoriteVO.setItemType(favorite.getItemType());
            favoriteVO.setItemId(favorite.getItemId());
            favoriteVO.setTargetName(favorite.getTargetName());
            favoriteVO.setTargetImage(favorite.getTargetImage());
            favoriteVO.setCreateTime(favorite.getCreateTime());
            favoriteVOList.add(favoriteVO);
        }
        
        return favoriteVOList;
    }
    
    /**
     * 将UserLike实体列表转换为LikeVO列表
     */
    private List<LikeVO> convertToLikeVOList(List<UserLike> likeList) {
        List<LikeVO> likeVOList = new ArrayList<>();
        if (likeList == null || likeList.isEmpty()) {
            return likeVOList;
        }
        
        for (UserLike like : likeList) {
            LikeVO likeVO = new LikeVO();
            likeVO.setId(like.getId());
            likeVO.setTargetType(like.getTargetType());
            likeVO.setTargetId(like.getTargetId());
            likeVO.setCreateTime(like.getCreateTime());
            likeVOList.add(likeVO);
        }
        
        return likeVOList;
    }

    /**
     * 将UserSetting列表转换为UserPreferencesVO
     */
    private UserPreferencesVO convertToUserPreferencesVO(List<UserSetting> settings) {
        UserPreferencesVO preferencesVO = new UserPreferencesVO();
        
        if (settings == null || settings.isEmpty()) {
            // 返回默认值
            preferencesVO.setLanguage("zh-CN");
            preferencesVO.setTheme("light");
            preferencesVO.setSystemNotification(true);
            preferencesVO.setOrderNotification(true);
            preferencesVO.setCommentNotification(true);
            preferencesVO.setLikeNotification(true);
            return preferencesVO;
        }
        
        // 从设置列表中提取各项配置
        for (UserSetting setting : settings) {
            String key = setting.getSettingKey();
            String value = setting.getSettingValue();
            
            switch (key) {
                case "language":
                    preferencesVO.setLanguage(value);
                    break;
                case "theme":
                    preferencesVO.setTheme(value);
                    break;
                case "systemNotification":
                    preferencesVO.setSystemNotification(Boolean.parseBoolean(value));
                    break;
                case "orderNotification":
                    preferencesVO.setOrderNotification(Boolean.parseBoolean(value));
                    break;
                case "commentNotification":
                    preferencesVO.setCommentNotification(Boolean.parseBoolean(value));
                    break;
                case "likeNotification":
                    preferencesVO.setLikeNotification(Boolean.parseBoolean(value));
                    break;
            }
        }
        
        // 设置默认值（如果某些配置不存在）
        if (preferencesVO.getLanguage() == null) {
            preferencesVO.setLanguage("zh-CN");
        }
        if (preferencesVO.getTheme() == null) {
            preferencesVO.setTheme("light");
        }
        if (preferencesVO.getSystemNotification() == null) {
            preferencesVO.setSystemNotification(true);
        }
        if (preferencesVO.getOrderNotification() == null) {
            preferencesVO.setOrderNotification(true);
        }
        if (preferencesVO.getCommentNotification() == null) {
            preferencesVO.setCommentNotification(true);
        }
        if (preferencesVO.getLikeNotification() == null) {
            preferencesVO.setLikeNotification(true);
        }
        
        return preferencesVO;
    }
    
    /**
     * 插入或更新用户设置
     */
    private void upsertSetting(String userId, String key, String value, String type, Date now) {
        // 查询是否已存在该设置
        UserSetting existingSetting = userSettingMapper.selectByUserIdAndKey(userId, key);
        
        if (existingSetting != null) {
            // 更新现有设置
            existingSetting.setSettingValue(value);
            existingSetting.setUpdateTime(now);
            userSettingMapper.updateById(existingSetting);
        } else {
            // 插入新设置
            UserSetting newSetting = new UserSetting();
            newSetting.setUserId(userId);
            newSetting.setSettingKey(key);
            newSetting.setSettingValue(value);
            newSetting.setSettingType(type);
            newSetting.setCreateTime(now);
            newSetting.setUpdateTime(now);
            userSettingMapper.insert(newSetting);
        }
    }
}
