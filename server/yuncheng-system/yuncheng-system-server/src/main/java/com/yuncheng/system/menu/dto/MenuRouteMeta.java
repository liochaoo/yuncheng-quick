package com.yuncheng.system.menu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/** Vben 动态路由元数据。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MenuRouteMeta(
        String title,
        String icon,
        String activeIcon,
        String activePath,
        Integer order,
        String badge,
        String badgeType,
        String badgeVariants,
        Boolean affixTab,
        Integer affixTabOrder,
        Boolean hideInMenu,
        Boolean hideChildrenInMenu,
        Boolean hideInBreadcrumb,
        Boolean hideInTab,
        Boolean keepAlive,
        Boolean fullPathKey,
        Boolean openInNewWindow,
        Boolean noBasicLayout,
        Integer maxNumOfOpenTab,
        Map<String, String> query,
        String link,
        String iframeSrc
) {
}
