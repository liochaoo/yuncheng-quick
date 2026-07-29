package com.yuncheng.system.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.common.constant.BuiltInUserIds;
import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.common.context.CurrentUserContext;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.api.user.SystemUserBatchCreateCommand;
import com.yuncheng.system.api.user.SystemUserBatchCreateItem;
import com.yuncheng.system.api.user.SystemUserCommandApi;
import com.yuncheng.system.api.user.SystemUserCreateCommand;
import com.yuncheng.system.permission.cache.UserAccessCacheService;
import com.yuncheng.system.role.service.UserRoleService;
import com.yuncheng.system.session.service.LoginSessionService;
import com.yuncheng.system.user.dto.UserCreateRequest;
import com.yuncheng.system.user.dto.UserUpdateRequest;
import com.yuncheng.system.user.entity.SystemUser;
import com.yuncheng.system.user.enums.UserUniqueField;
import com.yuncheng.system.user.mapper.SystemUserMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 新增、编辑、启停和删除用户。 */
@Service
public class UserCommandService implements SystemUserCommandApi {

    private static final int BATCH_SIZE = 500;

    private final SystemUserMapper userMapper;
    private final UserQueryService userQueryService;
    private final UserRoleService userRoleService;
    private final UserAccessService userAccessService;
    private final UserUniquenessService uniquenessService;
    private final CurrentUserContext currentUserContext;
    private final PasswordPolicyService passwordPolicyService;
    private final UserPasswordHistoryService passwordHistoryService;
    private final UserInputService inputService;
    private final UserAccessCacheService cacheService;
    private final LoginSessionService sessionService;

    public UserCommandService(
            SystemUserMapper userMapper,
            UserQueryService userQueryService,
            UserRoleService userRoleService,
            UserAccessService userAccessService,
            UserUniquenessService uniquenessService,
            CurrentUserContext currentUserContext,
            PasswordPolicyService passwordPolicyService,
            UserPasswordHistoryService passwordHistoryService,
            UserInputService inputService,
            UserAccessCacheService cacheService,
            LoginSessionService sessionService
    ) {
        this.userMapper = userMapper;
        this.userQueryService = userQueryService;
        this.userRoleService = userRoleService;
        this.userAccessService = userAccessService;
        this.uniquenessService = uniquenessService;
        this.currentUserContext = currentUserContext;
        this.passwordPolicyService = passwordPolicyService;
        this.passwordHistoryService = passwordHistoryService;
        this.inputService = inputService;
        this.cacheService = cacheService;
        this.sessionService = sessionService;
    }

    @Transactional
    public Long create(UserCreateRequest request) {
        String username = inputService.normalizeNewUsername(request.username());
        uniquenessService.requireAvailable(UserUniqueField.USERNAME, username, null);
        String phone = inputService.normalizePhone(request.phone());
        String email = inputService.normalizeEmail(request.email());
        uniquenessService.requireAvailable(UserUniqueField.PHONE, phone, null);
        uniquenessService.requireAvailable(UserUniqueField.EMAIL, email, null);
        SystemUser user = new SystemUser();
        user.setUsername(username);
        user.setPasswordHash(passwordPolicyService.encodeNewPassword(request.password()));
        user.setPasswordChangedAt(Instant.now());
        user.setLoginFailedCount(0);
        user.setRealName(inputService.normalizeRealName(request.realName()));
        user.setPhone(phone);
        user.setEmail(email);
        user.setSortOrder(valueOrZero(request.sortOrder()));
        user.setEnabled(true);
        userMapper.insert(user);
        passwordHistoryService.recordCreatedUsers(List.of(user));
        userRoleService.replaceUserRoles(user.getId(), request.roleIds());
        return user.getId();
    }

    @Override
    @Transactional
    public Long create(SystemUserCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("创建系统用户参数不能为空");
        }
        Long userId = command.userId();
        if (userId != null && userId <= 0) {
            throw new IllegalArgumentException("系统用户主键必须大于 0");
        }
        if (userId != null && userMapper.selectById(userId) != null) {
            throw PlatformException.conflict("用户主键已经存在");
        }
        String username = inputService.normalizeNewUsername(command.username());
        String phone = inputService.normalizePhone(command.phone());
        String email = inputService.normalizeEmail(command.email());
        uniquenessService.requireAvailable(UserUniqueField.USERNAME, username, null);
        uniquenessService.requireAvailable(UserUniqueField.PHONE, phone, null);
        uniquenessService.requireAvailable(UserUniqueField.EMAIL, email, null);
        SystemUser user = new SystemUser();
        if (userId != null) {
            user.setId(userId);
        }
        user.setUsername(username);
        user.setPasswordHash(passwordPolicyService.encodeNewPassword(command.password()));
        user.setPasswordChangedAt(Instant.now());
        user.setLoginFailedCount(0);
        user.setRealName(inputService.normalizeRealName(command.realName()));
        user.setPhone(phone);
        user.setEmail(email);
        user.setSortOrder(command.sortOrder());
        user.setEnabled(command.enabled());
        userMapper.insert(user);
        passwordHistoryService.recordCreatedUsers(List.of(user));
        return user.getId();
    }

    @Override
    @Transactional
    public Map<String, Long> createBatch(SystemUserBatchCreateCommand command) {
        if (command == null || command.users() == null || command.users().isEmpty()) {
            throw new IllegalArgumentException("批量创建系统用户参数不能为空");
        }
        List<SystemUser> users = new ArrayList<>(command.users().size());
        Set<Long> userIds = new HashSet<>();
        Set<String> usernames = new HashSet<>();
        Set<String> phones = new HashSet<>();
        Set<String> emails = new HashSet<>();
        String passwordHash = passwordPolicyService.encodeNewPassword(command.password());
        for (SystemUserBatchCreateItem item : command.users()) {
            if (item == null) {
                throw new IllegalArgumentException("批量创建的用户信息不能为空");
            }
            Long userId = item.userId();
            if (userId != null && (userId <= 0 || !userIds.add(userId))) {
                throw PlatformException.conflict("批量创建的用户主键无效或重复");
            }
            String username = inputService.normalizeNewUsername(item.username());
            String phone = inputService.normalizePhone(item.phone());
            String email = inputService.normalizeEmail(item.email());
            requireUniqueInBatch(usernames, username, "登录名");
            requireUniqueInBatch(phones, phone, "手机号码");
            requireUniqueInBatch(emails, email, "电子邮箱");
            SystemUser user = new SystemUser();
            user.setId(userId);
            user.setUsername(username);
            user.setPasswordHash(passwordHash);
            user.setPasswordChangedAt(Instant.now());
            user.setLoginFailedCount(0);
            user.setRealName(inputService.normalizeRealName(item.realName()));
            user.setPhone(phone);
            user.setEmail(email);
            user.setSortOrder(item.sortOrder());
            user.setEnabled(item.enabled());
            users.add(user);
        }
        requireBatchAvailable(users);
        userMapper.insert(users, BATCH_SIZE);
        passwordHistoryService.recordCreatedUsers(users);
        Map<String, Long> result = new LinkedHashMap<>(users.size());
        users.forEach(user -> result.put(user.getUsername(), user.getId()));
        return result;
    }

    @Transactional
    public void update(Long userId, UserUpdateRequest request) {
        SystemUser user = userQueryService.requireUser(userId);
        userAccessService.requireCanManage(userId);
        String phone = inputService.normalizePhone(request.phone());
        String email = inputService.normalizeEmail(request.email());
        uniquenessService.requireAvailable(UserUniqueField.PHONE, phone, userId);
        uniquenessService.requireAvailable(UserUniqueField.EMAIL, email, userId);
        user.setRealName(inputService.normalizeRealName(request.realName()));
        user.setPhone(phone);
        user.setEmail(email);
        user.setSortOrder(valueOrZero(request.sortOrder()));
        userMapper.updateById(user);
        userRoleService.replaceUserRoles(userId, request.roleIds());
        cacheService.clearAllAfterCommit(List.of(userId));
    }

    @Transactional
    public void changeStatus(Long userId, boolean enabled) {
        SystemUser user = userQueryService.requireUser(userId);
        userAccessService.requireCanManage(userId);
        if (Boolean.TRUE.equals(user.getEnabled()) == enabled) {
            return;
        }
        if (!enabled && BuiltInUserIds.ADMINISTRATOR == userId) {
            throw PlatformException.conflict("初始管理员账号不能停用");
        }
        if (!enabled && currentUserContext.getUserId().equals(userId)) {
            throw PlatformException.conflict("不能停用当前登录用户");
        }
        if (!enabled) {
            requireNotLastSuperAdmin(userId);
            sessionService.deleteAllByUserId(userId);
        }
        user.setEnabled(enabled);
        userMapper.updateById(user);
        cacheService.clearAllAfterCommit(List.of(userId));
    }

    @Transactional
    public void delete(Long userId) {
        userQueryService.requireUser(userId);
        requireNotBuiltInAdministrator(userId);
        userAccessService.requireCanManage(userId);
        if (currentUserContext.getUserId().equals(userId)) {
            throw PlatformException.conflict("不能删除当前登录用户");
        }
        requireNotLastSuperAdmin(userId);
        sessionService.deleteAllByUserId(userId);
        userRoleService.deleteByUserId(userId);
        passwordHistoryService.deleteByUserId(userId);
        userMapper.deleteById(userId);
        cacheService.clearAllAfterCommit(List.of(userId));
    }

    @Transactional
    public void batchDelete(List<Long> userIds) {
        if (userIds.contains(BuiltInUserIds.ADMINISTRATOR)) {
            throw PlatformException.conflict("初始管理员账号不能删除");
        }
        for (Long userId : userIds.stream().distinct().toList()) {
            delete(userId);
        }
    }

    private void requireNotBuiltInAdministrator(Long userId) {
        if (BuiltInUserIds.ADMINISTRATOR == userId) {
            throw PlatformException.conflict("初始管理员账号不能删除");
        }
    }

    private void requireNotLastSuperAdmin(Long userId) {
        if (!userRoleService.roleCodes(userId).contains(SystemRoleCodes.SUPER_ADMIN)) {
            return;
        }
        if (userMapper.countEnabledUsersByRoleCode(SystemRoleCodes.SUPER_ADMIN, userId) == 0) {
            throw PlatformException.conflict("系统必须至少保留一个启用的超级管理员");
        }
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private void requireBatchAvailable(List<SystemUser> users) {
        for (int from = 0; from < users.size(); from += BATCH_SIZE) {
            List<SystemUser> batch = users.subList(from, Math.min(from + BATCH_SIZE, users.size()));
            List<Long> ids = batch.stream().map(SystemUser::getId).filter(java.util.Objects::nonNull).toList();
            List<String> batchPhones = batch.stream().map(SystemUser::getPhone).filter(java.util.Objects::nonNull).toList();
            List<String> batchEmails = batch.stream().map(SystemUser::getEmail).filter(java.util.Objects::nonNull).toList();
            LambdaQueryWrapper<SystemUser> wrapper = new LambdaQueryWrapper<SystemUser>()
                    .in(SystemUser::getUsername, batch.stream().map(SystemUser::getUsername).toList());
            if (!ids.isEmpty()) {
                wrapper.or().in(SystemUser::getId, ids);
            }
            if (!batchPhones.isEmpty()) {
                wrapper.or().in(SystemUser::getPhone, batchPhones);
            }
            if (!batchEmails.isEmpty()) {
                wrapper.or().in(SystemUser::getEmail, batchEmails);
            }
            if (userMapper.selectCount(wrapper) > 0) {
                throw PlatformException.conflict("批量创建的用户中存在已被使用的主键、登录名、手机号码或电子邮箱");
            }
        }
    }

    private <T> void requireUniqueInBatch(Set<T> values, T value, String fieldName) {
        if (value != null && !values.add(value)) {
            throw PlatformException.conflict("批量创建的用户" + fieldName + "存在重复");
        }
    }
}
