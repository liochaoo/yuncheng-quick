package com.yuncheng.system.role.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 角色用户关系中的用户主键列表请求。 */
public record RoleUserIdsRequest(
        @NotEmpty(message = "用户列表不能为空")
        @Size(max = 100, message = "单次最多处理 100 个用户")
        List<@NotNull(message = "用户主键不能为空")
                @Positive(message = "用户主键必须为正数") Long> ids
) {
}
