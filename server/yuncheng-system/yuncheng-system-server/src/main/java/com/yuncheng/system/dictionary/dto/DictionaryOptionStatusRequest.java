package com.yuncheng.system.dictionary.dto;

import jakarta.validation.constraints.NotNull;

/** 数据字典选项启停请求。 */
public record DictionaryOptionStatusRequest(
        @NotNull(message = "启停状态不能为空") Boolean enabled
) {
}
