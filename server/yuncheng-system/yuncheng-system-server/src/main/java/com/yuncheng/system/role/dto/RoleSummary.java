package com.yuncheng.system.role.dto;

import com.yuncheng.system.role.enums.RoleType;

/** 用户关联的角色摘要。 */
public record RoleSummary(
        String id,
        String roleCode,
        String roleName,
        RoleType roleType
) {
}
