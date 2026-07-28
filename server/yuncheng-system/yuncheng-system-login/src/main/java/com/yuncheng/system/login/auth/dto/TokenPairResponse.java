package com.yuncheng.system.login.auth.dto;

/** 返回给非 Cookie 客户端的 Token 组合。 */
public record TokenPairResponse(
        String accessToken,
        String refreshToken
) {
}
