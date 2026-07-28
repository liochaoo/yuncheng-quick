package com.yuncheng.system.file.dto;

import com.yuncheng.framework.file.enums.FileAccessType;
import java.time.Instant;

/** 文件管理详情。 */
public record FileDetail(
        String id,
        String originalName,
        String fileExtension,
        String contentType,
        long fileSize,
        String sha256,
        String storagePlatform,
        String objectKey,
        String policyCode,
        FileAccessType accessType,
        String businessType,
        String businessId,
        String businessPosition,
        int sortOrder,
        String previewUrl,
        String downloadUrl,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {
}
