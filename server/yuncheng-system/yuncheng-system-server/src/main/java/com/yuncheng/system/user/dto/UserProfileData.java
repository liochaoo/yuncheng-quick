package com.yuncheng.system.user.dto;

import java.time.Instant;
import java.util.List;

/** system-login 使用的用户个人资料原始数据。 */
public record UserProfileData(
        Long userId,
        String username,
        String realName,
        String avatar,
        String phone,
        String email,
        boolean enabled,
        List<String> roleNames,
        Instant createdAt,
        Instant passwordChangedAt
) {

    public UserProfileData {
        roleNames = roleNames == null ? List.of() : List.copyOf(roleNames);
    }
}
