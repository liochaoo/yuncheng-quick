package com.yuncheng.system.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.common.constant.BuiltInUserIds;
import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.common.context.CurrentUserContext;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.api.role.SystemUserRoleBinding;
import com.yuncheng.system.role.dto.RoleSummary;
import com.yuncheng.system.role.entity.SystemRole;
import com.yuncheng.system.role.entity.SystemUserRole;
import com.yuncheng.system.role.enums.RoleType;
import com.yuncheng.system.role.mapper.RoleSummaryRow;
import com.yuncheng.system.role.mapper.SystemUserRoleMapper;
import com.yuncheng.system.user.mapper.SystemUserMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/** 维护用户和角色之间的关系。 */
@Service
public class UserRoleService {

    private static final int BATCH_SIZE = 500;

    private final SystemUserRoleMapper userRoleMapper;
    private final SystemUserMapper userMapper;
    private final RoleQueryService roleQueryService;
    private final RoleAccessService roleAccessService;
    private final CurrentUserContext currentUserContext;

    public UserRoleService(
            SystemUserRoleMapper userRoleMapper,
            SystemUserMapper userMapper,
            RoleQueryService roleQueryService,
            RoleAccessService roleAccessService,
            CurrentUserContext currentUserContext
    ) {
        this.userRoleMapper = userRoleMapper;
        this.userMapper = userMapper;
        this.roleQueryService = roleQueryService;
        this.roleAccessService = roleAccessService;
        this.currentUserContext = currentUserContext;
    }

    public boolean exists(Long userId, Long roleId) {
        requireRelationIds(userId, roleId);
        return userRoleMapper.selectCount(new LambdaQueryWrapper<SystemUserRole>()
                .eq(SystemUserRole::getUserId, userId)
                .eq(SystemUserRole::getRoleId, roleId)) > 0;
    }

    public void bind(Long userId, Long roleId) {
        requireRelationIds(userId, roleId);
        userRoleMapper.insert(relation(userId, roleId));
    }

    public void bindBatch(Collection<SystemUserRoleBinding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return;
        }
        List<SystemUserRole> relations = new LinkedHashSet<>(bindings).stream()
                .map(binding -> {
                    if (binding == null) {
                        throw new IllegalArgumentException("用户角色绑定关系不能为空");
                    }
                    requireRelationIds(binding.userId(), binding.roleId());
                    return relation(binding.userId(), binding.roleId());
                })
                .toList();
        insertBatch(relations);
    }

    public Map<Long, List<RoleSummary>> summariesByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<RoleSummary>> result = new HashMap<>();
        for (RoleSummaryRow row : userRoleMapper.selectRoleSummariesByUserIds(userIds)) {
            result.computeIfAbsent(row.userId(), ignored -> new ArrayList<>())
                    .add(new RoleSummary(
                            row.roleId().toString(), row.roleCode(), row.roleName(),
                            row.roleType()
                    ));
        }
        return result;
    }

    public List<String> roleCodes(Long userId) {
        return userRoleMapper.selectRoleCodes(userId);
    }

    public Set<Long> roleIdsByUserId(Long userId) {
        return userRoleMapper.selectList(new LambdaQueryWrapper<SystemUserRole>()
                        .eq(SystemUserRole::getUserId, userId))
                .stream()
                .map(SystemUserRole::getRoleId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    public List<Long> userIdsByRoleId(Long roleId) {
        return userRoleMapper.selectUserIdsByRoleId(roleId);
    }

    public Set<Long> systemRoleUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(userRoleMapper.selectSystemRoleUserIds(userIds));
    }

    public boolean hasSystemRole(Long userId) {
        return userRoleMapper.countSystemRolesByUserId(userId) > 0;
    }

    public void requireAtLeastOneRoleAfterRemoval(Long roleId, Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        if (!userRoleMapper.selectUserIdsHavingOnlyRole(roleId, userIds).isEmpty()) {
            throw PlatformException.conflict("用户必须至少保留一个角色");
        }
    }

    public long countByRoleId(Long roleId) {
        return userRoleMapper.selectCount(new LambdaQueryWrapper<SystemUserRole>()
                .eq(SystemUserRole::getRoleId, roleId));
    }

    public void replaceUserRoles(Long userId, Collection<Long> requestedRoleIds) {
        Set<Long> targetIds = requestedRoleIds == null
                ? Set.of()
                : new LinkedHashSet<>(requestedRoleIds);
        if (targetIds.isEmpty()) {
            throw PlatformException.badRequest("至少需要选择一个角色");
        }
        if (targetIds.contains(null)) {
            throw PlatformException.badRequest("角色主键不能为空");
        }
        Set<Long> existingIds = roleIdsByUserId(userId);
        Map<Long, SystemRole> targetRoles = roleQueryService.requireRoles(targetIds);
        Set<Long> addedIds = new HashSet<>(targetIds);
        addedIds.removeAll(existingIds);
        for (Long addedId : addedIds) {
            SystemRole role = targetRoles.get(addedId);
            if (role.getRoleType() == RoleType.SYSTEM && !roleAccessService.isSuperAdmin()) {
                throw PlatformException.forbidden("系统内置角色只能由超级管理员分配");
            }
        }
        protectSuperAdminRemoval(userId, existingIds, targetIds);
        Set<Long> removedIds = new HashSet<>(existingIds);
        removedIds.removeAll(targetIds);
        if (!removedIds.isEmpty()) {
            userRoleMapper.delete(new LambdaQueryWrapper<SystemUserRole>()
                    .eq(SystemUserRole::getUserId, userId)
                    .in(SystemUserRole::getRoleId, removedIds));
        }
        insertBatch(relations(userId, addedIds));
    }

    public void bindUserRolesBatch(Map<Long, ? extends Collection<Long>> userRoleIds) {
        if (userRoleIds == null || userRoleIds.isEmpty()) {
            return;
        }
        Set<Long> allRoleIds = userRoleIds.values().stream()
                .flatMap(Collection::stream)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (allRoleIds.isEmpty()) {
            throw PlatformException.badRequest("至少需要选择一个角色");
        }
        Map<Long, SystemRole> roles = roleQueryService.requireRoles(allRoleIds);
        if (!roleAccessService.isSuperAdmin()
                && roles.values().stream().anyMatch(role -> role.getRoleType() == RoleType.SYSTEM)) {
            throw PlatformException.forbidden("系统内置角色只能由超级管理员分配");
        }
        List<SystemUserRole> relations = new ArrayList<>();
        userRoleIds.forEach((userId, roleIds) -> {
            Set<Long> distinctRoleIds = new LinkedHashSet<>(roleIds);
            if (distinctRoleIds.isEmpty() || distinctRoleIds.contains(null)) {
                throw PlatformException.badRequest("至少需要选择一个角色");
            }
            distinctRoleIds.forEach(roleId -> relations.add(relation(userId, roleId)));
        });
        insertBatch(relations);
    }

    public void addUsersToRole(Long roleId, Collection<Long> userIds) {
        Set<Long> existingUserIds = new HashSet<>(userRoleMapper.selectList(
                        new LambdaQueryWrapper<SystemUserRole>()
                                .eq(SystemUserRole::getRoleId, roleId)
                                .in(SystemUserRole::getUserId, userIds))
                .stream().map(SystemUserRole::getUserId).toList());
        List<SystemUserRole> relations = new LinkedHashSet<>(userIds).stream()
                .filter(userId -> !existingUserIds.contains(userId))
                .map(userId -> relation(userId, roleId))
                .toList();
        insertBatch(relations);
    }

    public void removeUsersFromRole(Long roleId, Collection<Long> userIds) {
        protectBuiltInAdministratorRoleRemoval(roleId, userIds);
        userRoleMapper.delete(new LambdaQueryWrapper<SystemUserRole>()
                .eq(SystemUserRole::getRoleId, roleId)
                .in(SystemUserRole::getUserId, userIds));
    }

    public void deleteByUserId(Long userId) {
        if (BuiltInUserIds.ADMINISTRATOR == userId) {
            throw PlatformException.conflict("初始管理员账号不能删除");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SystemUserRole>()
                .eq(SystemUserRole::getUserId, userId));
    }

    private void protectSuperAdminRemoval(Long userId, Set<Long> existingIds, Set<Long> targetIds) {
        SystemRole superRole = roleQueryService.requireRoles(existingIds).values().stream()
                .filter(role -> SystemRoleCodes.SUPER_ADMIN.equals(role.getRoleCode()))
                .findFirst()
                .orElse(null);
        if (superRole == null || targetIds.contains(superRole.getId())) {
            return;
        }
        if (BuiltInUserIds.ADMINISTRATOR == userId) {
            throw PlatformException.conflict("初始管理员必须保留超级管理员角色");
        }
        if (currentUserContext.getUserId().equals(userId)) {
            throw PlatformException.conflict("不能移除自己当前使用的超级管理员角色");
        }
        if (userMapper.countEnabledUsersByRoleCode(SystemRoleCodes.SUPER_ADMIN, userId) == 0) {
            throw PlatformException.conflict("系统必须至少保留一个启用的超级管理员");
        }
    }

    private void protectBuiltInAdministratorRoleRemoval(Long roleId, Collection<Long> userIds) {
        if (userIds == null || !userIds.contains(BuiltInUserIds.ADMINISTRATOR)) {
            return;
        }
        SystemRole role = roleQueryService.requireRole(roleId);
        if (SystemRoleCodes.SUPER_ADMIN.equals(role.getRoleCode())) {
            throw PlatformException.conflict("初始管理员必须保留超级管理员角色");
        }
    }

    private List<SystemUserRole> relations(Long userId, Collection<Long> roleIds) {
        return roleIds.stream().map(roleId -> relation(userId, roleId)).toList();
    }

    private void insertBatch(List<SystemUserRole> relations) {
        if (!relations.isEmpty()) {
            userRoleMapper.insert(relations, BATCH_SIZE);
        }
    }

    private SystemUserRole relation(Long userId, Long roleId) {
        SystemUserRole relation = new SystemUserRole();
        relation.setUserId(userId);
        relation.setRoleId(roleId);
        return relation;
    }

    private void requireRelationIds(Long userId, Long roleId) {
        if (userId == null || roleId == null) {
            throw new IllegalArgumentException("用户主键和角色主键不能为空");
        }
        if (userId <= 0 || roleId <= 0) {
            throw new IllegalArgumentException("用户主键和角色主键必须大于 0");
        }
    }
}
