package com.yuncheng.system.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.api.role.SystemRoleCommandApi;
import com.yuncheng.system.api.role.SystemRoleCreateCommand;
import com.yuncheng.system.menu.service.DefaultHomeMenuService;
import com.yuncheng.system.permission.service.RoleMenuRelationService;
import com.yuncheng.system.role.dto.RoleCreateRequest;
import com.yuncheng.system.role.dto.RoleUpdateRequest;
import com.yuncheng.system.role.entity.SystemRole;
import com.yuncheng.system.role.enums.RoleType;
import com.yuncheng.system.role.enums.RoleUniqueField;
import com.yuncheng.system.role.mapper.SystemRoleMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 新增、编辑和删除角色。 */
@Service
public class RoleCommandService implements SystemRoleCommandApi {

    private static final Pattern ROLE_CODE_PATTERN = Pattern.compile("[a-z][a-z0-9_-]{0,49}");
    private static final int BATCH_SIZE = 500;

    private final SystemRoleMapper roleMapper;
    private final RoleQueryService roleQueryService;
    private final RoleAccessService roleAccessService;
    private final RoleUniquenessService uniquenessService;
    private final UserRoleService userRoleService;
    private final RoleMenuRelationService roleMenuRelationService;
    private final DefaultHomeMenuService defaultHomeMenuService;

    public RoleCommandService(
            SystemRoleMapper roleMapper,
            RoleQueryService roleQueryService,
            RoleAccessService roleAccessService,
            RoleUniquenessService uniquenessService,
            UserRoleService userRoleService,
            RoleMenuRelationService roleMenuRelationService,
            DefaultHomeMenuService defaultHomeMenuService
    ) {
        this.roleMapper = roleMapper;
        this.roleQueryService = roleQueryService;
        this.roleAccessService = roleAccessService;
        this.uniquenessService = uniquenessService;
        this.userRoleService = userRoleService;
        this.roleMenuRelationService = roleMenuRelationService;
        this.defaultHomeMenuService = defaultHomeMenuService;
    }

    @Transactional
    public Long create(RoleCreateRequest request) {
        String roleCode = normalizeRoleCode(request.roleCode());
        String roleName = normalizeRoleName(request.roleName());
        roleAccessService.requireCanCreate(request.roleType());
        uniquenessService.requireAvailable(RoleUniqueField.ROLE_CODE, roleCode, null);
        uniquenessService.requireAvailable(RoleUniqueField.ROLE_NAME, roleName, null);
        SystemRole role = new SystemRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setRoleType(request.roleType());
        role.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        roleMapper.insert(role);
        grantDefaultHome(role);
        return role.getId();
    }

    @Override
    @Transactional
    public Long create(SystemRoleCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("创建系统角色参数不能为空");
        }
        if (command.roleType() == null) {
            throw new IllegalArgumentException("系统角色类型不能为空");
        }
        String roleCode = normalizeRoleCode(command.roleCode());
        String roleName = normalizeRoleName(command.roleName());
        uniquenessService.requireAvailable(RoleUniqueField.ROLE_CODE, roleCode, null);
        uniquenessService.requireAvailable(RoleUniqueField.ROLE_NAME, roleName, null);
        SystemRole role = new SystemRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setRoleType(RoleType.valueOf(command.roleType().name()));
        role.setSortOrder(command.sortOrder());
        roleMapper.insert(role);
        grantDefaultHome(role);
        return role.getId();
    }

    @Override
    @Transactional
    public Map<String, Long> createBatch(List<SystemRoleCreateCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new IllegalArgumentException("批量创建系统角色参数不能为空");
        }
        List<SystemRole> roles = new ArrayList<>(commands.size());
        Set<String> roleCodes = new HashSet<>();
        Set<String> roleNames = new HashSet<>();
        for (SystemRoleCreateCommand command : commands) {
            if (command == null || command.roleType() == null) {
                throw new IllegalArgumentException("批量创建的系统角色及角色类型不能为空");
            }
            String roleCode = normalizeRoleCode(command.roleCode());
            String roleName = normalizeRoleName(command.roleName());
            if (!roleCodes.add(roleCode)) {
                throw PlatformException.conflict("批量创建的角色编码存在重复");
            }
            if (!roleNames.add(roleName)) {
                throw PlatformException.conflict("批量创建的角色名称存在重复");
            }
            SystemRole role = new SystemRole();
            role.setRoleCode(roleCode);
            role.setRoleName(roleName);
            role.setRoleType(RoleType.valueOf(command.roleType().name()));
            role.setSortOrder(command.sortOrder());
            roles.add(role);
        }
        requireBatchAvailable(roles);
        roleMapper.insert(roles, BATCH_SIZE);
        Long homeMenuId = defaultHomeMenuService.requireHomeMenuId();
        Map<Long, List<Long>> homePermissions = new LinkedHashMap<>();
        roles.stream()
                .filter(role -> !SystemRoleCodes.SUPER_ADMIN.equals(role.getRoleCode()))
                .forEach(role -> homePermissions.put(role.getId(), List.of(homeMenuId)));
        roleMenuRelationService.replaceBatch(homePermissions);
        Map<String, Long> result = new LinkedHashMap<>(roles.size());
        roles.forEach(role -> result.put(role.getRoleCode(), role.getId()));
        return result;
    }

    @Transactional
    public void update(Long roleId, RoleUpdateRequest request) {
        SystemRole role = roleQueryService.requireRole(roleId);
        roleAccessService.requireCanManage(role);
        if (SystemRoleCodes.SUPER_ADMIN.equals(role.getRoleCode())) {
            roleAccessService.requireNotSuperAdminRole(role, "修改");
        }
        String roleName = normalizeRoleName(request.roleName());
        uniquenessService.requireAvailable(RoleUniqueField.ROLE_NAME, roleName, roleId);
        role.setRoleName(roleName);
        role.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        roleMapper.updateById(role);
    }

    @Transactional
    public void delete(Long roleId) {
        SystemRole role = roleQueryService.requireRole(roleId);
        roleAccessService.requireCanManage(role);
        roleAccessService.requireDeletable(role);
        if (userRoleService.countByRoleId(roleId) > 0) {
            throw PlatformException.conflict("角色仍然关联用户，不能删除");
        }
        roleMenuRelationService.deleteByRoleId(roleId);
        roleMapper.deleteById(roleId);
    }

    @Transactional
    public void batchDelete(List<Long> roleIds) {
        for (Long roleId : roleIds.stream().distinct().toList()) {
            delete(roleId);
        }
    }

    private String normalizeRoleCode(String roleCode) {
        if (roleCode == null) {
            throw PlatformException.badRequest("角色编码不能为空");
        }
        String normalized = roleCode.trim().toLowerCase(Locale.ROOT);
        if (!ROLE_CODE_PATTERN.matcher(normalized).matches()) {
            throw PlatformException.badRequest("角色编码只能包含字母、数字、下划线和连字符，并以字母开头，长度不能超过 50 个字符");
        }
        return normalized;
    }

    private String normalizeRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw PlatformException.badRequest("角色名称不能为空");
        }
        String normalized = roleName.trim();
        if (normalized.length() > 100) {
            throw PlatformException.badRequest("角色名称不能超过 100 个字符");
        }
        return normalized;
    }

    private void grantDefaultHome(SystemRole role) {
        if (SystemRoleCodes.SUPER_ADMIN.equals(role.getRoleCode())) {
            return;
        }
        roleMenuRelationService.replace(
                role.getId(),
                List.of(defaultHomeMenuService.requireHomeMenuId())
        );
    }

    private void requireBatchAvailable(List<SystemRole> roles) {
        for (int from = 0; from < roles.size(); from += BATCH_SIZE) {
            List<SystemRole> batch = roles.subList(from, Math.min(from + BATCH_SIZE, roles.size()));
            long count = roleMapper.selectCount(new LambdaQueryWrapper<SystemRole>()
                    .in(SystemRole::getRoleCode, batch.stream().map(SystemRole::getRoleCode).toList())
                    .or()
                    .in(SystemRole::getRoleName, batch.stream().map(SystemRole::getRoleName).toList()));
            if (count > 0) {
                throw PlatformException.conflict("批量创建的角色中存在已被使用的角色编码或角色名称");
            }
        }
    }

}
