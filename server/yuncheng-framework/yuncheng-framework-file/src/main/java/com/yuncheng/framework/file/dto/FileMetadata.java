package com.yuncheng.framework.file.dto;

import com.yuncheng.framework.file.enums.FileAccessType;
import java.time.Instant;

/** 文件表中可供内部模块查询的完整元数据。 */
public record FileMetadata(
        Long id,
        String storagePlatform,
        String objectKey,
        String originalName,
        String fileExtension,
        String contentType,
        long fileSize,
        String sha256,
        String policyCode,
        FileAccessType accessType,
        String businessType,
        Long businessId,
        String businessPosition,
        int sortOrder,
        Instant createdAt,
        Long createdBy,
        Instant updatedAt,
        Long updatedBy
) {
}
