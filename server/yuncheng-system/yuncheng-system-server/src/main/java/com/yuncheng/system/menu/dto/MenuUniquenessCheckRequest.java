package com.yuncheng.system.menu.dto;

import com.yuncheng.system.menu.enums.MenuUniqueField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 菜单字段唯一性预校验请求。 */
public record MenuUniquenessCheckRequest(
        @Positive(message = "菜单主键必须为正数") Long id,
        @Positive(message = "上级菜单主键必须为正数") Long parentId,
        @NotNull(message = "校验字段不能为空") MenuUniqueField field,
        @NotBlank(message = "校验值不能为空")
        @Size(max = 255, message = "校验值不能超过 255 个字符") String value
) {
}
