# API 测试

## 目录结构

```
e2e/
├── api-tests/              # API 测试
│   └── user/               # 用户模块
│       ├── login.spec.js
│       └── register.spec.js
├── helpers/                # 辅助工具
│   ├── api-helper.js
│   ├── auth-helper.js
│   └── error-collector.js
├── fixtures/               # 测试数据
│   └── test-data.json
├── run-api-test.bat        # 启动脚本
├── run-e2e.ps1             # PowerShell 脚本
└── README.md               # 本文档
```

## 运行测试

```bash
# 启动开发服务器（端口 8083）
npm run dev

# 运行测试
cd e2e
run-api-test.bat user/login      # 运行单个测试
run-api-test.bat user            # 运行用户模块
run-api-test.bat                 # 运行所有测试
```

## 测试进度

**总 API 数量**: 166  
**已完成**: 2 (1.2%)

### 用户模块 (2/35+)
- ✅ POST /user/login - 用户登录
- ✅ POST /user/register - 用户注册
- ⏳ POST /user/logout
- ⏳ GET /user/me
- ⏳ PUT /user
- ⏳ ... (其余 30+ 个)

### 其他模块
- ⏳ 茶叶模块 (约 40 个)
- ⏳ 订单模块 (约 30 个)
- ⏳ 论坛模块 (约 30 个)
- ⏳ 店铺模块 (约 20 个)
- ⏳ 消息模块 (约 15 个)

## 测试标准

每个测试验证：
1. API 调用成功（返回正确的业务状态码）
2. 无控制台错误

## 辅助工具

- `helpers/auth-helper.js` - 登录认证（mockLogin）
- `helpers/error-collector.js` - 错误收集
- `helpers/api-helper.js` - API 监控
- `fixtures/test-data.json` - 测试数据

## 参考

- API 定义: `../openapi_new.yaml`
- 测试示例: `api-tests/user/login.spec.js`
