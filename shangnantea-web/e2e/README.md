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
run-api-test.bat                 # 交互式选择测试
run-api-test.bat user/login      # 直接运行指定测试
```

### 交互式选择

运行 `run-api-test.bat` 后会显示菜单：

```
请选择要测试的接口：

[用户模块]
  1. POST /user/login - 用户登录
  2. POST /user/register - 用户注册

[其他选项]
  0. 运行所有测试
  q. 退出

请输入序号:
```

## 测试进度

**总 API 数量**: 166  
**已完成**: 2 (1.2%)

### 测试脚本列表

#### 用户模块 (2/35+)
| 编号 | API | 状态 | 文件 |
|------|-----|------|------|
| 1 | POST /user/login | ✅ | user/login.spec.js |
| 2 | POST /user/register | ✅ | user/register.spec.js |
| 3 | POST /user/logout | ⏳ | user/logout.spec.js |
| 4 | GET /user/me | ⏳ | user/me.spec.js |
| 5 | PUT /user | ⏳ | user/update.spec.js |
| 6 | POST /user/avatar | ⏳ | user/avatar.spec.js |
| 7 | PUT /user/password | ⏳ | user/password.spec.js |
| 8 | POST /user/password/reset | ⏳ | user/reset-password.spec.js |
| 9 | GET /user/addresses | ⏳ | user/addresses-list.spec.js |
| 10 | POST /user/addresses | ⏳ | user/addresses-add.spec.js |
| ... | ... | ⏳ | ... |

#### 茶叶模块 (0/40+)
| 编号 | API | 状态 | 文件 |
|------|-----|------|------|
| 50 | GET /tea/list | ⏳ | tea/list.spec.js |
| 51 | POST /tea/list | ⏳ | tea/add.spec.js |
| 52 | GET /tea/{id} | ⏳ | tea/detail.spec.js |
| ... | ... | ⏳ | ... |

#### 订单模块 (0/30+)
| 编号 | API | 状态 | 文件 |
|------|-----|------|------|
| 100 | GET /order/list | ⏳ | order/list.spec.js |
| 101 | POST /order | ⏳ | order/create.spec.js |
| ... | ... | ⏳ | ... |

#### 论坛模块 (0/30+)
| 编号 | API | 状态 | 文件 |
|------|-----|------|------|
| 130 | GET /forum/list | ⏳ | forum/list.spec.js |
| ... | ... | ⏳ | ... |

#### 店铺模块 (0/20+)
| 编号 | API | 状态 | 文件 |
|------|-----|------|------|
| 150 | GET /shop/list | ⏳ | shop/list.spec.js |
| ... | ... | ⏳ | ... |

#### 消息模块 (0/15+)
| 编号 | API | 状态 | 文件 |
|------|-----|------|------|
| 160 | GET /message/center | ⏳ | message/center.spec.js |
| ... | ... | ⏳ | ... |

## 测试报告

测试完成后，报告保存在 `e2e-report/` 目录：

```
e2e-report/
├── test-report.json           # 测试报告（每次覆盖）
└── error-screenshot.png       # 错误截图（仅在失败时生成）
```

**报告内容**：
- 测试统计（总数、通过、失败）
- 错误详情（类型、消息、时间）
- 测试时间戳

**查看报告**：
```bash
cat e2e-report/test-report.json
```

## 辅助工具

- `helpers/auth-helper.js` - 登录认证（mockLogin）
- `helpers/error-collector.js` - 错误收集
- `helpers/api-helper.js` - API 监控
- `fixtures/test-data.json` - 测试数据

## 参考

- API 定义: `../openapi_new.yaml`
- 测试示例: `api-tests/user/login.spec.js`
