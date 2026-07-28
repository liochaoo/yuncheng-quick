package com.yuncheng.system.dictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** 编辑数据字典选项请求。 */
public record DictionaryOptionUpdateRequest(
        @NotBlank(message = "选项标签不能为空")
        @Size(max = 100, message = "选项标签不能超过 100 个字符")
        String optionLabel,
        @Size(max = 500, message = "选项说明不能超过 500 个字符")
        String description,
        @PositiveOrZero(message = "排序号不能小于 0") Integer sortOrder
) {
}
