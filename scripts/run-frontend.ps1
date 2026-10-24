$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$FrontendDir = Join-Path $ProjectRoot "smart-library-frontend"

Write-Host 'Starting Smart Library frontend: http://localhost:3000' -ForegroundColor Green
Write-Host 'If port 3000 is busy, Vite will show another available port.' -ForegroundColor Yellow

Push-Location $FrontendDir
npm run dev
Pop-Location
