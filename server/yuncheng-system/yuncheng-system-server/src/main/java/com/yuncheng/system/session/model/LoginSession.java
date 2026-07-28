package com.yuncheng.system.session.model;

import java.time.Instant;

/** Redis 中保存的登录会话。 */
public record LoginSession(
        String sessionId,
        Long userId,
        String username,
        String realName,
        String clientType,
        String loginIp,
        String userAgent,
        Instant createdAt,
        Instant expiresAt,
        String refreshJti
) {
}
