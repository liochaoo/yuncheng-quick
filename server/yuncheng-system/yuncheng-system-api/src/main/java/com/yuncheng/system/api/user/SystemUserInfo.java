package com.yuncheng.system.api.user;

/** 系统用户基础信息。 */
public record SystemUserInfo(
        Long userId,
        String username,
        String realName,
        boolean enabled
) {
}
