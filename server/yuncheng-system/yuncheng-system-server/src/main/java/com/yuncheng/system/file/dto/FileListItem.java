package com.yuncheng.system.file.dto;

import com.yuncheng.framework.file.enums.FileAccessType;
import java.time.Instant;

/** 文件管理列表项。 */
public record FileListItem(
        String id,
        String originalName,
        String fileExtension,
        String contentType,
        long fileSize,
        String storagePlatform,
        String policyCode,
        FileAccessType accessType,
        String businessType,
        String businessId,
        String businessPosition,
        int sortOrder,
        String previewUrl,
        String downloadUrl,
        Instant createdAt
) {
}
