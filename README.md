# 智慧图书馆系统 (Smart Library System)

## 项目简介

参赛级学生智慧图书馆系统，采用前后端分离架构，功能涵盖图书检索借阅、可视化座位预约、智能室内导航、个性化推荐、个人学习中心等模块。

## 技术栈

### 前端
- Vue 3 + Vite + TypeScript
- Element Plus UI 组件库
- Vue Router 4 路由管理
- Pinia 状态管理
- Axios HTTP 请求
- ECharts 数据可视化
- SVG 交互式图形

### 后端
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0
- JWT 身份认证
- Spring Scheduling 定时任务

## 项目结构

```
smart-library/
├── smart-library-frontend/     # 前端项目
│   ├── src/
│   │   ├── api/               # API 接口封装
│   │   ├── components/        # 公共组件
│   │   ├── router/            # 路由配置
│   │   ├── store/             # Pinia 状态管理
│   │   ├── utils/             # 工具函数
│   │   └── views/             # 页面视图
│   ├── package.json
│   └── vite.config.ts
│
└── smart-library-backend/      # 后端项目
    ├── src/main/java/com/smartlibrary/
    │   ├── config/            # 配置类
    │   ├── controller/        # 控制器
    │   ├── dto/               # 数据传输对象
    │   ├── entity/            # 实体类
    │   ├── exception/         # 异常处理
    │   ├── interceptor/       # 拦截器
    │   ├── mapper/            # 数据访问层
    │   ├── service/           # 业务逻辑层
    │   └── utils/             # 工具类
    ├── src/main/resources/
    │   ├── db/schema.sql      # 数据库脚本
    │   └── application.yml    # 应用配置
    └── pom.xml
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 1. 数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 执行数据库脚本
source smart-library-backend/src/main/resources/db/schema.sql
```

### 2. 启动后端服务

```bash
cd smart-library-backend

# 方式一：使用 Maven 运行
mvn spring-boot:run

# 方式二：先打包再运行
mvn clean package
java -jar target/smart-library-backend-1.0.0.jar
```

后端服务默认运行在 http://localhost:8080

### 3. 启动前端服务

```bash
cd smart-library-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务默认运行在 http://localhost:5173

### 4. 访问系统

打开浏览器访问 http://localhost:5173

## 一键启动脚本（局域网使用）

如果想在**一个班级 / 实验室的局域网**里用，不用敲任何命令，直接双击项目根目录下的这几个文件：

| 文件 | 作用 |
|---|---|
| `start-library.bat` | 一键启动后端 + 前端，并打印本机地址和局域网地址 |
| `stop-library.bat` | 停止所有服务，释放 3000 / 8080 端口 |
| `allow-firewall.bat` | 放行 Windows 防火墙的 3000 / 8080 端口（手机打不开时用，需要点“是”授权） |
| `update-github.bat` | 改完代码后双击，把改动一次性提交并推送到 GitHub |

`start-library.bat` 会自动完成：检查 Java → 缺 jar 时用 Maven 自动打包 → 首次运行生成随机 `JWT_SECRET` 保存到 `.local\jwt-secret.txt` 并长期复用 → 后台启动后端（demo / H2 配置）→ 后台启动前端（监听所有网卡，手机可访问）→ 打印访问地址。运行日志写在 `logs\` 目录。

> 首次启动后请立刻用 `admin` / `admin` 登录，并到「个人中心」修改密码。
> `.local\jwt-secret.txt` 是机密文件，不要提交到代码仓库或发给别人；删除它会让所有人登录失效（下次启动会重新生成）。

## 配置说明（环境变量）

数据库连接、JWT 密钥等敏感配置已全部外置为环境变量，配置文件中只保留本地开发的默认值。**不设环境变量时按默认值运行，本地开发无需任何操作**；部署到服务器时只需设置环境变量，不必再改代码或配置文件。

| 环境变量 | 用途 | 默认值 |
|---|---|---|
| `DB_URL` | MySQL 连接地址 | `jdbc:mysql://localhost:3306/smart_library?...` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `root` |
| `JWT_SECRET` | 登录令牌签名密钥（上线必须改） | `smart-library-secret-key-2024-very-long-and-secure` |
| `JWT_EXPIRATION` | 令牌有效期（毫秒） | `86400000`（24 小时） |
| `CORS_ALLOWED_ORIGINS` | 允许跨域的前端地址，多个用逗号分隔 | `*`（全部放行，上线建议收紧） |

设置示例（Windows PowerShell / Linux）：

```powershell
$env:DB_PASSWORD = "你的真实密码"
$env:JWT_SECRET = "换成一段足够长的随机字符串"
java -jar target/smart-library-backend-1.0.0.jar
```

> 注意：`JWT_SECRET` 一旦更换，之前发出的令牌全部失效，所有人需要重新登录。

## 测试账号

| 账号 | 密码 | 角色 | 说明 |
|------|------|------|------|
| admin | admin | 管理员 | 系统内置管理员，后端启动时自动创建（BCrypt 加密存储） |
| 20255030345 | 123456 | 读者 | 旧演示数据，可用 `scripts/cleanup-test-data.ps1` 清理 |

> 密码统一为 4-32 位字母或数字；新注册读者必须使用 11 位学号，`admin` 为保留账号不可注册。

## 功能模块

### 第一优先级（已完成）
- [x] 用户认证模块（JWT + 7天登录保持）
- [x] 图书检索与借阅（多维度搜索、分类浏览、扫码借还）
- [x] 可视化座位预约系统（SVG交互式座位图、楼层切换、违规管理）

### 第二优先级（已完成）
- [x] 智能室内导航（SVG平面图、最优路线）
- [x] 个性化推荐与书单广场（15个优质书单）
- [x] 个人学习中心（阅读打卡、笔记、月度报告）

### 第三优先级
- [ ] 研讨室预约
- [ ] 失物招领
- [ ] 图书馆大数据看板

## 核心亮点

1. **SVG可视化座位图**：绿色=空闲、红色=已占用、黄色=15分钟内释放，点击直接预约
2. **智能室内导航**：SVG绘制楼层平面图，自动生成最优路线
3. **ECharts数据可视化**：月度阅读报告、借阅趋势分析
4. **定时任务**：预约超时自动释放、临时离开超时收回

## 常见问题

### Q1: 后端启动报错 "Communications link failure"
**原因**：MySQL 服务未启动或配置错误
**解决**：
1. 确认 MySQL 服务已启动
2. 检查 application.yml 中的数据库连接配置
3. 确认数据库 `smart_library` 已创建

### Q2: 前端登录报错 "Network Error"
**原因**：后端服务未启动或跨域配置错误
**解决**：
1. 确认后端服务已启动在 8080 端口
2. 检查 vite.config.ts 中的 proxy 配置
3. 确认后端 CorsConfig 已正确配置

### Q3: 预约座位后没有自动释放
**原因**：定时任务未启动
**解决**：
1. 确认后端正常运行
2. 检查 SchedulingConfig 上的 @EnableScheduling 注解
3. 查看后端日志确认定时任务执行

### Q4: 如何修改数据库密码？
**解决**：推荐用环境变量覆盖（不改任何文件）：`$env:DB_USERNAME="你的用户名"; $env:DB_PASSWORD="你的密码"`。
也可以改 `smart-library-backend/src/main/resources/application.yml` 里 `${DB_PASSWORD:root}` 冒号后面的默认值：
```yaml
spring:
  datasource:
    username: ${DB_USERNAME:你的用户名}
    password: ${DB_PASSWORD:你的密码}
```

### Q5: 管理员密码忘了怎么办？
**解决**：管理员账号固定为 `admin`，密码存储在数据库里且是 BCrypt 哈希（不可逆，看不回明文）。
最省事的办法是删掉后端目录下的 `data/smart_library.mv.db`（演示库）后重启，系统会重新建库并创建 `admin` / `admin`；
MySQL 环境则执行 `smart-library-backend/src/main/resources/db/schema.sql` 里的 admin 那一条 `INSERT` 重新写入。登录后请立刻到「个人中心」改掉默认密码。

## 开发团队

- 项目类型：大学生竞赛项目
- 开发周期：3天
- 代码规范：企业级开发标准，中文注释

## 许可证

本项目采用 [MIT License](LICENSE) 开源，可自由使用、修改、二次分发，保留版权声明即可。

