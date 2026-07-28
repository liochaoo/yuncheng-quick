package com.yuncheng.system.permission.service;

import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.menu.constant.MenuPermissionCodes;
import com.yuncheng.system.menu.entity.SystemMenu;
import com.yuncheng.system.permission.constant.AuthorizationPermissionCodes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/** 定义只允许系统角色持有的敏感菜单权限范围。 */
@Service
public class PermissionGrantPolicyService {

    private static final Set<String> PROTECTED_ROOT_CODES = Set.of(
            AuthorizationPermissionCodes.QUERY,
            MenuPermissionCodes.QUERY
    );

    public Set<Long> protectedMenuIds(Collection<SystemMenu> menus) {
        Map<Long, List<Long>> children = new HashMap<>();
        ArrayDeque<Long> pending = new ArrayDeque<>();
        for (SystemMenu menu : menus) {
            if (menu.getParentId() != null) {
                children.computeIfAbsent(menu.getParentId(), ignored -> new ArrayList<>()).add(menu.getId());
            }
            String permissionCode = menu.getPermissionCode();
            if (permissionCode != null && PROTECTED_ROOT_CODES.contains(permissionCode)) {
                pending.add(menu.getId());
            }
        }
        Set<Long> protectedIds = new HashSet<>();
        while (!pending.isEmpty()) {
            Long menuId = pending.removeFirst();
            if (!protectedIds.add(menuId)) {
                continue;
            }
            pending.addAll(children.getOrDefault(menuId, List.of()));
        }
        return Set.copyOf(protectedIds);
    }

    public void requireAssignableToCustomRole(
            Collection<Long> requestedMenuIds,
            Collection<Long> protectedMenuIds
    ) {
        Set<Long> requestedProtected = intersection(requestedMenuIds, protectedMenuIds);
        if (!requestedProtected.isEmpty()) {
            throw PlatformException.forbidden("权限管理和菜单管理权限只能授予系统角色");
        }
    }

    private Set<Long> intersection(Collection<Long> values, Collection<Long> retainedValues) {
        Set<Long> retained = new HashSet<>(retainedValues);
        Set<Long> result = new HashSet<>(values);
        result.retainAll(retained);
        return result;
    }
}
