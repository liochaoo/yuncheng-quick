package com.yuncheng.system.menu.service;

import com.yuncheng.system.menu.dto.MenuRoute;
import com.yuncheng.system.menu.dto.MenuRouteMeta;
import com.yuncheng.system.menu.entity.SystemMenu;
import com.yuncheng.system.menu.enums.MenuType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

/** 将有效菜单转换为 Vben 动态路由。 */
@Service
public class MenuRouteService {

    private final MenuQueryService menuQueryService;

    public MenuRouteService(MenuQueryService menuQueryService) {
        this.menuQueryService = menuQueryService;
    }

    public List<MenuRoute> getUserMenus(Long userId) {
        List<SystemMenu> menus = menuQueryService.effectiveRoutes(userId);
        Map<Long, List<SystemMenu>> children = new HashMap<>();
        List<SystemMenu> roots = new ArrayList<>();
        for (SystemMenu menu : menus) {
            if (menu.getParentId() == null) {
                roots.add(menu);
            } else {
                children.computeIfAbsent(menu.getParentId(), ignored -> new ArrayList<>()).add(menu);
            }
        }
        return roots.stream()
                .map(menu -> toRoute(menu, children))
                .filter(Objects::nonNull)
                .toList();
    }

    private MenuRoute toRoute(SystemMenu menu, Map<Long, List<SystemMenu>> children) {
        List<MenuRoute> childRoutes = children.getOrDefault(menu.getId(), List.of()).stream()
                .map(child -> toRoute(child, children))
                .filter(Objects::nonNull)
                .toList();
        if (menu.getMenuType() == MenuType.CATALOG && childRoutes.isEmpty()) {
            return null;
        }
        return new MenuRoute(
                menu.getRouteName(),
                menu.getRoutePath(),
                component(menu),
                menu.getRedirect(),
                meta(menu),
                childRoutes
        );
    }

    private String component(SystemMenu menu) {
        return switch (menu.getMenuType()) {
            case CATALOG, LINK -> null;
            case MENU -> menu.getComponentPath();
            case EMBEDDED -> "IFrameView";
            case BUTTON -> null;
        };
    }

    private MenuRouteMeta meta(SystemMenu menu) {
        return new MenuRouteMeta(
                menu.getMenuName(), menu.getIcon(), menu.getActiveIcon(), menu.getActivePath(),
                menu.getSortOrder(), menu.getBadge(), menu.getBadgeType(), menu.getBadgeVariants(), truth(menu.getAffixTab()),
                menu.getAffixTabOrder(), truth(menu.getHideInMenu()), truth(menu.getHideChildrenInMenu()),
                truth(menu.getHideInBreadcrumb()), truth(menu.getHideInTab()), truth(menu.getKeepAlive()),
                menu.getFullPathKey(), truth(menu.getOpenInNewWindow()), truth(menu.getNoBasicLayout()),
                menu.getMaxNumOfOpenTab(), menuQueryService.parseQuery(menu.getQueryParams()),
                menu.getLink(), menu.getIframeSrc()
        );
    }

    private Boolean truth(Boolean value) {
        return Boolean.TRUE.equals(value) ? Boolean.TRUE : null;
    }
}
