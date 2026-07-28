package com.yuncheng.system.permission.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yuncheng.system.menu.enums.MenuType;
import java.util.List;

/** 角色授权使用的菜单权限树节点。 */
public record PermissionMenuNode(
        String id,
        String parentId,
        String menuName,
        MenuType menuType,
        String permissionCode,
        boolean grantable,
        boolean systemRoleOnly,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<PermissionMenuNode> children
) {
}
