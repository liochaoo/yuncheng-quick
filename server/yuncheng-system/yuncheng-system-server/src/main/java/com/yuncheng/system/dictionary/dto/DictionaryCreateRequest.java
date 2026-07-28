package com.yuncheng.system.dictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** 新增数据字典请求。 */
public record DictionaryCreateRequest(
        @NotBlank(message = "字典编码不能为空")
        @Size(max = 50, message = "字典编码不能超过 50 个字符")
        @Pattern(
                regexp = "[A-Za-z][A-Za-z0-9_-]*",
                message = "字典编码只能包含字母、数字、下划线和连字符，并以字母开头"
        )
        String dictionaryCode,
        @NotBlank(message = "字典名称不能为空")
        @Size(max = 100, message = "字典名称不能超过 100 个字符")
        String dictionaryName,
        @Size(max = 500, message = "字典说明不能超过 500 个字符")
        String description,
        @PositiveOrZero(message = "排序号不能小于 0") Integer sortOrder
) {
}
