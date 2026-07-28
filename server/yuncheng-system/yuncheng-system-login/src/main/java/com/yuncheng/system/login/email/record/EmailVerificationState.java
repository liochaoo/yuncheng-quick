package com.yuncheng.system.login.email.record;

import java.time.Instant;

/** Redis 中保存的邮箱验证码状态。 */
public record EmailVerificationState(
        String codeHash,
        int failureCount,
        Instant sentAt,
        Instant expiresAt
) {
}
