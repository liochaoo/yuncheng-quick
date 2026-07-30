package com.yuncheng.system.organization.dto;

import com.yuncheng.system.api.organization.SystemOrgType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** 新增组织请求。 */
public record OrgCreateRequest(
        @Positive(message = "上级组织主键必须大于 0") Long parentId,
        @NotNull(message = "组织类型不能为空") SystemOrgType orgType,
        @NotBlank(message = "组织编码不能为空")
        @Size(max = 64, message = "组织编码不能超过 64 个字符")
        @Pattern(
                regexp = "[A-Za-z][A-Za-z0-9_-]*",
                message = "组织编码只能包含字母、数字、下划线和连字符，并以字母开头"
        )
        String orgCode,
        @NotBlank(message = "组织名称不能为空")
        @Size(max = 100, message = "组织名称不能超过 100 个字符")
        String orgName,
        @PositiveOrZero(message = "排序号不能小于 0") Integer sortOrder,
        @Size(max = 500, message = "组织说明不能超过 500 个字符")
        String description
) {
}
