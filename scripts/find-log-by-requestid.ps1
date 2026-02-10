<#
.SYNOPSIS
    按 requestId 快速从后端日志中筛选对应请求的日志片段。

.DESCRIPTION
    配合后端输出的响应头 X-Request-Id 使用：
    - 在 APIFox 里复制响应头 X-Request-Id
    - 执行本脚本即可在终端输出匹配行（含异常栈、业务错误等）

.PARAMETER Id
    requestId（X-Request-Id），支持完整或前缀匹配（建议完整）。

.PARAMETER LogFile
    日志文件路径，默认使用后端配置的 logs/shangnantea-server.log

.EXAMPLE
    .\scripts\find-log-by-requestid.ps1 -Id 4b7a0d0b2a4c4f0d8f5f3a9d1c2b3e4f
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$Id,

    [Parameter(Mandatory = $false)]
    # 默认从 shangnantea-server 模块的落盘日志读取（脚本通常在 shangnantea 目录下执行）
    [string]$LogFile = "shangnantea-server/logs/shangnantea-server.log"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $LogFile)) {
    Write-Host "找不到日志文件：$LogFile" -ForegroundColor Red
    Write-Host "请确认后端已启动并产生日志，或传入 -LogFile 指定正确路径。" -ForegroundColor Yellow
    exit 2
}

Write-Host "按 requestId 筛选日志：$Id" -ForegroundColor Cyan
Write-Host "日志文件：$LogFile" -ForegroundColor DarkGray
Write-Host ""

# 日志 pattern 中包含： [requestId:xxxx]
$pattern = "[requestId:$Id]"

$hits = Select-String -Path $LogFile -Pattern $pattern -SimpleMatch

if (-not $hits) {
    Write-Host "未在日志文件中找到该 requestId。" -ForegroundColor Yellow
    Write-Host "如果你刚加了 requestId 能力但日志文件里还没有 requestId 字段，请重启后端后再跑一次请求。" -ForegroundColor Yellow
    exit 0
}

$hits | ForEach-Object { $_.Line }

