package com.yuncheng.system.organization.dto;

import com.yuncheng.system.organization.enums.OrgUniqueField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 组织字段唯一性预校验请求。 */
public record OrgUniquenessCheckRequest(
        @Positive(message = "组织主键必须为正数") Long id,
        @Positive(message = "上级组织主键必须为正数") Long parentId,
        @NotNull(message = "校验字段不能为空") OrgUniqueField field,
        @NotBlank(message = "校验值不能为空")
        @Size(max = 100, message = "校验值不能超过 100 个字符") String value
) {
}
