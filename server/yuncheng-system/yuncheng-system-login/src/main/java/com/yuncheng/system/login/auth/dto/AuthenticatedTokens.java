package com.yuncheng.system.login.auth.dto;

import java.time.Instant;

/** 登录或刷新成功后的内部 Token 结果。 */
public record AuthenticatedTokens(
        String accessToken,
        String refreshToken,
        Instant sessionExpiresAt
) {
}
