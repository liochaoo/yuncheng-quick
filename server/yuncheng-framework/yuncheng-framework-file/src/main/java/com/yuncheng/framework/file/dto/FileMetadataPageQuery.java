package com.yuncheng.framework.file.dto;

import com.yuncheng.framework.file.enums.FileAccessType;

/** 文件元数据分页查询条件。 */
public record FileMetadataPageQuery(
        int page,
        int pageSize,
        String originalName,
        String storagePlatform,
        String policyCode,
        FileAccessType accessType,
        String businessType
) {
}
