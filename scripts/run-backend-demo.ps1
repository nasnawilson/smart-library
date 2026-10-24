param(
    [string]$MavenVersion = "3.9.9"
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BackendDir = Join-Path $ProjectRoot 'smart-library-backend'
$ToolsDir = Join-Path $ProjectRoot '.tools'
$MavenDirName = "apache-maven-$MavenVersion"
$MavenZipName = "apache-maven-$MavenVersion-bin.zip"
$MavenDir = Join-Path $ToolsDir $MavenDirName
$MavenZip = Join-Path $ToolsDir $MavenZipName
$MavenUrl = "https://repo.huaweicloud.com/apache/maven/maven-3/$MavenVersion/binaries/$MavenZipName"
$MvnCmd = Join-Path $MavenDir 'bin\mvn.cmd'

Write-Host 'Smart Library backend H2 demo starter' -ForegroundColor Cyan

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host 'Java was not found. Please install JDK 17 or higher first.' -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $MvnCmd)) {
    New-Item -ItemType Directory -Force -Path $ToolsDir | Out-Null

    if (-not (Test-Path $MavenZip)) {
        Write-Host "Downloading Maven $MavenVersion. This is required only once..." -ForegroundColor Yellow
        Invoke-WebRequest -Uri $MavenUrl -OutFile $MavenZip
    }

    Write-Host 'Extracting Maven...' -ForegroundColor Yellow
    Expand-Archive -Path $MavenZip -DestinationPath $ToolsDir -Force
}

Write-Host 'Starting backend: http://localhost:8080' -ForegroundColor Green
Write-Host 'H2 console: http://localhost:8080/h2-console' -ForegroundColor Green
Write-Host 'JDBC URL: jdbc:h2:file:./data/smart_library' -ForegroundColor Green

$PomPath = Join-Path $BackendDir 'pom.xml'
& $MvnCmd -f $PomPath spring-boot:run '-Dspring-boot.run.profiles=demo'
