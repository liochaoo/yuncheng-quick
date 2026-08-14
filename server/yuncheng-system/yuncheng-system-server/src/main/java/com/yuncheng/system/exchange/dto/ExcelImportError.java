package com.yuncheng.system.exchange.dto;

/** Excel 导入的一条行级校验错误。 */
public record ExcelImportError(int rowNumber, String field, String message) {
}
