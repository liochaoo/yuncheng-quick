package com.yuncheng.system.organization.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 批量恢复已选组织的请求。 */
public record OrgIdsRequest(
        @NotEmpty(message = "组织主键列表不能为空")
        @Size(max = 100, message = "单次最多查询 100 个组织")
        List<@NotNull(message = "组织主键不能为空")
                @Positive(message = "组织主键必须大于 0") Long> ids
) {
}
