# Portfolio 项目分离指南

## 当前情况
- portfolio 文件夹在商南茶城项目目录下
- 已经被提交到商南茶城的 Git 仓库
- node_modules 也被提交了（需要清理）

## 分离步骤

### 方案一：简单分离（推荐，快速）

#### 1. 创建 portfolio 独立仓库

```powershell
# 在商南茶城项目根目录外创建新文件夹
cd ..
mkdir portfolio-standalone
cd portfolio-standalone

# 初始化 Git 仓库
git init

# 复制 portfolio 文件夹内容（不包括 node_modules）
Copy-Item -Path "..\shangnantea\portfolio\*" -Destination "." -Recurse -Exclude "node_modules"

# 创建 .gitignore
@"
# 依赖
node_modules/
.pnp/
.pnp.js

# 构建输出
dist/
dist-ssr/
*.local

# 编辑器
.vscode/
.idea/
*.suo
*.ntvs*
*.njsproj
*.sln
*.sw?

# 系统文件
.DS_Store
Thumbs.db

# 日志
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
pnpm-debug.log*

# 环境变量
.env
.env.local
.env.*.local

# 临时文件
*.tmp
*.temp
*.bak
*.backup
"@ | Out-File -FilePath ".gitignore" -Encoding utf8

# 提交到本地仓库
git add .
git commit -m "Initial commit: Portfolio website"

# 在 GitHub 创建新仓库后，关联并推送
# git remote add origin https://github.com/你的用户名/portfolio.git
# git branch -M main
# git push -u origin main
```

#### 2. 从商南茶城仓库中移除 portfolio

```powershell
# 回到商南茶城项目
cd ..\shangnantea

# 将 portfolio 添加到 .gitignore
Add-Content -Path ".gitignore" -Value "`n# Portfolio 项目（已独立）`nportfolio/"

# 从 Git 追踪中移除（但保留本地文件）
git rm -r --cached portfolio/

# 提交更改
git add .gitignore
git commit -m "Remove portfolio folder from repository (moved to separate repo)"

# 推送到远程
git push origin main
```

---

### 方案二：保留 Git 历史（高级，如果你想保留 portfolio 的提交历史）

```powershell
# 1. 克隆商南茶城仓库
cd ..
git clone https://github.com/你的用户名/shangnantea.git portfolio-with-history
cd portfolio-with-history

# 2. 使用 git filter-branch 只保留 portfolio 文件夹的历史
git filter-branch --subdirectory-filter portfolio -- --all

# 3. 清理
git reset --hard
git gc --aggressive
git prune

# 4. 重命名远程仓库
git remote rename origin old-origin
git remote add origin https://github.com/你的用户名/portfolio.git

# 5. 推送到新仓库
git push -u origin main

# 6. 回到商南茶城项目，移除 portfolio
cd ..\shangnantea
git rm -r --cached portfolio/
echo "portfolio/" >> .gitignore
git add .gitignore
git commit -m "Remove portfolio folder (moved to separate repo)"
git push origin main
```

---

## 分离后的目录结构

### 之前
```
C:\wendang\bishe\tea1\shangnantea\
├── shangnantea-server/
├── shangnantea-web/
├── portfolio/          ← 在这里
├── docs/
└── ...
```

### 之后
```
C:\wendang\bishe\tea1\
├── shangnantea/
│   ├── shangnantea-server/
│   ├── shangnantea-web/
│   └── docs/
│
└── portfolio/          ← 独立项目
    ├── src/
    ├── public/
    ├── package.json
    └── ...
```

---

## 分离后的好处

1. **独立管理**
   - 商南茶城：`git clone https://github.com/你的用户名/shangnantea.git`
   - 个人博客：`git clone https://github.com/你的用户名/portfolio.git`

2. **独立部署**
   - 更新商南茶城不影响个人博客
   - 更新个人博客不需要拉取商南茶城代码

3. **仓库更干净**
   - 商南茶城仓库不包含 portfolio 的 node_modules
   - 每个项目的 .gitignore 更精准

4. **更专业**
   - GitHub 上两个独立项目
   - 个人博客可以单独展示给别人看

---

## 在 GitHub 上创建新仓库

1. 访问 https://github.com/new
2. 仓库名：`portfolio`
3. 描述：`Personal portfolio website with Vue 3 and multiple theme effects`
4. 选择 Public（公开）
5. **不要**勾选 "Add a README file"
6. **不要**勾选 "Add .gitignore"
7. 点击 "Create repository"
8. 复制仓库地址，用于上面的命令

---

## 注意事项

1. **备份**：分离前先备份整个 shangnantea 文件夹
2. **node_modules**：新仓库不要提交 node_modules
3. **环境变量**：如果有敏感信息，确保在 .gitignore 中
4. **远程推送**：分离后记得推送到 GitHub

---

## 快速命令（复制粘贴版）

```powershell
# === 第一步：创建独立仓库 ===
cd ..
mkdir portfolio-standalone
cd portfolio-standalone
git init

# 复制文件（排除 node_modules）
robocopy "..\shangnantea\portfolio" "." /E /XD node_modules

# 创建 .gitignore
"node_modules/`ndist/`n.vscode/`n*.log" | Out-File .gitignore -Encoding utf8

# 提交
git add .
git commit -m "Initial commit: Portfolio website"

# === 第二步：在 GitHub 创建仓库后 ===
# git remote add origin https://github.com/你的用户名/portfolio.git
# git push -u origin main

# === 第三步：从商南茶城移除 ===
cd ..\shangnantea
git rm -r --cached portfolio/
"portfolio/" | Add-Content .gitignore
git add .gitignore
git commit -m "Remove portfolio (moved to separate repo)"
git push origin main
```

---

## 完成后验证

```powershell
# 检查商南茶城仓库
cd shangnantea
git status  # 应该显示 portfolio/ 不再被追踪

# 检查 portfolio 仓库
cd ..\portfolio-standalone
git status  # 应该显示干净的工作树
git remote -v  # 应该显示新的 GitHub 仓库地址
```
