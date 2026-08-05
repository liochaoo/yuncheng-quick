package com.yuncheng.init.system.service;

import com.yuncheng.common.constant.BuiltInOrgIds;
import com.yuncheng.common.constant.BuiltInUserIds;
import com.yuncheng.init.system.config.AdminInitializationProperties;
import com.yuncheng.system.api.role.SystemUserRoleApi;
import com.yuncheng.system.api.user.SystemUserCommandApi;
import com.yuncheng.system.api.user.SystemUserCreateCommand;
import com.yuncheng.system.api.user.SystemUserInfo;
import com.yuncheng.system.api.user.SystemUserQueryApi;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 初始化系统管理员账号并绑定超级管理员角色。 */
@Service
public class SystemAdminInitializer {

    private static final Logger log = LoggerFactory.getLogger(SystemAdminInitializer.class);

    private final AdminInitializationProperties adminProperties;
    private final SystemUserQueryApi userQueryApi;
    private final SystemUserCommandApi userCommandApi;
    private final SystemUserRoleApi userRoleApi;

    public SystemAdminInitializer(
            AdminInitializationProperties adminProperties,
            SystemUserQueryApi userQueryApi,
            SystemUserCommandApi userCommandApi,
            SystemUserRoleApi userRoleApi
    ) {
        this.adminProperties = adminProperties;
        this.userQueryApi = userQueryApi;
        this.userCommandApi = userCommandApi;
        this.userRoleApi = userRoleApi;
    }

    public void initialize(Long superAdminRoleId) {
        Long userId = initializeAdminUser();
        if (!userRoleApi.exists(userId, superAdminRoleId)) {
            userRoleApi.bind(userId, superAdminRoleId);
        }
    }

    private Long initializeAdminUser() {
        String username = normalizeUsername(adminProperties.getUsername());
        SystemUserInfo existingById = userQueryApi.findById(BuiltInUserIds.ADMINISTRATOR)
                .orElse(null);
        if (existingById != null) {
            if (!username.equals(existingById.username())) {
                throw initializationConflict(
                        "固定管理员主键已被其他账号使用，userId=" + BuiltInUserIds.ADMINISTRATOR
                );
            }
            return existingById.userId();
        }
        SystemUserInfo existingByUsername = userQueryApi.findByUsername(username).orElse(null);
        if (existingByUsername != null) {
            throw initializationConflict(
                    "管理员登录名已绑定其他主键，username=" + username
                            + "，existingUserId=" + existingByUsername.userId()
            );
        }
        return userCommandApi.create(new SystemUserCreateCommand(
                BuiltInUserIds.ADMINISTRATOR,
                username,
                requirePassword(adminProperties.getPassword()),
                adminProperties.getRealName(),
                null,
                null,
                0,
                true,
                BuiltInOrgIds.DEFAULT_ORG,
                false
        ));
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw initializationConflict("管理员登录名不能为空");
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String requirePassword(String password) {
        if (password == null || password.isBlank()) {
            throw initializationConflict("首次初始化管理员时必须配置管理员密码");
        }
        return password;
    }

    private IllegalStateException initializationConflict(String message) {
        log.error("管理员账号初始化冲突：{}", message);
        return new IllegalStateException(message);
    }
}
