@echo off
chcp 65001 >nul
echo =======================================
echo E2E 测试 - 浅层扫描（只访问页面）
echo =======================================
echo.

cd /d "%~dp0.."

REM 检查开发服务器是否运行
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-e2e.ps1" -TestFile "console-error-scan.spec.js"

pause
