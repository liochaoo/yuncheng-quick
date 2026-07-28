package com.yuncheng.system.role.mapper;

import com.yuncheng.system.role.enums.RoleType;

/** 用户角色批量查询结果。 */
public record RoleSummaryRow(
        Long userId,
        Long roleId,
        String roleCode,
        String roleName,
        RoleType roleType
) {
}
