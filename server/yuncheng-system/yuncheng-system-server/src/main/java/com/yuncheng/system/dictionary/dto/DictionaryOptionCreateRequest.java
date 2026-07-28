package com.yuncheng.system.dictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** 新增数据字典选项请求。 */
public record DictionaryOptionCreateRequest(
        @NotBlank(message = "选项值不能为空")
        @Size(max = 100, message = "选项值不能超过 100 个字符")
        @Pattern(
                regexp = "[A-Za-z0-9][A-Za-z0-9._:-]*",
                message = "选项值只能包含字母、数字、点、下划线、冒号和连字符"
        )
        String optionValue,
        @NotBlank(message = "选项标签不能为空")
        @Size(max = 100, message = "选项标签不能超过 100 个字符")
        String optionLabel,
        @Size(max = 500, message = "选项说明不能超过 500 个字符")
        String description,
        @PositiveOrZero(message = "排序号不能小于 0") Integer sortOrder
) {
}
