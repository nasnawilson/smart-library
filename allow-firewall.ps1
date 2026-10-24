<#
  智慧图书馆系统 —— 放行防火墙端口

  作用：在 Windows 防火墙里放行 TCP 3000（前端页面）和 8080（后端接口），
        这样同一个 WiFi 下的手机、其他电脑才能访问。

  用法：双击 allow-firewall.bat，弹出用户账户控制时点“是”
#>

$IsAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $IsAdmin) {
    Write-Host ''
    Write-Host '  需要管理员权限，正在弹出授权窗口，请点“是”。' -ForegroundColor Yellow
    Write-Host ''
    Start-Process powershell -Verb RunAs -ArgumentList '-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', "`"$PSCommandPath`""
    Start-Sleep -Seconds 1
    exit 0
}

Write-Host ''
Write-Host '  ============================================' -ForegroundColor DarkCyan
Write-Host '        智慧图书馆系统 · 放行防火墙端口' -ForegroundColor DarkCyan
Write-Host '  ============================================' -ForegroundColor DarkCyan

foreach ($port in @(3000, 8080)) {
    $name = "SmartLibrary $port"
    netsh advfirewall firewall delete rule name="$name" 2>$null | Out-Null
    netsh advfirewall firewall add rule name="$name" dir=in action=allow protocol=TCP localport=$port 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  已放行 TCP $port" -ForegroundColor Green
    } else {
        Write-Host "  放行 TCP $port 失败，请手动在防火墙里添加" -ForegroundColor Red
    }
}

Write-Host ''
Write-Host '  完成。现在同一个 WiFi 下的手机可以用局域网地址访问了。' -ForegroundColor Green
Write-Host ''
Start-Sleep -Seconds 3
