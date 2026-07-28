package com.yuncheng.system.file.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 文件主键列表请求。 */
public record FileIdListRequest(
        @NotEmpty(message = "文件列表不能为空")
        @Size(max = 100, message = "单次最多处理 100 个文件")
        List<@NotNull(message = "文件主键不能为空")
                @Positive(message = "文件主键必须为正数") Long> ids
) {
}
