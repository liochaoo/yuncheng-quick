package com.yuncheng.system.menu.dto;

/** 菜单详情及其直接关联的展示信息。 */
public record MenuDetail(
        MenuItem menu,
        String parentName
) {
}
