package com.yuncheng.system.organization.dto;

import com.yuncheng.system.api.organization.SystemOrgType;

/** 组织路径中推导出的单级身份。 */
public record OrgIdentity(
        String id,
        SystemOrgType orgType,
        String orgCode,
        String orgName
) {
}
