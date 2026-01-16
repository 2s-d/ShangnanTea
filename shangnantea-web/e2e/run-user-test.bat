@echo off
chcp 65001 >nul
echo =======================================
echo E2E 测试 - 用户模块深度测试
echo =======================================
echo.

cd /d "%~dp0.."

REM 检查开发服务器是否运行
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-e2e.ps1" -TestFile "user-module.spec.js"

pause
