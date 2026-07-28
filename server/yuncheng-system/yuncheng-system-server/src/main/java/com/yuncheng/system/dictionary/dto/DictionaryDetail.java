package com.yuncheng.system.dictionary.dto;

import java.time.Instant;

/** 数据字典详情。 */
public record DictionaryDetail(
        String id,
        String dictionaryCode,
        String dictionaryName,
        String description,
        int sortOrder,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {
}
