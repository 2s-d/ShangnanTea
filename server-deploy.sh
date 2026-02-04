#!/bin/bash

# 商南茶项目服务器一键部署脚本
# 适用于 Ubuntu 22.04/24.04
# 作者：Kiro AI Assistant
# 日期：2026-02-05

set -e  # 遇到错误立即退出

echo "=========================================="
echo "  商南茶项目服务器环境部署脚本"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印函数
print_success() {
    echo -e "${GREEN}[成功]${NC} $1"
}

print_error() {
    echo -e "${RED}[错误]${NC} $1"
}

print_info() {
    echo -e "${YELLOW}[信息]${NC} $1"
}

# 检查是否为 root 用户
if [ "$EUID" -ne 0 ]; then 
    print_error "请使用 root 用户运行此脚本"
    exit 1
fi

print_info "开始部署环境..."

# 1. 更新系统
print_info "步骤 1/8: 更新系统软件包..."
apt update -y
apt upgrade -y
print_success "系统更新完成"

# 2. 安装基础工具
print_info "步骤 2/8: 安装基础工具..."
apt install -y curl wget git vim unzip software-properties-common
print_success "基础工具安装完成"

# 3. 安装 Java 21
print_info "步骤 3/8: 安装 Java 21..."
apt install -y openjdk-21-jdk
java -version
print_success "Java 21 安装完成"

# 4. 安装 Maven
print_info "步骤 4/8: 安装 Maven..."
apt install -y maven
mvn -version
print_success "Maven 安装完成"

# 5. 安装 MySQL 8.0
print_info "步骤 5/8: 安装 MySQL 8.0..."
apt install -y mysql-server
systemctl start mysql
systemctl enable mysql

# 设置 MySQL root 密码
MYSQL_ROOT_PASSWORD="Shangnantea@2026"
print_info "设置 MySQL root 密码为: ${MYSQL_ROOT_PASSWORD}"
mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}';"
mysql -e "FLUSH PRIVILEGES;"

print_success "MySQL 8.0 安装完成"

# 6. 安装 Node.js 18
print_info "步骤 6/8: 安装 Node.js 18..."
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt install -y nodejs
node -v
npm -v
print_success "Node.js 18 安装完成"

# 7. 安装 Nginx
print_info "步骤 7/8: 安装 Nginx..."
apt install -y nginx
systemctl start nginx
systemctl enable nginx
print_success "Nginx 安装完成"

# 8. 安装 Redis（可选，如果需要）
print_info "步骤 8/8: 安装 Redis..."
apt install -y redis-server
systemctl start redis-server
systemctl enable redis-server
print_success "Redis 安装完成"

# 创建项目目录
print_info "创建项目目录..."
mkdir -p /var/www/shangnantea
mkdir -p /opt/shangnantea-server
print_success "项目目录创建完成"

# 配置防火墙
print_info "配置防火墙规则..."
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP
ufw allow 443/tcp   # HTTPS
ufw allow 8080/tcp  # 后端服务
ufw allow 8082/tcp  # 前端服务（开发用）
print_success "防火墙规则配置完成"

echo ""
echo "=========================================="
echo "  环境部署完成！"
echo "=========================================="
echo ""
print_success "所有软件已安装完成！"
echo ""
echo "安装的软件版本："
echo "  - Java: $(java -version 2>&1 | head -n 1)"
echo "  - Maven: $(mvn -version | head -n 1)"
echo "  - Node.js: $(node -v)"
echo "  - npm: $(npm -v)"
echo "  - MySQL: $(mysql --version)"
echo "  - Nginx: $(nginx -v 2>&1)"
echo "  - Redis: $(redis-server --version)"
echo ""
echo "MySQL 信息："
echo "  - Root 密码: ${MYSQL_ROOT_PASSWORD}"
echo "  - 请妥善保管此密码！"
echo ""
echo "项目目录："
echo "  - 前端: /var/www/shangnantea"
echo "  - 后端: /opt/shangnantea-server"
echo ""
echo "服务器 IP 地址："
echo "  - $(curl -s ifconfig.me)"
echo ""
print_info "下一步：上传项目代码并部署"
echo "=========================================="
