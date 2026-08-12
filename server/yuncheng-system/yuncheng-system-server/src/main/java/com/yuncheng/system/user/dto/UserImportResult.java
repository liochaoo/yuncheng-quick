package com.yuncheng.system.user.dto;

import java.util.List;

/** 用户导入结果；校验失败时不会写入任何用户。 */
public record UserImportResult(
        boolean success,
        int totalCount,
        int importedCount,
        int errorCount,
        List<UserImportError> errors
) {
}
