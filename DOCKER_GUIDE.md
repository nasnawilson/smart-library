# Docker 一键启动指南

## 前置条件

只需要安装 **Docker Desktop**：
- 下载地址：https://www.docker.com/products/docker-desktop/
- 安装后启动 Docker Desktop，确保任务栏图标变绿（Engine Running）

无需手动安装：MySQL、JDK、Maven、Node.js

## 一键启动

在 `smart-library` 目录下执行：

```powershell
# 构建并启动所有服务（首次执行约需 5-10 分钟下载镜像和依赖）
docker compose up -d --build

# 查看启动日志
docker compose logs -f

# 查看容器状态
docker compose ps
```

## 访问系统

启动成功后访问：**http://localhost:5173**

测试账号：
| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin | 管理员 |
| 20255030345 | 123456 | 读者 |
| 20255030346 | 123456 | 读者 |

## 常用命令

```powershell
# 停止所有服务
docker compose down

# 停止并清空数据库（重置数据）
docker compose down -v

# 重新构建后端（修改了 Java 代码后）
docker compose up -d --build backend

# 重新构建前端（修改了 Vue 代码后）
docker compose up -d --build frontend

# 查看某个服务日志
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql

# 进入 MySQL 容器执行 SQL
docker exec -it smart-library-mysql mysql -uroot -proot smart_library
```

## 服务说明

| 服务 | 端口 | 说明 |
|------|------|------|
| frontend | 5173 → 80 | Nginx 部署 Vue 静态文件 |
| backend | 8080 → 8080 | Spring Boot 应用 |
| mysql | 3306 → 3306 | MySQL 8.0 数据库 |

启动顺序：mysql → backend → frontend（依赖关系自动管理）

## 故障排查

### 1. 前端登录报错 Network Error

**原因**：后端尚未完全启动（Spring Boot 启动需 30-60 秒）

**解决**：
```powershell
docker compose logs backend
# 看到 "Started SmartLibraryApplication in X seconds" 即启动完成
```

### 2. MySQL 数据初始化失败

**原因**：旧的数据卷里没有 schema

**解决**：
```powershell
docker compose down -v   # 清空数据卷
docker compose up -d     # 重新启动，会重新执行 schema.sql
```

### 3. 端口被占用

**原因**：本机已有进程占用 5173/8080/3306

**解决**：修改 `docker-compose.yml` 中的端口映射，比如 `"15173:80"`

### 4. 镜像下载慢

**解决**：配置 Docker 镜像加速器（Docker Desktop → Settings → Docker Engine）：

```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ]
}
```

## 不想用 Docker？

如果您本机已有 MySQL 和 JDK，可以直接：

1. 安装 Maven：`scoop install maven` 或下载安装
2. 在 MySQL 中执行 `schema.sql`
3. 修改 `application.yml` 的数据库密码
4. 启动后端：`cd smart-library-backend; mvn spring-boot:run`
5. 启动前端：`cd smart-library-frontend; npm run dev`
