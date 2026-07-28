package com.yuncheng.system.dictionary.dto;

import java.time.Instant;

/** 数据字典选项列表项。 */
public record DictionaryOptionListItem(
        String id,
        String dictionaryId,
        String optionValue,
        String optionLabel,
        String description,
        int sortOrder,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
