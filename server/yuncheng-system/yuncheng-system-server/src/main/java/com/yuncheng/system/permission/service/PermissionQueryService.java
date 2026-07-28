package com.yuncheng.system.permission.service;

import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.system.api.permission.SystemPermissionQueryApi;
import com.yuncheng.system.api.role.SystemRoleType;
import com.yuncheng.system.menu.entity.SystemMenu;
import com.yuncheng.system.menu.enums.MenuType;
import com.yuncheng.system.menu.service.DefaultHomeMenuService;
import com.yuncheng.system.menu.service.MenuQueryService;
import com.yuncheng.system.permission.dto.PermissionMenuNode;
import com.yuncheng.system.permission.dto.RolePermissionResponse;
import com.yuncheng.system.role.entity.SystemRole;
import com.yuncheng.system.role.service.RoleAccessService;
import com.yuncheng.system.role.service.RoleQueryService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** 查询授权菜单树和角色当前权限。 */
@Service
public class PermissionQueryService implements SystemPermissionQueryApi {

    private final MenuQueryService menuQueryService;
    private final RoleQueryService roleQueryService;
    private final RoleAccessService roleAccessService;
    private final RoleMenuRelationService relationService;
    private final PermissionGrantPolicyService grantPolicyService;
    private final DefaultHomeMenuService defaultHomeMenuService;

    public PermissionQueryService(
            MenuQueryService menuQueryService,
            RoleQueryService roleQueryService,
            RoleAccessService roleAccessService,
            RoleMenuRelationService relationService,
            PermissionGrantPolicyService grantPolicyService,
            DefaultHomeMenuService defaultHomeMenuService
    ) {
        this.menuQueryService = menuQueryService;
        this.roleQueryService = roleQueryService;
        this.roleAccessService = roleAccessService;
        this.relationService = relationService;
        this.grantPolicyService = grantPolicyService;
        this.defaultHomeMenuService = defaultHomeMenuService;
    }

    public List<PermissionMenuNode> menuTree() {
        List<SystemMenu> menus = menuQueryService.allMenus();
        Set<Long> systemRoleOnlyMenuIds = grantPolicyService.protectedMenuIds(menus);
        Long defaultHomeMenuId = defaultHomeMenuService.requireHomeMenuId(menus);
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
                .map(menu -> node(menu, children, defaultHomeMenuId, systemRoleOnlyMenuIds))
                .toList();
    }

    public RolePermissionResponse rolePermission(Long roleId) {
        SystemRole role = roleQueryService.requireRole(roleId);
        boolean superRole = SystemRoleCodes.SUPER_ADMIN.equals(role.getRoleCode());
        roleAccessService.requireCanManage(role);
        Set<Long> menuIds;
        if (superRole) {
            menuIds = menuQueryService.allMenus().stream()
                    .filter(menu -> menu.getMenuType() != MenuType.CATALOG)
                    .map(SystemMenu::getId)
                    .collect(Collectors.toSet());
        } else {
            menuIds = relationService.menuIdsByRoleId(roleId);
        }
        return new RolePermissionResponse(
                roleId.toString(),
                superRole,
                menuIds.stream().sorted().map(String::valueOf).toList()
        );
    }

    @Override
    public List<Long> findAssignableMenuIds(SystemRoleType roleType) {
        if (roleType == null) {
            throw new IllegalArgumentException("角色类型不能为空");
        }
        List<SystemMenu> menus = menuQueryService.allMenus();
        Set<Long> protectedMenuIds = roleType == SystemRoleType.CUSTOM
                ? grantPolicyService.protectedMenuIds(menus)
                : Set.of();
        return menus.stream()
                .filter(menu -> menu.getMenuType() != MenuType.CATALOG)
                .map(SystemMenu::getId)
                .filter(menuId -> !protectedMenuIds.contains(menuId))
                .toList();
    }

    private PermissionMenuNode node(
            SystemMenu menu,
            Map<Long, List<SystemMenu>> children,
            Long defaultHomeMenuId,
            Set<Long> systemRoleOnlyMenuIds
    ) {
        return new PermissionMenuNode(
                menu.getId().toString(),
                menu.getParentId() == null ? null : menu.getParentId().toString(),
                menu.getMenuName(),
                menu.getMenuType(),
                menu.getPermissionCode(),
                !defaultHomeMenuId.equals(menu.getId()),
                systemRoleOnlyMenuIds.contains(menu.getId()),
                children.getOrDefault(menu.getId(), List.of()).stream()
                        .map(child -> node(child, children, defaultHomeMenuId, systemRoleOnlyMenuIds))
                        .toList()
        );
    }
}
