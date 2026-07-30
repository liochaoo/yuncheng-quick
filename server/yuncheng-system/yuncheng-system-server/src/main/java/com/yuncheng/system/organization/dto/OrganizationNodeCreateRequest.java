package com.yuncheng.system.organization.dto;

import com.yuncheng.system.api.organization.SystemOrganizationNodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** 新增组织节点请求。 */
public record OrganizationNodeCreateRequest(
        @Positive(message = "上级组织主键必须大于 0") Long parentId,
        @NotNull(message = "节点类型不能为空") SystemOrganizationNodeType nodeType,
        @NotBlank(message = "节点编码不能为空")
        @Size(max = 64, message = "节点编码不能超过 64 个字符")
        @Pattern(
                regexp = "[A-Za-z][A-Za-z0-9_-]*",
                message = "节点编码只能包含字母、数字、下划线和连字符，并以字母开头"
        )
        String nodeCode,
        @NotBlank(message = "节点名称不能为空")
        @Size(max = 100, message = "节点名称不能超过 100 个字符")
        String nodeName,
        @PositiveOrZero(message = "排序号不能小于 0") Integer sortOrder,
        @Size(max = 500, message = "节点说明不能超过 500 个字符")
        String description
) {
}
