package com.yuncheng.system.menu.dto;

/** 删除菜单前的影响范围。 */
public record MenuDeleteImpact(long menuCount, long roleRelationCount) {
}
