package com.yuncheng.system.menu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.menu.dto.MenuSaveRequest;
import com.yuncheng.system.menu.entity.SystemMenu;
import com.yuncheng.system.menu.enums.MenuType;
import com.yuncheng.system.menu.mapper.SystemMenuMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/** 统一校验并整理菜单字段。 */
@Service
public class MenuValidationService {

    private static final Pattern ROUTE_NAME_PATTERN = Pattern.compile("[A-Z][A-Za-z0-9]*");
    private static final Pattern PERMISSION_PATTERN = Pattern.compile("[a-z0-9-]+:[a-z0-9-]+:[a-z0-9-]+");
    private static final Pattern LOCAL_ICON_PATTERN = Pattern.compile("lucide:[a-z0-9]+(?:-[a-z0-9]+)*");
    private static final Pattern INVALID_LOCAL_PATH_PATTERN = Pattern.compile("[\\\\?#\\p{Cntrl}\\s]");
    private static final Set<String> BADGE_TYPES = Set.of("dot", "normal");
    private static final Set<String> BADGE_VARIANTS = Set.of(
            "default", "destructive", "primary", "success", "warning"
    );

    private final SystemMenuMapper menuMapper;
    private final MenuQueryService menuQueryService;
    private final MenuUniquenessService uniquenessService;
    private final JsonMapper jsonMapper;

    public MenuValidationService(
            SystemMenuMapper menuMapper,
            MenuQueryService menuQueryService,
            MenuUniquenessService uniquenessService,
            JsonMapper jsonMapper
    ) {
        this.menuMapper = menuMapper;
        this.menuQueryService = menuQueryService;
        this.uniquenessService = uniquenessService;
        this.jsonMapper = jsonMapper;
    }

    public SystemMenu prepare(MenuSaveRequest request, SystemMenu existing) {
        Long menuId = existing == null ? null : existing.getId();
        SystemMenu parent = request.parentId() == null ? null : menuQueryService.requireMenu(request.parentId());
        if (menuId != null && menuId.equals(request.parentId())) {
            throw structure("菜单不能选择自己作为上级菜单");
        }
        if (menuId != null && request.parentId() != null
                && menuMapper.selectSubtreeIds(menuId).contains(request.parentId())) {
            throw structure("不能把菜单移动到自己的下级节点中");
        }
        requireParentAllowed(parent, request.menuType());
        requireChildrenAllowed(menuId, request.menuType());

        SystemMenu menu = existing == null ? new SystemMenu() : existing;
        menu.setParentId(request.parentId());
        menu.setMenuType(request.menuType());
        menu.setMenuName(request.menuName().trim());
        menu.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());

        if (request.menuType() == MenuType.BUTTON) {
            prepareButton(menu, request);
        } else {
            prepareRoute(menu, request);
        }
        uniquenessService.requireAvailable(menu, menuId);
        return menu;
    }

    private void prepareButton(SystemMenu menu, MenuSaveRequest request) {
        String permissionCode = lower(required(request.permissionCode(), "按钮权限码不能为空"));
        if (!PERMISSION_PATTERN.matcher(permissionCode).matches()) {
            throw structure("权限码必须使用 domain:resource:action 格式");
        }
        menu.setPermissionCode(permissionCode);
        menu.setRouteName(null);
        menu.setRoutePath(null);
        menu.setComponentPath(null);
        menu.setRedirect(null);
        menu.setIcon(null);
        menu.setActiveIcon(null);
        menu.setActivePath(null);
        menu.setBadge(null);
        menu.setBadgeType(null);
        menu.setBadgeVariants(null);
        menu.setAffixTab(false);
        menu.setAffixTabOrder(null);
        menu.setHideInMenu(false);
        menu.setHideChildrenInMenu(false);
        menu.setHideInBreadcrumb(false);
        menu.setHideInTab(false);
        menu.setKeepAlive(false);
        menu.setFullPathKey(true);
        menu.setOpenInNewWindow(false);
        menu.setNoBasicLayout(false);
        menu.setMaxNumOfOpenTab(null);
        menu.setQueryParams(null);
        menu.setLink(null);
        menu.setIframeSrc(null);
    }

    private void prepareRoute(SystemMenu menu, MenuSaveRequest request) {
        String routeName = required(request.routeName(), "路由名称不能为空");
        if (!ROUTE_NAME_PATTERN.matcher(routeName).matches()) {
            throw structure("路由名称必须使用大驼峰格式");
        }
        String routePath = required(request.routePath(), "路由路径不能为空");
        if (!routePath.startsWith("/")) {
            throw structure("路由路径必须以 / 开头");
        }
        requireSafeLocalPath(routePath, "路由路径");
        menu.setRouteName(routeName);
        menu.setRoutePath(routePath);
        boolean supportsActivePath = request.menuType() == MenuType.EMBEDDED
                || request.menuType() == MenuType.MENU;
        boolean supportsChildVisibility = request.menuType() == MenuType.CATALOG
                || request.menuType() == MenuType.MENU;
        boolean supportsPageOptions = supportsActivePath;
        boolean supportsTabVisibility = request.menuType() != MenuType.LINK;
        menu.setRedirect(supportsChildVisibility
                ? optionalLocalPath(request.redirect(), "重定向路径")
                : null);
        menu.setIcon(localIcon(request.icon(), "图标"));
        menu.setActiveIcon(localIcon(request.activeIcon(), "激活图标"));
        menu.setActivePath(supportsActivePath
                ? optionalLocalPath(request.activePath(), "激活菜单路径")
                : null);
        prepareBadge(menu, request);
        boolean affixTab = supportsActivePath && value(request.affixTab());
        menu.setAffixTab(affixTab);
        menu.setAffixTabOrder(affixTab ? request.affixTabOrder() : null);
        menu.setHideInMenu(value(request.hideInMenu()));
        menu.setHideChildrenInMenu(supportsChildVisibility && value(request.hideChildrenInMenu()));
        menu.setHideInBreadcrumb(supportsTabVisibility && value(request.hideInBreadcrumb()));
        menu.setHideInTab(supportsTabVisibility && value(request.hideInTab()));
        menu.setKeepAlive(request.menuType() == MenuType.MENU && value(request.keepAlive()));
        menu.setFullPathKey(request.fullPathKey() == null || request.fullPathKey());
        menu.setOpenInNewWindow(request.menuType() == MenuType.LINK
                && value(request.openInNewWindow()));
        menu.setNoBasicLayout(supportsPageOptions && value(request.noBasicLayout()));
        menu.setMaxNumOfOpenTab(supportsPageOptions ? request.maxNumOfOpenTab() : null);
        menu.setQueryParams(writeQuery(request.query()));

        switch (request.menuType()) {
            case CATALOG -> {
                menu.setComponentPath(null);
                menu.setPermissionCode(null);
                menu.setLink(null);
                menu.setIframeSrc(null);
            }
            case MENU -> {
                String componentPath = required(request.componentPath(), "页面组件路径不能为空");
                if (!componentPath.startsWith("/") || componentPath.endsWith(".vue")) {
                    throw structure("页面组件路径必须以 / 开头，并且不能包含 .vue 后缀");
                }
                requireSafeLocalPath(componentPath, "页面组件路径");
                String permissionCode = lower(nullable(request.permissionCode()));
                if (permissionCode != null && !PERMISSION_PATTERN.matcher(permissionCode).matches()) {
                    throw structure("权限码必须使用 domain:resource:action 格式");
                }
                menu.setComponentPath(componentPath);
                menu.setPermissionCode(permissionCode);
                menu.setLink(null);
                menu.setIframeSrc(null);
            }
            case EMBEDDED -> {
                menu.setComponentPath(null);
                menu.setPermissionCode(null);
                menu.setLink(null);
                menu.setIframeSrc(requiredHttpUrl(request.iframeSrc(), "内嵌页面地址"));
            }
            case LINK -> {
                menu.setComponentPath(null);
                menu.setPermissionCode(null);
                menu.setLink(requiredHttpUrl(request.link(), "链接地址"));
                menu.setIframeSrc(null);
            }
            case BUTTON -> throw structure("按钮节点字段整理错误");
        }
    }

    private void prepareBadge(SystemMenu menu, MenuSaveRequest request) {
        String badgeType = nullable(request.badgeType());
        if (badgeType != null && !BADGE_TYPES.contains(badgeType)) {
            throw structure("徽标类型不正确");
        }
        String badgeVariants = nullable(request.badgeVariants());
        if (badgeVariants != null && !BADGE_VARIANTS.contains(badgeVariants)) {
            throw structure("徽标样式不正确");
        }
        menu.setBadgeType(badgeType);
        menu.setBadge("normal".equals(badgeType) ? nullable(request.badge()) : null);
        menu.setBadgeVariants(badgeType == null ? null : badgeVariants);
    }

    private void requireParentAllowed(SystemMenu parent, MenuType childType) {
        if (parent == null) {
            if (childType == MenuType.BUTTON) {
                throw structure("按钮不能作为一级节点");
            }
            return;
        }
        if (!allowsChild(parent.getMenuType(), childType)) {
            if (childType == MenuType.BUTTON) {
                throw structure("按钮不能选择按钮作为上级节点");
            }
            throw structure("目录、页面、内嵌和链接只能选择目录或页面作为上级节点");
        }
    }

    private void requireChildrenAllowed(Long menuId, MenuType parentType) {
        if (menuId == null) {
            return;
        }
        List<SystemMenu> children = menuMapper.selectList(new LambdaQueryWrapper<SystemMenu>()
                .eq(SystemMenu::getParentId, menuId));
        for (SystemMenu child : children) {
            if (!allowsChild(parentType, child.getMenuType())) {
                throw structure("修改类型后将无法容纳现有下级节点，请先调整下级菜单");
            }
        }
    }

    private boolean allowsChild(MenuType parentType, MenuType childType) {
        if (childType == MenuType.BUTTON) {
            return parentType != MenuType.BUTTON;
        }
        return parentType == MenuType.CATALOG || parentType == MenuType.MENU;
    }

    private String writeQuery(Map<String, String> query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        try {
            return jsonMapper.writeValueAsString(query);
        } catch (JacksonException exception) {
            throw structure("路由查询参数无法转换为 JSON");
        }
    }

    private PlatformException structure(String message) {
        return PlatformException.badRequest(message);
    }

    private String required(String value, String message) {
        String result = nullable(value);
        if (result == null) {
            throw structure(message);
        }
        return result;
    }

    private String nullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private String localIcon(String value, String fieldName) {
        String icon = nullable(value);
        if (icon != null && !LOCAL_ICON_PATTERN.matcher(icon).matches()) {
            throw structure(fieldName + "必须使用平台允许的 Lucide 本地图标");
        }
        return icon;
    }

    private String requiredHttpUrl(String value, String fieldName) {
        String url = required(value, fieldName + "不能为空");
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && uri.isAbsolute()
                    && uri.getHost() != null) {
                return url;
            }
        } catch (URISyntaxException ignored) {
            // 由下方统一返回面向用户的校验信息。
        }
        throw structure(fieldName + "必须是以 http:// 或 https:// 开头的完整地址");
    }

    private void requireSafeLocalPath(String path, String fieldName) {
        if (INVALID_LOCAL_PATH_PATTERN.matcher(path).find()
                || path.contains("://")
                || path.contains("..")) {
            throw structure(fieldName + "包含不允许的路径内容");
        }
    }

    private String optionalLocalPath(String value, String fieldName) {
        String path = nullable(value);
        if (path != null) {
            if (!path.startsWith("/")) {
                throw structure(fieldName + "必须以 / 开头");
            }
            requireSafeLocalPath(path, fieldName);
        }
        return path;
    }

    private boolean value(Boolean value) {
        return Boolean.TRUE.equals(value);
    }
}
