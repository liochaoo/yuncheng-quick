package com.yuncheng.framework.security.context;

import com.yuncheng.common.context.CurrentUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** 携带平台当前用户信息的 JWT 认证结果。 */
public final class CurrentUserJwtAuthenticationToken extends JwtAuthenticationToken {

    private final CurrentUser currentUser;

    public CurrentUserJwtAuthenticationToken(
            JwtAuthenticationToken source,
            CurrentUser currentUser
    ) {
        super(source.getToken(), source.getAuthorities(), source.getName());
        setDetails(source.getDetails());
        this.currentUser = currentUser;
    }

    public CurrentUser getCurrentUser() {
        return currentUser;
    }
}
