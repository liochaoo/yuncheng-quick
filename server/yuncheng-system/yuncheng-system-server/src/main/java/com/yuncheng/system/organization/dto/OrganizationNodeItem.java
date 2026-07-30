package com.yuncheng.system.organization.dto;

import com.yuncheng.system.api.organization.SystemOrganizationNodeType;
import java.util.List;

/** 异步树和节点选择共同使用的组织节点摘要。 */
public record OrganizationNodeItem(
        String id,
        String parentId,
        SystemOrganizationNodeType nodeType,
        String nodeCode,
        String nodeName,
        String fullPath,
        int depth,
        int sortOrder,
        boolean hasChildren,
        boolean protectedNode,
        List<String> ancestorIds
) {
}
