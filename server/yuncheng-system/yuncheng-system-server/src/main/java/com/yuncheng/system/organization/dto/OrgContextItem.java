package com.yuncheng.system.organization.dto;

import com.yuncheng.system.api.organization.SystemOrgType;
import java.util.List;

/** 根据直接归属节点动态推导的完整组织上下文。 */
public record OrgContextItem(
        String id,
        String parentId,
        SystemOrgType orgType,
        String orgCode,
        String orgName,
        String fullPath,
        int depth,
        int sortOrder,
        boolean hasChildren,
        boolean protectedOrg,
        List<String> ancestorIds,
        OrgIdentity topOrganization,
        OrgIdentity organization,
        OrgIdentity topDepartment,
        OrgIdentity department,
        OrgIdentity topGroup,
        OrgIdentity group
) {
}
