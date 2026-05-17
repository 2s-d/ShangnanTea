# 剩余2个前端项目部署指南

> 服务器端已全部配置完成，只需为各项目添加 GitHub Actions 工作流

## ✅ 已完成的项目

1. **奇门遁甲小程序** (qimen) → qimen.paku.uno ✅
2. **商南茶城前端** (shangnantea-web) → tea.paku.uno ✅
3. **茶城品牌官网** (shangnantea-official) → teagw.paku.uno ✅

## 🎯 待配置的项目

### 1. 专注大数据看板 (zhuanzhu-kanban)

**仓库**: https://github.com/2s-d/zhunanzhu-kanban  
**域名**: kanban.paku.uno  
**服务器目录**: /var/www/kanban ✅ 已创建  
**Nginx 配置**: ✅ 已配置（纯静态托管）

**需要做的**:
1. Clone 仓库到本地（如果还没有）
2. 创建 `.github/workflows/deploy-frontend.yml`：

```yaml
name: Deploy Focus Kanban Frontend to Server

on:
  push:
    branches:
      - master  # 或 main，根据实际分支
    paths:
      - '**'
      - '.github/workflows/deploy-frontend.yml'

jobs:
  deploy-frontend:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install dependencies
        run: npm install
      
      - name: Build frontend
        run: npm run build
        env:
          NODE_ENV: production
      
      - name: Pack frontend dist (flat for web root)
        run: cd dist && tar czf ../kanban-frontend-dist.tar.gz .
      
      - name: Deploy frontend tarball to server
        uses: appleboy/scp-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          port: ${{ secrets.SERVER_SSH_PORT }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          source: "kanban-frontend-dist.tar.gz"
          target: "/var/www/kanban/"
          overwrite: true
      
      - name: Extract frontend on server
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          port: ${{ secrets.SERVER_SSH_PORT }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          command_timeout: 5m
          script: |
            set -e
            cd /var/www/kanban
            tar xzf kanban-frontend-dist.tar.gz
            rm -f kanban-frontend-dist.tar.gz
            echo "✅ 专注大数据看板前端部署完成: $(date)"
```

3. 配置 GitHub Secrets（4个密钥，应该已经配置过）
4. 推送代码触发部署

---

### 2. 专注 Flutter Web (zhuanzhu-app)

**仓库**: https://github.com/2s-d/zhuanzhu-app  
**域名**: zhuanzhu.paku.uno  
**服务器目录**: /var/www/zhuanzhu ✅ 已创建  
**Nginx 配置**: ✅ 已配置（纯静态托管）

**需要做的**:
1. Clone 仓库到本地（如果还没有）
2. 创建 `.github/workflows/deploy-frontend.yml`：

```yaml
name: Deploy Focus Flutter Web to Server

on:
  push:
    branches:
      - main  # 或 master，根据实际分支
    paths:
      - '**'
      - '.github/workflows/deploy-frontend.yml'

jobs:
  deploy-frontend:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Setup Flutter
        uses: subosito/flutter-action@v2
        with:
          flutter-version: '3.x'  # 根据项目需要调整
          channel: 'stable'
      
      - name: Install dependencies
        run: flutter pub get
      
      - name: Build Flutter Web
        run: flutter build web --release
      
      - name: Pack frontend dist (flat for web root)
        run: cd build/web && tar czf ../../zhuanzhu-frontend-dist.tar.gz .
      
      - name: Deploy frontend tarball to server
        uses: appleboy/scp-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          port: ${{ secrets.SERVER_SSH_PORT }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          source: "zhuanzhu-frontend-dist.tar.gz"
          target: "/var/www/zhuanzhu/"
          overwrite: true
      
      - name: Extract frontend on server
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          port: ${{ secrets.SERVER_SSH_PORT }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          command_timeout: 5m
          script: |
            set -e
            cd /var/www/zhuanzhu
            tar xzf zhuanzhu-frontend-dist.tar.gz
            rm -f zhuanzhu-frontend-dist.tar.gz
            echo "✅ 专注Flutter Web前端部署完成: $(date)"
```

3. 配置 GitHub Secrets（4个密钥，应该已经配置过）
4. 推送代码触发部署

---

## 🔑 GitHub Secrets 配置

所有项目需要配置相同的4个 Secrets：

| Secret 名称 | 值 |
|------------|-----|
| `SERVER_HOST` | 108.160.131.86 |
| `SERVER_SSH_PORT` | 1234 |
| `SERVER_USER` | root |
| `SERVER_SSH_KEY` | SSH 私钥内容 |

**配置方法**：
1. 进入 GitHub 仓库
2. Settings → Secrets and variables → Actions
3. New repository secret
4. 添加上述4个密钥

---

## 📊 服务器端配置总结

### Nginx 配置 (focus-apps.conf)

所有4个前端项目都已配置为**纯静态托管**：

```nginx
# kanban.paku.uno → /var/www/kanban
# tea.paku.uno → /var/www/tea
# teagw.paku.uno → /var/www/teagw
# zhuanzhu.paku.uno → /var/www/zhuanzhu
```

每个配置都包含：
- ✅ 静态文件托管（root + index.html）
- ✅ SPA 路由支持（try_files $uri $uri/ /index.html）
- ✅ Gzip 压缩
- ✅ SSL/HTTPS
- ❌ 无后端代理（纯静态）

### 部署目录

```bash
/var/www/
├── kanban/     # 专注大数据看板
├── tea/        # 商南茶城前端
├── teagw/      # 茶城品牌官网
├── zhuanzhu/   # 专注 Flutter Web
└── qimen/      # 奇门遁甲小程序
```

所有目录已创建，权限已设置，Nginx 已重载。

---

## ✅ 验证部署

部署完成后，访问以下域名验证：

- https://kanban.paku.uno （专注大数据看板）
- https://tea.paku.uno （商南茶城前端）✅ 已部署
- https://teagw.paku.uno （茶城品牌官网）
- https://zhuanzhu.paku.uno （专注 Flutter Web）
- https://qimen.paku.uno （奇门遁甲小程序）✅ 已部署

---

## 🎉 总结

**服务器端**: ✅ 100% 完成  
**GitHub Actions**: 
- ✅ qimen (奇门遁甲)
- ✅ shangnantea-web (商南茶城)
- ✅ shangnantea-official (茶城官网)
- ⏳ zhuanzhu-kanban (专注看板) - 待配置
- ⏳ zhuanzhu-app (专注Flutter) - 待配置

只需为最后2个项目添加 GitHub Actions 工作流文件即可！

---

**创建时间**: 2026-05-17  
**服务器**: 108.160.131.86:1234  
**状态**: 服务器端全部就绪，等待前端代码推送
