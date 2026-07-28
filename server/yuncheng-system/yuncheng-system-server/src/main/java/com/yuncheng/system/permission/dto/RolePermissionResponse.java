package com.yuncheng.system.permission.dto;

import java.util.List;

/** 角色当前菜单授权。 */
public record RolePermissionResponse(String roleId, boolean readOnly, List<String> menuIds) {
}
