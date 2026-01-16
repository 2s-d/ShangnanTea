# E2E 测试运行脚本（改进版）
# 功能：智能检测端口占用，避免重复启动服务器

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  E2E 测试自动化脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 8083 端口是否已被占用
Write-Host "检查端口 8083 状态..." -ForegroundColor Yellow
$portCheck = Get-NetTCPConnection -LocalPort 8083 -State Listen -ErrorAction SilentlyContinue

$serverProcess = $null
$needCleanup = $false

if ($portCheck) {
    Write-Host "✓ 端口 8083 已有服务运行，将直接使用现有服务" -ForegroundColor Green
    Write-Host ""
    
    # 等待一下确保服务完全就绪
    Write-Host "等待服务就绪..." -ForegroundColor Yellow
    Start-Sleep -Seconds 3
} else {
    Write-Host "✗ 端口 8083 未被占用，正在启动开发服务器..." -ForegroundColor Yellow
    Write-Host ""
    
    # 启动开发服务器
    $serverProcess = Start-Process -FilePath "npm" -ArgumentList "run", "serve" -PassThru -WindowStyle Normal
    $needCleanup = $true
    
    Write-Host "等待开发服务器启动（约 15 秒）..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    
    # 再次检查端口是否成功启动
    $portCheck = Get-NetTCPConnection -LocalPort 8083 -State Listen -ErrorAction SilentlyContinue
    if ($portCheck) {
        Write-Host "✓ 开发服务器已成功启动在端口 8083" -ForegroundColor Green
    } else {
        Write-Host "✗ 警告：端口 8083 未检测到服务，可能启动失败" -ForegroundColor Red
    }
    Write-Host ""
}

# 运行 E2E 测试
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  开始运行 E2E 测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

& npx playwright test

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  测试完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 如果是脚本启动的服务器，则关闭它
if ($needCleanup -and $serverProcess) {
    Write-Host "正在关闭开发服务器..." -ForegroundColor Yellow
    Stop-Process -Id $serverProcess.Id -Force -ErrorAction SilentlyContinue
    Write-Host "✓ 开发服务器已关闭" -ForegroundColor Green
} else {
    Write-Host "ℹ 保留现有的开发服务器（端口 8083）" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "按任意键打开测试报告..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# 打开测试报告
& npx playwright show-report e2e-report

Write-Host ""
Write-Host "完成！" -ForegroundColor Green

