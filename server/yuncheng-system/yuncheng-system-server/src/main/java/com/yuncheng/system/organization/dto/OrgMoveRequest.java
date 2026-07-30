package com.yuncheng.system.organization.dto;

import jakarta.validation.constraints.Positive;

/** 移动组织请求，空上级表示移动为顶级组织。 */
public record OrgMoveRequest(
        @Positive(message = "上级组织主键必须大于 0") Long parentId
) {
}
