# 数据库脚本

本目录提供与 Yuncheng Quick 发布版本对应的数据库脚本，供不使用 Flyway 或需要手动管理数据库版本的场景使用。

当前支持 MySQL 8.4。

## 目录结构

```text
database/
└── mysql/
    └── v0.1.0-alpha.1/
        └── full/
            ├── 01_schema.sql
            └── 02_base_data.sql
```

每个版本可以包含：

- `full/`：用于创建该版本所需的完整表结构和基线数据；
- `upgrade/`：用于从上一发布版本升级到当前版本，首个发布版本没有升级脚本。

## 使用 Flyway

后端默认开启 Flyway，连接空数据库后会自动执行：

```text
server/yuncheng-boot/src/main/resources/db/migration
```

可以通过环境变量关闭：

```text
SPRING_FLYWAY_ENABLED=false
```

使用 Flyway 管理数据库时，不需要再手动执行本目录中的脚本。

## 手动初始化数据库

关闭 Flyway，创建空数据库后，按文件名顺序执行目标版本 `full/` 目录中的脚本：

1. `01_schema.sql`
2. `02_base_data.sql`

全量脚本负责表结构和系统基线数据。管理员账号、平台保留角色及其关系会在后端首次启动时由初始化程序补充。

## 手动升级数据库

从后续版本开始，升级脚本放在目标版本的 `upgrade/` 目录中。升级时从当前版本开始，按照版本顺序依次执行，不能跳过中间版本。

手动管理数据库版本时应保持 Flyway 关闭，避免同时使用两套迁移方式。

## 版本对应关系

数据库脚本版本应与使用的源码版本或 Git Tag 保持一致。已经发布的版本脚本保持不变，后续数据库变化通过新版本目录交付。
