package com.yuncheng.system.login.auth.dto;

/** Web 登录结果；强制修改密码时不签发普通访问令牌。 */
public record LoginResponse(
        String accessToken,
        boolean passwordChangeRequired,
        String passwordChangeToken
) {
}
