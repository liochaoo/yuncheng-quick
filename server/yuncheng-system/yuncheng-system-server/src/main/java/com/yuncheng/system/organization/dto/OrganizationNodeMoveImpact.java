package com.yuncheng.system.organization.dto;

/** 移动组织节点影响。 */
public record OrganizationNodeMoveImpact(
        int nodeCount,
        String newFullPath
) {
}
