# 验证码功能快速启动指南

## 🚀 5分钟快速启动

### 第一步：配置 QQ 邮箱（必需）

1. **获取授权码**
   - 登录 QQ 邮箱：https://mail.qq.com
   - 设置 → 账户 → 开启 SMTP 服务
   - 生成授权码（16位字符）

2. **填写配置**
   
   打开 `application.yml`，找到邮箱配置部分：
   
   ```yaml
   spring:
     mail:
       username: 你的QQ邮箱@qq.com
       password: 你的16位授权码
   ```

3. **完成！**
   
   现在邮箱验证码功能已可用。

---

### 第二步：配置短信（可选）

#### 选项A：使用模拟模式（推荐开发测试）

**无需任何配置**，默认就是模拟模式：

```yaml
tencent:
  sms:
    enabled: false  # 模拟发送
```

**特点**：
- ✅ 无需申请资质
- ✅ 不消耗费用
- ✅ 验证码打印在日志中
- ✅ 适合开发测试

#### 选项B：使用真实短信（生产环境）

1. **申请腾讯云短信**
   - 注册：https://cloud.tencent.com
   - 个人实名认证
   - 申请短信签名和模板
   - 获取 SecretId、SecretKey、SDK AppID、模板ID

2. **填写配置**
   
   ```yaml
   tencent:
     sms:
       enabled: true
       secret-id: 你的SecretId
       secret-key: 你的SecretKey
       sdk-app-id: 你的SdkAppId
       sign-name: 商南茶城
       template-id: 你的模板ID
   ```

3. **完成！**
   
   现在短信验证码会真实发送。

---

## 📝 测试验证码功能

### 1. 启动项目

```bash
cd shangnantea-server
mvn spring-boot:run
```

### 2. 测试邮箱验证码

```bash
POST http://localhost:8080/api/user/verification-code/send
Content-Type: application/json

{
  "contact": "your_email@example.com",
  "contactType": "email",
  "sceneType": "register"
}
```

**预期结果**：
- 返回 `{"code": 2025, "message": "验证码已发送"}`
- 邮箱收到验证码邮件

### 3. 测试短信验证码（模拟模式）

```bash
POST http://localhost:8080/api/user/verification-code/send
Content-Type: application/json

{
  "contact": "13800138000",
  "contactType": "phone",
  "sceneType": "register"
}
```

**预期结果**：
- 返回 `{"code": 2025, "message": "验证码已发送"}`
- 后端日志打印验证码：
  ```
  【模拟发送短信】手机号: 13800138000, 验证码: 123456, 场景: 注册
  ```

### 4. 测试注册（带验证码）

```bash
POST http://localhost:8080/api/user/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456",
  "confirmPassword": "123456",
  "phone": "13800138000",
  "email": "test@example.com",
  "verificationCode": "123456",
  "contactType": "phone"
}
```

**预期结果**：
- 验证码正确：返回 `{"code": 2001, "message": "注册成功，请登录"}`
- 验证码错误：返回 `{"code": 2102, "message": "注册失败，用户名已存在或数据格式错误"}`

---

## 🔍 查看验证码（开发调试）

### 方法1：查看后端日志

```
生成验证码: contact=13800138000, code=123456
```

### 方法2：查看 Redis

```bash
# 连接 Redis
redis-cli

# 查看所有验证码
keys verification_code:*

# 查看具体验证码
get verification_code:register:13800138000
```

---

## ⚙️ 配置文件完整示例

```yaml
spring:
  # 邮箱配置
  mail:
    host: smtp.qq.com
    port: 587
    username: 2109382180@qq.com          # 改成你的
    password: abcdefghijklmnop            # 改成你的授权码
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
  
  # Redis配置
  redis:
    host: localhost
    port: 6379
    timeout: 3000ms
    database: 0

# 腾讯云短信配置
tencent:
  sms:
    enabled: false                        # true=真实发送，false=模拟发送
    secret-id: YOUR_SECRET_ID            # 改成你的SecretId
    secret-key: YOUR_SECRET_KEY          # 改成你的SecretKey
    sdk-app-id: YOUR_SDK_APP_ID          # 改成你的SdkAppId
    sign-name: 商南茶城                   # 改成你的签名
    template-id: YOUR_TEMPLATE_ID        # 改成你的模板ID
```

---

## 🎯 支持的场景类型

| sceneType | 说明 | 短信内容 |
|-----------|------|----------|
| `register` | 注册 | 您正在进行**注册**操作 |
| `reset_password` | 重置密码 | 您正在进行**重置密码**操作 |
| `change_phone` | 更换手机号 | 您正在进行**更换手机号**操作 |

---

## 🛡️ 防刷机制

- ✅ **60秒限制**：同一联系方式60秒内只能发送一次
- ✅ **每日限制**：同一联系方式每天最多发送10次
- ✅ **5分钟有效期**：验证码5分钟后自动过期

---

## 📚 更多文档

- `verification-code-implementation-summary.md` - 完整实现总结
- `tencent-sms-setup.md` - 腾讯云短信详细配置（⭐推荐）
- `openapi_new.yaml` - API 接口文档

---

## ✅ 检查清单

- [ ] Redis 已启动（`redis-server`）
- [ ] QQ 邮箱授权码已配置
- [ ] 项目已启动（`mvn spring-boot:run`）
- [ ] 测试邮箱验证码成功
- [ ] 测试短信验证码成功（模拟模式）
- [ ] 测试注册接口成功

---

**配置完成！现在你的验证码功能已经可以使用了！** 🎉
