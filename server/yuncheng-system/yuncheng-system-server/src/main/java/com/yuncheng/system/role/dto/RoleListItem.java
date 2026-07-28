package com.yuncheng.system.role.dto;

import com.yuncheng.system.role.enums.RoleType;
import java.time.Instant;

/** 角色列表项。 */
public record RoleListItem(
        String id,
        String roleCode,
        String roleName,
        RoleType roleType,
        int sortOrder,
        long userCount,
        Instant createdAt,
        Instant updatedAt
) {
}
