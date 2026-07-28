package com.yuncheng.demo.service;

import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.init.system.config.AdminInitializationProperties;
import com.yuncheng.system.api.role.SystemRoleInfo;
import com.yuncheng.system.api.role.SystemRoleQueryApi;
import com.yuncheng.system.api.role.SystemUserRoleApi;
import com.yuncheng.system.api.user.SystemUserCommandApi;
import com.yuncheng.system.api.user.SystemUserCreateCommand;
import com.yuncheng.system.api.user.SystemUserInfo;
import com.yuncheng.system.api.user.SystemUserQueryApi;
import org.springframework.stereotype.Service;

/** 初始化开发测试使用的示例用户。 */
@Service
public class DemoUserInitializer {

    private static final String DEMO_USERNAME = "zhangs";
    private static final String DEMO_REAL_NAME = "张三";
    private static final int DEMO_SORT_ORDER = 100;

    private final AdminInitializationProperties adminProperties;
    private final SystemUserQueryApi userQueryApi;
    private final SystemUserCommandApi userCommandApi;
    private final SystemRoleQueryApi roleQueryApi;
    private final SystemUserRoleApi userRoleApi;

    public DemoUserInitializer(
            AdminInitializationProperties adminProperties,
            SystemUserQueryApi userQueryApi,
            SystemUserCommandApi userCommandApi,
            SystemRoleQueryApi roleQueryApi,
            SystemUserRoleApi userRoleApi
    ) {
        this.adminProperties = adminProperties;
        this.userQueryApi = userQueryApi;
        this.userCommandApi = userCommandApi;
        this.roleQueryApi = roleQueryApi;
        this.userRoleApi = userRoleApi;
    }

    public void initialize() {
        Long roleId = requireDefaultRole().roleId();
        Long userId = initializeDemoUser();
        if (!userRoleApi.exists(userId, roleId)) {
            userRoleApi.bind(userId, roleId);
        }
    }

    private SystemRoleInfo requireDefaultRole() {
        return roleQueryApi.findByCode(SystemRoleCodes.DEFAULT_USER)
                .orElseThrow(() -> new IllegalStateException(
                        "初始化示例用户失败：未找到一般用户角色，roleCode="
                                + SystemRoleCodes.DEFAULT_USER
                ));
    }

    private Long initializeDemoUser() {
        return userQueryApi.findByUsername(DEMO_USERNAME)
                .map(SystemUserInfo::userId)
                .orElseGet(() -> userCommandApi.create(new SystemUserCreateCommand(
                        null,
                        DEMO_USERNAME,
                        adminProperties.getPassword(),
                        DEMO_REAL_NAME,
                        null,
                        null,
                        DEMO_SORT_ORDER,
                        true
                )));
    }
}
