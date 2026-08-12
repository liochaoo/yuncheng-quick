package com.yuncheng.system.user.dto;

/** 用户导入的一条行级校验错误。 */
public record UserImportError(int rowNumber, String field, String message) {
}
