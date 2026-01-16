@echo off
chcp 65001 >nul
echo ========================================
echo   API 测试启动脚本
echo ========================================
echo.

REM 检查是否传入了测试文件参数
if "%1"=="" (
    echo 运行所有 API 测试...
    powershell -ExecutionPolicy Bypass -File "%~dp0run-e2e.ps1" -TestFile "api-tests"
) else (
    echo 运行指定的 API 测试: %1
    powershell -ExecutionPolicy Bypass -File "%~dp0run-e2e.ps1" -TestFile "api-tests/%1"
)

echo.
echo ========================================
echo   测试完成
echo ========================================
pause
