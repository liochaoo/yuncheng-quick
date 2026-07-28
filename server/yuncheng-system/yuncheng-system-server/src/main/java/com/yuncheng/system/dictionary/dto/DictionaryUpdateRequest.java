package com.yuncheng.system.dictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** 编辑数据字典请求。 */
public record DictionaryUpdateRequest(
        @NotBlank(message = "字典名称不能为空")
        @Size(max = 100, message = "字典名称不能超过 100 个字符")
        String dictionaryName,
        @Size(max = 500, message = "字典说明不能超过 500 个字符")
        String description,
        @PositiveOrZero(message = "排序号不能小于 0") Integer sortOrder
) {
}
