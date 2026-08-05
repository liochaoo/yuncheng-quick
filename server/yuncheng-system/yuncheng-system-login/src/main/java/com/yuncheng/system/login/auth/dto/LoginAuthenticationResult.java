package com.yuncheng.system.login.auth.dto;

/** 登录认证内部结果。 */
public record LoginAuthenticationResult(
        AuthenticatedTokens tokens,
        String passwordChangeToken
) {

    public static LoginAuthenticationResult authenticated(AuthenticatedTokens tokens) {
        return new LoginAuthenticationResult(tokens, null);
    }

    public static LoginAuthenticationResult passwordChangeRequired(String token) {
        return new LoginAuthenticationResult(null, token);
    }

    public boolean requiresPasswordChange() {
        return passwordChangeToken != null;
    }
}
