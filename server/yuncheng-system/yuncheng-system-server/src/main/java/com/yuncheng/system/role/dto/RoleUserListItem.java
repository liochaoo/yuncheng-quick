package com.yuncheng.system.role.dto;

import java.util.List;

/** 角色用户列表项，不包含用户联系方式。 */
public record RoleUserListItem(
        String id,
        String username,
        String realName,
        List<RoleSummary> roles,
        boolean enabled
) {
}
