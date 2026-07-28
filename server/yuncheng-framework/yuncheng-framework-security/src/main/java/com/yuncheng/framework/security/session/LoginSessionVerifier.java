package com.yuncheng.framework.security.session;

/** 验证 JWT 指向的登录会话是否仍然有效。 */
public interface LoginSessionVerifier {

    boolean isActive(String sessionId, Long userId);
}
