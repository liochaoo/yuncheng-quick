# 云程快速开发平台

Yuncheng Quick 是一套基于 Spring Boot 4 和 Vben Admin 5.7 的企业级管理系统快速开发框架。

> 当前版本：`v0.1.0-alpha.1`
>
> 项目仍处于早期预览阶段，适合学习、体验和参与测试。用于生产环境前，请自行完成安全、性能、兼容性和业务适配评估。

## 项目介绍

云程快速开发平台面向企业管理系统的快速开发场景，提供统一的前后端工程结构以及常用的系统管理能力。项目采用前后端同仓管理，后端为基于 Spring Boot 4 的模块化单体，前端基于 Vben Admin 5.7 的 Element Plus 版本进行开发。

![云程快速开发平台架构图](docs/images/architecture.png)

## 已实现功能

- 用户管理：用户创建、编辑、启停、密码重置和用户详情
- 角色管理：角色维护、用户分配和角色授权
- 菜单管理：目录、菜单、按钮和动态路由
- 权限认证：接口权限码、前端按钮权限和角色菜单授权
- 登录认证：JWT、Redis 在线会话和 Refresh Token 轮换
- 在线会话：会话查询、详情查看和强制下线
- 安全策略：登录安全和密码策略配置
- 系统日志：登录日志、操作日志和日志清理
- 数据字典：字典和字典选项维护
- 文件能力：本地磁盘、MinIO 和阿里云 OSS 存储适配
- 基础能力：邮件、验证码、缓存、异步日志和集群定时任务

## 技术架构

### 后端

- Java 21
- Spring Boot 4.1
- Spring Security
- MyBatis-Plus
- Flyway
- MySQL 8.4
- Redis 8.2
- Maven

### 前端

- Vben Admin 5.7
- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- pnpm

## 项目结构

```text
yuncheng-quick
├── server/                    # Spring Boot 后端
│   ├── yuncheng-parent/       # Maven 父工程与依赖版本
│   ├── yuncheng-common/       # 公共模型和通用工具
│   ├── yuncheng-framework/    # Web、安全、缓存、文件等基础设施
│   ├── yuncheng-system/       # 用户、角色、菜单、权限等系统模块
│   ├── yuncheng-init/         # 系统初始化
│   ├── yuncheng-demo/         # 开发演示能力
│   └── yuncheng-boot/         # 应用启动模块
├── web/                       # Vben Admin 前端工作区
├── docs/                      # 项目文档
├── LICENSE                    # Apache License 2.0
├── NOTICE                     # 项目版权与归属声明
└── THIRD_PARTY_NOTICES.md     # 第三方开源软件声明
```

## 环境要求

- JDK 21
- Maven 3.9+
- MySQL 8.4
- Redis 8.2
- Node.js 22.18+ 或 24
- pnpm 10+

## 本地启动

### 1. 准备数据库

启动 MySQL 和 Redis，并创建开发数据库：

```sql
CREATE DATABASE yuncheng_quick
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
```

本地开发默认使用：

- MySQL：`localhost:3306`
- 数据库：`yuncheng_quick`
- 数据库账号：`root`
- 数据库密码：`root`
- Redis：`localhost:6379`
- Redis 密码：空

可通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`REDIS_HOST`、`REDIS_PORT` 和 `REDIS_PASSWORD` 等环境变量覆盖。

### 2. 启动后端

```bash
cd server
mvn clean package -DskipTests
java -jar yuncheng-boot/target/yuncheng-boot.jar
```

后端接口默认地址：

```text
http://localhost:8087/api
```

### 3. 启动前端

```bash
cd web
pnpm install
pnpm run dev
```

前端默认地址：

```text
http://localhost:7777
```

### 4. 登录系统

本地首次启动账号：

```text
用户名：admin
密码：admin@123456
```

该账号仅用于本地开发。部署任何可联网访问的环境前，必须通过 `PLATFORM_INIT_ADMIN_PASSWORD` 设置独立的高强度密码。

## 文件存储

文件默认保存在后端运行目录下的：

```text
data/files
```

如需使用 MinIO 或阿里云 OSS，请通过环境变量启用对应存储平台并配置有效凭据。仓库中的 `change-me` 仅为示例占位值。

## 配置说明

- 本地启动默认使用 `dev` Profile
- 生产部署通过 `SPRING_PROFILES_ACTIVE=prod` 启用生产 Profile
- 生产 Profile 启动前必须配置 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`PLATFORM_AUTH_JWT_SECRET` 和 `PLATFORM_INIT_ADMIN_PASSWORD`
- Redis、邮件和对象存储凭据应根据部署环境单独配置
- 不要将真实密码、Token、密钥或生产配置提交到仓库

## 设计文档

项目的总体架构、模块划分、认证授权和扩展方式请参阅：

- [云程快速开发平台设计说明](docs/design.md)

## 问题反馈

本项目目前主要通过 GitHub Issues 收集缺陷和功能建议，相关内容将结合项目定位、影响范围和版本规划进行评估。当前暂不接受外部 Pull Request。

- Bug 报告：[GitHub Issues](https://github.com/liochaoo/yuncheng-quick/issues)
- 功能建议：[GitHub Issues](https://github.com/liochaoo/yuncheng-quick/issues)
- 安全问题：请阅读 [SECURITY.md](SECURITY.md)

## 开源许可

除另有声明的第三方组件外，Yuncheng Quick 采用 [Apache License 2.0](LICENSE) 发布。

前端基于 Vben Admin 5.7 开发，Vben Admin 采用 MIT License，其许可证原文保留在 [`web/LICENSE`](web/LICENSE)。详细信息请参阅 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

Copyright 2026 Yuncheng
