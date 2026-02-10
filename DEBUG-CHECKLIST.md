# 🔍 调试检查清单

## 准备工作
- [ ] 确保手机和电脑在同一网络（用于 Chrome Remote Debugging）
- [ ] 或准备数据线连接手机和电脑
- [ ] 电脑上安装了 Chrome 浏览器
- [ ] 可以 SSH 连接到服务器

---

## 测试 1: 独立测试页面

### 操作
在手机上访问: `https://paku.uno/test-draco.html`

### 检查项
- [ ] 页面能否正常打开？
- [ ] WebGL 状态: ✓ 支持 / ✗ 不支持
- [ ] WebAssembly 状态: ✓ 支持 / ✗ 不支持
- [ ] 协议显示: https:
- [ ] 能否看到旋转的茶杯模型？
- [ ] 日志中是否有红色错误？

### 记录结果
```
WebGL: [填写]
WebAssembly: [填写]
模型显示: [是/否]
错误信息: [填写]
```

---

## 测试 2: Chrome Remote Debugging

### 操作步骤
1. 电脑打开 Chrome
2. 地址栏输入: `chrome://inspect`
3. 手机打开 `https://paku.uno`
4. 在电脑的 Chrome DevTools 中点击 "inspect"
5. 切换到 "Console" 标签

### 检查项
- [ ] 能否看到手机浏览器的控制台？
- [ ] 是否有 `[Init]` 开头的日志？
- [ ] 是否有 `[WebGL]` 检测通过的日志？
- [ ] 是否有 `[Draco]` 相关的日志？
- [ ] 是否有 `[Model]` 加载的日志？
- [ ] 是否有红色错误信息？

### 关键日志检查
```
期望看到:
✓ [WebGL] WebGL 支持检测通过
✓ [Draco] ✓ 浏览器支持 WebAssembly
✓ [Draco] draco_decoder.wasm 响应状态: 200
✓ [Model] ✓ 模型加载成功

实际看到:
[复制粘贴实际日志]
```

---

## 测试 3: 服务器文件检查

### 操作
在电脑 PowerShell 运行:
```powershell
.\check-server.ps1
```

或者:
```powershell
ssh root@108.160.131.86 'bash -s' < check-server-draco-files.sh
```

### 检查项
- [ ] `/var/www/portfolio/draco/` 目录存在？
- [ ] `draco_decoder.wasm` 文件存在？大小约 285KB？
- [ ] `draco_wasm_wrapper.js` 文件存在？
- [ ] `/var/www/portfolio/models/cloud_tea.glb` 存在？大小约 5.7MB？
- [ ] WASM MIME 类型已配置？
- [ ] Nginx 服务运行正常？

### 记录结果
```
Draco 目录: [存在/不存在]
draco_decoder.wasm: [大小]
draco_wasm_wrapper.js: [大小]
cloud_tea.glb: [大小]
MIME 类型: [已配置/未配置]
```

---

## 测试 4: 直接访问文件

### 操作
在手机浏览器直接访问以下 URL:

1. `https://paku.uno/draco/draco_decoder.wasm`
2. `https://paku.uno/draco/draco_wasm_wrapper.js`
3. `https://paku.uno/models/cloud_tea.glb`

### 检查项
- [ ] draco_decoder.wasm 能否下载？
- [ ] draco_wasm_wrapper.js 能否下载？
- [ ] cloud_tea.glb 能否下载？
- [ ] 是否有 SSL 证书错误？
- [ ] 是否有 404 错误？

### 记录结果
```
draco_decoder.wasm: [可访问/404/其他错误]
draco_wasm_wrapper.js: [可访问/404/其他错误]
cloud_tea.glb: [可访问/404/其他错误]
```

---

## 测试 5: 不同浏览器测试

### 操作
在手机上尝试不同浏览器访问 `https://paku.uno`

### 检查项
- [ ] Chrome: [能加载/不能加载]
- [ ] Firefox: [能加载/不能加载]
- [ ] Safari (iOS): [能加载/不能加载]
- [ ] 小米浏览器: [能加载/不能加载]
- [ ] UC 浏览器: [能加载/不能加载]
- [ ] QQ 浏览器: [能加载/不能加载]
- [ ] 微信内置浏览器: [能加载/不能加载]

### 记录结果
```
能加载的浏览器: [列出]
不能加载的浏览器: [列出]
```

---

## 问题诊断

### 情况 A: WebAssembly 不支持
**症状**: 
- [ ] 测试页面显示 WebAssembly ✗ 不支持
- [ ] Chrome 调试看到 "浏览器不支持 WebAssembly"

**原因**: 浏览器版本太旧或不支持 WebAssembly

**解决方案**:
1. 更新浏览器到最新版本
2. 使用 Chrome 或 Firefox
3. 如果都不行，需要使用非 Draco 压缩的模型

---

### 情况 B: Draco 文件无法访问
**症状**:
- [ ] 直接访问 WASM 文件返回 404
- [ ] Chrome 调试看到 "draco_decoder.wasm 访问失败"

**原因**: 文件未正确部署或路径错误

**解决方案**:
1. 检查 GitHub Actions 部署日志
2. 手动检查服务器文件
3. 重新触发部署

---

### 情况 C: MIME 类型错误
**症状**:
- [ ] 文件可以访问但 Content-Type 不是 `application/wasm`
- [ ] Chrome 调试看到 MIME 类型相关错误

**原因**: Nginx 未配置 WASM MIME 类型

**解决方案**:
运行修复脚本:
```bash
ssh root@108.160.131.86
bash fix-nginx-wasm-mime.sh
```

---

### 情况 D: 模型文件问题
**症状**:
- [ ] Draco 文件加载成功
- [ ] 但模型加载失败
- [ ] Chrome 调试看到 "模型加载失败"

**原因**: 模型文件损坏或格式问题

**解决方案**:
1. 重新导出模型
2. 使用非 Draco 压缩版本
3. 检查模型文件完整性

---

### 情况 E: HTTPS 混合内容
**症状**:
- [ ] Chrome 调试看到 "Mixed Content" 警告
- [ ] 某些资源被阻止

**原因**: HTTPS 页面加载 HTTP 资源

**解决方案**:
确保所有资源使用 HTTPS 或相对路径

---

## 修复步骤

### 如果是 MIME 类型问题
```bash
# 1. 连接服务器
ssh root@108.160.131.86

# 2. 运行修复脚本
bash fix-nginx-wasm-mime.sh

# 3. 退出
exit

# 4. 清除手机浏览器缓存
# 5. 重新测试
```

### 如果是文件缺失问题
```bash
# 1. 检查 GitHub Actions 部署状态
# 访问: https://github.com/2s-d/portfolio/actions

# 2. 如果部署失败，重新推送
git commit --allow-empty -m "触发重新部署"
git push

# 3. 等待部署完成（约 2-3 分钟）
# 4. 重新测试
```

### 如果是浏览器兼容性问题
```
方案 1: 建议用户使用 Chrome
方案 2: 提供非 Draco 压缩的降级版本
方案 3: 添加浏览器检测和提示
```

---

## 完成后提供的信息

请将以下信息发给我:

### 1. 环境信息
```
手机型号: [填写]
操作系统: [Android/iOS] [版本]
浏览器: [名称] [版本]
网络: [WiFi/4G/5G]
```

### 2. 测试结果
```
测试页面: [能显示/不能显示]
WebGL: [支持/不支持]
WebAssembly: [支持/不支持]
模型显示: [是/否]
```

### 3. 日志信息
```
[复制粘贴 Chrome Remote Debugging 的完整日志]
```

### 4. 服务器检查结果
```
[复制粘贴 check-server.ps1 的输出]
```

### 5. 截图
- [ ] 测试页面截图
- [ ] Chrome DevTools 控制台截图
- [ ] 主应用截图（显示问题）

---

**创建时间**: 2026-02-08
**用途**: 系统化诊断手机端 3D 模型加载问题
