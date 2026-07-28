package com.yuncheng.system.role.dto;

import com.yuncheng.system.role.enums.RoleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 新增角色请求。 */
public record RoleCreateRequest(
        @NotBlank(message = "角色编码不能为空")
        @Size(max = 50, message = "角色编码不能超过 50 个字符")
        @Pattern(regexp = "[A-Za-z][A-Za-z0-9_-]*", message = "角色编码只能包含字母、数字、下划线和连字符，并以字母开头")
        String roleCode,
        @NotBlank(message = "角色名称不能为空") @Size(max = 100, message = "角色名称不能超过 100 个字符") String roleName,
        @NotNull(message = "角色类型不能为空") RoleType roleType,
        Integer sortOrder
) {
}
