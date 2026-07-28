package com.yuncheng.system.user.service;

import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.permission.cache.UserAccessCacheService;
import com.yuncheng.system.session.service.LoginSessionService;
import com.yuncheng.system.user.entity.SystemUser;
import com.yuncheng.system.user.enums.PasswordChangeSource;
import com.yuncheng.system.user.enums.UserUniqueField;
import com.yuncheng.system.user.mapper.SystemUserMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 统一维护用户邮箱和密码等账号安全数据。 */
@Service
public class UserAccountService {

    private final SystemUserMapper userMapper;
    private final UserQueryService userQueryService;
    private final UserUniquenessService uniquenessService;
    private final UserInputService inputService;
    private final PasswordPolicyService passwordPolicyService;
    private final UserPasswordHistoryService passwordHistoryService;
    private final LoginSessionService sessionService;
    private final UserAccessCacheService cacheService;

    public UserAccountService(
            SystemUserMapper userMapper,
            UserQueryService userQueryService,
            UserUniquenessService uniquenessService,
            UserInputService inputService,
            PasswordPolicyService passwordPolicyService,
            UserPasswordHistoryService passwordHistoryService,
            LoginSessionService sessionService,
            UserAccessCacheService cacheService
    ) {
        this.userMapper = userMapper;
        this.userQueryService = userQueryService;
        this.uniquenessService = uniquenessService;
        this.inputService = inputService;
        this.passwordPolicyService = passwordPolicyService;
        this.passwordHistoryService = passwordHistoryService;
        this.sessionService = sessionService;
        this.cacheService = cacheService;
    }

    public void requireNewPassword(String password) {
        passwordPolicyService.requireNewPassword(password);
    }

    public void requirePasswordMatches(Long userId, String password) {
        SystemUser user = userQueryService.requireUser(userId);
        passwordPolicyService.requireLoginInput(password);
        if (!passwordPolicyService.matches(password, user.getPasswordHash())) {
            throw PlatformException.badRequest("当前密码不正确");
        }
    }

    @Transactional
    public void changeEmail(Long userId, String email) {
        SystemUser user = userQueryService.requireUser(userId);
        String normalizedEmail = inputService.requireEmail(email);
        uniquenessService.requireAvailable(UserUniqueField.EMAIL, normalizedEmail, userId);
        user.setEmail(normalizedEmail);
        userMapper.updateById(user);
    }

    @Transactional
    public void changeAvatar(Long userId, String avatar) {
        SystemUser user = userQueryService.requireUser(userId);
        user.setAvatar(avatar);
        userMapper.updateById(user);
        cacheService.clearAllAfterCommit(List.of(userId));
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        SystemUser user = requireUserForUpdate(userId);
        passwordPolicyService.requireLoginInput(currentPassword);
        if (!passwordPolicyService.matches(currentPassword, user.getPasswordHash())) {
            throw PlatformException.badRequest("当前密码不正确");
        }
        replacePassword(user, newPassword, PasswordChangeSource.SELF_CHANGE);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        resetPassword(userId, newPassword, PasswordChangeSource.ADMIN_RESET);
    }

    @Transactional
    public void recoverPassword(Long userId, String newPassword) {
        resetPassword(userId, newPassword, PasswordChangeSource.RECOVERY_RESET);
    }

    private void resetPassword(
            Long userId,
            String newPassword,
            PasswordChangeSource source
    ) {
        SystemUser user = requireUserForUpdate(userId);
        replacePassword(user, newPassword, source);
    }

    private void replacePassword(
            SystemUser user,
            String newPassword,
            PasswordChangeSource source
    ) {
        passwordPolicyService.requireNewPassword(newPassword);
        int historyCount = passwordPolicyService.currentHistoryCount();
        passwordHistoryService.requireNotRecentlyUsed(user.getId(), newPassword, historyCount);
        String passwordHash = passwordPolicyService.encodeNewPassword(newPassword);
        user.setPasswordHash(passwordHash);
        user.setPasswordChangedAt(Instant.now());
        userMapper.updateById(user);
        userMapper.clearLoginFailureState(user.getId());
        passwordHistoryService.record(user.getId(), passwordHash, source);
        sessionService.deleteAllByUserId(user.getId());
    }

    private SystemUser requireUserForUpdate(Long userId) {
        SystemUser user = userId == null ? null : userMapper.selectByIdForUpdate(userId);
        if (user == null) {
            throw PlatformException.notFound("用户不存在");
        }
        return user;
    }
}
