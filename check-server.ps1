# PowerShell 脚本：检查服务器 Draco 文件
# 使用方法: .\check-server.ps1

Write-Host "=== 检查服务器 Draco 文件 ===" -ForegroundColor Cyan
Write-Host ""

$serverIP = "108.160.131.86"
$serverUser = "root"

Write-Host "连接到服务器: $serverUser@$serverIP" -ForegroundColor Yellow
Write-Host ""

# 创建临时检查脚本
$checkScript = @"
echo '=== 1. 检查 Draco 文件 ==='
ls -lh /var/www/portfolio/draco/ 2>/dev/null || echo '目录不存在'
echo ''

echo '=== 2. 检查模型文件 ==='
ls -lh /var/www/portfolio/models/ 2>/dev/null || echo '目录不存在'
echo ''

echo '=== 3. 测试 WASM 文件访问 ==='
curl -I http://localhost/draco/draco_decoder.wasm 2>/dev/null | grep -E '(HTTP|Content-Type)'
echo ''

echo '=== 4. 检查 MIME 类型配置 ==='
grep -r 'wasm' /etc/nginx/mime.types 2>/dev/null || echo '未找到 wasm MIME 类型'
echo ''

echo '=== 5. 检查 Nginx 状态 ==='
systemctl status nginx | grep -E '(Active|Main PID)'
echo ''

echo '=== 检查完成 ==='
"@

# 执行远程命令
Write-Host "执行检查..." -ForegroundColor Yellow
ssh "$serverUser@$serverIP" $checkScript

Write-Host ""
Write-Host "=== 完成 ===" -ForegroundColor Green
Write-Host ""
Write-Host "如果发现 MIME 类型问题，运行以下命令修复:" -ForegroundColor Yellow
Write-Host "  ssh $serverUser@$serverIP" -ForegroundColor Cyan
Write-Host "  然后在服务器上运行: bash fix-nginx-wasm-mime.sh" -ForegroundColor Cyan
