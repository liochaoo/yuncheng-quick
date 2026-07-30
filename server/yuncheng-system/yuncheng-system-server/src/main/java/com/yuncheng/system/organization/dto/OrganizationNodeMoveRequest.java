package com.yuncheng.system.organization.dto;

import jakarta.validation.constraints.Positive;

/** 移动组织节点请求，空上级表示移动为顶级节点。 */
public record OrganizationNodeMoveRequest(
        @Positive(message = "上级组织主键必须大于 0") Long parentId
) {
}
