# 高校综合信息管理系统

高校综合信息管理系统是信息系统实践课程结项项目，采用前后端分离架构，面向高校基础数据维护、人员管理、权限管理、课程公告、业务申请、附件管理、审批流转和日志审计等场景。

当前主分支已整理为结项版代码，可从 GitHub 克隆后在本地运行、构建和验收。项目采用 Vue 3 + Spring Boot + MyBatis + MySQL 技术路线，围绕统一认证、RBAC 权限、动态菜单、业务数据维护、附件管理、工作流审批和日志审计完成完整闭环。

## 项目状态

- 版本阶段：结项版
- 运行方式：本地原生开发、前后端分离运行
- 后端服务：Spring Boot，默认端口 `8080`
- 前端服务：Vite，默认端口 `5173`
- 数据库：MySQL，默认库名 `xtu_system`
- 验证日期：2026-05-17

已验证命令：

```bash
cd backend && TEST_DB_PASSWORD=123456 mvn test
cd backend && mvn -DskipTests package
cd frontend && npm ci
cd frontend && npm run build
cd frontend && npm run test:e2e
```

验证结果：

- 后端自动化测试通过，7 个测试用例通过。
- 后端 jar 打包通过。
- 前端依赖安装和生产构建通过。
- 前端 Playwright 冒烟测试通过，2 个 E2E 用例通过。

## 技术栈

前端：

- Vue 3
- JavaScript
- Vite
- Pinia
- Vue Router
- Element Plus
- Axios
- Playwright

后端：

- Java 17
- Spring Boot 3
- Spring Security
- JWT
- MyBatis XML
- MySQL 8.0
- JUnit 5

部署：

- Ubuntu
- Nginx
- systemd
- MySQL
- Shell 备份脚本

## 项目结构

```text
xtu_system/
├── backend/                       # Spring Boot 后端工程
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/com/xtu/system
│       │   ├── common/            # 通用响应、异常、分页、工具类
│       │   ├── config/            # 安全、Web、JWT 配置
│       │   └── modules/           # 认证、系统、组织、人员、业务、附件、工作流模块
│       ├── main/resources/
│       │   ├── db/migration/      # MySQL 初始化脚本
│       │   └── mapper/            # MyBatis XML
│       └── test/                  # 后端集成测试
├── frontend/                      # Vue 3 前端工程
│   ├── package.json
│   ├── vite.config.js
│   ├── nginx.conf
│   └── src/
│       ├── api/                   # 接口封装
│       ├── components/            # 公共组件
│       ├── layout/                # 后台主布局
│       ├── router/                # 静态路由、动态路由、路由守卫
│       ├── stores/                # 登录态、菜单、权限状态
│       ├── utils/                 # 请求封装
│       └── views/                 # 页面模块
├── deploy/                        # Nginx、systemd、备份恢复配置
├── docs/                          # 计划书、报告、接口、数据库、Git 材料
└── README.md
```

## 功能范围

结项版已覆盖以下模块：

- 登录认证：账号密码登录、退出登录、JWT 鉴权、当前用户信息、修改密码。
- 权限体系：用户、角色、菜单、按钮权限、后端动态菜单、前端按钮级权限控制。
- 工作台：用户、学生、教师、课程、申请、待办等实时统计。
- 系统管理：用户管理、角色管理、菜单管理、登录日志、操作日志。
- 组织管理：部门树查询、部门下拉、新增、编辑、删除。
- 人员管理：学生管理、教师管理、导入、导出、批量删除、账号创建和解绑。
- 业务管理：课程管理、公告管理、申请管理，支持附件绑定和附件数量展示。
- 工作流：申请待办、已办、审批通过、审批驳回、流转记录。
- 附件管理：上传、下载、查询、删除，业务记录删除时联动清理附件。
- 审计日志：登录日志、操作日志自动记录和分页查询。

## 快速运行

### 1. 环境要求

- JDK 17
- Maven
- Node.js 22 或兼容版本
- npm
- MySQL 8.0
- Git

### 2. 克隆仓库

```bash
git clone https://github.com/l1160/xtu_system.git
cd xtu_system
```

### 3. 创建数据库

如果 MySQL 账号为 `root`，密码为 `123456`：

```bash
mysql -uroot -p123456 -e "CREATE DATABASE IF NOT EXISTS xtu_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

数据库表结构和初始化数据由后端启动时执行：

```text
backend/src/main/resources/db/migration/
├── V1__init_user_module.sql
├── V2__business_modules.sql
└── V3__workflow_attachment_log.sql
```

### 4. 启动后端

```bash
cd backend
DB_USERNAME=root DB_PASSWORD=123456 mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

如果数据库账号或密码不同，通过环境变量调整：

```bash
DB_URL='jdbc:mysql://127.0.0.1:3306/xtu_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai' \
DB_USERNAME=root \
DB_PASSWORD=你的密码 \
mvn spring-boot:run
```

### 5. 启动前端

另开一个终端：

```bash
cd frontend
npm ci
npm run dev
```

前端默认地址：

```text
http://localhost:5173
```

前端开发环境通过 Vite 将 `/api` 代理到后端 `http://127.0.0.1:8080`。

## 默认账号

```text
管理员：admin / admin123
教师：teacher01 / teacher123
```

## 常用命令

后端编译：

```bash
cd backend
mvn -DskipTests compile
```

后端测试：

```bash
cd backend
TEST_DB_PASSWORD=123456 mvn test
```

后端打包：

```bash
cd backend
mvn -DskipTests package
```

前端安装依赖：

```bash
cd frontend
npm ci
```

前端构建：

```bash
cd frontend
npm run build
```

前端 E2E：

```bash
cd frontend
npm run test:e2e:install
npm run test:e2e
```

## 测试与验收

结项验收重点覆盖：

- 管理员账号登录并访问核心页面。
- 教师账号登录后菜单和按钮权限收敛。
- 用户管理状态切换、重置密码、分配角色。
- 学生和教师新增、导入、导出、批量删除、账号创建。
- 课程、公告、申请新增、编辑、附件上传、删除联动清理。
- 申请提交、审批通过、审批驳回、流转记录查询。
- 登录日志和操作日志写入。

自动化测试覆盖：

- 后端集成测试：认证、工作台、人员账号、业务模块、工作流、附件和日志。
- 前端 E2E：管理员核心页面访问、教师账号菜单和权限收敛。

## 生产构建

后端生成 jar：

```bash
cd backend
mvn -DskipTests package
```

生成文件：

```text
backend/target/xtu-system-backend-0.1.0.jar
```

前端生成静态资源：

```bash
cd frontend
npm ci
npm run build
```

生成目录：

```text
frontend/dist/
```

## 部署说明

原生部署推荐使用：

- Nginx 托管前端 `dist`。
- Nginx 反向代理 `/api` 到 Spring Boot。
- systemd 管理后端 jar。
- MySQL 保存业务数据。
- Shell 脚本定期备份和恢复数据库。

部署配置位于：

```text
deploy/
├── README.md
├── nginx/xtu_system.conf
├── systemd/xtu-system-backend.service
└── scripts/
    ├── backup_mysql.sh
    └── restore_mysql.sh
```

## Release 发布建议

可以将结项版发布为 GitHub Release，例如 `v1.0.0`。建议附件包含：

- 后端 jar：`backend/target/xtu-system-backend-0.1.0.jar`
- 前端构建包：`frontend/dist/`
- 数据库脚本：`backend/src/main/resources/db/migration/`
- 项目说明文档：`README.md` 和 `docs/`

Release 不能免除运行环境配置，使用者仍需准备 Java 17、MySQL 和前端静态资源服务；如果使用开发模式运行，还需要 Maven、Node.js 和 npm。

## Git 协作约定

- 每个功能模块单独创建分支和 Pull Request。
- 每个 PR 只提交当前模块相关文件，不混入无关代码。
- 文档、前端、后端、测试、部署配置尽量分开提交。
- PR 创建后先检查文件范围、提交说明和测试记录，再合并。
- 不提交 `node_modules/`、`target/`、`dist/`、上传附件和本地环境变量文件。

## 文档索引

- [项目计划书](docs/plan/project_plan.md)
- [Git 过程说明](docs/git/git_process.md)
- [中期与结项报告](docs/report/中期与结项报告.md)
- [软件说明书](<docs/report/软件说明书 .md>)
- [数据库设计](docs/report/数据库设计.md)
- [接口文档](<docs/report/接口文档 .md>)
- [项目目录和接口清单](<docs/report/项目目录和接口清单 .md>)
- [部署说明](deploy/README.md)

## 注意事项

- 首次启动前请先创建 `xtu_system` 数据库。
- 默认后端数据库密码是 `root`，如果本机密码是 `123456`，请使用 `DB_PASSWORD=123456` 覆盖。
- 前端 `npm ci` 当前可能提示依赖安全告警，不影响项目运行；如需正式上线，可单独升级依赖并回归测试。
- 生成目录 `frontend/dist/`、`backend/target/`、`node_modules/` 不应提交到仓库。
