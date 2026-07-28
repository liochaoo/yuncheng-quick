package com.yuncheng.system.role.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 角色主键列表请求。 */
public record RoleIdListRequest(
        @NotEmpty(message = "角色列表不能为空")
        @Size(max = 100, message = "单次最多处理 100 个角色")
        List<@NotNull(message = "角色主键不能为空")
                @Positive(message = "角色主键必须为正数") Long> ids
) {
}
