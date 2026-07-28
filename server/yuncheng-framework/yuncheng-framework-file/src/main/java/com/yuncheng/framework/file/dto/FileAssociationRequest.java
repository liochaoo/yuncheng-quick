package com.yuncheng.framework.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 文件业务关联请求。 */
public record FileAssociationRequest(
        @NotBlank(message = "业务类型不能为空")
        @Size(max = 64, message = "业务类型不能超过64个字符")
        String businessType,
        @NotNull(message = "业务主键不能为空")
        @Positive(message = "业务主键必须大于0")
        Long businessId,
        @NotBlank(message = "业务位置不能为空")
        @Size(max = 64, message = "业务位置不能超过64个字符")
        String businessPosition,
        Integer sortOrder
) {
}
