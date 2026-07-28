package com.yuncheng.framework.security.jwt;

import java.time.Instant;

/** 一次签发得到的用户 Token。 */
public record IssuedTokens(
        String accessToken,
        String refreshToken,
        String refreshJti,
        Instant sessionExpiresAt
) {
}
