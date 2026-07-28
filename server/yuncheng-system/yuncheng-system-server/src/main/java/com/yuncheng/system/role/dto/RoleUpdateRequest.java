package com.yuncheng.system.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 编辑角色请求。 */
public record RoleUpdateRequest(
        @NotBlank(message = "角色名称不能为空") @Size(max = 100, message = "角色名称不能超过 100 个字符") String roleName,
        Integer sortOrder
) {
}
