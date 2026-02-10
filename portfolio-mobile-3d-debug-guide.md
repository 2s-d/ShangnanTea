# Portfolio 手机端 3D 模型加载问题调试指南

## 问题描述
- **桌面端**: 3D 茶杯模型加载正常
- **手机端**: 3D 模型无法加载，只显示背景光晕
- **时间线**: 
  - 昨天（IP + HTTP）: 手机可以加载，但慢（5-6秒）
  - 今天（域名 + HTTPS）: 手机完全无法加载

## 已部署的调试工具

### 1. 主应用详细日志
访问 `https://paku.uno` 并打开手机浏览器的控制台（使用 Chrome Remote Debugging）

**查看日志标签**:
- `[Init]` - 初始化信息（User Agent、URL、协议）
- `[WebGL]` - WebGL 支持检测
- `[Renderer]` - 渲染器创建
- `[Scene]` - 场景创建
- `[Camera]` - 相机创建
- `[Light]` - 灯光添加
- `[Draco]` - Draco 解码器配置和文件访问测试
- `[Model]` - 模型加载过程

### 2. 独立测试页面
访问 `https://paku.uno/test-draco.html`

这是一个独立的测试页面，不依赖 Vue，可以帮助排除框架相关问题。

**页面显示内容**:
- User Agent（设备信息）
- WebGL 支持状态
- WebAssembly 支持状态
- 当前协议（HTTP/HTTPS）
- 详细的加载日志
- 3D 模型预览（如果加载成功）

## 调试步骤

### 步骤 1: 使用 Chrome Remote Debugging
1. 在电脑上打开 Chrome，访问 `chrome://inspect`
2. 用数据线连接手机，或确保手机和电脑在同一网络
3. 在手机上打开 `https://paku.uno`
4. 在电脑的 Chrome DevTools 中查看手机浏览器的控制台

### 步骤 2: 检查关键日志

#### 必须通过的检查项:
- ✓ `[WebGL] WebGL 支持检测通过`
- ✓ `[Draco] ✓ 浏览器支持 WebAssembly`
- ✓ `[Draco] ✓ WebAssembly.Memory 测试通过`
- ✓ `[Draco] draco_decoder.wasm 响应状态: 200`
- ✓ `[Draco] draco_wasm_wrapper.js 响应状态: 200`
- ✓ `[Model] ✓ DRACOLoader 已设置到 GLTFLoader`

#### 如果失败，查看错误信息:
- `❌ 浏览器不支持 WebAssembly` → 手机浏览器太旧
- `draco_decoder.wasm 访问失败` → 文件路径或 HTTPS 问题
- `❌ DRACOLoader 未能创建` → Three.js 加载问题

### 步骤 3: 测试独立页面
访问 `https://paku.uno/test-draco.html`

如果独立页面可以加载，说明问题在 Vue 应用中。
如果独立页面也无法加载，说明是环境问题（浏览器、HTTPS、Draco 文件）。

## 可能的问题和解决方案

### 问题 1: WebAssembly 不支持
**症状**: 日志显示 `浏览器不支持 WebAssembly`

**原因**: 
- 手机浏览器版本太旧
- 某些国产浏览器阉割了 WebAssembly 支持

**解决方案**:
1. 更新手机浏览器到最新版本
2. 尝试使用 Chrome、Firefox、Safari 等主流浏览器
3. 如果无法解决，需要使用非 Draco 压缩的模型

### 问题 2: Draco 解码器文件无法加载
**症状**: 
- `draco_decoder.wasm 访问失败`
- `draco_wasm_wrapper.js 访问失败`

**原因**:
- HTTPS 混合内容问题
- 文件路径错误
- Nginx 配置问题（MIME 类型）

**解决方案**:
1. 检查 Nginx 配置，确保 `.wasm` 文件的 MIME 类型正确:
   ```nginx
   types {
       application/wasm wasm;
   }
   ```
2. 检查文件是否真的存在于服务器:
   ```bash
   ls -la /var/www/portfolio/draco/
   ```
3. 直接在浏览器访问测试:
   - `https://paku.uno/draco/draco_decoder.wasm`
   - `https://paku.uno/draco/draco_wasm_wrapper.js`

### 问题 3: 模型文件本身的问题
**症状**: Draco 文件加载成功，但模型加载失败

**原因**: 
- 模型文件损坏
- 模型文件太大，手机内存不足
- 模型使用了手机不支持的特性

**解决方案**:
1. 在桌面端重新导出模型，确保兼容性
2. 考虑使用更小的模型或降低质量
3. 使用非 Draco 压缩的 GLB 文件

### 问题 4: HTTPS 相关问题
**症状**: HTTP 可以加载，HTTPS 无法加载

**原因**:
- 混合内容（Mixed Content）阻止
- SSL 证书问题
- 某些资源仍使用 HTTP

**解决方案**:
1. 确保所有资源都使用 HTTPS 或相对路径
2. 检查浏览器控制台是否有混合内容警告
3. 检查 SSL 证书是否有效

## 临时解决方案：使用非压缩模型

如果 Draco 解码器在手机上始终无法工作，可以使用非压缩的 GLB 文件：

### 方案 A: 导出非压缩 GLB
使用 Blender 重新导出模型，不勾选 Draco 压缩选项。

### 方案 B: 条件加载
检测设备类型，手机使用简化版模型：

```javascript
const isMobile = /Android|webOS|iPhone|iPad|iPod/i.test(navigator.userAgent)
const modelPath = isMobile ? '/models/cloud_tea_simple.glb' : '/models/cloud_tea.glb'

// 手机端不使用 DRACOLoader
if (!isMobile) {
  const dracoLoader = new DRACOLoader()
  dracoLoader.setDecoderPath('/draco/')
  loader.setDRACOLoader(dracoLoader)
}
```

## 下一步行动

1. **立即**: 在手机上访问 `https://paku.uno/test-draco.html`，查看测试结果
2. **使用 Chrome Remote Debugging** 查看主应用的详细日志
3. **根据日志信息** 确定具体是哪个环节失败
4. **如果是 WebAssembly 问题**: 考虑使用非压缩模型
5. **如果是文件加载问题**: 检查服务器配置和文件权限
6. **如果是模型本身问题**: 重新导出或优化模型

## 联系信息

如果需要进一步帮助，请提供：
1. Chrome Remote Debugging 的完整控制台日志
2. 测试页面 (`test-draco.html`) 的显示结果
3. 手机型号和浏览器版本
4. 是否尝试过其他浏览器

---

**最后更新**: 2026-02-08
**部署状态**: ✓ 已部署到生产环境
