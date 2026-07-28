package com.yuncheng.system.dictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 数据字典选项值唯一性预校验。 */
public record DictionaryOptionUniquenessCheckRequest(
        @NotBlank(message = "选项值不能为空")
        @Size(max = 100, message = "选项值不能超过 100 个字符")
        @Pattern(
                regexp = "[A-Za-z0-9][A-Za-z0-9._:-]*",
                message = "选项值只能包含字母、数字、点、下划线、冒号和连字符"
        )
        String value
) {
}
