package com.yuncheng.init.system.service;

import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.system.api.role.SystemRoleCommandApi;
import com.yuncheng.system.api.role.SystemRoleCreateCommand;
import com.yuncheng.system.api.role.SystemRoleInfo;
import com.yuncheng.system.api.role.SystemRoleQueryApi;
import com.yuncheng.system.api.role.SystemRoleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 初始化平台保留角色。 */
@Service
public class SystemRoleInitializer {

    private static final Logger log = LoggerFactory.getLogger(SystemRoleInitializer.class);

    private final SystemRoleQueryApi roleQueryApi;
    private final SystemRoleCommandApi roleCommandApi;

    public SystemRoleInitializer(
            SystemRoleQueryApi roleQueryApi,
            SystemRoleCommandApi roleCommandApi
    ) {
        this.roleQueryApi = roleQueryApi;
        this.roleCommandApi = roleCommandApi;
    }

    public Long initialize() {
        Long superAdminRoleId = initializeRole(
                SystemRoleCodes.SUPER_ADMIN,
                "超级管理员",
                SystemRoleType.SYSTEM,
                0
        );
        initializeRole(
                SystemRoleCodes.DEFAULT_USER,
                "一般用户",
                SystemRoleType.CUSTOM,
                100
        );
        return superAdminRoleId;
    }

    private Long initializeRole(
            String roleCode,
            String roleName,
            SystemRoleType roleType,
            int sortOrder
    ) {
        SystemRoleInfo existing = roleQueryApi.findByCode(roleCode).orElse(null);
        if (existing == null) {
            return roleCommandApi.create(new SystemRoleCreateCommand(
                    roleCode,
                    roleName,
                    roleType,
                    sortOrder
            ));
        }
        if (existing.roleType() != roleType) {
            throw initializationConflict(
                    "角色类型与平台约定不一致，roleCode=" + roleCode
                            + "，expected=" + roleType
                            + "，actual=" + existing.roleType()
            );
        }
        return existing.roleId();
    }

    private IllegalStateException initializationConflict(String message) {
        log.error("平台保留角色初始化冲突：{}", message);
        return new IllegalStateException(message);
    }
}
