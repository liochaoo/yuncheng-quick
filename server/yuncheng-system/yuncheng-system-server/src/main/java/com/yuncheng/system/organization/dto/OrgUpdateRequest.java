package com.yuncheng.system.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** 编辑组织请求，组织类型和上级通过独立规则维护。 */
public record OrgUpdateRequest(
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
