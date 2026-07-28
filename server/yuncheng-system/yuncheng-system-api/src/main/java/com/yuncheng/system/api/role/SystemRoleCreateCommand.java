package com.yuncheng.system.api.role;

/** 创建系统角色参数。 */
public record SystemRoleCreateCommand(
        String roleCode,
        String roleName,
        SystemRoleType roleType,
        int sortOrder
) {
}
