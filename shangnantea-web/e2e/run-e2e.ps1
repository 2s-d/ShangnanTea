# E2E Test Runner with Smart Port Detection
# Checks if port 8083 is already in use before starting a new server

param(
    [string]$TestFile = ""
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  E2E Test Automation Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Change to project root directory
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

# Check if port 8083 is already in use
Write-Host "Checking port 8083 status..." -ForegroundColor Yellow
$portCheck = Get-NetTCPConnection -LocalPort 8083 -State Listen -ErrorAction SilentlyContinue

$serverProcess = $null
$needCleanup = $false

if ($portCheck) {
    Write-Host "[OK] Port 8083 is already running, using existing server" -ForegroundColor Green
    Write-Host ""
    
    # Wait a bit to ensure service is ready
    Write-Host "Waiting for service to be ready..." -ForegroundColor Yellow
    Start-Sleep -Seconds 3
} else {
    Write-Host "[INFO] Port 8083 is free, starting dev server..." -ForegroundColor Yellow
    Write-Host ""
    
    # Start dev server
    $serverProcess = Start-Process -FilePath "npm" -ArgumentList "run", "serve" -PassThru -WindowStyle Normal
    $needCleanup = $true
    
    Write-Host "Waiting for dev server to start (about 15 seconds)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    
    # Check if port is now listening
    $portCheck = Get-NetTCPConnection -LocalPort 8083 -State Listen -ErrorAction SilentlyContinue
    if ($portCheck) {
        Write-Host "[OK] Dev server started successfully on port 8083" -ForegroundColor Green
    } else {
        Write-Host "[WARNING] Port 8083 not detected, server may have failed to start" -ForegroundColor Red
    }
    Write-Host ""
}

# Run E2E tests
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Running E2E Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($TestFile) {
    Write-Host "Running specific test: $TestFile" -ForegroundColor Yellow
    & npx playwright test "e2e/$TestFile"
} else {
    Write-Host "Running all tests..." -ForegroundColor Yellow
    & npx playwright test
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Tests Completed" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Clean up if we started the server
if ($needCleanup -and $serverProcess) {
    Write-Host "Stopping dev server..." -ForegroundColor Yellow
    Stop-Process -Id $serverProcess.Id -Force -ErrorAction SilentlyContinue
    Write-Host "[OK] Dev server stopped" -ForegroundColor Green
} else {
    Write-Host "[INFO] Keeping existing dev server (port 8083)" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Press any key to open test report..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Open test report
& npx playwright show-report e2e-report

Write-Host ""
Write-Host "Done!" -ForegroundColor Green
