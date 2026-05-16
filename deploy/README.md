# 原生部署说明

本文档为结项版部署说明，提供 Ubuntu 服务器上的原生部署操作参考。课程结项演示可优先使用本地开发方式运行，后续上线服务器时按本文档执行。

本目录提供 Ubuntu 服务器上线参考，部署形态为：

- Nginx 托管前端 `dist`
- Nginx 反向代理 `/api` 到 Spring Boot
- systemd 管理后端 jar
- MySQL 8.0 保存业务数据
- `deploy/scripts` 提供数据库备份和恢复脚本

本项目结项版不依赖 Docker 部署，推荐使用 Nginx + systemd + MySQL 的原生部署方式。

## 一、服务器目录

建议目录：

```text
/opt/xtu-system/
├── backend/
│   ├── xtu-system-backend.jar
│   └── storage/uploads/
├── frontend/
│   └── dist/
├── logs/
└── backup/
```

## 二、构建产物

后端：

```bash
cd backend
mvn clean package -DskipTests
```

生成：

```text
backend/target/xtu-system-backend-0.1.0.jar
```

前端：

```bash
cd frontend
npm install
npm run build
```

生成：

```text
frontend/dist/
```

## 三、后端 systemd

复制服务文件：

```bash
sudo cp deploy/systemd/xtu-system-backend.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable xtu-system-backend
sudo systemctl start xtu-system-backend
```

查看状态：

```bash
sudo systemctl status xtu-system-backend
journalctl -u xtu-system-backend -f
```

## 四、Nginx

复制配置：

```bash
sudo cp deploy/nginx/xtu_system.conf /etc/nginx/conf.d/xtu_system.conf
sudo nginx -t
sudo systemctl reload nginx
```

访问：

```text
http://服务器IP/
```

## 五、备份恢复

备份：

```bash
chmod +x deploy/scripts/backup_mysql.sh
DB_PASSWORD=123456 deploy/scripts/backup_mysql.sh
```

恢复：

```bash
chmod +x deploy/scripts/restore_mysql.sh
DB_PASSWORD=123456 deploy/scripts/restore_mysql.sh /opt/xtu-system/backup/xtu_system_20260504_200000.sql.gz
```

## 六、上线检查清单

- MySQL 服务已启动，数据库账号可连接。
- 后端 jar 可通过 `systemctl status` 查看为 running。
- Nginx `nginx -t` 校验通过。
- 浏览器可打开前端首页。
- 管理员账号可登录。
- `/api/auth/login` 返回 200。
- 附件上传目录存在且后端进程有读写权限。
- 登录日志和操作日志正常写入。

## 七、环境变量建议

后端生产环境建议通过 systemd 或 shell 环境变量注入以下配置：

```bash
DB_URL='jdbc:mysql://127.0.0.1:3306/xtu_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false'
DB_USERNAME='root'
DB_PASSWORD='替换为生产密码'
JWT_SECRET='替换为生产密钥'
UPLOAD_DIR='/opt/xtu-system/backend/storage/uploads'
```

正式服务器上线前，应将数据库密码、JWT 密钥、上传目录和 Nginx 域名替换为服务器实际配置。
