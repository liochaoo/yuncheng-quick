package com.yuncheng.system.user.service;

import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.security.dto.SecurityPolicyData;
import com.yuncheng.system.user.entity.SystemUser;
import com.yuncheng.system.user.mapper.SystemUserMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 原子维护账号登录失败窗口和临时锁定状态。 */
@Service
public class UserLoginSecurityService {

    private static final String LOGIN_FAILURE_MESSAGE = "用户名或密码错误";

    private final SystemUserMapper userMapper;

    public UserLoginSecurityService(SystemUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public boolean isLocked(SystemUser user, Instant now) {
        return user.getLoginLockedUntil() != null && user.getLoginLockedUntil().isAfter(now);
    }

    @Transactional
    public boolean recordPasswordFailure(
            Long userId,
            String verifiedPasswordHash,
            SecurityPolicyData.LoginFailure policy
    ) {
        SystemUser user = userMapper.selectByIdForUpdate(userId);
        Instant now = Instant.now();
        if (user == null
                || !Boolean.TRUE.equals(user.getEnabled())
                || !Objects.equals(user.getPasswordHash(), verifiedPasswordHash)) {
            return false;
        }
        if (isLocked(user, now)) {
            return true;
        }

        boolean previousLockExpired = user.getLoginLockedUntil() != null;
        Instant windowStartedAt = user.getLoginFailureWindowStartedAt();
        int failedCount;
        if (previousLockExpired
                || windowStartedAt == null
                || !windowStartedAt.plus(policy.windowMinutes(), ChronoUnit.MINUTES).isAfter(now)) {
            windowStartedAt = now;
            failedCount = 1;
        } else {
            failedCount = Math.min(
                    valueOrZero(user.getLoginFailedCount()) + 1,
                    policy.maxFailedAttempts()
            );
        }
        Instant lockedUntil = failedCount >= policy.maxFailedAttempts()
                ? now.plus(policy.lockMinutes(), ChronoUnit.MINUTES)
                : null;
        userMapper.updateLoginFailureState(userId, failedCount, windowStartedAt, lockedUntil);
        return lockedUntil != null;
    }

    @Transactional
    public SystemUser completePasswordSuccess(Long userId, String verifiedPasswordHash) {
        SystemUser user = userMapper.selectByIdForUpdate(userId);
        Instant now = Instant.now();
        if (user == null
                || !Boolean.TRUE.equals(user.getEnabled())
                || isLocked(user, now)
                || !Objects.equals(user.getPasswordHash(), verifiedPasswordHash)) {
            throw PlatformException.unauthorized(LOGIN_FAILURE_MESSAGE);
        }
        userMapper.clearLoginFailureState(userId);
        user.setLoginFailedCount(0);
        user.setLoginFailureWindowStartedAt(null);
        user.setLoginLockedUntil(null);
        return user;
    }

    @Transactional
    public void unlock(Long userId) {
        if (userMapper.selectByIdForUpdate(userId) == null) {
            throw PlatformException.notFound("用户不存在");
        }
        userMapper.clearLoginFailureState(userId);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
