package com.yuncheng.system.user.dto;

import com.yuncheng.system.api.organization.SystemOrgType;

/** 用户列表展示的主归属摘要。 */
public record UserPrimaryOrgSummary(
        String id,
        SystemOrgType orgType,
        String orgCode,
        String orgName,
        String fullPath,
        int otherOrgCount
) {
}
