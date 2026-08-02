CREATE TABLE system_user (
    id BIGINT NOT NULL COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '登录名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码摘要',
    password_changed_at DATETIME(3) NOT NULL COMMENT '密码最后修改时间',
    login_failed_count INT NOT NULL DEFAULT 0 COMMENT '当前观察窗口内登录失败次数',
    login_failure_window_started_at DATETIME(3) NULL COMMENT '登录失败观察窗口开始时间',
    login_locked_until DATETIME(3) NULL COMMENT '登录临时锁定截止时间',
    real_name VARCHAR(64) NOT NULL COMMENT '姓名',
    avatar VARCHAR(500) NULL COMMENT '头像地址',
    phone VARCHAR(32) NULL COMMENT '手机号码',
    email VARCHAR(254) NULL COMMENT '电子邮箱',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by BIGINT NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_user_username (username),
    UNIQUE KEY uk_system_user_phone (phone),
    UNIQUE KEY uk_system_user_email (email),
    KEY idx_system_user_sort (sort_order, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户';

CREATE TABLE system_user_password_history (
    id BIGINT NOT NULL COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户主键',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码摘要',
    change_source VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '密码设置来源',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by BIGINT NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    KEY idx_user_password_history_user_time (user_id, created_at, id),
    CONSTRAINT fk_user_password_history_user FOREIGN KEY (user_id) REFERENCES system_user (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户密码历史';

CREATE TABLE system_role (
    id BIGINT NOT NULL COMMENT '主键',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_type VARCHAR(20) NOT NULL COMMENT '角色类型',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by BIGINT NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_role_code (role_code),
    UNIQUE KEY uk_system_role_name (role_name),
    KEY idx_system_role_sort (sort_order, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色';

CREATE TABLE system_dictionary (
    id BIGINT NOT NULL COMMENT '主键',
    dictionary_code VARCHAR(50) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '字典编码',
    dictionary_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    description VARCHAR(500) NULL COMMENT '字典说明',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by BIGINT NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_dictionary_code (dictionary_code),
    KEY idx_system_dictionary_sort (sort_order, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统数据字典';

CREATE TABLE system_dictionary_option (
    id BIGINT NOT NULL COMMENT '主键',
    dictionary_id BIGINT NOT NULL COMMENT '字典主键',
    option_value VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin NOT NULL COMMENT '选项值',
    option_label VARCHAR(100) NOT NULL COMMENT '选项标签',
    description VARCHAR(500) NULL COMMENT '选项说明',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by BIGINT NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_dictionary_option_value (dictionary_id, option_value),
    KEY idx_system_dictionary_option_query (dictionary_id, enabled, sort_order, id),
    CONSTRAINT fk_system_dictionary_option_dictionary FOREIGN KEY (dictionary_id)
        REFERENCES system_dictionary (id) ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统数据字典选项';

CREATE TABLE system_org (
    id BIGINT NOT NULL COMMENT '主键',
    parent_id BIGINT NULL COMMENT '上级组织',
    parent_scope_id BIGINT GENERATED ALWAYS AS (IFNULL(parent_id, 0)) STORED
        COMMENT '同级唯一约束辅助字段',
    org_type VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '组织类型',
    org_code VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '组织编码',
    org_name VARCHAR(100) NOT NULL COMMENT '组织名称',
    path_ids VARCHAR(512) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '组织主键路径',
    full_path VARCHAR(2500) NOT NULL COMMENT '完整组织路径',
    depth INT NOT NULL COMMENT '组织层级',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    description VARCHAR(500) NULL COMMENT '组织说明',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by BIGINT NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_org_code (org_code),
    UNIQUE KEY uk_system_org_parent_name (parent_scope_id, org_name),
    KEY idx_system_org_parent_sort (parent_id, sort_order, id),
    KEY idx_system_org_path (path_ids),
    CONSTRAINT fk_system_org_parent FOREIGN KEY (parent_id)
        REFERENCES system_org (id) ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统组织';

CREATE TABLE system_user_org (
    id BIGINT NOT NULL COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户主键',
    org_id BIGINT NOT NULL COMMENT '直接归属组织主键',
    is_primary TINYINT NOT NULL DEFAULT 0 COMMENT '是否主归属',
    primary_user_id BIGINT GENERATED ALWAYS AS (
        CASE WHEN is_primary = 1 THEN user_id ELSE NULL END
    ) STORED COMMENT '主归属唯一约束辅助字段',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by BIGINT NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_user_org (user_id, org_id),
    UNIQUE KEY uk_system_user_org_primary (primary_user_id),
    KEY idx_system_user_org_org (org_id, is_primary, user_id),
    CONSTRAINT ck_system_user_org_primary CHECK (is_primary IN (0, 1)),
    CONSTRAINT fk_system_user_org_user FOREIGN KEY (user_id)
        REFERENCES system_user (id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_system_user_org_org FOREIGN KEY (org_id)
        REFERENCES system_org (id) ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户组织归属关系';

CREATE TABLE system_menu (
    id BIGINT NOT NULL COMMENT '主键',
    parent_id BIGINT NULL COMMENT '上级菜单',
    menu_type VARCHAR(20) NOT NULL COMMENT '菜单类型',
    menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    route_name VARCHAR(100) NULL COMMENT '路由名称',
    route_path VARCHAR(255) NULL COMMENT '完整绝对路由路径',
    component_path VARCHAR(255) NULL COMMENT '组件路径',
    redirect VARCHAR(255) NULL COMMENT '重定向路径',
    permission_code VARCHAR(128) NULL COMMENT '权限码',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    icon VARCHAR(500) NULL COMMENT '菜单图标',
    active_icon VARCHAR(500) NULL COMMENT '激活图标',
    active_path VARCHAR(255) NULL COMMENT '激活菜单路径',
    badge VARCHAR(100) NULL COMMENT '徽标内容',
    badge_type VARCHAR(20) NULL COMMENT '徽标类型',
    badge_variants VARCHAR(32) NULL COMMENT '徽标样式',
    affix_tab TINYINT NOT NULL DEFAULT 0 COMMENT '是否固定标签页',
    affix_tab_order INT NULL COMMENT '固定标签页排序',
    hide_in_menu TINYINT NOT NULL DEFAULT 0 COMMENT '是否在菜单中隐藏',
    hide_children_in_menu TINYINT NOT NULL DEFAULT 0 COMMENT '是否隐藏子菜单',
    hide_in_breadcrumb TINYINT NOT NULL DEFAULT 0 COMMENT '是否在面包屑中隐藏',
    hide_in_tab TINYINT NOT NULL DEFAULT 0 COMMENT '是否在标签页中隐藏',
    keep_alive TINYINT NOT NULL DEFAULT 0 COMMENT '是否缓存页面',
    full_path_key TINYINT NOT NULL DEFAULT 1 COMMENT '是否使用完整路径作为路由键',
    open_in_new_window TINYINT NOT NULL DEFAULT 0 COMMENT '是否在新窗口打开',
    no_basic_layout TINYINT NOT NULL DEFAULT 0 COMMENT '是否不使用基础布局',
    max_num_of_open_tab INT NULL COMMENT '最大标签页数量',
    query_params JSON NULL COMMENT '路由查询参数',
    link VARCHAR(1000) NULL COMMENT '外部链接',
    iframe_src VARCHAR(1000) NULL COMMENT '内嵌页面地址',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by BIGINT NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_menu_route_name (route_name),
    UNIQUE KEY uk_system_menu_route_path (route_path),
    UNIQUE KEY uk_system_menu_permission_code (permission_code),
    KEY idx_system_menu_parent_sort (parent_id, sort_order, id),
    CONSTRAINT fk_system_menu_parent FOREIGN KEY (parent_id) REFERENCES system_menu (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统菜单及权限';

CREATE TABLE system_user_role (
    id BIGINT NOT NULL COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户主键',
    role_id BIGINT NOT NULL COMMENT '角色主键',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by BIGINT NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_user_role (user_id, role_id),
    KEY idx_system_user_role_role (role_id, user_id),
    CONSTRAINT fk_system_user_role_user FOREIGN KEY (user_id) REFERENCES system_user (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_system_user_role_role FOREIGN KEY (role_id) REFERENCES system_role (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关系';

CREATE TABLE system_role_menu (
    id BIGINT NOT NULL COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色主键',
    menu_id BIGINT NOT NULL COMMENT '菜单主键',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by BIGINT NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_role_menu (role_id, menu_id),
    KEY idx_system_role_menu_menu (menu_id, role_id),
    CONSTRAINT fk_system_role_menu_role FOREIGN KEY (role_id) REFERENCES system_role (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_system_role_menu_menu FOREIGN KEY (menu_id) REFERENCES system_menu (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单权限关系';

CREATE TABLE system_security_policy (
    id BIGINT NOT NULL COMMENT '主键',
    policy_key VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '策略标识',
    registration_enabled TINYINT NOT NULL COMMENT '是否开放用户注册',
    password_recovery_enabled TINYINT NOT NULL COMMENT '是否开放找回密码',
    profile_email_enabled TINYINT NOT NULL COMMENT '是否允许个人中心修改邮箱',
    login_captcha_enabled TINYINT NOT NULL COMMENT '登录是否要求图形验证码',
    password_min_length INT NOT NULL COMMENT '密码最小字符数',
    password_max_length INT NOT NULL COMMENT '密码最大字符数',
    password_require_lowercase TINYINT NOT NULL COMMENT '密码是否要求小写字母',
    password_require_uppercase TINYINT NOT NULL COMMENT '密码是否要求大写字母',
    password_require_digit TINYINT NOT NULL COMMENT '密码是否要求数字',
    password_require_special TINYINT NOT NULL COMMENT '密码是否要求特殊字符',
    password_history_count INT NOT NULL COMMENT '禁止重复使用的最近密码次数',
    login_max_failed_attempts INT NOT NULL COMMENT '登录失败锁定阈值',
    login_failure_window_minutes INT NOT NULL COMMENT '登录失败观察窗口分钟数',
    login_lock_minutes INT NOT NULL COMMENT '登录临时锁定分钟数',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by BIGINT NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_security_policy_key (policy_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统安全策略';

CREATE TABLE system_log_login (
    id BIGINT NOT NULL COMMENT '主键',
    event_type VARCHAR(20) NOT NULL COMMENT '事件类型',
    success TINYINT NOT NULL COMMENT '是否成功',
    user_id BIGINT NULL COMMENT '用户主键',
    login_name VARCHAR(50) NULL COMMENT '登录名',
    real_name VARCHAR(64) NULL COMMENT '姓名',
    client_type VARCHAR(32) NULL COMMENT '客户端类型',
    session_id VARCHAR(64) NULL COMMENT '会话标识',
    ip VARCHAR(64) NULL COMMENT '客户端 IP',
    user_agent VARCHAR(1000) NULL COMMENT '客户端 User-Agent',
    failure_reason VARCHAR(500) NULL COMMENT '失败原因',
    trace_id CHAR(32) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '链路标识',
    occurred_at DATETIME(3) NOT NULL COMMENT '发生时间',
    PRIMARY KEY (id),
    KEY idx_system_log_login_time (occurred_at, id),
    KEY idx_system_log_login_user (user_id, occurred_at),
    KEY idx_system_log_login_name (login_name, occurred_at),
    KEY idx_system_log_login_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志';

CREATE TABLE system_log_operation (
    id BIGINT NOT NULL COMMENT '主键',
    action VARCHAR(100) NOT NULL COMMENT '操作名称',
    class_name VARCHAR(255) NOT NULL COMMENT '执行类名',
    method_name VARCHAR(100) NOT NULL COMMENT '执行方法名',
    http_method VARCHAR(16) NULL COMMENT 'HTTP 方法',
    request_path VARCHAR(500) NULL COMMENT '请求路径',
    request_params TEXT NULL COMMENT '脱敏后的请求参数',
    success TINYINT NOT NULL COMMENT '是否成功',
    error_message VARCHAR(1000) NULL COMMENT '失败原因',
    duration_millis BIGINT NOT NULL COMMENT '执行耗时毫秒数',
    user_id BIGINT NULL COMMENT '用户主键',
    username VARCHAR(50) NULL COMMENT '登录名',
    real_name VARCHAR(64) NULL COMMENT '姓名',
    ip VARCHAR(64) NULL COMMENT '客户端 IP',
    user_agent VARCHAR(1000) NULL COMMENT '客户端 User-Agent',
    trace_id CHAR(32) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT '链路标识',
    occurred_at DATETIME(3) NOT NULL COMMENT '发生时间',
    PRIMARY KEY (id),
    KEY idx_system_log_operation_time (occurred_at, id),
    KEY idx_system_log_operation_user (user_id, occurred_at),
    KEY idx_system_log_operation_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';

CREATE TABLE platform_file (
    id BIGINT NOT NULL COMMENT '主键',
    storage_platform VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '存储平台标识',
    object_key VARCHAR(1000) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '存储平台内的对象键',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_extension VARCHAR(32) NULL COMMENT '文件扩展名',
    content_type VARCHAR(255) NOT NULL COMMENT '内容类型',
    file_size BIGINT NOT NULL COMMENT '文件大小',
    sha256 CHAR(64) NOT NULL COMMENT 'SHA-256摘要',
    policy_code VARCHAR(50) NOT NULL COMMENT '上传策略编码',
    access_type VARCHAR(16) NOT NULL DEFAULT 'PRIVATE' COMMENT '访问类型',
    business_type VARCHAR(64) NULL COMMENT '关联业务类型',
    business_id BIGINT NULL COMMENT '关联业务主键',
    business_position VARCHAR(64) NULL COMMENT '关联业务位置',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by BIGINT NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_platform_file_object (storage_platform, object_key),
    KEY idx_platform_file_business (business_type, business_id, business_position, sort_order, id),
    KEY idx_platform_file_sha256 (sha256),
    KEY idx_platform_file_created (created_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台文件';
