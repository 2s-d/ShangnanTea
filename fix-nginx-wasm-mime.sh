#!/bin/bash

# 修复 Nginx WASM MIME 类型配置
# 在服务器上运行: bash fix-nginx-wasm-mime.sh

echo "=== 修复 Nginx WASM MIME 类型 ==="
echo ""

# 检查是否已经配置
if grep -q "application/wasm" /etc/nginx/mime.types; then
    echo "✓ WASM MIME 类型已经配置"
else
    echo "添加 WASM MIME 类型到 /etc/nginx/mime.types"
    
    # 备份原文件
    sudo cp /etc/nginx/mime.types /etc/nginx/mime.types.backup
    
    # 在 types 块中添加 wasm 类型（在最后一个 } 之前）
    sudo sed -i '/^}/i \    application/wasm                                       wasm;' /etc/nginx/mime.types
    
    echo "✓ WASM MIME 类型已添加"
fi

echo ""
echo "当前 MIME 类型配置:"
grep -A 2 -B 2 "wasm" /etc/nginx/mime.types

echo ""
echo "测试 Nginx 配置:"
sudo nginx -t

if [ $? -eq 0 ]; then
    echo ""
    echo "重新加载 Nginx:"
    sudo systemctl reload nginx
    echo "✓ Nginx 已重新加载"
else
    echo ""
    echo "✗ Nginx 配置测试失败，请检查配置"
    exit 1
fi

echo ""
echo "=== 修复完成 ==="
echo ""
echo "测试 WASM 文件访问:"
curl -I http://localhost/draco/draco_decoder.wasm | grep "Content-Type"
