@echo off
chcp 65001 >nul
REM E2E 测试快捷启动脚本
echo ========================================
echo   E2E 测试自动化脚本
echo ========================================
echo.

REM 切换到项目根目录
cd /d "%~dp0\.."

REM 调用上级目录的 run-e2e.ps1
powershell -ExecutionPolicy Bypass -File "%~dp0..\run-e2e.ps1"

pause
