@echo off
chcp 65001 >nul
echo ========================================
echo   API 测试启动脚本
echo ========================================
echo.

REM 如果传入了参数，直接运行指定测试
if not "%1"=="" (
    echo 运行指定的 API 测试: %1
    powershell -ExecutionPolicy Bypass -File "%~dp0run-e2e.ps1" -TestFile "api-tests/%1"
    goto :end
)

REM 显示测试列表
echo 请选择要测试的接口：
echo.
echo [用户模块]
echo   1. POST /user/login - 用户登录
echo   2. POST /user/register - 用户注册
echo.
echo [其他选项]
echo   0. 运行所有测试
echo   q. 退出
echo.

REM 获取用户输入
set /p choice=请输入序号: 

REM 处理用户选择
if "%choice%"=="0" (
    echo.
    echo 运行所有 API 测试...
    powershell -ExecutionPolicy Bypass -File "%~dp0run-e2e.ps1" -TestFile "api-tests"
    goto :end
)

if "%choice%"=="1" (
    echo.
    echo 运行测试: POST /user/login
    powershell -ExecutionPolicy Bypass -File "%~dp0run-e2e.ps1" -TestFile "api-tests/user/login.spec.js"
    goto :end
)

if "%choice%"=="2" (
    echo.
    echo 运行测试: POST /user/register
    powershell -ExecutionPolicy Bypass -File "%~dp0run-e2e.ps1" -TestFile "api-tests/user/register.spec.js"
    goto :end
)

if /i "%choice%"=="q" (
    echo.
    echo 已取消测试
    goto :end
)

echo.
echo 无效的选择！
pause
goto :eof

:end
echo.
echo ========================================
echo   测试完成
echo ========================================
pause
