@echo off
REM E2E 测试快捷启动脚本
echo ========================================
echo   E2E 测试自动化脚本
echo ========================================
echo.

cd /d "%~dp0\.."
powershell -ExecutionPolicy Bypass -File "%~dp0run-test.ps1"

pause
