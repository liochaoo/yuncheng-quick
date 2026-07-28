package com.yuncheng.system.dictionary.dto;

/** 普通业务消费的数据字典选项。 */
public record DictionaryOptionItem(
        String value,
        String label,
        boolean enabled
) {
}
