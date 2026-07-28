package com.yuncheng.system.role.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuncheng.common.context.CurrentUserContext;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.system.api.role.SystemUserRoleApi;
import com.yuncheng.system.api.role.SystemUserRoleBinding;
import com.yuncheng.system.permission.cache.UserAccessCacheService;
import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.system.role.dto.RoleUserPageQuery;
import com.yuncheng.system.role.dto.RoleSummary;
import com.yuncheng.system.role.dto.RoleUserListItem;
import com.yuncheng.system.role.entity.SystemRole;
import com.yuncheng.system.user.entity.SystemUser;
import com.yuncheng.system.user.mapper.SystemUserMapper;
import com.yuncheng.system.user.service.UserAccessService;
import com.yuncheng.system.user.service.UserQueryService;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 从角色角度查询和维护关联用户。 */
@Service
public class RoleUserService implements SystemUserRoleApi {

    private final SystemUserMapper userMapper;
    private final RoleQueryService roleQueryService;
    private final RoleAccessService roleAccessService;
    private final UserRoleService userRoleService;
    private final UserQueryService userQueryService;
    private final UserAccessService userAccessService;
    private final UserAccessCacheService cacheService;
    private final CurrentUserContext currentUserContext;

    public RoleUserService(
            SystemUserMapper userMapper,
            RoleQueryService roleQueryService,
            RoleAccessService roleAccessService,
            UserRoleService userRoleService,
            UserQueryService userQueryService,
            UserAccessService userAccessService,
            UserAccessCacheService cacheService,
            CurrentUserContext currentUserContext
    ) {
        this.userMapper = userMapper;
        this.roleQueryService = roleQueryService;
        this.roleAccessService = roleAccessService;
        this.userRoleService = userRoleService;
        this.userQueryService = userQueryService;
        this.userAccessService = userAccessService;
        this.cacheService = cacheService;
        this.currentUserContext = currentUserContext;
    }

    @Override
    public boolean exists(Long userId, Long roleId) {
        return userRoleService.exists(userId, roleId);
    }

    @Override
    @Transactional
    public void bind(Long userId, Long roleId) {
        userRoleService.bind(userId, roleId);
        cacheService.clearAllAfterCommit(List.of(userId));
    }

    @Override
    @Transactional
    public void bindBatch(Collection<SystemUserRoleBinding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return;
        }
        userRoleService.bindBatch(bindings);
        cacheService.clearAllAfterCommit(bindings.stream()
                .map(SystemUserRoleBinding::userId)
                .distinct()
                .toList());
    }

    public PageResult<RoleUserListItem> assignedUsers(Long roleId, RoleUserPageQuery query) {
        SystemRole role = roleQueryService.requireRole(roleId);
        roleAccessService.requireCanManage(role);
        return page(roleId, query, true);
    }

    public PageResult<RoleUserListItem> candidateUsers(Long roleId, RoleUserPageQuery query) {
        SystemRole role = roleQueryService.requireRole(roleId);
        roleAccessService.requireCanManage(role);
        return page(roleId, query, false);
    }

    @Transactional
    public void addUsers(Long roleId, List<Long> userIds) {
        SystemRole role = roleQueryService.requireRole(roleId);
        roleAccessService.requireCanManage(role);
        List<SystemUser> users = userQueryService.requireUsers(userIds);
        requireEnabledUsers(users);
        userAccessService.requireCanManage(users);
        userRoleService.addUsersToRole(roleId, userIds);
        cacheService.clearAllAfterCommit(userIds);
    }

    @Transactional
    public void removeUsers(Long roleId, List<Long> userIds) {
        SystemRole role = roleQueryService.requireRole(roleId);
        roleAccessService.requireCanManage(role);
        List<SystemUser> users = userQueryService.requireUsers(userIds);
        userAccessService.requireCanManage(users);
        userRoleService.requireAtLeastOneRoleAfterRemoval(roleId, userIds);
        protectSuperAdminUsers(role, users);
        userRoleService.removeUsersFromRole(roleId, userIds);
        cacheService.clearAllAfterCommit(userIds);
    }

    private PageResult<RoleUserListItem> page(Long roleId, RoleUserPageQuery query, boolean assigned) {
        String username = normalizedUsername(query.getUsername());
        String realName = normalizedText(query.getRealName());
        IPage<SystemUser> page = userMapper.selectRoleUserPage(
                new Page<>(query.getPage(), query.getPageSize()),
                roleId,
                username,
                realName,
                assigned,
                !assigned && !roleAccessService.isSuperAdmin()
        );
        return PageResult.of(toListItems(page.getRecords()), page.getTotal(), query);
    }

    private List<RoleUserListItem> toListItems(List<SystemUser> users) {
        List<Long> userIds = users.stream().map(SystemUser::getId).toList();
        Map<Long, List<RoleSummary>> roles = userRoleService.summariesByUserIds(userIds);
        return users.stream()
                .map(user -> new RoleUserListItem(
                        user.getId().toString(), user.getUsername(), user.getRealName(),
                        roles.getOrDefault(user.getId(), List.of()), Boolean.TRUE.equals(user.getEnabled())
                ))
                .toList();
    }

    private String normalizedUsername(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizedText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void requireEnabledUsers(List<SystemUser> users) {
        if (users.stream().anyMatch(user -> !Boolean.TRUE.equals(user.getEnabled()))) {
            throw PlatformException.conflict("停用的用户不能新增到角色");
        }
    }

    private void protectSuperAdminUsers(SystemRole role, List<SystemUser> users) {
        if (!SystemRoleCodes.SUPER_ADMIN.equals(role.getRoleCode())) {
            return;
        }
        Set<Long> assignedUserIds = Set.copyOf(userRoleService.userIdsByRoleId(role.getId()));
        List<SystemUser> assignedTargets = users.stream()
                .filter(user -> assignedUserIds.contains(user.getId()))
                .toList();
        if (assignedTargets.stream().anyMatch(user -> user.getId().equals(currentUserContext.getUserId()))) {
            throw PlatformException.conflict("不能移除自己当前使用的超级管理员角色");
        }
        long enabledTargets = assignedTargets.stream().filter(user -> Boolean.TRUE.equals(user.getEnabled())).count();
        long enabledSuperAdmins = userMapper.countEnabledUsersByRoleCode(SystemRoleCodes.SUPER_ADMIN, null);
        if (enabledSuperAdmins - enabledTargets < 1) {
            throw PlatformException.conflict("系统必须至少保留一个启用的超级管理员");
        }
    }
}
