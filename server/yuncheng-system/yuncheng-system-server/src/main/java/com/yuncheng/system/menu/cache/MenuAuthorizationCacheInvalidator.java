package com.yuncheng.system.menu.cache;

import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.system.menu.mapper.SystemMenuMapper;
import com.yuncheng.system.permission.cache.UserAccessCacheService;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/** 根据菜单变化的实际影响范围清理用户菜单和权限码缓存。 */
@Service
public class MenuAuthorizationCacheInvalidator {

    private final SystemMenuMapper menuMapper;
    private final UserAccessCacheService cacheService;

    public MenuAuthorizationCacheInvalidator(
            SystemMenuMapper menuMapper,
            UserAccessCacheService cacheService
    ) {
        this.menuMapper = menuMapper;
        this.cacheService = cacheService;
    }

    public void clearAfterCommit(Collection<Long> changedMenuIds) {
        List<Long> affectedRoleIds = changedMenuIds == null || changedMenuIds.isEmpty()
                ? List.of()
                : menuMapper.selectRelatedRoleIds(List.copyOf(changedMenuIds));
        // 超级管理员没有菜单关联关系，但其菜单和权限码来自全部有效菜单。
        cacheService.clearAuthorizationForRolesAfterCommit(
                affectedRoleIds,
                Set.of(SystemRoleCodes.SUPER_ADMIN)
        );
    }
}
