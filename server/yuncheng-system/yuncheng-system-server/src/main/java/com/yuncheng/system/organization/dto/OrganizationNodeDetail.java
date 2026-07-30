package com.yuncheng.system.organization.dto;

import com.yuncheng.system.api.organization.SystemOrganizationNodeType;
import java.time.Instant;

/** 组织节点详情。 */
public record OrganizationNodeDetail(
        String id,
        String parentId,
        String parentName,
        SystemOrganizationNodeType nodeType,
        String nodeCode,
        String nodeName,
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
