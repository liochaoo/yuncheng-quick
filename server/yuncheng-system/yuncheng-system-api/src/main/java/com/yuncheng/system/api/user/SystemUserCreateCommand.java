package com.yuncheng.system.api.user;

/** 创建系统用户参数，用户主键为空时由持久层生成。 */
public record SystemUserCreateCommand(
        Long userId,
        String username,
        String password,
        String realName,
        String phone,
        String email,
        int sortOrder,
        boolean enabled,
        Long primaryOrgId,
        boolean passwordChangeRequired
) {
}
