package com.yuncheng.system.role.dto;

import com.yuncheng.system.role.enums.RoleUniqueField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 角色字段唯一性预校验请求。 */
public record RoleUniquenessCheckRequest(
        @Positive(message = "角色主键必须为正数") Long id,
        @NotNull(message = "校验字段不能为空") RoleUniqueField field,
        @NotBlank(message = "校验值不能为空")
        @Size(max = 100, message = "校验值不能超过 100 个字符") String value
) {
}
