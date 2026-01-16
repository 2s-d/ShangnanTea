# E2E 测试运行指南

## 方法 1：手动运行（推荐用于调试）

### 步骤 1：打开第一个终端，启动开发服务器
```bash
cd C:\wendang\bishe\tea1\shangnantea\shangnantea-web
npm run serve
```

等待看到类似输出：
```
App running at:
- Local:   http://localhost:8083/
```

### 步骤 2：打开第二个终端，运行测试
```bash
cd C:\wendang\bishe\tea1\shangnantea\shangnantea-web
npx playwright test
```

### 步骤 3：查看报告
```bash
npx playwright show-report e2e-report
```

---

## 方法 2：使用脚本（一键运行）

双击 `shangnantea-web/e2e/run-test.bat` 即可。

---

## 测试报告位置

- **HTML 报告**: `e2e-report/index.html`
- **JSON 报告**: `e2e-report/error-summary.json`
- **截图**: `e2e-screenshots/`

### 查看错误汇总

运行以下命令查看友好格式的错误汇总：
```bash
node e2e/show-errors.js
```

这会显示：
- 总测试页面数和错误统计
- 错误类型分布（控制台、网络、资源、运行时）
- 每个页面的详细错误信息和堆栈跟踪

---

## 常见问题

### Q: 端口 8083 被占用？
A: 先停止占用端口的进程，或修改 `playwright.config.js` 中的 `baseURL`

### Q: 测试失败？
A: 查看 `e2e-report/index.html` 中的详细错误信息和截图

### Q: 如何只运行特定测试？
A: `npx playwright test console-error-scan.spec.js`
