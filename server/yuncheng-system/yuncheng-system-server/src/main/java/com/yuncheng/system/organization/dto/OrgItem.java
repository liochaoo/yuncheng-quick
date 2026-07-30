package com.yuncheng.system.organization.dto;

import com.yuncheng.system.api.organization.SystemOrgType;
import java.util.List;

/** 异步树和节点选择共同使用的组织摘要。 */
public record OrgItem(
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
        List<String> ancestorIds
) {
}
