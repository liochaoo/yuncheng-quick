package com.yuncheng.system.dictionary.dto;

import java.time.Instant;

/** 数据字典列表项。 */
public record DictionaryListItem(
        String id,
        String dictionaryCode,
        String dictionaryName,
        String description,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
}
