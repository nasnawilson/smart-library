<#
  清理演示/测试数据（H2 演示库）

  作用：
    1. 删除除管理员 admin 以外的所有读者账号
    2. 清空所有借阅记录、预约记录
    3. 按清空后的数据重算图书库存（borrow_num / in_library）
    4. 所有座位恢复为空闲

  保留：
    - 管理员账号 admin（角色强制修正为 ADMIN）
    - book_info 全部书目、seat_info 全部座位、book_list 书单

  用法（必须先把后端服务停掉，否则数据库文件被占用）：
    powershell -ExecutionPolicy Bypass -File .\scripts\cleanup-test-data.ps1
    powershell -ExecutionPolicy Bypass -File .\scripts\cleanup-test-data.ps1 -Force   # 跳过确认
#>
param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BackendDir = Join-Path $ProjectRoot "smart-library-backend"
$H2Jar = Join-Path $env:USERPROFILE ".m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar"

if (-not (Test-Path $H2Jar)) {
    Write-Host "找不到 H2 工具 jar，请先启动一次后端或执行 mvn compile。" -ForegroundColor Red
    exit 1
}

$DbUrl = "jdbc:h2:file:$BackendDir\data\smart_library;MODE=MySQL;DATABASE_TO_LOWER=TRUE"

$SqlParts = @(
    # 先把被预约占用的座位释放，再删预约记录
    'UPDATE seat_info SET status=0 WHERE id IN (SELECT seat_id FROM reserve_record)',
    'DELETE FROM reserve_record',
    'DELETE FROM borrow_record',
    # 只保留管理员，其余读者全部删除
    "DELETE FROM sys_user WHERE student_id <> 'admin'",
    # 确保管理员账号仍在、角色正确
    "UPDATE sys_user SET role='ADMIN' WHERE student_id='admin'",
    # 按剩余借阅记录重算库存与在馆状态
    'UPDATE book_info SET borrow_num=(SELECT COUNT(*) FROM borrow_record WHERE borrow_record.book_id=book_info.id AND borrow_record.return_flag=0)',
    'UPDATE book_info SET in_library=CASE WHEN borrow_num < total_num THEN 1 ELSE 0 END',
    # 所有座位恢复空闲
    'UPDATE seat_info SET status=0'
)

$Sql = [string]::Join("; ", $SqlParts) + ";"

Write-Host "将要执行：删除所有读者账号（保留 admin）、清空借阅与预约、重算库存与座位状态。" -ForegroundColor Cyan
if (-not $Force) {
    $answer = Read-Host "确认执行请输入 YES"
    if ($answer -ne "YES") {
        Write-Host "已取消。" -ForegroundColor Yellow
        exit 0
    }
}

java -cp $H2Jar org.h2.tools.Shell -url $DbUrl -user sa -sql $Sql
if ($LASTEXITCODE -ne 0) {
    Write-Host "清理失败。请确认后端服务已停止，然后重新执行本脚本。" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "清理完成：仅保留管理员 admin，借阅/预约已清空，图书库存与座位状态已重算。" -ForegroundColor Green
