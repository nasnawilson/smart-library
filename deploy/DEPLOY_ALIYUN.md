# 阿里云部署说明

服务器：`120.76.200.28`

## 1. 上传部署包

在本机 PowerShell 中执行：

```powershell
scp .\smart-library-deploy.zip root@120.76.200.28:/root/
```

## 2. 服务器解压

SSH 登录服务器后执行：

```bash
mkdir -p /opt/smart-library
unzip -o /root/smart-library-deploy.zip -d /opt/smart-library
```

## 3. 初始化数据库

如果已经创建过数据库和用户，直接导入表结构和初始数据：

```bash
mysql -usmartlib -p smart_library < /opt/smart-library/db/schema.sql
```

密码是你创建 `smartlib` 用户时设置的密码。

## 4. 启动后端服务

```bash
cp /opt/smart-library/deploy/smart-library-backend.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable --now smart-library-backend
systemctl status smart-library-backend
```

如果需要看后端日志：

```bash
journalctl -u smart-library-backend -f
```

## 5. 配置 Nginx

```bash
cp /opt/smart-library/deploy/nginx-smart-library.conf /etc/nginx/conf.d/smart-library.conf
nginx -t
systemctl enable --now nginx
systemctl restart nginx
```

## 6. 访问网站

```text
http://120.76.200.28
```

默认管理员账号：

```text
admin / admin
```

> 后端首次启动会自动创建该账号并把旧明文密码升级为 BCrypt 哈希；登录后请先到"个人中心"修改密码。

## 7. 常用检查命令

```bash
systemctl status smart-library-backend
systemctl status nginx
curl http://127.0.0.1:8080/dashboard/stats
curl http://120.76.200.28
```

## 8. 注意事项

- 阿里云安全组需要开放 `80`、`443`、`22`。
- 正式使用时不建议直接开放 `8080`。
- 如果改了数据库密码，需要同步修改后端 `application-prod.yml` 后重新打包。
