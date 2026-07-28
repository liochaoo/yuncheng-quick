package com.yuncheng.system.api.user;

/** 批量创建系统用户时的单条用户信息。 */
public record SystemUserBatchCreateItem(
        Long userId,
        String username,
        String realName,
        String phone,
        String email,
        int sortOrder,
        boolean enabled
) {
}
