package com.yuncheng.framework.file.dto;

import com.yuncheng.framework.file.enums.FileAccessType;
import java.time.Instant;

/** 提供给业务界面的文件信息。 */
public record FileRecord(
        String id,
        String originalName,
        String fileExtension,
        String contentType,
        long fileSize,
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
