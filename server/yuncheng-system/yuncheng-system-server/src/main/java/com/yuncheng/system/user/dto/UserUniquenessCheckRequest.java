package com.yuncheng.system.user.dto;

import com.yuncheng.system.user.enums.UserUniqueField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 用户字段唯一性预校验请求。 */
public record UserUniquenessCheckRequest(
        @Positive(message = "用户主键必须为正数") Long id,
        @NotNull(message = "校验字段不能为空") UserUniqueField field,
        @NotBlank(message = "校验值不能为空")
        @Size(max = 254, message = "校验值不能超过 254 个字符") String value
) {
}
