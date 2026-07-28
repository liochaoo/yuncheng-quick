package com.yuncheng.system.api.role;

/** 系统角色基础信息。 */
public record SystemRoleInfo(
        Long roleId,
        String roleCode,
        String roleName,
        SystemRoleType roleType
) {
}
