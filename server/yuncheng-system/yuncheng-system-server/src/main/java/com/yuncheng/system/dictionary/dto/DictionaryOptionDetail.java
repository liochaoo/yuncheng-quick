package com.yuncheng.system.dictionary.dto;

import java.time.Instant;

/** 数据字典选项详情。 */
public record DictionaryOptionDetail(
        String id,
        String dictionaryId,
        String optionValue,
        String optionLabel,
        String description,
        int sortOrder,
        boolean enabled,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {
}
