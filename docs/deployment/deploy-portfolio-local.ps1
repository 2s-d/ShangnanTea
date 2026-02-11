# 个人博客本地构建并上传脚本（Windows PowerShell 版本）
# 在 Windows 电脑上运行

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  个人博客本地构建并部署" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 配置服务器信息
$SERVER_USER = "root"
$SERVER_IP = "你的服务器IP"  # 修改为你的服务器 IP
$SERVER_PATH = "/var/www/portfolio"

# 1. 进入项目目录
Set-Location portfolio

# 2. 安装依赖
Write-Host "[1/4] 安装依赖..." -ForegroundColor Yellow
npm install

# 3. 构建项目
Write-Host "[2/4] 构建项目..." -ForegroundColor Yellow
npm run build

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ 构建成功" -ForegroundColor Green
} else {
    Write-Host "✗ 构建失败" -ForegroundColor Red
    exit 1
}

# 4. 压缩 dist 文件夹
Write-Host "[3/4] 压缩文件..." -ForegroundColor Yellow
Compress-Archive -Path "dist\*" -DestinationPath "portfolio-dist.zip" -Force
Write-Host "✓ 文件已压缩" -ForegroundColor Green

# 5. 使用 SCP 上传（需要安装 OpenSSH 或使用 WinSCP）
Write-Host "[4/4] 上传到服务器..." -ForegroundColor Yellow
Write-Host ""
Write-Host "请选择上传方式：" -ForegroundColor Cyan
Write-Host "1) 使用 SCP 命令（需要 OpenSSH）"
Write-Host "2) 手动上传（使用 WinSCP 或 FileZilla）"
$choice = Read-Host "请输入选项 (1/2)"

if ($choice -eq "1") {
    # 使用 SCP 上传
    scp portfolio-dist.zip "${SERVER_USER}@${SERVER_IP}:/tmp/"
    
    # 在服务器上解压
    Write-Host "在服务器上部署..." -ForegroundColor Yellow
    ssh "${SERVER_USER}@${SERVER_IP}" @"
        # 备份旧文件
        TIMESTAMP=`$(date +%Y%m%d_%H%M%S)
        if [ -d $SERVER_PATH/dist ]; then
            mv $SERVER_PATH/dist $SERVER_PATH/dist.backup_`$TIMESTAMP
        fi
        
        # 创建新目录
        mkdir -p $SERVER_PATH/dist
        
        # 解压新文件
        unzip -o /tmp/portfolio-dist.zip -d $SERVER_PATH/dist
        
        # 清理临时文件
        rm /tmp/portfolio-dist.zip
        
        # 重启 Nginx
        systemctl reload nginx
        
        echo '✓ 部署完成'
"@
    
    # 清理本地临时文件
    Remove-Item portfolio-dist.zip
    
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "  部署完成！" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "访问地址: http://$SERVER_IP/portfolio/" -ForegroundColor Yellow
    
} else {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "  手动上传步骤" -ForegroundColor Cyan
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. 使用 WinSCP 或 FileZilla 连接服务器" -ForegroundColor Yellow
    Write-Host "   服务器: $SERVER_IP"
    Write-Host "   用户名: $SERVER_USER"
    Write-Host ""
    Write-Host "2. 上传 portfolio-dist.zip 到服务器的 /tmp/ 目录" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "3. 在服务器上运行以下命令：" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "cd /tmp" -ForegroundColor Green
    Write-Host "unzip -o portfolio-dist.zip -d /var/www/portfolio/dist" -ForegroundColor Green
    Write-Host "systemctl reload nginx" -ForegroundColor Green
    Write-Host "rm portfolio-dist.zip" -ForegroundColor Green
    Write-Host ""
    Write-Host "压缩文件位置: $(Get-Location)\portfolio-dist.zip" -ForegroundColor Cyan
}

Set-Location ..
