# UserController 结构对比分析

## 一、UserController 应该有的完整结构（基于 openapi.yaml）

### 1. 认证相关接口
- ✅ `POST /user/login` - 用户登录
- ✅ `POST /user/register` - 用户注册
- ✅ `POST /user/logout` - 退出登录
- ✅ `POST /user/refresh` - 刷新令牌

### 2. 用户信息管理
- ✅ `GET /user/me` - 获取当前用户信息
- ✅ `GET /user/{userId}` - 获取指定用户信息
- ✅ `PUT /user` - 更新用户信息
- ✅ `POST /user/avatar` - 上传头像

### 3. 密码管理
- ✅ `PUT /user/password` - 修改密码
- ✅ `POST /user/password/reset` - 密码找回/重置

### 4. 收货地址管理
- ✅ `GET /user/addresses` - 获取收货地址列表
- ✅ `POST /user/addresses` - 添加收货地址
- ✅ `PUT /user/addresses/{id}` - 更新收货地址
- ✅ `DELETE /user/addresses/{id}` - 删除收货地址
- ✅ `PUT /user/addresses/{id}/default` - 设置默认地址

### 5. 商家认证
- ✅ `GET /user/shop-certification` - 获取商家认证状态
- ✅ `POST /user/shop-certification` - 提交商家认证申请

### 6. 关注功能
- ✅ `GET /user/follows` - 获取关注列表
- ✅ `POST /user/follows` - 添加关注
- ✅ `DELETE /user/follows/{id}` - 取消关注

### 7. 收藏功能
- ✅ `GET /user/favorites` - 获取收藏列表
- ✅ `POST /user/favorites` - 添加收藏
- ✅ `DELETE /user/favorites/{id}` - 取消收藏

### 8. 点赞功能
- ✅ `POST /user/likes` - 点赞
- ✅ `DELETE /user/likes/{id}` - 取消点赞

### 9. 用户偏好设置
- ✅ `GET /user/preferences` - 获取用户偏好设置
- ✅ `PUT /user/preferences` - 更新用户偏好设置

### 10. 管理员功能
- ✅ `GET /user/admin/users` - 获取用户列表（管理员）
- ✅ `POST /user/admin/users` - 创建管理员账号（管理员）
- ✅ `PUT /user/admin/users/{userId}` - 更新用户信息（管理员）
- ✅ `DELETE /user/admin/users/{userId}` - 删除用户（管理员）
- ✅ `PUT /user/admin/users/{userId}/role` - 更新用户角色（管理员，已废弃）
- ✅ `PUT /user/admin/users/{userId}/status` - 启用/禁用用户（管理员）
- ✅ `GET /user/admin/certifications` - 获取商家认证申请列表（管理员）
- ✅ `PUT /user/admin/certifications/{id}` - 审核认证申请（管理员）

---

## 二、当前 UserController 中存在的问题

### 1. 多余的接口（openapi.yaml 中未定义）
- ❌ `GET /user/info` - 获取当前用户信息（与 `/user/me` 功能重复）
- ❌ `PUT /user/update` - 更新用户信息（与 `PUT /user` 功能重复）
- ❌ `POST /user/change-password` - 修改密码（与 `PUT /user/password` 功能重复）
- ❌ `GET /user/list` - 获取用户列表（与 `GET /user/admin/users` 功能重复）
- ❌ `DELETE /user/{id}` - 删除用户（与 `DELETE /user/admin/users/{userId}` 功能重复）
- ❌ `PUT /user/{userId}` - 更新用户信息（与 `PUT /user` 和 `PUT /user/admin/users/{userId}` 功能重复）

### 2. 路径不一致的问题
- ⚠️ `PUT /user/update` 应该改为 `PUT /user`
- ⚠️ `POST /user/change-password` 应该改为 `PUT /user/password`
- ⚠️ `GET /user/list` 应该改为 `GET /user/admin/users`（需要管理员权限）
- ⚠️ `DELETE /user/{id}` 应该改为 `DELETE /user/admin/users/{userId}`（需要管理员权限）

### 3. 缺少的接口
**无** - 所有 openapi.yaml 中定义的接口都已实现

---

## 三、建议的 UserController 标准结构

```java
@RestController
@RequestMapping({"/user", "/api/user"})
@Validated
public class UserController {
    
    // ==================== 认证相关 ====================
    @PostMapping("/login")
    public Result<TokenVO> login(@RequestBody @Valid LoginDTO loginDTO)
    
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody @Valid RegisterDTO registerDTO)
    
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request)
    
    @PostMapping("/refresh")
    public Result<TokenVO> refreshToken(HttpServletRequest request)
    
    // ==================== 用户信息管理 ====================
    @GetMapping("/me")
    @RequiresLogin
    public Result<UserVO> getUserInfo()
    
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable String userId)
    
    @PutMapping
    @RequiresLogin
    public Result<UserVO> updateUser(@RequestBody Map<String, Object> user)
    
    @PostMapping("/avatar")
    @RequiresLogin
    public Result<Object> uploadAvatar(HttpServletRequest request)
    
    // ==================== 密码管理 ====================
    @PutMapping("/password")
    @RequiresLogin
    public Result<String> updatePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO)
    
    @PostMapping("/password/reset")
    public Result<String> resetPassword(@RequestBody Map<String, Object> body)
    
    // ==================== 收货地址管理 ====================
    @GetMapping("/addresses")
    @RequiresLogin
    public Result<Object> listAddresses()
    
    @PostMapping("/addresses")
    @RequiresLogin
    public Result<Object> addAddress(@RequestBody Map<String, Object> body)
    
    @PutMapping("/addresses/{id}")
    @RequiresLogin
    public Result<Boolean> updateAddress(@PathVariable String id, @RequestBody Map<String, Object> body)
    
    @DeleteMapping("/addresses/{id}")
    @RequiresLogin
    public Result<Boolean> deleteAddress(@PathVariable String id)
    
    @PutMapping("/addresses/{id}/default")
    @RequiresLogin
    public Result<Boolean> setDefaultAddress(@PathVariable String id)
    
    // ==================== 商家认证 ====================
    @GetMapping("/shop-certification")
    @RequiresLogin
    public Result<Object> getShopCertification()
    
    @PostMapping("/shop-certification")
    @RequiresLogin
    public Result<Boolean> submitShopCertification(@RequestBody Map<String, Object> body)
    
    // ==================== 关注功能 ====================
    @GetMapping("/follows")
    @RequiresLogin
    public Result<Object> listFollows(@RequestParam(required = false) String type)
    
    @PostMapping("/follows")
    @RequiresLogin
    public Result<Boolean> addFollow(@RequestBody Map<String, Object> body)
    
    @DeleteMapping("/follows/{id}")
    @RequiresLogin
    public Result<Boolean> deleteFollow(@PathVariable String id)
    
    // ==================== 收藏功能 ====================
    @GetMapping("/favorites")
    @RequiresLogin
    public Result<Object> listFavorites(@RequestParam(required = false) String type)
    
    @PostMapping("/favorites")
    @RequiresLogin
    public Result<Boolean> addFavorite(@RequestBody Map<String, Object> body)
    
    @DeleteMapping("/favorites/{id}")
    @RequiresLogin
    public Result<Boolean> deleteFavorite(@PathVariable String id)
    
    // ==================== 点赞功能 ====================
    @PostMapping("/likes")
    @RequiresLogin
    public Result<Boolean> addLike(@RequestBody Map<String, Object> body)
    
    @DeleteMapping("/likes/{id}")
    @RequiresLogin
    public Result<Boolean> deleteLike(@PathVariable String id)
    
    // ==================== 用户偏好设置 ====================
    @GetMapping("/preferences")
    @RequiresLogin
    public Result<Object> getPreferences()
    
    @PutMapping("/preferences")
    @RequiresLogin
    public Result<Object> updatePreferences(@RequestBody Map<String, Object> body)
    
    // ==================== 管理员功能 ====================
    @GetMapping("/admin/users")
    @RequiresRoles({1})
    public Result<Object> getAdminUsers(...)
    
    @PostMapping("/admin/users")
    @RequiresRoles({1})
    public Result<Boolean> createAdminUser(@RequestBody Map<String, Object> body)
    
    @PutMapping("/admin/users/{userId}")
    @RequiresRoles({1})
    public Result<Boolean> updateAdminUser(@PathVariable String userId, @RequestBody Map<String, Object> body)
    
    @DeleteMapping("/admin/users/{userId}")
    @RequiresRoles({1})
    public Result<Boolean> deleteAdminUser(@PathVariable String userId)
    
    @PutMapping("/admin/users/{userId}/role")
    @RequiresRoles({1})
    @Deprecated
    public Result<Boolean> updateUserRole(@PathVariable String userId, @RequestBody Map<String, Object> body)
    
    @PutMapping("/admin/users/{userId}/status")
    @RequiresRoles({1})
    public Result<Boolean> updateUserStatus(@PathVariable String userId, @RequestBody Map<String, Object> body)
    
    @GetMapping("/admin/certifications")
    @RequiresRoles({1})
    public Result<Object> getAdminCertifications(...)
    
    @PutMapping("/admin/certifications/{id}")
    @RequiresRoles({1})
    public Result<Boolean> auditCertification(@PathVariable Integer id, @RequestBody Map<String, Object> body)
}
```

---

## 四、需要清理的接口

### 需要删除的重复接口：
1. `GET /user/info` - 删除，使用 `GET /user/me` 代替
2. `PUT /user/update` - 删除，使用 `PUT /user` 代替
3. `POST /user/change-password` - 删除，使用 `PUT /user/password` 代替
4. `GET /user/list` - 删除，使用 `GET /user/admin/users` 代替
5. `DELETE /user/{id}` - 删除，使用 `DELETE /user/admin/users/{userId}` 代替
6. `PUT /user/{userId}` - 删除，使用 `PUT /user` 或 `PUT /user/admin/users/{userId}` 代替

---

## 五、总结

### ✅ 优点：
- 所有 openapi.yaml 中定义的接口都已实现
- 接口功能完整，覆盖了用户模块的所有需求

### ⚠️ 需要改进：
- 存在6个重复接口，需要清理
- 部分接口路径不符合 openapi.yaml 规范
- 建议统一使用 openapi.yaml 中定义的路径

### 📝 建议：
1. 删除重复接口，统一使用 openapi.yaml 中定义的路径
2. 确保所有需要登录的接口都添加 `@RequiresLogin` 注解
3. 确保所有管理员接口都添加 `@RequiresRoles({1})` 注解
4. 保持接口路径与 openapi.yaml 完全一致

