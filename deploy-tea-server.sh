#!/bin/bash
# 商南茶城前端服务器部署脚本
# 基于奇门遁甲项目的部署方式

set -e

echo "=========================================="
echo "商南茶城前端服务器部署准备"
echo "=========================================="

# 1. 创建部署目录
echo "📁 创建部署目录..."
sudo mkdir -p /var/www/tea
sudo chown -R $USER:$USER /var/www/tea
echo "✅ 目录创建完成: /var/www/tea"

# 2. 上传并配置 Nginx
echo ""
echo "📝 配置 Nginx..."
echo "请手动执行以下命令上传配置文件："
echo "  scp nginx-tea.conf your-server:/tmp/"
echo ""
echo "然后在服务器上执行："
echo "  sudo mv /tmp/nginx-tea.conf /etc/nginx/sites-available/tea.conf"
echo "  sudo ln -s /etc/nginx/sites-available/tea.conf /etc/nginx/sites-enabled/"
echo "  sudo nginx -t"
echo "  sudo systemctl reload nginx"

# 3. 验证目录
echo ""
echo "📊 验证部署目录..."
ls -la /var/www/

echo ""
echo "=========================================="
echo "✅ 服务器准备完成！"
echo "=========================================="
echo ""
echo "下一步："
echo "1. 推送代码到 GitHub: git push origin main"
echo "2. 查看 Actions 运行: https://github.com/2s-d/ShangnanTea/actions"
echo "3. 访问网站: https://tea.paku.uno"
