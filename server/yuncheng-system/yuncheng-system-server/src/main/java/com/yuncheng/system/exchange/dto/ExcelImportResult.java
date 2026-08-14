package com.yuncheng.system.exchange.dto;

import java.util.List;

/** Excel 导入结果；校验失败时不会写入任何业务数据。 */
public record ExcelImportResult(
        boolean success,
        int totalCount,
        int importedCount,
        int errorCount,
        List<ExcelImportError> errors
) {
}
