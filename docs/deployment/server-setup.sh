#!/bin/bash
# Vultr 服务器一键部署脚本
# 适用于 Ubuntu 22.04/20.04

echo "=========================================="
echo "开始配置服务器环境..."
echo "=========================================="

# 更新系统
echo "1. 更新系统包..."
apt update && apt upgrade -y

# 安装基础工具
echo "2. 安装基础工具..."
apt install -y curl wget git vim unzip

# 安装 Java 21
echo "3. 安装 Java 21..."
apt install -y openjdk-21-jdk
java -version

# 安装 Maven
echo "4. 安装 Maven..."
apt install -y maven
mvn -version

# 安装 MySQL 8.0
echo "5. 安装 MySQL 8.0..."
apt install -y mysql-server
systemctl start mysql
systemctl enable mysql

# 配置 MySQL（设置 root 密码为 123456）
echo "6. 配置 MySQL..."
mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123456';"
mysql -e "FLUSH PRIVILEGES;"

# 安装 Node.js 18
echo "7. 安装 Node.js 18..."
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt install -y nodejs
node -v
npm -v

# 安装 Nginx
echo "8. 安装 Nginx..."
apt install -y nginx
systemctl start nginx
systemctl enable nginx

# 创建项目目录
echo "9. 创建项目目录..."
mkdir -p /var/www/shangnantea
mkdir -p /opt/shangnantea-server

# 配置防火墙
echo "10. 配置防火墙..."
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 8080/tcp
ufw allow 8082/tcp
ufw --force enable

echo "=========================================="
echo "服务器环境配置完成！"
echo "=========================================="
echo ""
echo "已安装："
echo "  - Java 21"
echo "  - Maven"
echo "  - MySQL 8.0 (root密码: 123456)"
echo "  - Node.js 18"
echo "  - Nginx"
echo ""
echo "下一步："
echo "1. 上传项目代码"
echo "2. 配置数据库"
echo "3. 部署后端和前端"
