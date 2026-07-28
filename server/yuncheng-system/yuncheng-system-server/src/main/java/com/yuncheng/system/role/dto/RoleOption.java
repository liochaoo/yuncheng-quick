package com.yuncheng.system.role.dto;

import com.yuncheng.system.role.enums.RoleType;

/** 角色选择项。 */
public record RoleOption(
        String id,
        String roleCode,
        String roleName,
        RoleType roleType,
        boolean disabled
) {
}
