package com.yuncheng.system.permission.dto;

import com.yuncheng.system.permission.constant.PermissionLimits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 角色菜单权限整体保存请求。 */
public record RolePermissionRequest(
        @NotNull(message = "菜单权限列表不能为空")
        @Size(
                max = PermissionLimits.MAX_ROLE_PERMISSION_COUNT,
                message = "单次最多分配 " + PermissionLimits.MAX_ROLE_PERMISSION_COUNT + " 个菜单权限"
        )
        List<@NotNull(message = "菜单主键不能为空")
                @Positive(message = "菜单主键必须为正数") Long> menuIds
) {
}
