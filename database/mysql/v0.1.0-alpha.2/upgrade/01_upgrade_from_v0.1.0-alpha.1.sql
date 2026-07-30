CREATE TABLE system_organization_node (
    id BIGINT NOT NULL COMMENT '主键',
    parent_id BIGINT NULL COMMENT '上级组织节点',
    parent_scope_id BIGINT GENERATED ALWAYS AS (IFNULL(parent_id, 0)) STORED
        COMMENT '同级唯一约束辅助字段',
    node_type VARCHAR(20) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '节点类型',
    node_code VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '节点编码',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    path_ids VARCHAR(512) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '节点主键路径',
    full_path VARCHAR(2500) NOT NULL COMMENT '完整组织路径',
    depth INT NOT NULL COMMENT '节点深度',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    description VARCHAR(500) NULL COMMENT '节点说明',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    updated_by BIGINT NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_organization_node_code (node_code),
    UNIQUE KEY uk_system_organization_parent_name (parent_scope_id, node_name),
    KEY idx_system_organization_parent_sort (parent_id, sort_order, id),
    KEY idx_system_organization_path (path_ids),
    CONSTRAINT fk_system_organization_parent FOREIGN KEY (parent_id)
        REFERENCES system_organization_node (id) ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统组织节点';

INSERT INTO system_menu (
    id, parent_id, menu_type, menu_name, route_name, route_path,
    component_path, redirect, permission_code, sort_order, icon,
    affix_tab, created_at, created_by, updated_at, updated_by
)
VALUES (
    100000000000000200, 100000000000000100, 'MENU', '组织管理',
    'SystemOrganization', '/system/organization', '/system/organization/index',
    NULL, 'system:organization:query', 15, 'lucide:building-2',
    0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
);

INSERT INTO system_menu (
    id, parent_id, menu_type, menu_name, route_name, route_path,
    component_path, redirect, permission_code, sort_order, icon,
    affix_tab, created_at, created_by, updated_at, updated_by
)
VALUES
    (100000000000000201, 100000000000000200, 'BUTTON', '新增组织节点', NULL, NULL,
     NULL, NULL, 'system:organization:add', 10, NULL, 0,
     CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000202, 100000000000000200, 'BUTTON', '编辑组织节点', NULL, NULL,
     NULL, NULL, 'system:organization:edit', 20, NULL, 0,
     CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000203, 100000000000000200, 'BUTTON', '移动组织节点', NULL, NULL,
     NULL, NULL, 'system:organization:move', 30, NULL, 0,
     CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000204, 100000000000000200, 'BUTTON', '删除组织节点', NULL, NULL,
     NULL, NULL, 'system:organization:delete', 40, NULL, 0,
     CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0);
