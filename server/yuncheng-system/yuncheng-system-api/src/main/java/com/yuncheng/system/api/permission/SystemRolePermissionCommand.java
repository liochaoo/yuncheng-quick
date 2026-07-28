package com.yuncheng.system.api.permission;

import java.util.List;

/** 整体保存单个角色菜单权限的参数。 */
public record SystemRolePermissionCommand(Long roleId, List<Long> menuIds) {
}
