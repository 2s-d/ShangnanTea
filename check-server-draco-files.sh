#!/bin/bash

# 检查服务器上 Draco 文件的脚本
# 使用方法: ssh root@108.160.131.86 'bash -s' < check-server-draco-files.sh

echo "=== 检查 Portfolio Draco 文件 ==="
echo ""

echo "1. 检查文件是否存在:"
echo "---"
ls -lh /var/www/portfolio/draco/
echo ""

echo "2. 检查文件权限:"
echo "---"
stat /var/www/portfolio/draco/draco_decoder.wasm
stat /var/www/portfolio/draco/draco_wasm_wrapper.js
echo ""

echo "3. 检查文件大小:"
echo "---"
du -h /var/www/portfolio/draco/*
echo ""

echo "4. 检查 Nginx 配置中的 MIME 类型:"
echo "---"
grep -r "wasm" /etc/nginx/ || echo "未找到 wasm MIME 类型配置"
echo ""

echo "5. 测试文件是否可以通过 HTTP 访问:"
echo "---"
curl -I http://localhost/draco/draco_decoder.wasm
echo ""
curl -I http://localhost/draco/draco_wasm_wrapper.js
echo ""

echo "6. 检查模型文件:"
echo "---"
ls -lh /var/www/portfolio/models/
echo ""

echo "7. 检查 Nginx 错误日志（最后 20 行）:"
echo "---"
tail -n 20 /var/log/nginx/error.log
echo ""

echo "=== 检查完成 ==="
