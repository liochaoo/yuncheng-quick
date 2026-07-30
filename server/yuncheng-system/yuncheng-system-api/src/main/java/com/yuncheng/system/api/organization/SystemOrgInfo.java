package com.yuncheng.system.api.organization;

/** 系统组织基础信息。 */
public record SystemOrgInfo(
        Long orgId,
        Long parentId,
        SystemOrgType orgType,
        String orgCode,
        String orgName,
        String fullPath
) {
}
