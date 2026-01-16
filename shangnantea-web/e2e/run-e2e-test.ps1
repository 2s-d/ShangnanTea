# E2E 测试自动化脚本
# 功能：自动启动开发服务器、运行测试、生成报告

param(
    [switch]$SkipServerStart,  # 跳过启动服务器（如果已经在运行）
    [switch]$ShowReport,       # 测试完成后自动打开报告
    [switch]$Headless,         # 无头模式运行（不显示浏览器）
    [string]$TestFile = ""     # 指定测试文件（可选）
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  E2E 测试自动化脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 切换到项目根目录
Set-Location $projectRoot

# 检查 node_modules 是否存在
if (-not (Test-Path "node_modules")) {
    Write-Host "❌ node_modules 不存在，正在安装依赖..." -ForegroundColor Yellow
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ 依赖安装失败" -ForegroundColor Red
        exit 1
    }
}

# 检查 Playwright 浏览器是否已安装
Write-Host "🔍 检查 Playwright 浏览器..." -ForegroundColor Cyan
$playwrightCheck = npx playwright --version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Playwright 未安装，正在安装..." -ForegroundColor Yellow
    npx playwright install chromium
}

# 函数：检查端口是否被占用
function Test-Port {
    param([int]$Port)
    try {
        $connection = New-Object System.Net.Sockets.TcpClient("localhost", $Port)
        $connection.Close()
        return $true
    } catch {
        return $false
    }
}

# 函数：等待服务器启动
function Wait-ForServer {
    param([int]$Port, [int]$TimeoutSeconds = 60)
    
    Write-Host "⏳ 等待服务器启动 (端口 $Port)..." -ForegroundColor Yellow
    $elapsed = 0
    while ($elapsed -lt $TimeoutSeconds) {
        if (Test-Port -Port $Port) {
            Write-Host "✅ 服务器已启动！" -ForegroundColor Green
            Start-Sleep -Seconds 2  # 额外等待2秒确保完全就绪
            return $true
        }
        Start-Sleep -Seconds 1
        $elapsed++
        Write-Host "." -NoNewline
    }
    Write-Host ""
    Write-Host "❌ 服务器启动超时" -ForegroundColor Red
    return $false
}

# 启动开发服务器
$serverProcess = $null
if (-not $SkipServerStart) {
    Write-Host "🚀 启动开发服务器..." -ForegroundColor Cyan
    
    # 检查端口 8083 是否已被占用
    if (Test-Port -Port 8083) {
        Write-Host "⚠️  端口 8083 已被占用，假设服务器已在运行" -ForegroundColor Yellow
        Write-Host "   如需重启服务器，请先手动停止现有服务" -ForegroundColor Yellow
    } else {
        # 启动服务器（后台进程）
        $serverProcess = Start-Process -FilePath "npm" -ArgumentList "run", "serve" -PassThru -WindowStyle Minimized
        
        # 等待服务器启动
        if (-not (Wait-ForServer -Port 8083 -TimeoutSeconds 60)) {
            Write-Host "❌ 无法启动开发服务器" -ForegroundColor Red
            if ($serverProcess) {
                Stop-Process -Id $serverProcess.Id -Force
            }
            exit 1
        }
    }
} else {
    Write-Host "⏭️  跳过服务器启动（使用 -SkipServerStart 参数）" -ForegroundColor Yellow
    
    # 检查服务器是否在运行
    if (-not (Test-Port -Port 8083)) {
        Write-Host "❌ 服务器未运行！请先启动开发服务器或移除 -SkipServerStart 参数" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ 检测到服务器正在运行" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  开始运行 E2E 测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 构建测试命令
$testArgs = @("playwright", "test")

if ($TestFile) {
    $testArgs += $TestFile
}

if ($Headless) {
    # 修改 playwright.config.js 临时设置 headless
    Write-Host "🔧 设置为无头模式..." -ForegroundColor Cyan
}

# 运行测试
try {
    Write-Host "🧪 执行测试..." -ForegroundColor Cyan
    & npx @testArgs
    $testExitCode = $LASTEXITCODE
} catch {
    Write-Host "❌ 测试执行出错: $_" -ForegroundColor Red
    $testExitCode = 1
} finally {
    # 清理：停止服务器
    if ($serverProcess -and -not $SkipServerStart) {
        Write-Host ""
        Write-Host "🛑 停止开发服务器..." -ForegroundColor Cyan
        Stop-Process -Id $serverProcess.Id -Force -ErrorAction SilentlyContinue
        
        # 等待进程完全停止
        Start-Sleep -Seconds 2
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  测试完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 显示测试结果
if ($testExitCode -eq 0) {
    Write-Host "✅ 所有测试通过！" -ForegroundColor Green
} else {
    Write-Host "❌ 部分测试失败" -ForegroundColor Red
}

# 显示报告位置
Write-Host ""
Write-Host "📊 测试报告位置:" -ForegroundColor Cyan
Write-Host "   - HTML 报告: e2e-report/index.html" -ForegroundColor White
Write-Host "   - JSON 报告: e2e-report/error-summary.json" -ForegroundColor White
Write-Host "   - 截图: e2e-screenshots/" -ForegroundColor White

# 自动打开报告
if ($ShowReport) {
    Write-Host ""
    Write-Host "📖 打开测试报告..." -ForegroundColor Cyan
    & npx playwright show-report e2e-report
}

Write-Host ""
Write-Host "💡 提示: 使用 -ShowReport 参数可自动打开测试报告" -ForegroundColor Yellow
Write-Host "💡 提示: 使用 -SkipServerStart 参数可跳过启动服务器" -ForegroundColor Yellow
Write-Host ""

exit $testExitCode
