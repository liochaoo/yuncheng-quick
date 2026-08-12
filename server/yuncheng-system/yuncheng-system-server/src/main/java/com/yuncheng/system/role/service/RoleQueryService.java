package com.yuncheng.system.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.system.api.role.SystemRoleInfo;
import com.yuncheng.system.api.role.SystemRoleQueryApi;
import com.yuncheng.system.api.role.SystemRoleType;
import com.yuncheng.system.role.dto.RoleDetail;
import com.yuncheng.system.role.dto.RoleListItem;
import com.yuncheng.system.role.dto.RoleOption;
import com.yuncheng.system.role.dto.RoleOptionPageQuery;
import com.yuncheng.system.role.dto.RolePageQuery;
import com.yuncheng.system.role.entity.SystemRole;
import com.yuncheng.system.role.enums.RoleType;
import com.yuncheng.system.role.mapper.RoleUserCountRow;
import com.yuncheng.system.role.mapper.SystemRoleMapper;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 查询角色及角色选择项。 */
@Service
public class RoleQueryService implements SystemRoleQueryApi {

    private final SystemRoleMapper roleMapper;
    private final RoleAccessService roleAccessService;

    public RoleQueryService(SystemRoleMapper roleMapper, RoleAccessService roleAccessService) {
        this.roleMapper = roleMapper;
        this.roleAccessService = roleAccessService;
    }

    @Override
    public Optional<SystemRoleInfo> findByCode(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            return Optional.empty();
        }
        String normalized = roleCode.trim().toLowerCase(Locale.ROOT);
        SystemRole role = roleMapper.selectOne(new LambdaQueryWrapper<SystemRole>()
                .eq(SystemRole::getRoleCode, normalized));
        return Optional.ofNullable(role).map(this::toSystemRoleInfo);
    }

    public PageResult<RoleListItem> page(RolePageQuery query) {
        String roleCode = normalizedCode(query.getRoleCode());
        String roleName = normalizedText(query.getRoleName());
        LambdaQueryWrapper<SystemRole> wrapper = new LambdaQueryWrapper<SystemRole>()
                .like(StringUtils.hasText(roleCode), SystemRole::getRoleCode, roleCode)
                .like(StringUtils.hasText(roleName), SystemRole::getRoleName, roleName)
                .eq(query.getRoleType() != null, SystemRole::getRoleType, query.getRoleType())
                .orderByAsc(SystemRole::getSortOrder, SystemRole::getId);
        IPage<SystemRole> page = roleMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()),
                wrapper
        );
        Map<Long, Long> userCounts = userCounts(page.getRecords().stream().map(SystemRole::getId).toList());
        List<RoleListItem> items = page.getRecords().stream()
                .map(role -> toListItem(role, userCounts.getOrDefault(role.getId(), 0L)))
                .toList();
        return PageResult.of(items, page.getTotal(), query);
    }

    public RoleDetail detail(Long roleId) {
        SystemRole role = requireRole(roleId);
        long userCount = userCounts(List.of(roleId)).getOrDefault(roleId, 0L);
        return new RoleDetail(
                id(role.getId()), role.getRoleCode(), role.getRoleName(), role.getRoleType(),
                role.getSortOrder(), userCount,
                role.getCreatedAt(), id(role.getCreatedBy()), role.getUpdatedAt(), id(role.getUpdatedBy())
        );
    }

    public PageResult<RoleOption> pageOptions(RoleOptionPageQuery query) {
        String keyword = normalizedText(query.getKeyword());
        String roleCode = normalizedCode(query.getRoleCode());
        String roleName = normalizedText(query.getRoleName());
        LambdaQueryWrapper<SystemRole> wrapper = new LambdaQueryWrapper<SystemRole>()
                .eq(!roleAccessService.isSuperAdmin(), SystemRole::getRoleType, RoleType.CUSTOM)
                .and(StringUtils.hasText(keyword), condition -> condition
                        .like(SystemRole::getRoleCode, keyword.toLowerCase(Locale.ROOT))
                        .or()
                        .like(SystemRole::getRoleName, keyword))
                .like(StringUtils.hasText(roleCode), SystemRole::getRoleCode, roleCode)
                .like(StringUtils.hasText(roleName), SystemRole::getRoleName, roleName)
                .orderByAsc(SystemRole::getSortOrder, SystemRole::getId);
        IPage<SystemRole> page = roleMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()),
                wrapper
        );
        List<RoleOption> items = page.getRecords().stream()
                .map(role -> toOption(role, false))
                .toList();
        return PageResult.of(items, page.getTotal(), query);
    }

    public List<RoleOption> optionsByIds(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        boolean superAdmin = roleAccessService.isSuperAdmin();
        return requireRoles(roleIds).values().stream()
                .sorted((left, right) -> {
                    int sort = Integer.compare(left.getSortOrder(), right.getSortOrder());
                    return sort != 0 ? sort : Long.compare(left.getId(), right.getId());
                })
                .map(role -> toOption(role, !superAdmin && role.getRoleType() == RoleType.SYSTEM))
                .toList();
    }

    public SystemRole requireRole(Long roleId) {
        SystemRole role = roleId == null ? null : roleMapper.selectById(roleId);
        if (role == null) {
            throw PlatformException.notFound("角色不存在");
        }
        return role;
    }

    public Map<Long, SystemRole> requireRoles(Collection<Long> roleIds) {
        Set<Long> distinctIds = roleIds == null ? Set.of() : Set.copyOf(roleIds);
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, SystemRole> roles = roleMapper.selectByIds(distinctIds).stream()
                .collect(Collectors.toMap(SystemRole::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        if (roles.size() != distinctIds.size()) {
            throw PlatformException.notFound("选择的角色中存在已被删除的数据");
        }
        return roles;
    }

    public List<SystemRole> assignableRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<SystemRole>()
                .eq(!roleAccessService.isSuperAdmin(), SystemRole::getRoleType, RoleType.CUSTOM)
                .orderByAsc(SystemRole::getSortOrder, SystemRole::getId));
    }

    public Map<String, SystemRole> rolesByCodes(Collection<String> roleCodes) {
        Set<String> normalizedCodes = roleCodes == null
                ? Set.of()
                : roleCodes.stream()
                        .map(this::normalizedCode)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        if (normalizedCodes.isEmpty()) {
            return Map.of();
        }
        return roleMapper.selectList(new LambdaQueryWrapper<SystemRole>()
                        .in(SystemRole::getRoleCode, normalizedCodes)
                        .eq(!roleAccessService.isSuperAdmin(), SystemRole::getRoleType, RoleType.CUSTOM))
                .stream()
                .collect(Collectors.toMap(
                        SystemRole::getRoleCode,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Map<Long, Long> userCounts(List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return Map.of();
        }
        return roleMapper.selectUserCounts(roleIds).stream()
                .collect(Collectors.toMap(RoleUserCountRow::roleId, RoleUserCountRow::userCount));
    }

    private RoleListItem toListItem(SystemRole role, long userCount) {
        return new RoleListItem(
                id(role.getId()), role.getRoleCode(), role.getRoleName(), role.getRoleType(),
                role.getSortOrder(), userCount, role.getCreatedAt(), role.getUpdatedAt()
        );
    }

    private SystemRoleInfo toSystemRoleInfo(SystemRole role) {
        return new SystemRoleInfo(
                role.getId(), role.getRoleCode(), role.getRoleName(),
                SystemRoleType.valueOf(role.getRoleType().name())
        );
    }

    private RoleOption toOption(SystemRole role, boolean disabled) {
        return new RoleOption(
                id(role.getId()), role.getRoleCode(), role.getRoleName(), role.getRoleType(), disabled
        );
    }

    private String normalizedCode(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : null;
    }

    private String normalizedText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String id(Long value) {
        return value == null ? null : value.toString();
    }
}
