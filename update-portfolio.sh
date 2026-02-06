#!/bin/bash
# 个人博客更新脚本
# 用于更新已部署的个人博客项目（纯前端）

set -e

echo "=========================================="
echo "  个人博客更新脚本"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}[成功]${NC} $1"
}

print_error() {
    echo -e "${RED}[错误]${NC} $1"
}

print_info() {
    echo -e "${YELLOW}[信息]${NC} $1"
}

# 项目路径配置
PORTFOLIO_DIR="/var/www/portfolio"

# 检查是否为 root 用户
if [ "$EUID" -ne 0 ]; then 
    print_error "请使用 root 用户运行此脚本"
    exit 1
fi

print_info "开始更新个人博客..."

# 1. 进入项目目录
cd $PORTFOLIO_DIR

# 2. 拉取最新代码（如果使用 Git）
if [ -d ".git" ]; then
    print_info "拉取最新代码..."
    git pull origin main
    print_success "代码拉取完成"
else
    print_info "未检测到 Git 仓库，跳过代码拉取"
    print_info "请手动上传新代码到 $PORTFOLIO_DIR"
    read -p "代码已上传？按回车继续..."
fi

# 3. 安装依赖（如果 package.json 有变化）
print_info "检查并安装依赖..."
npm install

# 4. 构建项目
print_info "开始构建项目..."
npm run build

if [ $? -eq 0 ]; then
    print_success "项目构建成功"
else
    print_error "项目构建失败，请检查错误信息"
    exit 1
fi

# 5. 备份旧文件
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
if [ -d "dist.backup" ]; then
    rm -rf dist.backup
fi
if [ -d "dist" ]; then
    print_info "备份旧的前端文件..."
    mv dist dist.backup_$TIMESTAMP
    print_success "旧文件已备份为 dist.backup_$TIMESTAMP"
fi

# 6. 重启 Nginx
print_info "重新加载 Nginx 配置..."
systemctl reload nginx
print_success "Nginx 已重新加载"

echo ""
echo "=========================================="
echo "  更新完成！"
echo "=========================================="
echo ""
print_success "个人博客已成功更新"
echo ""
echo "访问地址："
echo "  - http://$(curl -s ifconfig.me)/portfolio/"
echo ""
echo "常用命令："
echo "  - 查看 Nginx 日志: tail -f /var/log/nginx/access.log"
echo "  - 重启 Nginx: systemctl restart nginx"
echo "  - 恢复备份: rm -rf dist && mv dist.backup_$TIMESTAMP dist"
echo "=========================================="
