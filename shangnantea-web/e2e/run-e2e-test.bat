@echo off
REM E2E 测试快捷启动脚本（批处理版本）
REM 双击即可运行

echo ========================================
echo   E2E 测试自动化脚本
echo ========================================
echo.

cd /d "%~dp0\.."

REM 运行 PowerShell 脚本
powershell -ExecutionPolicy Bypass -File "%~dp0run-e2e-test.ps1"

pause
