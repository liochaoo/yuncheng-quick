package com.yuncheng.system.organization.dto;

import com.yuncheng.system.api.organization.SystemOrgType;
import java.time.Instant;

/** 组织详情。 */
public record OrgDetail(
        String id,
        String parentId,
        String parentName,
        SystemOrgType orgType,
        String orgCode,
        String orgName,
        String fullPath,
        int depth,
        int sortOrder,
        String description,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {
}
