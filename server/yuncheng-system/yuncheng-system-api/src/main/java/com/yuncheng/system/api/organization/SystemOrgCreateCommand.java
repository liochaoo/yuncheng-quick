package com.yuncheng.system.api.organization;

/** 系统内部创建组织的命令。 */
public record SystemOrgCreateCommand(
        Long orgId,
        Long parentId,
        SystemOrgType orgType,
        String orgCode,
        String orgName,
        Integer sortOrder,
        String description
) {
}
