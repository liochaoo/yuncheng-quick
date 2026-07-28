package com.yuncheng.framework.security.session;

import com.yuncheng.common.context.CurrentUser;
import com.yuncheng.framework.security.context.CurrentUserInfoProvider;
import com.yuncheng.framework.security.context.CurrentUserJwtAuthenticationToken;
import com.yuncheng.framework.security.jwt.JwtTokenService;
import com.yuncheng.framework.security.web.SecurityResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** 在 JWT 验签通过后校验 Redis 登录会话。 */
@Component
public class JwtSessionAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtSessionAuthenticationFilter.class);

    private final LoginSessionVerifier sessionVerifier;
    private final ObjectProvider<CurrentUserInfoProvider> currentUserInfoProvider;
    private final SecurityResponseWriter responseWriter;

    public JwtSessionAuthenticationFilter(
            LoginSessionVerifier sessionVerifier,
            ObjectProvider<CurrentUserInfoProvider> currentUserInfoProvider,
            SecurityResponseWriter responseWriter
    ) {
        this.sessionVerifier = sessionVerifier;
        this.currentUserInfoProvider = currentUserInfoProvider;
        this.responseWriter = responseWriter;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication) || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }
        String subject = jwtAuthentication.getToken().getSubject();
        Long userId;
        try {
            if (subject == null || subject.isBlank()) {
                throw new NumberFormatException("用户标识不能为空");
            }
            userId = Long.valueOf(subject);
            if (userId <= 0) {
                throw new NumberFormatException("用户标识必须为正数");
            }
        } catch (NumberFormatException exception) {
            SecurityContextHolder.clearContext();
            responseWriter.write(response, 401, "登录凭据无效或已过期");
            return;
        }
        String sessionId = jwtAuthentication.getToken().getClaimAsString(JwtTokenService.SESSION_ID_CLAIM);
        try {
            if (sessionId == null || !sessionVerifier.isActive(sessionId, userId)) {
                SecurityContextHolder.clearContext();
                responseWriter.write(response, 401, "登录会话无效或已过期");
                return;
            }
            CurrentUser currentUser = currentUserInfoProvider.getObject()
                    .loadForAuthentication(userId);
            if (currentUser == null || !userId.equals(currentUser.userId())) {
                SecurityContextHolder.clearContext();
                responseWriter.write(response, 401, "登录会话无效或已过期");
                return;
            }
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new CurrentUserJwtAuthenticationToken(jwtAuthentication, currentUser));
            SecurityContextHolder.setContext(context);
        } catch (RuntimeException exception) {
            log.error("建立当前登录用户上下文失败，userId={}", userId, exception);
            SecurityContextHolder.clearContext();
            responseWriter.write(response, 503, "认证服务暂时不可用");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
