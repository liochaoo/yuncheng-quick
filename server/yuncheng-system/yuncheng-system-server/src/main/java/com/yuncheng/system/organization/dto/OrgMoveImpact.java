package com.yuncheng.system.organization.dto;

/** 移动组织影响。 */
public record OrgMoveImpact(
        int orgCount,
        long userCount,
        long relationCount,
        String newFullPath
) {
}
