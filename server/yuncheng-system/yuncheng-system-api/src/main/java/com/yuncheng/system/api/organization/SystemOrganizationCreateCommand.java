package com.yuncheng.system.api.organization;

/** 系统内部创建组织节点的命令。 */
public record SystemOrganizationCreateCommand(
        Long nodeId,
        Long parentId,
        SystemOrganizationNodeType nodeType,
        String nodeCode,
        String nodeName,
        Integer sortOrder,
        String description
) {
}
