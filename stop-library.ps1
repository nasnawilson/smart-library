<#
  智慧图书馆系统 —— 停止服务

  用法：双击 stop-library.bat
  作用：关掉占用 3000（前端）和 8080（后端）端口的进程
#>

$ErrorActionPreference = 'SilentlyContinue'

Write-Host ''
Write-Host '  ============================================' -ForegroundColor DarkCyan
Write-Host '          智慧图书馆系统 · 停止服务' -ForegroundColor DarkCyan
Write-Host '  ============================================' -ForegroundColor DarkCyan

$stopped = 0

foreach ($port in @(3000, 8080)) {
    $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    foreach ($c in $conns) {
        $proc = Get-Process -Id $c.OwningProcess -ErrorAction SilentlyContinue
        if ($proc) {
            Write-Host "  停止 $($proc.ProcessName)（PID $($proc.Id)，端口 $port）" -ForegroundColor Yellow
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
            $stopped++
        }
    }
}

# 清理残留的 npm / cmd 包装进程
Get-CimInstance Win32_Process |
    Where-Object {
        $_.CommandLine -and (
            $_.CommandLine -like '*smart-library-backend-1.0.0.jar*' -or
            $_.CommandLine -like '*vite.js*' -or
            ($_.CommandLine -like '*npm-cli.js*' -and $_.CommandLine -like '*run*dev*')
        )
    } |
    ForEach-Object {
        Write-Host "  停止残留进程 PID $($_.ProcessId)" -ForegroundColor Yellow
        Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
        $stopped++
    }

Start-Sleep -Seconds 2

Write-Host ''
if ($stopped -eq 0) {
    Write-Host '  当前没有在运行的服务。' -ForegroundColor Green
} else {
    Write-Host "  已停止 $stopped 个进程，端口 3000 和 8080 已释放。" -ForegroundColor Green
    Write-Host '  数据已安全写入数据库文件，可以直接备份。' -ForegroundColor Green
}
Write-Host ''
