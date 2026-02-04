# 验证码系统实现总结

## 📋 实现概述

验证码系统已完全实现，支持**邮箱验证码（真实发送）**和**短信验证码（云片网络）**。所有代码已就绪，只需填写配置即可使用。

---

## ✅ 已完成的功能

### 1. 后端接口层
- ✅ **Controller 层**：`UserController.sendVerificationCode()` - 接收前端请求
- ✅ **Service 层**：`UserServiceImpl.sendVerificationCode()` - 业务逻辑实现
- ✅ **DTO 验证**：`SendVerificationCodeDTO` - 参数校验

### 2. 验证码发送功能
- ✅ **邮箱验证码**：使用 Spring Mail + QQ邮箱SMTP（真实发送）
- ✅ **短信验证码**：使用云片网络API（支持真实发送 + 模拟发送）
- ✅ **自动切换**：根据配置自动切换真实/模拟发送

### 3. 验证码存储与验证
- ✅ **Redis 存储**：格式 `verification_code:{sceneType}:{contact}`，TTL 5分钟
- ✅ **验证方法**：`verifyCode()` - 验证码校验，验证成功后自动删除

### 4. 防刷机制
- ✅ **60秒限制**：同一联系方式60秒内只能发送一次
- ✅ **每日限制**：同一联系方式每天最多发送10次
- ✅ **Redis 实现**：使用 Redis 过期时间自动清理

### 5. 业务集成
- ✅ **注册接口**：已集成验证码验证（`RegisterDTO` 新增 `verificationCode` 和 `contactType` 字段）
- ✅ **重置密码接口**：已集成验证码验证（移除所有 TODO）

### 6. 状态码定义
- ✅ **2025**：验证码已发送
- ✅ **2149**：发送验证码失败
- ✅ **2150**：发送过于频繁，请稍后再试
- ✅ **2151**：验证码已过期（预留）

---

## 🔧 配置说明

### 必需配置项

在 `application.yml` 中需要配置以下内容：

```yaml
spring:
  # 邮箱配置（QQ邮箱）
  mail:
    host: smtp.qq.com
    port: 587
    username: 你的QQ邮箱@qq.com          # 替换成你的QQ邮箱
    password: 你的授权码                  # 替换成你的授权码（不是QQ密码！）
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true

# 云片网络短信配置
yunpian:
  sms:
    enabled: false                        # true=真实发送，false=模拟发送
    api-key: YOUR_YUNPIAN_API_KEY        # 替换成你的云片网络ApiKey
    api-url: https://sms.yunpian.com/v2/sms/single_send.json
```

---

## 📝 如何获取配置

### 1. QQ邮箱授权码

1. 登录 QQ 邮箱网页版：https://mail.qq.com
2. 点击 **设置** → **账户**
3. 找到 **POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务**
4. 开启 **POP3/SMTP服务** 或 **IMAP/SMTP服务**
5. 点击 **生成授权码**，按提示操作（需要手机验证）
6. 复制生成的授权码（16位字符），填入 `spring.mail.password`

**注意**：授权码不是QQ密码！

### 2. 云片网络 API Key

详细步骤请参考：`shangnantea-server/docs/yunpian-sms-setup.md`

简要步骤：
1. 注册云片网络账号：https://www.yunpian.com
2. 完成实名认证
3. 充值（测试可充值少量金额）
4. 获取 API Key：控制台 → 开发者中心 → API Key
5. 填入 `yunpian.sms.api-key`
6. 设置 `yunpian.sms.enabled: true` 启用真实发送

---

## 🚀 使用方式

### 1. 邮箱验证码（立即可用）

只需配置 QQ 邮箱授权码，即可使用邮箱验证码功能：

```yaml
spring:
  mail:
    username: 2109382180@qq.com
    password: abcdefghijklmnop  # 你的授权码
```

**测试方法**：
```bash
POST /api/user/verification-code/send
{
  "contact": "user@example.com",
  "contactType": "email",
  "sceneType": "register"
}
```

### 2. 短信验证码（模拟模式）

未配置云片网络时，短信验证码会自动使用**模拟发送**：

```yaml
yunpian:
  sms:
    enabled: false  # 模拟模式
```

**模拟发送行为**：
- 不会真实发送短信
- 验证码会打印在后端日志中
- 可以用于开发测试

**测试方法**：
```bash
POST /api/user/verification-code/send
{
  "contact": "13800138000",
  "contactType": "phone",
  "sceneType": "register"
}
```

查看后端日志，会看到：
```
【模拟发送短信】手机号: 13800138000, 验证码: 123456, 场景: 注册
短信内容: 【商南茶城】您正在进行注册操作，验证码是：123456，5分钟内有效，请勿泄露。
```

### 3. 短信验证码（真实发送）

配置云片网络后，短信验证码会真实发送：

```yaml
yunpian:
  sms:
    enabled: true                          # 启用真实发送
    api-key: your_actual_api_key_here     # 你的API Key
```

---

## 🔍 验证码验证

### 注册接口

**请求示例**：
```bash
POST /api/user/register
{
  "username": "testuser",
  "password": "123456",
  "confirmPassword": "123456",
  "phone": "13800138000",
  "email": "user@example.com",
  "verificationCode": "123456",
  "contactType": "phone"
}
```

**验证流程**：
1. 根据 `contactType` 确定联系方式（phone 或 email）
2. 调用 `verifyCode(contact, "register", verificationCode)` 验证
3. 验证成功后删除验证码，继续注册流程
4. 验证失败返回 2102 错误码

### 重置密码接口

**请求示例**：
```bash
POST /api/user/password/reset
{
  "username": "testuser",
  "phone": "13800138000",
  "newPassword": "newpass123",
  "verificationCode": "123456"
}
```

**验证流程**：
1. 验证用户名和手机号是否匹配
2. 调用 `verifyCode(phone, "reset_password", verificationCode)` 验证
3. 验证成功后删除验证码，重置密码
4. 验证失败返回 2114 错误码

---

## 📦 依赖项

已添加到 `pom.xml`：

```xml
<!-- Mail -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- HTTP Client for Yunpian SMS -->
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
</dependency>

<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

---

## 🎯 下一步工作

### 后端（已完成 ✅）
- ✅ 验证码发送接口
- ✅ 验证码验证方法
- ✅ 注册接口集成
- ✅ 重置密码接口集成
- ✅ 防刷机制
- ✅ 邮箱发送（真实）
- ✅ 短信发送（云片网络 + 模拟）

### 前端（待实现）
- ⏳ 注册页面添加验证码输入框
- ⏳ 注册页面添加"发送验证码"按钮
- ⏳ 重置密码页面添加验证码输入框
- ⏳ 重置密码页面添加"发送验证码"按钮
- ⏳ 倒计时功能（60秒）
- ⏳ 调用 `sendVerificationCode` API

### 配置（待填写）
- ⏳ 填写 QQ 邮箱授权码
- ⏳ 申请并填写云片网络 API Key（可选）

---

## 🐛 常见问题

### 1. 邮件发送失败

**错误信息**：`Authentication failed`

**解决方法**：
- 确认使用的是**授权码**，不是QQ密码
- 确认已开启 QQ 邮箱的 SMTP 服务
- 重新生成授权码

### 2. 短信发送失败（真实模式）

**错误信息**：云片网络返回错误码

**解决方法**：
- 检查 API Key 是否正确
- 检查账户余额是否充足
- 检查短信内容是否包含敏感词
- 查看云片网络控制台的发送记录

### 3. 验证码验证失败

**可能原因**：
- 验证码已过期（5分钟）
- 验证码输入错误
- 验证码已被使用（验证成功后会删除）
- Redis 连接失败

**排查方法**：
- 查看后端日志
- 检查 Redis 是否正常运行
- 使用 Redis 客户端查看 key 是否存在

---

## 📚 相关文档

- `yunpian-sms-setup.md` - 云片网络详细配置指南
- `code-message-mapping.md` - 状态码映射表
- `openapi_new.yaml` - API 接口文档

---

## 🎉 总结

验证码系统已**完全实现**，包括：
- ✅ 后端所有代码
- ✅ 邮箱验证码（真实发送）
- ✅ 短信验证码（云片网络 + 模拟）
- ✅ 注册和重置密码集成
- ✅ 防刷机制
- ✅ 自动切换真实/模拟发送

**只需填写配置即可使用！**

---

**最后更新时间**：2026-02-04
