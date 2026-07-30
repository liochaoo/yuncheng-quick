package com.yuncheng.system.api.organization;

/** 系统组织节点基础信息。 */
public record SystemOrganizationInfo(
        Long nodeId,
        Long parentId,
        SystemOrganizationNodeType nodeType,
        String nodeCode,
        String nodeName,
        String fullPath
) {
}
