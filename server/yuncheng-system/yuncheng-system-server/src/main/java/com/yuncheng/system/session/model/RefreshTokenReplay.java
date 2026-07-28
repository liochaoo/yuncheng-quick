package com.yuncheng.system.session.model;

import java.time.Instant;

/** Refresh Token 重叠期内可重复返回的首次轮换结果。 */
public record RefreshTokenReplay(
        String sessionId,
        Long userId,
        String clientType,
        String refreshJti,
        String accessToken,
        String refreshToken,
        Instant sessionExpiresAt
) {
}
