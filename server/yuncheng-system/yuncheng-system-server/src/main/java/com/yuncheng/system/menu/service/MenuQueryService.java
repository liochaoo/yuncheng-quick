package com.yuncheng.system.menu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.menu.dto.MenuItem;
import com.yuncheng.system.menu.dto.MenuDetail;
import com.yuncheng.system.menu.entity.SystemMenu;
import com.yuncheng.system.menu.mapper.SystemMenuMapper;
import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.system.role.service.UserRoleService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/** 查询菜单管理树和用户有效菜单。 */
@Service
public class MenuQueryService {

    private static final TypeReference<Map<String, String>> QUERY_TYPE = new TypeReference<>() { };

    private final SystemMenuMapper menuMapper;
    private final UserRoleService userRoleService;
    private final JsonMapper jsonMapper;

    public MenuQueryService(
            SystemMenuMapper menuMapper,
            UserRoleService userRoleService,
            JsonMapper jsonMapper
    ) {
        this.menuMapper = menuMapper;
        this.userRoleService = userRoleService;
        this.jsonMapper = jsonMapper;
    }

    public List<MenuItem> tree() {
        List<SystemMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SystemMenu>()
                .orderByAsc(SystemMenu::getParentId, SystemMenu::getSortOrder, SystemMenu::getId));
        return buildItems(menus, null);
    }

    public MenuDetail detail(Long menuId) {
        SystemMenu menu = requireMenu(menuId);
        String parentName = menu.getParentId() == null
                ? null
                : requireMenu(menu.getParentId()).getMenuName();
        return new MenuDetail(toItem(menu, List.of()), parentName);
    }

    public SystemMenu requireMenu(Long menuId) {
        SystemMenu menu = menuId == null ? null : menuMapper.selectById(menuId);
        if (menu == null) {
            throw PlatformException.notFound("菜单不存在");
        }
        return menu;
    }

    public List<SystemMenu> allMenus() {
        return menuMapper.selectList(new LambdaQueryWrapper<SystemMenu>()
                .orderByAsc(SystemMenu::getParentId, SystemMenu::getSortOrder, SystemMenu::getId));
    }

    public List<SystemMenu> effectiveRoutes(Long userId) {
        List<SystemMenu> authorizedMenus;
        if (userRoleService.roleCodes(userId).contains(SystemRoleCodes.SUPER_ADMIN)) {
            authorizedMenus = menuMapper.selectAllEffectiveRoutes();
        } else {
            authorizedMenus = menuMapper.selectEffectiveRoutesByUserId(userId);
        }
        return authorizedMenus;
    }

    public Map<String, String> parseQuery(String queryParams) {
        if (queryParams == null || queryParams.isBlank()) {
            return null;
        }
        try {
            return jsonMapper.readValue(queryParams, QUERY_TYPE);
        } catch (JacksonException exception) {
            throw new IllegalStateException("菜单路由查询参数不是有效的 JSON", exception);
        }
    }

    private List<MenuItem> buildItems(List<SystemMenu> menus, Long parentId) {
        Map<Long, List<SystemMenu>> children = new HashMap<>();
        List<SystemMenu> roots = new ArrayList<>();
        for (SystemMenu menu : menus) {
            if (menu.getParentId() == null) {
                roots.add(menu);
            } else {
                children.computeIfAbsent(menu.getParentId(), ignored -> new ArrayList<>()).add(menu);
            }
        }
        List<SystemMenu> start = parentId == null ? roots : children.getOrDefault(parentId, List.of());
        return start.stream().map(menu -> toItem(menu, buildChildren(menu, children))).toList();
    }

    private List<MenuItem> buildChildren(SystemMenu menu, Map<Long, List<SystemMenu>> children) {
        return children.getOrDefault(menu.getId(), List.of()).stream()
                .map(child -> toItem(child, buildChildren(child, children)))
                .toList();
    }

    private MenuItem toItem(SystemMenu menu, List<MenuItem> children) {
        return new MenuItem(
                id(menu.getId()), id(menu.getParentId()), menu.getMenuType(), menu.getMenuName(),
                menu.getRouteName(), menu.getRoutePath(), menu.getComponentPath(), menu.getRedirect(),
                menu.getPermissionCode(), menu.getSortOrder(), menu.getIcon(),
                menu.getActiveIcon(), menu.getActivePath(), menu.getBadge(), menu.getBadgeType(),
                menu.getBadgeVariants(), value(menu.getAffixTab()), menu.getAffixTabOrder(),
                value(menu.getHideInMenu()), value(menu.getHideChildrenInMenu()),
                value(menu.getHideInBreadcrumb()), value(menu.getHideInTab()), value(menu.getKeepAlive()),
                value(menu.getFullPathKey()), value(menu.getOpenInNewWindow()), value(menu.getNoBasicLayout()),
                menu.getMaxNumOfOpenTab(), parseQuery(menu.getQueryParams()), menu.getLink(), menu.getIframeSrc(),
                menu.getCreatedAt(), id(menu.getCreatedBy()), menu.getUpdatedAt(), id(menu.getUpdatedBy()), children
        );
    }

    private boolean value(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private String id(Long value) {
        return value == null ? null : value.toString();
    }
}
