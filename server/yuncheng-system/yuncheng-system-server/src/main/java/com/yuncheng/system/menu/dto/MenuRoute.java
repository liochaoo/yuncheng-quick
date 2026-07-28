package com.yuncheng.system.menu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** Vben 后端动态菜单节点。 */
public record MenuRoute(
        String name,
        String path,
        String component,
        String redirect,
        MenuRouteMeta meta,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<MenuRoute> children
) {
}
