# 商南茶城前端自动部署指南

> 基于奇门遁甲项目的成功经验，为商南茶城项目配置自动化部署

## 📦 项目信息

- **项目名称**：商南茶城前端 (shangnantea-web)
- **GitHub 仓库**：https://github.com/2s-d/ShangnanTea
- **分支**：main
- **前端目录**：shangnantea-web
- **构建工具**：Vite
- **构建命令**：`npm run build`
- **构建产物**：dist/
- **访问域名**：tea.paku.uno
- **后端端口**：8081 (Spring Boot)

---

## ✅ 已完成的配置

### 1. GitHub Actions 工作流文件

✅ 已创建：`.github/workflows/deploy-frontend.yml`

**功能：**
- 监听 main 分支的 shangnantea-web 目录变化
- 自动构建前端项目
- 打包并上传到服务器
- 自动解压到 /var/www/tea 目录

### 2. Nginx 配置文件

✅ 已创建：`nginx-tea.conf`

**功能：**
- 静态文件托管（/var/www/tea）
- SPA 路由支持
- API 代理到本地后端（8081 端口）
- WebSocket 支持
- Gzip 压缩
- SSL/HTTPS 配置

---

## 🚀 部署步骤

### 步骤 1：配置 GitHub Secrets

在 GitHub 仓库设置中添加以下 4 个 Secrets：

1. 进入仓库：https://github.com/2s-d/ShangnanTea
2. 点击 **Settings** → **Secrets and variables** → **Actions**
3. 点击 **New repository secret** 添加以下密钥：

| Secret 名称 | 说明 | 获取方式 |
|------------|------|----------|
| `SERVER_HOST` | 服务器 IP 或域名 | 你的服务器地址 |
| `SERVER_SSH_PORT` | SSH 端口 | 通常是 `22` |
| `SERVER_USER` | SSH 用户名 | 通常是 `root` 或 `ubuntu` |
| `SERVER_SSH_KEY` | SSH 私钥 | 运行 `cat ~/.ssh/id_rsa` 获取 |

**获取 SSH 私钥示例：**
```bash
# 在本地电脑运行
cat ~/.ssh/id_rsa
# 复制完整输出（包括 BEGIN 和 END 行）
```

---

### 步骤 2：服务器准备

#### 2.1 创建部署目录

SSH 登录到服务器，运行：

```bash
# 创建前端部署目录
sudo mkdir -p /var/www/tea

# 设置权限（替换 $USER 为你的用户名）
sudo chown -R $USER:$USER /var/www/tea

# 验证目录
ls -la /var/www/
```

#### 2.2 配置 Nginx

**方法 1：添加到现有配置文件**

如果你已有 `/etc/nginx/sites-available/focus-apps.conf`，可以将 `nginx-tea.conf` 的内容追加进去。

**方法 2：创建独立配置文件**

```bash
# 上传配置文件到服务器
scp nginx-tea.conf your-server:/tmp/

# SSH 登录服务器
ssh your-server

# 移动配置文件
sudo mv /tmp/nginx-tea.conf /etc/nginx/sites-available/tea.conf

# 创建软链接启用配置
sudo ln -s /etc/nginx/sites-available/tea.conf /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重载 Nginx
sudo systemctl reload nginx
```

#### 2.3 配置 SSL 证书

如果 tea.paku.uno 还没有 SSL 证书，需要申请：

```bash
# 使用 Certbot 申请证书
sudo certbot --nginx -d tea.paku.uno

# 或者使用现有的通配符证书
# 修改 nginx-tea.conf 中的证书路径
```

**注意：** 当前配置使用的是 kanban.paku.uno 的证书，需要根据实际情况修改：

```nginx
ssl_certificate /etc/letsencrypt/live/tea.paku.uno/fullchain.pem;
ssl_certificate_key /etc/letsencrypt/live/tea.paku.uno/privkey.pem;
```

---

### 步骤 3：推送代码触发部署

配置完成后，推送代码到 GitHub：

```bash
# 在项目根目录
cd C:\wendang\bishe\tea1\shangnantea

# 添加新文件
git add .github/workflows/deploy-frontend.yml
git add nginx-tea.conf
git add DEPLOYMENT-GUIDE.md

# 提交
git commit -m "feat: 添加商南茶城前端自动部署配置"

# 推送到 main 分支
git push origin main
```

**GitHub Actions 会自动：**
1. ✅ 检测到 shangnantea-web 目录变化
2. ✅ 安装依赖并构建前端
3. ✅ 打包并上传到服务器
4. ✅ 解压到 /var/www/tea
5. ✅ 前端立即生效

---

## 🔍 验证部署

### 1. 检查 GitHub Actions

访问：https://github.com/2s-d/ShangnanTea/actions

- 查看工作流运行状态
- 如果失败，查看日志排查问题

### 2. 检查服务器文件

```bash
# SSH 登录服务器
ssh your-server

# 检查文件是否部署成功
ls -la /var/www/tea/
# 应该看到 index.html 和其他静态文件

# 检查 Nginx 状态
sudo systemctl status nginx

# 查看 Nginx 日志
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

### 3. 访问网站

打开浏览器访问：**https://tea.paku.uno**

- ✅ 页面正常加载
- ✅ 路由跳转正常（SPA）
- ✅ API 请求正常（代理到 8081 端口）

---

## 🔧 后端配置

### 本地运行后端 + 内网穿透

后端继续在本地运行，使用内网穿透（cpolar 等）：

```bash
# 在本地运行后端
cd shangnantea-server
# 启动 Spring Boot 应用（端口 8081）

# 使用 cpolar 创建隧道
cpolar http 8081
```

### 配置内网穿透到服务器

在服务器上配置反向代理，将 127.0.0.1:8081 映射到内网穿透地址。

或者直接在服务器上运行后端（如果需要）。

---

## 📝 后续修改前端

每次修改 shangnantea-web 目录下的代码后：

```bash
# 提交并推送
git add shangnantea-web/
git commit -m "feat: 更新前端功能"
git push origin main

# GitHub Actions 会自动部署
# 等待 2-3 分钟后刷新 https://tea.paku.uno
```

---

## ⚠️ 注意事项

### 1. 环境变量配置

检查 `shangnantea-web/.env.production` 文件：

```env
# API 基础路径（使用相对路径，通过 Nginx 代理）
VITE_API_BASE_URL=/api

# 或使用完整域名
VITE_API_BASE_URL=https://tea.paku.uno/api
```

### 2. 构建优化

如果构建时间过长，可以考虑：

- 使用 npm ci 代替 npm install
- 启用构建缓存
- 优化依赖项

### 3. 回滚策略

如果部署出现问题，可以：

```bash
# SSH 登录服务器
ssh your-server

# 备份当前版本
cd /var/www
sudo cp -r tea tea-backup-$(date +%Y%m%d-%H%M%S)

# 恢复之前的版本
sudo cp -r tea-backup-YYYYMMDD-HHMMSS tea
```

---

## 📊 部署架构

```
GitHub (main 分支)
    ↓ push 触发
GitHub Actions
    ↓ 构建 + 打包
SCP 上传到服务器
    ↓ /var/www/tea/
Nginx 静态托管
    ↓ https://tea.paku.uno
用户访问
    ↓ API 请求 /api/*
Nginx 代理
    ↓ http://127.0.0.1:8081
本地后端（内网穿透）
```

---

## 🎯 下一步

- [ ] 配置 GitHub Secrets（4 个密钥）
- [ ] 服务器创建 /var/www/tea 目录
- [ ] 配置 Nginx（tea.conf）
- [ ] 申请 SSL 证书（tea.paku.uno）
- [ ] 推送代码触发首次部署
- [ ] 验证网站访问正常
- [ ] 配置后端内网穿透
- [ ] 测试 API 代理功能

---

## 📚 参考文档

- **部署模板**：`FRONTEND_DEPLOY_TEMPLATE.md`
- **奇门遁甲配置**：`qimen/.github/workflows/deploy-frontend.yml`
- **GitHub Actions 文档**：https://docs.github.com/actions
- **Nginx 文档**：https://nginx.org/en/docs/

---

**创建时间**：2026-05-17  
**项目**：商南茶城前端 (shangnantea-web)  
**状态**：✅ 配置完成，等待部署
