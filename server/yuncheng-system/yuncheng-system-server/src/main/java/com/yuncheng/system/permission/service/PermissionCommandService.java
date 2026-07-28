package com.yuncheng.system.permission.service;

import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.api.permission.SystemRolePermissionCommand;
import com.yuncheng.system.api.permission.SystemRolePermissionCommandApi;
import com.yuncheng.system.menu.entity.SystemMenu;
import com.yuncheng.system.menu.enums.MenuType;
import com.yuncheng.system.menu.mapper.SystemMenuMapper;
import com.yuncheng.system.menu.service.DefaultHomeMenuService;
import com.yuncheng.system.menu.service.MenuQueryService;
import com.yuncheng.system.permission.cache.UserAccessCacheService;
import com.yuncheng.system.role.entity.SystemRole;
import com.yuncheng.system.role.enums.RoleType;
import com.yuncheng.system.role.service.RoleAccessService;
import com.yuncheng.system.role.service.RoleQueryService;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 整体保存角色可以访问的菜单权限。 */
@Service
public class PermissionCommandService implements SystemRolePermissionCommandApi {

    private final SystemMenuMapper menuMapper;
    private final RoleQueryService roleQueryService;
    private final MenuQueryService menuQueryService;
    private final RoleAccessService roleAccessService;
    private final RoleMenuRelationService relationService;
    private final UserAccessCacheService cacheService;
    private final PermissionGrantPolicyService grantPolicyService;
    private final DefaultHomeMenuService defaultHomeMenuService;

    public PermissionCommandService(
            SystemMenuMapper menuMapper,
            RoleQueryService roleQueryService,
            MenuQueryService menuQueryService,
            RoleAccessService roleAccessService,
            RoleMenuRelationService relationService,
            UserAccessCacheService cacheService,
            PermissionGrantPolicyService grantPolicyService,
            DefaultHomeMenuService defaultHomeMenuService
    ) {
        this.menuMapper = menuMapper;
        this.roleQueryService = roleQueryService;
        this.menuQueryService = menuQueryService;
        this.roleAccessService = roleAccessService;
        this.relationService = relationService;
        this.cacheService = cacheService;
        this.grantPolicyService = grantPolicyService;
        this.defaultHomeMenuService = defaultHomeMenuService;
    }

    @Transactional
    public void save(Long roleId, Collection<Long> requestedMenuIds) {
        SystemRole role = roleQueryService.requireRole(roleId);
        roleAccessService.requireCanManage(role);
        if (SystemRoleCodes.SUPER_ADMIN.equals(role.getRoleCode())) {
            throw PlatformException.conflict("超级管理员自动拥有全部有效权限，不能单独授权");
        }
        Set<Long> requested = new LinkedHashSet<>(requestedMenuIds);
        if (requested.contains(null)) {
            throw PlatformException.badRequest("菜单主键不能为空");
        }
        List<SystemMenu> allMenus = menuQueryService.allMenus();
        requested.add(defaultHomeMenuService.requireHomeMenuId(allMenus));
        Map<Long, SystemMenu> requestedMenus = requireMenus(requested);
        Set<Long> grantMenuIds = requested.stream()
                .filter(menuId -> requestedMenus.get(menuId).getMenuType() != MenuType.CATALOG)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (role.getRoleType() == RoleType.CUSTOM) {
            grantPolicyService.requireAssignableToCustomRole(
                    grantMenuIds,
                    grantPolicyService.protectedMenuIds(allMenus)
            );
        }
        relationService.replace(roleId, grantMenuIds);
        cacheService.clearAuthorizationForRoleAfterCommit(roleId);
    }

    @Override
    @Transactional
    public void replaceBatch(Collection<SystemRolePermissionCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return;
        }
        Map<Long, SystemRolePermissionCommand> commandsByRoleId = new LinkedHashMap<>();
        for (SystemRolePermissionCommand command : commands) {
            if (command == null || command.roleId() == null) {
                throw new IllegalArgumentException("角色权限参数及角色主键不能为空");
            }
            if (commandsByRoleId.putIfAbsent(command.roleId(), command) != null) {
                throw PlatformException.badRequest("批量保存的角色主键存在重复");
            }
        }
        Map<Long, SystemRole> roles = roleQueryService.requireRoles(commandsByRoleId.keySet());
        List<SystemMenu> allMenus = menuQueryService.allMenus();
        Map<Long, SystemMenu> allMenusById = allMenus.stream()
                .collect(Collectors.toMap(SystemMenu::getId, Function.identity()));
        Long homeMenuId = defaultHomeMenuService.requireHomeMenuId(allMenus);
        Set<Long> protectedMenuIds = grantPolicyService.protectedMenuIds(allMenus);
        Map<Long, Set<Long>> targetsByRoleId = new LinkedHashMap<>();
        commandsByRoleId.forEach((roleId, command) -> {
            SystemRole role = roles.get(roleId);
            roleAccessService.requireCanManage(role);
            if (SystemRoleCodes.SUPER_ADMIN.equals(role.getRoleCode())) {
                throw PlatformException.conflict("超级管理员自动拥有全部有效权限，不能单独授权");
            }
            Set<Long> requested = command.menuIds() == null
                    ? new LinkedHashSet<>()
                    : new LinkedHashSet<>(command.menuIds());
            if (requested.contains(null)) {
                throw PlatformException.badRequest("菜单主键不能为空");
            }
            requested.add(homeMenuId);
            if (!allMenusById.keySet().containsAll(requested)) {
                throw PlatformException.notFound("选择的菜单权限中存在已被删除的数据");
            }
            Set<Long> grantMenuIds = requested.stream()
                    .filter(menuId -> allMenusById.get(menuId).getMenuType() != MenuType.CATALOG)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (role.getRoleType() == RoleType.CUSTOM) {
                grantPolicyService.requireAssignableToCustomRole(grantMenuIds, protectedMenuIds);
            }
            targetsByRoleId.put(roleId, grantMenuIds);
        });
        relationService.replaceBatch(targetsByRoleId);
        cacheService.clearAuthorizationForRolesAfterCommit(targetsByRoleId.keySet(), Set.of());
    }

    private Map<Long, SystemMenu> requireMenus(Collection<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, SystemMenu> menus = menuMapper.selectByIds(menuIds).stream()
                .collect(Collectors.toMap(SystemMenu::getId, Function.identity()));
        if (menus.size() != menuIds.size()) {
            throw PlatformException.notFound("选择的菜单权限中存在已被删除的数据");
        }
        return menus;
    }
}
