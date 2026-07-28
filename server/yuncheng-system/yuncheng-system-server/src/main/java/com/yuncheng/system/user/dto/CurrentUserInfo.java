package com.yuncheng.system.user.dto;

import java.util.List;

/** Vben 当前用户基本信息。 */
public record CurrentUserInfo(
        String userId,
        String username,
        String realName,
        String avatar,
        List<String> roles,
        String homePath
) {
}
