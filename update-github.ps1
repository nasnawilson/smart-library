$ErrorActionPreference = 'Stop'
$Root = $PSScriptRoot

$Git = 'C:\Program Files\Git\cmd\git.exe'
if (-not (Test-Path $Git)) {
    $cmd = Get-Command git.exe -ErrorAction SilentlyContinue
    if ($cmd) { $Git = $cmd.Source }
}
if (-not $Git) {
    Write-Host '[X] 找不到 git.exe，请确认 Git 已安装。' -ForegroundColor Red
    exit 1
}

Set-Location $Root
Write-Host '=== 上传改动到 GitHub ===' -ForegroundColor Cyan

$pending = & $Git status --porcelain
if (-not $pending) {
    Write-Host '没有新改动，不需要上传。' -ForegroundColor Yellow
    exit 0
}

Write-Host '本次检测到以下改动：' -ForegroundColor Green
$pending | ForEach-Object { Write-Host ('   ' + $_) }

$msg = Read-Host '这次改了什么？（直接回车用默认说明）'
if ([string]::IsNullOrWhiteSpace($msg)) {
    $msg = '更新：' + (Get-Date -Format 'yyyy-MM-dd HH:mm')
}

$msgFile = Join-Path $env:TEMP ('smart-library-commit-' + [guid]::NewGuid().ToString('N') + '.txt')
[System.IO.File]::WriteAllText($msgFile, $msg, (New-Object System.Text.UTF8Encoding($false)))

& $Git add -A
& $Git commit -F $msgFile
Remove-Item $msgFile -Force -ErrorAction SilentlyContinue

if ($LASTEXITCODE -ne 0) {
    Write-Host '[X] 本地提交失败，请看上面的提示。' -ForegroundColor Red
    exit 1
}

Write-Host '正在推送到 GitHub...' -ForegroundColor Cyan
& $Git push
if ($LASTEXITCODE -ne 0) {
    Write-Host '[X] 推送失败。常见原因：没登录 / 网络不通 / 仓库地址变了。' -ForegroundColor Red
    exit 1
}

Write-Host ''
Write-Host '[OK] 已上传，打开 https://github.com/nasnawilson/smart-library 就能看到。' -ForegroundColor Green
