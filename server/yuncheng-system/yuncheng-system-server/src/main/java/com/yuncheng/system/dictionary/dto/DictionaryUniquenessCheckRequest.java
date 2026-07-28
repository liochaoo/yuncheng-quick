package com.yuncheng.system.dictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 数据字典编码唯一性预校验。 */
public record DictionaryUniquenessCheckRequest(
        @NotBlank(message = "字典编码不能为空")
        @Size(max = 50, message = "字典编码不能超过 50 个字符")
        @Pattern(
                regexp = "[A-Za-z][A-Za-z0-9_-]*",
                message = "字典编码只能包含字母、数字、下划线和连字符，并以字母开头"
        )
        String value
) {
}
