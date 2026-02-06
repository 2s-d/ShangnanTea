#!/bin/bash
# 个人博客本地构建并上传脚本
# 在本地 Windows 电脑上运行（Git Bash 或 WSL）

echo "=========================================="
echo "  个人博客本地构建并部署"
echo "=========================================="
echo ""

# 配置服务器信息
SERVER_USER="root"
SERVER_IP="你的服务器IP"  # 修改为你的服务器 IP
SERVER_PATH="/var/www/portfolio"

# 1. 进入项目目录
cd portfolio

# 2. 安装依赖
echo "[1/4] 安装依赖..."
npm install

# 3. 构建项目
echo "[2/4] 构建项目..."
npm run build

if [ $? -eq 0 ]; then
    echo "✓ 构建成功"
else
    echo "✗ 构建失败"
    exit 1
fi

# 4. 压缩 dist 文件夹
echo "[3/4] 压缩文件..."
cd dist
tar -czf ../portfolio-dist.tar.gz .
cd ..
echo "✓ 文件已压缩"

# 5. 上传到服务器
echo "[4/4] 上传到服务器..."
scp portfolio-dist.tar.gz $SERVER_USER@$SERVER_IP:/tmp/

# 6. 在服务器上解压并替换
echo "在服务器上部署..."
ssh $SERVER_USER@$SERVER_IP << 'EOF'
    # 备份旧文件
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    if [ -d /var/www/portfolio/dist ]; then
        mv /var/www/portfolio/dist /var/www/portfolio/dist.backup_$TIMESTAMP
    fi
    
    # 创建新目录
    mkdir -p /var/www/portfolio/dist
    
    # 解压新文件
    tar -xzf /tmp/portfolio-dist.tar.gz -C /var/www/portfolio/dist
    
    # 清理临时文件
    rm /tmp/portfolio-dist.tar.gz
    
    # 重启 Nginx
    systemctl reload nginx
    
    echo "✓ 部署完成"
EOF

# 7. 清理本地临时文件
rm portfolio-dist.tar.gz

echo ""
echo "=========================================="
echo "  部署完成！"
echo "=========================================="
echo ""
echo "访问地址: http://$SERVER_IP/portfolio/"
echo ""
