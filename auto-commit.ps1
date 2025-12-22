Param(
    [int]$IntervalSeconds = 300
)

# 自动提交脚本：每隔指定秒数检测是否有变更，有则 git add -A 并提交
# 使用方法（在仓库根目录运行）：
#   powershell -ExecutionPolicy Bypass -File .\auto-commit.ps1
# 如需自定义间隔（秒），例如 120 秒：
#   powershell -ExecutionPolicy Bypass -File .\auto-commit.ps1 -IntervalSeconds 120

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $repoRoot

while ($true) {
    # 检查是否有变更
    $status = git status --short
    if (-not [string]::IsNullOrWhiteSpace($status)) {
        $now = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        git add -A
        git commit -m "chore: autosave $now" | Out-Null
        Write-Host "[$now] 已自动提交变更。" -ForegroundColor Green
    } else {
        $now = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        Write-Host "[$now] 无变更，跳过提交。" -ForegroundColor Yellow
    }

    Start-Sleep -Seconds $IntervalSeconds
}


