# 云片网络短信服务接入指南

## 一、注册云片网络账号

1. 访问云片网络官网：https://www.yunpian.com
2. 点击"注册"，完成个人/企业注册
3. 完成实名认证（个人可用身份证认证）

## 二、获取ApiKey

1. 登录云片网络控制台
2. 进入"账户设置" → "API密钥"
3. 复制你的ApiKey（类似：`1234567890abcdef1234567890abcdef`）

## 三、配置项目

### 1. 修改 `application.yml`

```yaml
yunpian:
  sms:
    enabled: true  # 改为true启用真实发送
    api-key: 你的ApiKey  # 粘贴你的ApiKey
```

### 2. 重启项目

配置完成后重启Spring Boot项目即可。

## 四、测试

### 1. 模拟发送（默认）

```yaml
yunpian:
  sms:
    enabled: false  # false表示模拟发送
```

- 不会真实发送短信
- 只在日志中打印
- 不消耗短信额度

### 2. 真实发送

```yaml
yunpian:
  sms:
    enabled: true  # true表示真实发送
    api-key: 你的ApiKey
```

- 会真实发送短信到用户手机
- 消耗短信额度（约0.05元/条）

## 五、云片网络短信内容规范

### 签名要求
- 必须包含【签名】
- 例如：【商南茶城】

### 内容要求
- 不能包含违禁词
- 验证码短信无需审核模板
- 格式：【签名】内容

### 示例
```
【商南茶城】您正在进行注册操作，验证码是：123456，5分钟内有效，请勿泄露。
```

## 六、费用说明

- 注册送测试额度
- 正式使用约 0.05元/条
- 充值最低50元起

## 七、常见问题

### Q1: 短信发送失败？
A: 检查ApiKey是否正确，账户余额是否充足

### Q2: 收不到短信？
A: 检查手机号格式，确认不在黑名单中

### Q3: 如何切换回模拟发送？
A: 将 `yunpian.sms.enabled` 改为 `false`

## 八、代码说明

### 自动切换逻辑

```java
if (smsEnabled && yunpianApiKey != null && !yunpianApiKey.isEmpty()) {
    // 真实发送
    return sendYunpianSms(phone, content);
} else {
    // 模拟发送
    logger.info("【模拟发送短信】...");
    return true;
}
```

- `enabled=true` 且 `api-key` 不为空 → 真实发送
- 其他情况 → 模拟发送

## 九、优势

✅ 个人可申请  
✅ 实名认证即可  
✅ 无需企业资质  
✅ 接入简单  
✅ 价格便宜  
✅ 支持测试额度  

---

**配置完成后，你的验证码功能就能真实发送短信了！**
