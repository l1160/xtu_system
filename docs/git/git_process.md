# Git 使用过程说明

本文档为结项归档版 Git 使用说明，用于说明本项目的协作流程、提交拆分规则和结项阶段仓库整理要求。

## 一、协作方式

项目采用 GitHub Pull Request 协作方式。每个功能模块单独创建分支，提交时只包含当前模块相关文件，避免把其他功能或临时文件混入同一个 PR。

## 二、推荐流程

```bash
git checkout main
git pull origin main
git checkout -b feature/module-name
```

开发完成后检查改动范围：

```bash
git status
git diff --stat
```

只添加当前功能相关文件：

```bash
git add path/to/file
git commit -m "实现某某功能"
```

推送并创建 PR：

```bash
git push -u origin feature/module-name
```

## 三、提交拆分原则

- 登录、用户管理、角色管理、菜单管理分别提交。
- 学生、教师、课程、公告、申请等业务模块分别提交。
- 文档和部署配置单独提交。
- 不提交 `node_modules/`、`target/`、`dist/`、上传附件和本地环境变量。

## 四、常用检查命令

```bash
git status --short
git diff --stat
git log --oneline --decorate --graph --all -20
```

## 五、注意事项

- 不使用 `git clean -fd` 清理未跟踪文件，除非已经确认这些文件都不需要。
- 不使用 `git reset --hard` 覆盖工作区，除非已经备份。
- 每次提交前确认 `git status` 中只有本次 PR 需要的文件。

## 六、结项阶段检查清单

- README 已更新为结项版。
- `docs/report/` 下的报告、软件说明书、数据库设计、接口文档、目录接口清单保持一致。
- 每个功能 PR 只包含对应模块文件。
- 提交信息使用正常功能描述，不包含无关协作痕迹。
- 不提交 `node_modules/`、`target/`、`dist/`、测试结果临时目录和本地环境配置。
- 合并 PR 前检查 Files changed，确认没有误删项目文件。
