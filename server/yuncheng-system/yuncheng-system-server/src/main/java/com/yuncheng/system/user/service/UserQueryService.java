package com.yuncheng.system.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuncheng.common.util.DataMaskingUtils;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.system.api.user.SystemUserInfo;
import com.yuncheng.system.api.user.SystemUserQueryApi;
import com.yuncheng.system.role.dto.RoleSummary;
import com.yuncheng.system.role.service.UserRoleService;
import com.yuncheng.system.security.service.SecurityPolicyService;
import com.yuncheng.system.user.dto.UserDetail;
import com.yuncheng.system.user.dto.UserFormData;
import com.yuncheng.system.user.dto.UserListItem;
import com.yuncheng.system.user.dto.UserPageQuery;
import com.yuncheng.system.user.dto.UserProfileData;
import com.yuncheng.system.user.entity.SystemUser;
import com.yuncheng.system.user.enums.UserUniqueField;
import com.yuncheng.system.user.mapper.SystemUserMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** 查询用户管理数据。 */
@Service
public class UserQueryService implements SystemUserQueryApi {

    private final SystemUserMapper userMapper;
    private final UserRoleService userRoleService;
    private final UserAccessService userAccessService;
    private final UserInputService inputService;
    private final UserUniquenessService uniquenessService;
    private final SecurityPolicyService securityPolicyService;

    public UserQueryService(
            SystemUserMapper userMapper,
            UserRoleService userRoleService,
            UserAccessService userAccessService,
            UserInputService inputService,
            UserUniquenessService uniquenessService,
            SecurityPolicyService securityPolicyService
    ) {
        this.userMapper = userMapper;
        this.userRoleService = userRoleService;
        this.userAccessService = userAccessService;
        this.inputService = inputService;
        this.uniquenessService = uniquenessService;
        this.securityPolicyService = securityPolicyService;
    }

    @Override
    public Optional<SystemUserInfo> findById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userMapper.selectById(userId))
                .map(this::toSystemUserInfo);
    }

    @Override
    public Optional<SystemUserInfo> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        String normalized = username.trim().toLowerCase(Locale.ROOT);
        SystemUser user = userMapper.selectOne(new LambdaQueryWrapper<SystemUser>()
                .eq(SystemUser::getUsername, normalized));
        return Optional.ofNullable(user).map(this::toSystemUserInfo);
    }

    public Optional<SystemUserInfo> findEnabledByUsernameAndEmail(String username, String email) {
        String normalizedUsername = inputService.normalizeUsername(username);
        String normalizedEmail = inputService.requireEmail(email);
        SystemUser user = userMapper.selectOne(new LambdaQueryWrapper<SystemUser>()
                .eq(SystemUser::getUsername, normalizedUsername)
                .eq(SystemUser::getEmail, normalizedEmail)
                .eq(SystemUser::getEnabled, true));
        return Optional.ofNullable(user).map(this::toSystemUserInfo);
    }

    public Optional<UserProfileData> findProfileById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userMapper.selectById(userId)).map(user -> {
            List<String> roleNames = userRoleService
                    .summariesByUserIds(List.of(userId))
                    .getOrDefault(userId, List.of())
                    .stream()
                    .map(RoleSummary::roleName)
                    .toList();
            return new UserProfileData(
                    user.getId(), user.getUsername(), user.getRealName(), user.getAvatar(),
                    user.getPhone(), user.getEmail(), Boolean.TRUE.equals(user.getEnabled()),
                    roleNames, user.getCreatedAt(), user.getPasswordChangedAt()
            );
        });
    }

    public boolean isUsernameAvailable(String username) {
        return uniquenessService.isAvailable(
                UserUniqueField.USERNAME,
                inputService.normalizeUsername(username),
                null
        );
    }

    public boolean isEmailAvailable(String email, Long excludedUserId) {
        return uniquenessService.isAvailable(
                UserUniqueField.EMAIL,
                inputService.requireEmail(email),
                excludedUserId
        );
    }

    public PageResult<UserListItem> page(UserPageQuery query) {
        normalize(query);
        IPage<SystemUser> page = userMapper.selectUserPage(
                new Page<>(query.getPage(), query.getPageSize()),
                query
        );
        List<UserListItem> items = toListItems(page.getRecords());
        return PageResult.of(items, page.getTotal(), query);
    }

    public UserDetail detail(Long userId) {
        SystemUser user = requireUser(userId);
        Instant now = Instant.now();
        int failureWindowMinutes = securityPolicyService.current().loginFailure().windowMinutes();
        return new UserDetail(
                id(user.getId()), user.getUsername(), user.getRealName(), user.getAvatar(),
                DataMaskingUtils.maskPhone(user.getPhone()),
                DataMaskingUtils.maskEmail(user.getEmail()),
                user.getSortOrder(), user.getEnabled(),
                isLoginLocked(user, now), user.getLoginLockedUntil(),
                effectiveFailedCount(user, now, failureWindowMinutes),
                user.getPasswordChangedAt(), roleIds(userId),
                user.getCreatedAt(), id(user.getCreatedBy()), user.getUpdatedAt(), id(user.getUpdatedBy())
        );
    }

    public UserFormData formData(Long userId) {
        SystemUser user = requireUser(userId);
        userAccessService.requireCanManage(userId);
        return new UserFormData(
                id(user.getId()), user.getUsername(), user.getRealName(),
                user.getPhone(), user.getEmail(), user.getSortOrder(),
                user.getEnabled(), roleIds(userId)
        );
    }

    public SystemUser requireUser(Long userId) {
        SystemUser user = userId == null ? null : userMapper.selectById(userId);
        if (user == null) {
            throw PlatformException.notFound("用户不存在");
        }
        return user;
    }

    public List<SystemUser> requireUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        List<SystemUser> users = userMapper.selectByIds(userIds);
        if (users.size() != userIds.stream().distinct().count()) {
            throw PlatformException.notFound("选择的用户中存在已被删除的数据");
        }
        return users;
    }

    private List<UserListItem> toListItems(List<SystemUser> users) {
        List<Long> userIds = users.stream().map(SystemUser::getId).toList();
        Map<Long, List<RoleSummary>> roles = userRoleService.summariesByUserIds(userIds);
        Instant now = Instant.now();
        int failureWindowMinutes = securityPolicyService.current().loginFailure().windowMinutes();
        return users.stream()
                .map(user -> toListItem(
                        user,
                        roles.getOrDefault(user.getId(), List.of()),
                        now,
                        failureWindowMinutes
                ))
                .toList();
    }

    private void normalize(UserPageQuery query) {
        if (query.getUsername() != null) {
            query.setUsername(query.getUsername().trim().toLowerCase(Locale.ROOT));
        }
        if (query.getRealName() != null) {
            query.setRealName(query.getRealName().trim());
        }
    }

    private UserListItem toListItem(
            SystemUser user,
            List<RoleSummary> roles,
            Instant now,
            int failureWindowMinutes
    ) {
        return new UserListItem(
                id(user.getId()), user.getUsername(), user.getRealName(), user.getAvatar(),
                DataMaskingUtils.maskPhone(user.getPhone()),
                DataMaskingUtils.maskEmail(user.getEmail()),
                user.getSortOrder(), user.getEnabled(),
                isLoginLocked(user, now), user.getLoginLockedUntil(),
                effectiveFailedCount(user, now, failureWindowMinutes),
                user.getPasswordChangedAt(),
                roles, user.getCreatedAt(), user.getUpdatedAt()
        );
    }

    private boolean isLoginLocked(SystemUser user, Instant now) {
        return user.getLoginLockedUntil() != null
                && user.getLoginLockedUntil().isAfter(now);
    }

    private int effectiveFailedCount(
            SystemUser user,
            Instant now,
            int failureWindowMinutes
    ) {
        Instant lockedUntil = user.getLoginLockedUntil();
        if (lockedUntil != null) {
            return lockedUntil.isAfter(now)
                    ? valueOrZero(user.getLoginFailedCount())
                    : 0;
        }
        Instant startedAt = user.getLoginFailureWindowStartedAt();
        if (startedAt == null) {
            return 0;
        }
        return startedAt.plus(failureWindowMinutes, ChronoUnit.MINUTES).isAfter(now)
                ? valueOrZero(user.getLoginFailedCount())
                : 0;
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private List<String> roleIds(Long userId) {
        return userRoleService.roleIdsByUserId(userId).stream()
                .sorted()
                .map(String::valueOf)
                .toList();
    }

    private SystemUserInfo toSystemUserInfo(SystemUser user) {
        return new SystemUserInfo(
                user.getId(), user.getUsername(), user.getRealName(),
                Boolean.TRUE.equals(user.getEnabled())
        );
    }

    private String id(Long value) {
        return value == null ? null : value.toString();
    }
}
