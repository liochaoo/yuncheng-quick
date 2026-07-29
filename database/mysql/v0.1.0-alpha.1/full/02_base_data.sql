INSERT INTO system_menu (
    id, parent_id, menu_type, menu_name, route_name, route_path,
    component_path, redirect, permission_code, sort_order, icon,
    affix_tab, created_at, created_by, updated_at, updated_by
)
VALUES (
    100000000000000001, NULL, 'MENU', '工作台', 'Workspace', '/workspace',
    '/workspace/index', NULL, 'platform:workspace:view', 10, 'lucide:layout-dashboard',
    1, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
);

INSERT INTO system_menu (
    id, parent_id, menu_type, menu_name, route_name, route_path,
    component_path, redirect, permission_code, sort_order, icon,
    affix_tab, created_at, created_by, updated_at, updated_by
)
VALUES (
    100000000000000100, NULL, 'CATALOG', '系统管理', 'System', '/system',
    NULL, '/system/user', NULL, 20, 'lucide:settings',
    0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
);

INSERT INTO system_menu (
    id, parent_id, menu_type, menu_name, route_name, route_path,
    component_path, redirect, permission_code, sort_order, icon,
    affix_tab, created_at, created_by, updated_at, updated_by
)
VALUES
    (
        100000000000000110, 100000000000000100, 'MENU', '用户管理', 'SystemUser', '/system/user',
        '/system/user/index', NULL, 'system:user:query', 10, 'lucide:users',
        0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
    ),
    (
        100000000000000120, 100000000000000100, 'MENU', '角色管理', 'SystemRole', '/system/role',
        '/system/role/index', NULL, 'system:role:query', 20, 'lucide:user-cog',
        0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
    ),
    (
        100000000000000130, 100000000000000100, 'MENU', '权限管理', 'SystemPermission', '/system/permission',
        '/system/permission/index', NULL, 'system:permission:query', 30, 'lucide:shield-check',
        0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
    ),
    (
        100000000000000140, 100000000000000100, 'MENU', '菜单管理', 'SystemMenu', '/system/menu',
        '/system/menu/index', NULL, 'system:menu:query', 40, 'lucide:list-tree',
        0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
    ),
    (
        100000000000000190, 100000000000000100, 'MENU', '字典管理', 'SystemDictionary', '/system/dictionary',
        '/system/dictionary/index', NULL, 'system:dictionary:query', 45, 'lucide:notebook-tabs',
        0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
    ),
    (
        100000000000000150, 100000000000000100, 'MENU', '文件管理', 'SystemFile', '/system/file',
        '/system/file/index', NULL, 'system:file:query', 50, 'lucide:files',
        0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
    ),
    (
        100000000000000160, 100000000000000100, 'MENU', '安全管理', 'SystemSecurity', '/system/security',
        '/system/security/index', NULL, 'system:security:query', 60, 'lucide:shield-alert',
        0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
    ),
    (
        100000000000000170, 100000000000000100, 'MENU', '系统日志', 'SystemLog', '/system/log',
        '/system/log/index', NULL, 'system:log:query', 70, 'lucide:scroll-text',
        0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
    ),
    (
        100000000000000180, 100000000000000100, 'MENU', '在线会话', 'SystemSession', '/system/session',
        '/system/session/index', NULL, 'system:session:query', 80, 'lucide:monitor-dot',
        0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0
    );

INSERT INTO system_menu (
    id, parent_id, menu_type, menu_name, route_name, route_path,
    component_path, redirect, permission_code, sort_order, icon,
    affix_tab, created_at, created_by, updated_at, updated_by
)
VALUES
    (100000000000000111, 100000000000000110, 'BUTTON', '新增用户', NULL, NULL,
     NULL, NULL, 'system:user:add', 10, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000112, 100000000000000110, 'BUTTON', '编辑用户', NULL, NULL,
     NULL, NULL, 'system:user:edit', 20, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000113, 100000000000000110, 'BUTTON', '启停用户', NULL, NULL,
     NULL, NULL, 'system:user:change-status', 30, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000114, 100000000000000110, 'BUTTON', '重置密码', NULL, NULL,
     NULL, NULL, 'system:user:reset-password', 40, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000115, 100000000000000110, 'BUTTON', '解除登录锁定', NULL, NULL,
     NULL, NULL, 'system:user:unlock', 50, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000116, 100000000000000110, 'BUTTON', '删除用户', NULL, NULL,
     NULL, NULL, 'system:user:delete', 60, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000121, 100000000000000120, 'BUTTON', '新增角色', NULL, NULL,
     NULL, NULL, 'system:role:add', 10, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000122, 100000000000000120, 'BUTTON', '编辑角色', NULL, NULL,
     NULL, NULL, 'system:role:edit', 20, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000124, 100000000000000120, 'BUTTON', '维护角色用户', NULL, NULL,
     NULL, NULL, 'system:role:assign-user', 30, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000125, 100000000000000120, 'BUTTON', '删除角色', NULL, NULL,
     NULL, NULL, 'system:role:delete', 40, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000131, 100000000000000130, 'BUTTON', '分配权限', NULL, NULL,
     NULL, NULL, 'system:permission:assign', 10, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000132, 100000000000000130, 'BUTTON', '清空缓存', NULL, NULL,
     NULL, NULL, 'system:permission:clear-cache', 20, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000141, 100000000000000140, 'BUTTON', '新增菜单', NULL, NULL,
     NULL, NULL, 'system:menu:add', 10, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000142, 100000000000000140, 'BUTTON', '编辑菜单', NULL, NULL,
     NULL, NULL, 'system:menu:edit', 20, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000144, 100000000000000140, 'BUTTON', '删除菜单', NULL, NULL,
     NULL, NULL, 'system:menu:delete', 30, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000191, 100000000000000190, 'BUTTON', '新增字典数据', NULL, NULL,
     NULL, NULL, 'system:dictionary:add', 10, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000192, 100000000000000190, 'BUTTON', '编辑字典数据', NULL, NULL,
     NULL, NULL, 'system:dictionary:edit', 20, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000193, 100000000000000190, 'BUTTON', '启停字典选项', NULL, NULL,
     NULL, NULL, 'system:dictionary:change-status', 30, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000194, 100000000000000190, 'BUTTON', '删除字典数据', NULL, NULL,
     NULL, NULL, 'system:dictionary:delete', 40, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000151, 100000000000000150, 'BUTTON', '上传文件', NULL, NULL,
     NULL, NULL, 'system:file:upload', 10, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000152, 100000000000000150, 'BUTTON', '预览文件', NULL, NULL,
     NULL, NULL, 'system:file:preview', 20, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000153, 100000000000000150, 'BUTTON', '下载文件', NULL, NULL,
     NULL, NULL, 'system:file:download', 30, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000154, 100000000000000150, 'BUTTON', '删除文件', NULL, NULL,
     NULL, NULL, 'system:file:delete', 40, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000161, 100000000000000160, 'BUTTON', '修改安全策略', NULL, NULL,
     NULL, NULL, 'system:security:edit', 10, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000171, 100000000000000170, 'BUTTON', '清理日志', NULL, NULL,
     NULL, NULL, 'system:log:clean', 10, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0),
    (100000000000000181, 100000000000000180, 'BUTTON', '强制下线', NULL, NULL,
     NULL, NULL, 'system:session:kickout', 10, NULL, 0, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), 0);
