package com.yuncheng.system.role.dto;

import com.yuncheng.system.role.enums.RoleType;
import java.time.Instant;

/** 角色详情。 */
public record RoleDetail(
        String id,
        String roleCode,
        String roleName,
        RoleType roleType,
        int sortOrder,
        long userCount,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {
}
