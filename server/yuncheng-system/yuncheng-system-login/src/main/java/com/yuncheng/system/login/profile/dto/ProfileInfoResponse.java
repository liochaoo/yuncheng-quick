package com.yuncheng.system.login.profile.dto;

import com.yuncheng.framework.file.dto.FileRecord;
import java.time.Instant;
import java.util.List;

/** 当前登录用户个人资料。 */
public record ProfileInfoResponse(
        String userId,
        String username,
        String realName,
        String avatar,
        FileRecord avatarFile,
        String phone,
        String email,
        boolean enabled,
        List<String> roleNames,
        Instant createdAt,
        Instant passwordChangedAt
) {

    public ProfileInfoResponse {
        roleNames = roleNames == null ? List.of() : List.copyOf(roleNames);
    }
}
