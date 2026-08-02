package com.yuncheng.system.user.dto;

import com.yuncheng.system.role.dto.RoleSummary;
import java.time.Instant;
import java.util.List;

/** 用户列表项，联系方式已经脱敏。 */
public record UserListItem(
        String id,
        String username,
        String realName,
        String avatar,
        String phone,
        String email,
        int sortOrder,
        boolean enabled,
        boolean loginLocked,
        Instant loginLockedUntil,
        int loginFailedCount,
        Instant passwordChangedAt,
        UserPrimaryOrgSummary primaryOrg,
        List<RoleSummary> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
