package com.yuncheng.system.permission.cache;

import com.yuncheng.framework.mybatis.transaction.AfterCommitExecutor;
import com.yuncheng.system.menu.cache.MenuCacheService;
import com.yuncheng.system.user.cache.UserContextCacheService;
import com.yuncheng.system.user.service.UserIdBatchQueryService;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/** 统一清理登录用户需要重新加载的可重建缓存。 */
@Service
public class UserAccessCacheService {

    private final UserContextCacheService userContextCacheService;
    private final PermissionCacheService permissionCacheService;
    private final MenuCacheService menuCacheService;
    private final AfterCommitExecutor afterCommitExecutor;
    private final CacheInvalidationExecutor cacheInvalidationExecutor;
    private final UserIdBatchQueryService userIdBatchQueryService;

    public UserAccessCacheService(
            UserContextCacheService userContextCacheService,
            PermissionCacheService permissionCacheService,
            MenuCacheService menuCacheService,
            AfterCommitExecutor afterCommitExecutor,
            CacheInvalidationExecutor cacheInvalidationExecutor,
            UserIdBatchQueryService userIdBatchQueryService
    ) {
        this.userContextCacheService = userContextCacheService;
        this.permissionCacheService = permissionCacheService;
        this.menuCacheService = menuCacheService;
        this.afterCommitExecutor = afterCommitExecutor;
        this.cacheInvalidationExecutor = cacheInvalidationExecutor;
        this.userIdBatchQueryService = userIdBatchQueryService;
    }

    public void clearAfterLogin(Long userId) {
        clearAllNow(userId);
    }

    public void clearAllAfterCommit(Collection<Long> userIds) {
        List<Long> snapshot = snapshot(userIds);
        submitAfterCommit(() -> clearAllNow(snapshot));
    }

    public void clearAuthorizationForRoleAfterCommit(Long roleId) {
        submitForRoles(Set.of(roleId), Set.of(), this::clearAuthorizationNow);
    }

    public void clearAuthorizationForRolesAfterCommit(
            Collection<Long> roleIds,
            Collection<String> roleCodes
    ) {
        submitForRoles(roleIds, roleCodes, this::clearAuthorizationNow);
    }

    public void clearAllNow(Long userId) {
        userContextCacheService.delete(userId);
        clearAuthorizationNow(userId);
    }

    public void clearAuthorizationNow(Long userId) {
        permissionCacheService.delete(userId);
        menuCacheService.delete(userId);
    }

    private void clearAllNow(Collection<Long> userIds) {
        userContextCacheService.deleteAll(userIds);
        clearAuthorizationNow(userIds);
    }

    private void clearAuthorizationNow(Collection<Long> userIds) {
        permissionCacheService.deleteAll(userIds);
        menuCacheService.deleteAll(userIds);
    }

    private void submitAfterCommit(Runnable task) {
        afterCommitExecutor.execute(() -> cacheInvalidationExecutor.execute(task));
    }

    private void submitForRoles(
            Collection<Long> roleIds,
            Collection<String> roleCodes,
            java.util.function.Consumer<Collection<Long>> invalidator
    ) {
        Set<Long> roleIdSnapshot = roleIds == null ? Set.of() : Set.copyOf(roleIds);
        Set<String> roleCodeSnapshot = roleCodes == null ? Set.of() : Set.copyOf(roleCodes);
        submitAfterCommit(() -> userIdBatchQueryService.forEachRoleBatch(
                roleIdSnapshot,
                roleCodeSnapshot,
                invalidator::accept
        ));
    }

    private List<Long> snapshot(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> distinctIds = new LinkedHashSet<>(userIds);
        distinctIds.remove(null);
        return List.copyOf(distinctIds);
    }
}
