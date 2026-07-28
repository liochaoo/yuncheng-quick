package com.yuncheng.demo.service;

import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.init.system.config.AdminInitializationProperties;
import com.yuncheng.system.api.permission.SystemPermissionQueryApi;
import com.yuncheng.system.api.permission.SystemRolePermissionCommand;
import com.yuncheng.system.api.permission.SystemRolePermissionCommandApi;
import com.yuncheng.system.api.role.SystemRoleCommandApi;
import com.yuncheng.system.api.role.SystemRoleCreateCommand;
import com.yuncheng.system.api.role.SystemRoleInfo;
import com.yuncheng.system.api.role.SystemRoleQueryApi;
import com.yuncheng.system.api.role.SystemRoleType;
import com.yuncheng.system.api.role.SystemUserRoleApi;
import com.yuncheng.system.api.role.SystemUserRoleBinding;
import com.yuncheng.system.api.user.SystemUserBatchCreateCommand;
import com.yuncheng.system.api.user.SystemUserBatchCreateItem;
import com.yuncheng.system.api.user.SystemUserCommandApi;
import com.yuncheng.system.api.user.SystemUserQueryApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 初始化用于本地功能和数据量验证的开发测试数据。 */
@Service
public class DemoMassDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DemoMassDataInitializer.class);

    private static final int USER_COUNT = 100_000;
    private static final int ROLE_COUNT = 5_000;
    private static final int USER_ROLE_BATCH_SIZE = 5_000;
    private static final long USER_RANDOM_SEED = 2026072201L;
    private static final long PERMISSION_RANDOM_SEED = 2026072202L;
    private static final long ROLE_ASSIGNMENT_RANDOM_SEED = 2026072203L;

    private final AdminInitializationProperties adminProperties;
    private final SystemUserQueryApi userQueryApi;
    private final SystemUserCommandApi userCommandApi;
    private final SystemRoleQueryApi roleQueryApi;
    private final SystemRoleCommandApi roleCommandApi;
    private final SystemUserRoleApi userRoleApi;
    private final SystemPermissionQueryApi permissionQueryApi;
    private final SystemRolePermissionCommandApi rolePermissionCommandApi;

    public DemoMassDataInitializer(
            AdminInitializationProperties adminProperties,
            SystemUserQueryApi userQueryApi,
            SystemUserCommandApi userCommandApi,
            SystemRoleQueryApi roleQueryApi,
            SystemRoleCommandApi roleCommandApi,
            SystemUserRoleApi userRoleApi,
            SystemPermissionQueryApi permissionQueryApi,
            SystemRolePermissionCommandApi rolePermissionCommandApi
    ) {
        this.adminProperties = adminProperties;
        this.userQueryApi = userQueryApi;
        this.userCommandApi = userCommandApi;
        this.roleQueryApi = roleQueryApi;
        this.roleCommandApi = roleCommandApi;
        this.userRoleApi = userRoleApi;
        this.permissionQueryApi = permissionQueryApi;
        this.rolePermissionCommandApi = rolePermissionCommandApi;
    }

    /**
     * 大数据初始化使用一个完整事务，失败时整体回滚，避免下次启动面对半成品数据。
     */
    @Transactional
    public void initialize() {
        if (isInitialized()) {
            log.info("开发测试大数据已经初始化，跳过本次处理");
            return;
        }
        log.info("开始初始化开发测试大数据：用户 {} 条，角色 {} 个", USER_COUNT, ROLE_COUNT);
        Map<String, Long> roleIds = createRoles();
        initializeRolePermissions(roleIds);
        GeneratedUsers generatedUsers = createUsers();
        initializeUserRoles(generatedUsers, roleIds);
        log.info("开发测试大数据初始化完成：用户 {} 条，角色 {} 个", USER_COUNT, ROLE_COUNT);
    }

    private boolean isInitialized() {
        return userQueryApi.findByUsername(username(USER_COUNT)).isPresent()
                && roleQueryApi.findByCode(roleCode(ROLE_COUNT)).isPresent();
    }

    private Map<String, Long> createRoles() {
        List<SystemRoleCreateCommand> commands = new ArrayList<>(ROLE_COUNT);
        for (int index = 1; index <= ROLE_COUNT; index++) {
            commands.add(new SystemRoleCreateCommand(
                    roleCode(index),
                    "测试角色" + number(index, 4),
                    SystemRoleType.CUSTOM,
                    1_000 + index
            ));
        }
        Map<String, Long> roleIds = roleCommandApi.createBatch(commands);
        log.info("开发测试角色初始化完成，共 {} 个", roleIds.size());
        return roleIds;
    }

    private void initializeRolePermissions(Map<String, Long> roleIds) {
        List<Long> assignableMenuIds = permissionQueryApi.findAssignableMenuIds(SystemRoleType.CUSTOM);
        if (assignableMenuIds.isEmpty()) {
            throw new IllegalStateException("初始化开发测试角色权限失败：没有可授权的菜单权限");
        }
        SplittableRandom random = new SplittableRandom(PERMISSION_RANDOM_SEED);
        List<SystemRolePermissionCommand> commands = new ArrayList<>(ROLE_COUNT);
        for (int index = 1; index <= ROLE_COUNT; index++) {
            List<Long> selectedMenuIds = assignableMenuIds.stream()
                    .filter(ignored -> random.nextInt(100) < 45)
                    .toList();
            if (selectedMenuIds.isEmpty()) {
                selectedMenuIds = List.of(assignableMenuIds.get(random.nextInt(assignableMenuIds.size())));
            }
            commands.add(new SystemRolePermissionCommand(
                    requireId(roleIds, roleCode(index), "测试角色"),
                    selectedMenuIds
            ));
        }
        rolePermissionCommandApi.replaceBatch(commands);
        log.info("开发测试角色权限初始化完成，共处理 {} 个角色", commands.size());
    }

    private GeneratedUsers createUsers() {
        SplittableRandom profileRandom = new SplittableRandom(USER_RANDOM_SEED);
        SplittableRandom roleRandom = new SplittableRandom(ROLE_ASSIGNMENT_RANDOM_SEED);
        List<SystemUserBatchCreateItem> users = new ArrayList<>(USER_COUNT);
        int[] assignedRoleIndexes = new int[USER_COUNT];
        int[] guaranteedRoleIndexes = shuffledRoleIndexes(roleRandom);
        for (int index = 1; index <= USER_COUNT; index++) {
            boolean hasPhone = profileRandom.nextInt(100) < 65;
            boolean hasEmail = profileRandom.nextInt(100) < 70;
            assignedRoleIndexes[index - 1] = index <= ROLE_COUNT
                    ? guaranteedRoleIndexes[index - 1]
                    : roleRandom.nextInt(1, ROLE_COUNT + 1);
            users.add(new SystemUserBatchCreateItem(
                    null,
                    username(index),
                    "测试用户" + number(index, 6),
                    hasPhone ? "130" + number(index, 8) : null,
                    hasEmail ? username(index) + "@example.com" : null,
                    1_000 + index,
                    true
            ));
        }
        Map<String, Long> userIds = userCommandApi.createBatch(new SystemUserBatchCreateCommand(
                adminProperties.getPassword(),
                users
        ));
        log.info("开发测试用户初始化完成，共 {} 条", userIds.size());
        return new GeneratedUsers(userIds, assignedRoleIndexes);
    }

    private int[] shuffledRoleIndexes(SplittableRandom random) {
        int[] roleIndexes = new int[ROLE_COUNT];
        for (int index = 0; index < ROLE_COUNT; index++) {
            roleIndexes[index] = index + 1;
        }
        for (int index = roleIndexes.length - 1; index > 0; index--) {
            int target = random.nextInt(index + 1);
            int value = roleIndexes[index];
            roleIndexes[index] = roleIndexes[target];
            roleIndexes[target] = value;
        }
        return roleIndexes;
    }

    private void initializeUserRoles(GeneratedUsers generatedUsers, Map<String, Long> roleIds) {
        Long defaultRoleId = requireDefaultRole().roleId();
        List<SystemUserRoleBinding> bindings = new ArrayList<>(USER_ROLE_BATCH_SIZE * 2);
        for (int index = 1; index <= USER_COUNT; index++) {
            Long userId = requireId(generatedUsers.userIds(), username(index), "测试用户");
            bindings.add(new SystemUserRoleBinding(userId, defaultRoleId));
            bindings.add(new SystemUserRoleBinding(
                    userId,
                    requireId(
                            roleIds,
                            roleCode(generatedUsers.assignedRoleIndexes()[index - 1]),
                            "测试角色"
                    )
            ));
            if (index % USER_ROLE_BATCH_SIZE == 0) {
                userRoleApi.bindBatch(bindings);
                bindings.clear();
            }
        }
        userRoleApi.bindBatch(bindings);
        log.info("开发测试用户角色关系初始化完成，每名用户均绑定一般用户角色和一个随机测试角色");
    }

    private SystemRoleInfo requireDefaultRole() {
        return roleQueryApi.findByCode(SystemRoleCodes.DEFAULT_USER)
                .orElseThrow(() -> new IllegalStateException(
                        "初始化开发测试数据失败：未找到一般用户角色，roleCode="
                                + SystemRoleCodes.DEFAULT_USER
                ));
    }

    private Long requireId(Map<String, Long> ids, String key, String dataName) {
        Long id = ids.get(key);
        if (id == null) {
            throw new IllegalStateException(dataName + "初始化后未返回主键：" + key);
        }
        return id;
    }

    private static String username(int index) {
        return "demo" + number(index, 6);
    }

    private static String roleCode(int index) {
        return "demo-role-" + number(index, 4);
    }

    private static String number(int value, int width) {
        return String.format(java.util.Locale.ROOT, "%0" + width + "d", value);
    }

    private record GeneratedUsers(Map<String, Long> userIds, int[] assignedRoleIndexes) {
    }
}
