<#
  智慧图书馆系统 —— 一键启动（局域网小范围使用）

  用法：
    双击 start-library.bat 即可（推荐）
    或右键本文件 → 使用 PowerShell 运行

  停止：
    双击 stop-library.bat

  说明：
    - 后端用 demo 配置（H2 文件数据库），数据存在 smart-library-backend\data\smart_library.mv.db
    - 前端页面会对整个局域网开放，同一 WiFi 下的手机/电脑都能访问
    - 登录密钥首次运行自动生成，保存在 .local\jwt-secret.txt，以后一直复用
#>

$ErrorActionPreference = 'Stop'

$Root        = $PSScriptRoot
$BackendDir  = Join-Path $Root 'smart-library-backend'
$FrontendDir = Join-Path $Root 'smart-library-frontend'
$JarRelPath  = 'target\smart-library-backend-1.0.0.jar'
$JarPath     = Join-Path $BackendDir $JarRelPath
$LogDir      = Join-Path $Root 'logs'
$SecretDir   = Join-Path $Root '.local'
$SecretFile  = Join-Path $SecretDir 'jwt-secret.txt'

function Step($msg) { Write-Host "`n[启动] $msg" -ForegroundColor Cyan }
function Ok($msg)   { Write-Host "       $msg" -ForegroundColor Green }
function Warn($msg) { Write-Host "       $msg" -ForegroundColor Yellow }
function Bad($msg)  { Write-Host "       $msg" -ForegroundColor Red }

Write-Host ''
Write-Host '  ============================================' -ForegroundColor DarkCyan
Write-Host '          智慧图书馆系统 · 一键启动' -ForegroundColor DarkCyan
Write-Host '  ============================================' -ForegroundColor DarkCyan

if (-not (Test-Path $LogDir))    { New-Item -ItemType Directory -Path $LogDir    | Out-Null }
if (-not (Test-Path $SecretDir)) { New-Item -ItemType Directory -Path $SecretDir | Out-Null }

# ---------------- 1. 找 Java ----------------
Step '检查 Java 环境'
$JavaExe = $null
$Candidates = @()
if ($env:JAVA_HOME) { $Candidates += (Join-Path $env:JAVA_HOME 'bin\java.exe') }
$Candidates += 'C:\Program Files\Java\jdk-17\bin\java.exe'
foreach ($c in $Candidates) {
    if (-not $JavaExe -and (Test-Path $c)) { $JavaExe = $c }
}
if (-not $JavaExe) {
    $cmd = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($cmd) { $JavaExe = $cmd.Source }
}
if (-not $JavaExe) {
    Bad '没有找到 Java。请先安装 JDK 17，装完再运行本脚本。'
    exit 1
}
Ok "Java：$JavaExe"

# ---------------- 2. 检查后端程序包 ----------------
Step '检查后端程序包'
if (-not (Test-Path $JarPath)) {
    Warn '没找到打包好的 jar，正在用 Maven 自动构建（第一次可能要一两分钟）'
    $Mvn = $null
    $mvnCmd = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($mvnCmd) { $Mvn = $mvnCmd.Source }
    if (-not $Mvn) {
        $ideaMvn = 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd'
        if (Test-Path $ideaMvn) { $Mvn = $ideaMvn }
    }
    if (-not $Mvn) {
        Bad '没找到 Maven。请用 IDEA 打开 smart-library-backend 先用 Maven 打包一次，然后再运行本脚本。'
        exit 1
    }
    $env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent $JavaExe)
    & $Mvn -B -DskipTests package -f (Join-Path $BackendDir 'pom.xml')
    if (-not (Test-Path $JarPath)) {
        Bad '构建失败，请把上面的报错发给开发者。'
        exit 1
    }
}
Ok 'jar 已就绪'

# ---------------- 3. 准备登录密钥 ----------------
Step '准备登录密钥'
if (Test-Path $SecretFile) {
    $env:JWT_SECRET = (Get-Content $SecretFile -Raw).Trim()
    Ok '复用本机已保存的密钥'
} else {
    $bytes = New-Object byte[] 36
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $env:JWT_SECRET = [Convert]::ToBase64String($bytes)
    Set-Content -Path $SecretFile -Value $env:JWT_SECRET -Encoding UTF8
    Ok '首次运行，已生成随机密钥并保存到 .local\jwt-secret.txt'
}

# ---------------- 4. 启动后端 ----------------
Step '启动后端服务（端口 8080）'
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
    Warn '8080 端口已被占用，判断后端已在运行，跳过启动'
} else {
    $outLog = Join-Path $LogDir "backend-$stamp.log"
    $errLog = Join-Path $LogDir "backend-$stamp.err.log"
    Start-Process -FilePath $JavaExe `
        -ArgumentList '-jar', $JarRelPath, '--spring.profiles.active=demo' `
        -WorkingDirectory $BackendDir `
        -RedirectStandardOutput $outLog `
        -RedirectStandardError $errLog `
        -WindowStyle Hidden | Out-Null

    $ready = $false
    for ($i = 0; $i -lt 90; $i++) {
        Start-Sleep -Seconds 1
        if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) { $ready = $true; break }
    }
    if ($ready) {
        Ok "后端已启动，日志：logs\backend-$stamp.log"
    } else {
        Bad '后端启动超时。请打开 logs 目录里最新的 backend-*.log 看报错。'
        exit 1
    }
}

# ---------------- 5. 启动前端 ----------------
Step '启动前端页面（端口 3000）'
if (Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue) {
    Warn '3000 端口已被占用，判断前端已在运行，跳过启动'
} else {
    if (-not (Test-Path (Join-Path $FrontendDir 'node_modules'))) {
        Warn '第一次运行，正在安装前端依赖，请耐心等几分钟'
        Start-Process -FilePath 'npm.cmd' -ArgumentList 'install' -WorkingDirectory $FrontendDir -Wait -NoNewWindow | Out-Null
    }
    $fOutLog = Join-Path $LogDir "frontend-$stamp.log"
    $fErrLog = Join-Path $LogDir "frontend-$stamp.err.log"
    Start-Process -FilePath 'npm.cmd' `
        -ArgumentList 'run', 'dev' `
        -WorkingDirectory $FrontendDir `
        -RedirectStandardOutput $fOutLog `
        -RedirectStandardError $fErrLog `
        -WindowStyle Hidden | Out-Null

    $ready = $false
    for ($i = 0; $i -lt 90; $i++) {
        Start-Sleep -Seconds 1
        if (Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue) { $ready = $true; break }
    }
    if ($ready) {
        Ok "前端已启动，日志：logs\frontend-$stamp.log"
    } else {
        Bad '前端启动超时。请打开 logs 目录里最新的 frontend-*.log 看报错。'
    }
}

# ---------------- 6. 放行防火墙 ----------------
$IsAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if ($IsAdmin) {
    foreach ($port in @(3000, 8080)) {
        $ruleName = "智慧图书馆 $port"
        $exists = netsh advfirewall firewall show rule name="$ruleName" 2>$null
        if (-not $exists) {
            netsh advfirewall firewall add rule name="$ruleName" dir=in action=allow protocol=TCP localport=$port 2>$null | Out-Null
        }
    }
}

# ---------------- 7. 输出访问地址 ----------------
$LanIp = (Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
          Where-Object { $_.IPAddress -notlike '127.*' -and $_.IPAddress -notlike '169.254.*' } |
          Select-Object -First 1).IPAddress
if (-not $LanIp) { $LanIp = '（未获取到局域网 IP）' }

Write-Host ''
Write-Host '  ============================================' -ForegroundColor DarkCyan
Write-Host '                 启 动 完 成' -ForegroundColor Green
Write-Host '  ============================================' -ForegroundColor DarkCyan
Write-Host ''
Write-Host '  本机访问：  ' -NoNewline
Write-Host 'http://localhost:3000' -ForegroundColor Green
Write-Host '  局域网访问：' -NoNewline
Write-Host "http://${LanIp}:3000" -ForegroundColor Green
Write-Host ''
Write-Host '  管理员账号：admin      密码：admin' -ForegroundColor Yellow
Write-Host '  读者账号：自行注册，学号填 11 位数字' -ForegroundColor Yellow
Write-Host ''
Write-Host '  --------------------------------------------' -ForegroundColor DarkGray
Write-Host '  注意事项：' -ForegroundColor DarkGray
Write-Host '   1. 这台电脑别睡眠、别关机，否则别人立刻断线' -ForegroundColor DarkGray
Write-Host '   2. 手机连同一个 WiFi 才能用上面的局域网地址' -ForegroundColor DarkGray
Write-Host '   3. 如果手机打不开，请用管理员身份运行本脚本一次，' -ForegroundColor DarkGray
Write-Host '      或在 Windows 防火墙里放行 TCP 3000 和 8080 端口' -ForegroundColor DarkGray
Write-Host '   4. 数据都在 smart-library-backend\data\smart_library.mv.db，' -ForegroundColor DarkGray
Write-Host '      停掉服务后复制这个文件就是备份' -ForegroundColor DarkGray
Write-Host '   5. 关掉服务：双击 stop-library.bat' -ForegroundColor DarkGray
Write-Host '  --------------------------------------------' -ForegroundColor DarkGray
Write-Host ''
