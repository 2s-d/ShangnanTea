# 🚀 快速调试步骤

## 第一步：测试页面（1分钟）
在手机上访问: **`https://paku.uno/test-draco.html`**

看看：
- WebGL: 是绿色 ✓ 还是红色 ✗？
- WebAssembly: 是绿色 ✓ 还是红色 ✗？
- 有没有看到旋转的茶杯？
- 日志里有没有红色错误？

## 第二步：Chrome 调试（2分钟）
1. 电脑打开 Chrome，地址栏输入: `chrome://inspect`
2. 手机打开 `https://paku.uno`
3. 在电脑上点击手机浏览器下面的 "inspect"
4. 查看 Console 标签，找带 `[Draco]` 和 `[Model]` 的日志

## 第三步：检查服务器（1分钟）
在你的电脑 PowerShell 运行:
```powershell
ssh root@108.160.131.86 'bash -s' < check-server-draco-files.sh
```

看看 Draco 文件是否存在，大小是否正常。

## 第四步：修复 MIME 类型（如果需要）
如果第三步发现 MIME 类型问题，运行:
```powershell
ssh root@108.160.131.86
bash fix-nginx-wasm-mime.sh
exit
```

## 告诉我这些信息

完成后，告诉我：
1. **测试页面显示什么？**（截图最好）
2. **Chrome 调试看到什么错误？**（复制日志）
3. **服务器检查的结果？**（复制输出）
4. **你的手机和浏览器？**（例如：小米 12 + 小米浏览器）

---

## 常见结果和解决方案

### ✅ 测试页面能看到茶杯
→ 问题在主应用，需要进一步调试

### ❌ WebAssembly 显示红色
→ 浏览器不支持，换 Chrome 试试

### ❌ Draco 文件 404
→ 服务器文件问题，需要重新部署

### ❌ MIME 类型错误
→ 运行 `fix-nginx-wasm-mime.sh` 修复

---

**所有详细信息**: 查看 `mobile-3d-issue-summary.md`
