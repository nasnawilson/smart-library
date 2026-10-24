# 新 PRD 改造说明

本次已按您提供的新 PRD 将项目核心数据模型调整为 6 张表：

- `sys_user`
- `book_info`
- `seat_info`
- `borrow_record`
- `reserve_record`
- `book_list`

## 兼容处理

为了避免前端页面大面积重写，后端仍保留原接口路径：

- `/auth/login`
- `/books`
- `/seats`
- `/borrows`
- `/reservations`
- `/book-lists`
- `/dashboard/stats`

后端实体层通过 MyBatis-Plus 注解把新 PRD 字段映射为前端已有字段。例如：

- `student_id` → `studentNo`
- `book_name` → `title`
- `cover` → `coverImage`
- `total_num - borrow_num` → `availableCount`
- `seat_num` → `seatNo`
- `reserve_date` → `reservationDate`

## 登录说明

新 PRD 要求密码为明文 `123456`，因此登录校验已从 MD5 改为明文校验。

测试账号：

| 学号 | 密码 |
|---|---|
| `20255030344` | `123456` |
| `20255030345` | `123456` |
| `20255030346` | `123456` |

> 以上为本次 PRD 改造当时的账号方案，保留记录。**账号方案后续已变更**：管理员改为 `admin` / `admin`，密码改为 BCrypt 加密存储，读者需使用 11 位学号注册，密码为 4-32 位字母或数字。

## 数据说明

数据库脚本已补齐：

- 3 个测试用户
- 1、2 楼各 50 个座位
- 50 条图书数据
- 3 条借阅记录
- 3 条预约记录
- 15 条书单数据
