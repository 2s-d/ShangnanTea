# E2E Test Runner
Write-Host "Starting E2E Tests..." -ForegroundColor Cyan

# Start dev server
Write-Host "Starting dev server..." -ForegroundColor Yellow
$server = Start-Process -FilePath "npm" -ArgumentList "run", "serve" -PassThru -WindowStyle Minimized

# Wait for server
Write-Host "Waiting for server..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Run tests
Write-Host "Running tests..." -ForegroundColor Green
& npx playwright test

# Stop server
Write-Host "Stopping server..." -ForegroundColor Yellow
Stop-Process -Id $server.Id -Force

Write-Host "Done!" -ForegroundColor Green
