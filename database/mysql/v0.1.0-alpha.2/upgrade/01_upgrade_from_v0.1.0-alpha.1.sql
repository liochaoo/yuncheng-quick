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
    is_primary TINYINT NOT NULL DEFAULT 0 COMMENT '是否主组织',
    primary_user_id BIGINT GENERATED ALWAYS AS (
        CASE WHEN is_primary = 1 THEN user_id ELSE NULL END
    ) STORED COMMENT '主组织唯一约束辅助字段',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户归属组织关系';

INSERT INTO system_org (
    id, parent_id, org_type, org_code, org_name, path_ids, full_path,
    depth, sort_order, description, created_at, created_by, updated_at, updated_by
)
VALUES (
    100000000000003001, NULL, 'ORGANIZATION', 'default', '默认组织',
    '/100000000000003001/', '默认组织', 1, 0, '平台初始化的默认组织',
    CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
);

INSERT INTO system_user_org (
    id, user_id, org_id, is_primary,
    created_at, created_by, updated_at, updated_by
)
SELECT
    u.id, u.id, 100000000000003001, 1,
    CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
FROM system_user u;

INSERT INTO system_menu (
    id, parent_id, menu_type, menu_name, route_name, route_path,
    component_path, redirect, permission_code, sort_order, icon,
    affix_tab, created_at, created_by, updated_at, updated_by
)
VALUES (
    100000000000000200, 100000000000000100, 'MENU', '组织管理',
    'SystemOrganization', '/system/organization', '/system/organization/index',
    NULL, 'system:organization:query', 5, 'lucide:building-2',
    0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
);

INSERT INTO system_menu (
    id, parent_id, menu_type, menu_name, route_name, route_path,
    component_path, redirect, permission_code, sort_order, icon,
    affix_tab, created_at, created_by, updated_at, updated_by
)
VALUES
    (100000000000000201, 100000000000000200, 'BUTTON', '新增组织', NULL, NULL,
     NULL, NULL, 'system:organization:add', 10, NULL, 0,
     CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000202, 100000000000000200, 'BUTTON', '编辑组织', NULL, NULL,
     NULL, NULL, 'system:organization:edit', 20, NULL, 0,
     CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000203, 100000000000000200, 'BUTTON', '移动组织', NULL, NULL,
     NULL, NULL, 'system:organization:move', 30, NULL, 0,
     CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000204, 100000000000000200, 'BUTTON', '删除组织', NULL, NULL,
     NULL, NULL, 'system:organization:delete', 40, NULL, 0,
     CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0);
