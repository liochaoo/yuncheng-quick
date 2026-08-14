package com.yuncheng.system.organization.dto;

/** 按数据库排序规则识别出的同级组织名称冲突。 */
public record OrgNameConflict(int rowNumber, int firstRowNumber) {
}
