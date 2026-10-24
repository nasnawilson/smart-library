# 智慧图书馆演示启动指南

本项目现在支持多种启动方式，适合不同环境。

## 推荐：一键启动前后端

如果要使用真实后端和文件数据库，推荐直接执行：

```powershell
.\scripts\run-all-demo.ps1
```

脚本会分别打开两个 PowerShell 窗口：

```text
后端：http://localhost:8080
前端：http://localhost:3000
```

管理员账号：

```text
admin / admin
```

## 方式一：只启动前端 Mock 演示

适合当前电脑未安装 Docker、Maven、MySQL 的情况。

```powershell
.\scripts\run-frontend.ps1
```

或手动执行：

```powershell
cd smart-library-frontend
npm run dev
```

默认地址：

```text
http://localhost:3000
```

如果 `3000` 端口被占用，请以终端输出的实际地址为准。

测试账号：

```text
admin / admin              （管理员）
20255030345 / 123456       （读者）
20255030346 / 123456       （读者）
```

当前开发环境默认启用：

```env
VITE_USE_MOCK=true
```

所以不需要后端也能演示登录、图书、座位、书单、首页统计等功能。

## 方式二：H2 演示后端

适合没有 MySQL、但希望体验真实 Spring Boot 接口的情况。

执行：

```powershell
.\scripts\run-backend-demo.ps1
```

脚本会自动下载 Maven 到项目本地 `.tools` 目录，并用 H2 文件数据库启动后端。用户操作会保存到后端数据库文件中，重启后仍然保留。

后端地址：

```text
http://localhost:8080
```

H2 控制台：

```text
http://localhost:8080/h2-console
```

H2 控制台连接信息：

```text
JDBC URL: jdbc:h2:file:./data/smart_library
User Name: sa
Password: 留空
```

如果要让前端连接 H2 后端，请修改：

```text
smart-library-frontend/.env.development
```

改为：

```env
VITE_USE_MOCK=false
```

然后重启前端。

## 方式三：Docker 一键真实环境

适合已安装 Docker Desktop 的情况。

```powershell
docker compose up -d --build
```

它会启动：

- MySQL
- Spring Boot 后端
- Nginx 前端

访问：

```text
http://localhost:5173
```

## 推荐顺序

当前电脑建议先用：

```powershell
.\scripts\run-all-demo.ps1
```

用于真实后端、文件数据库和完整流程演示。

后续如果要展示真实后端接口，再运行：

```powershell
.\scripts\run-backend-demo.ps1
```

并关闭前端 Mock。

## 清理测试数据

如果临时创建了测试用户，建议在后端停止后执行：

```powershell
.\scripts\cleanup-test-data.ps1
```

默认会清理学号前缀为 `20256` 的测试账号，并同步删除这些账号产生的借阅记录、座位记录，同时重算图书库存和座位状态。
