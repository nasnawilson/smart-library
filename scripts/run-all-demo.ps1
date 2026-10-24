$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BackendScript = Join-Path $PSScriptRoot "run-backend-demo.ps1"
$FrontendScript = Join-Path $PSScriptRoot "run-frontend.ps1"

Write-Host "Starting Smart Library demo..." -ForegroundColor Cyan
Write-Host "Backend:  http://localhost:8080" -ForegroundColor Green
Write-Host "Frontend: http://localhost:3000" -ForegroundColor Green
Write-Host "Admin:    20255030344 / 123456" -ForegroundColor Yellow

Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", "`"$BackendScript`"" -WorkingDirectory $ProjectRoot
Start-Sleep -Seconds 3
Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", "`"$FrontendScript`"" -WorkingDirectory $ProjectRoot

Write-Host "Startup commands have been launched in separate PowerShell windows." -ForegroundColor Green
