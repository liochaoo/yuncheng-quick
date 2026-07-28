package com.yuncheng.common.context;

import java.util.List;

/** 当前已认证用户的稳定上下文信息。 */
public record CurrentUser(
        Long userId,
        String username,
        String realName,
        String avatar,
        List<String> roleCodes
) {

    public CurrentUser {
        roleCodes = roleCodes == null ? List.of() : List.copyOf(roleCodes);
    }
}
