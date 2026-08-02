package com.yuncheng.system.user.dto;

import java.time.Instant;
import java.util.List;

/** 用户详情，联系方式已经脱敏。 */
public record UserDetail(
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
        List<String> roleIds,
        List<String> orgIds,
        String primaryOrgId,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {
}
