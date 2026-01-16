@echo off
chcp 65001 >nul
REM E2E 测试快捷启动脚本 - 运行所有测试
echo ========================================
echo   E2E 测试 - 运行所有测试
echo ========================================
echo.

REM 调用同目录下的 run-e2e.ps1
powershell -ExecutionPolicy Bypass -File "%~dp0run-e2e.ps1"

pause
