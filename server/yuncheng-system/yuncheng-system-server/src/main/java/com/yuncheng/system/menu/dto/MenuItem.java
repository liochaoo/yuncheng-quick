package com.yuncheng.system.menu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yuncheng.system.menu.enums.MenuType;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** 菜单管理树节点。 */
public record MenuItem(
        String id,
        String parentId,
        MenuType menuType,
        String menuName,
        String routeName,
        String routePath,
        String componentPath,
        String redirect,
        String permissionCode,
        int sortOrder,
        String icon,
        String activeIcon,
        String activePath,
        String badge,
        String badgeType,
        String badgeVariants,
        boolean affixTab,
        Integer affixTabOrder,
        boolean hideInMenu,
        boolean hideChildrenInMenu,
        boolean hideInBreadcrumb,
        boolean hideInTab,
        boolean keepAlive,
        boolean fullPathKey,
        boolean openInNewWindow,
        boolean noBasicLayout,
        Integer maxNumOfOpenTab,
        Map<String, String> query,
        String link,
        String iframeSrc,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<MenuItem> children
) {
}
