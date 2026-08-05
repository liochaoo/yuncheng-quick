package com.yuncheng.system.login.auth.service;

import com.yuncheng.framework.security.jwt.IssuedTokens;
import com.yuncheng.framework.security.jwt.JwtTokenService;
import com.yuncheng.framework.web.client.ClientRequestInfo;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.login.auth.dto.AuthenticatedTokens;
import com.yuncheng.system.login.auth.dto.LoginAuthenticationResult;
import com.yuncheng.system.login.auth.dto.LoginRequest;
import com.yuncheng.system.login.auth.enums.ClientType;
import com.yuncheng.system.permission.cache.UserAccessCacheService;
import com.yuncheng.system.session.model.LoginSession;
import com.yuncheng.system.session.service.LoginSessionService;
import com.yuncheng.system.security.service.SecurityPolicyService;
import com.yuncheng.system.user.entity.SystemUser;
import com.yuncheng.system.user.service.PasswordPolicyService;
import com.yuncheng.system.user.service.UserLoginQueryService;
import com.yuncheng.system.user.service.UserLoginSecurityService;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

/** 编排用户登录、Refresh Token 轮换和退出。 */
@Service
public class AuthenticationService {

    private static final String LOGIN_FAILURE_MESSAGE = "用户名或密码错误";
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserLoginQueryService userLoginQueryService;
    private final UserAccessCacheService userAccessCacheService;
    private final JwtTokenService jwtTokenService;
    private final LoginSessionService sessionService;
    private final PasswordPolicyService passwordPolicyService;
    private final UserLoginSecurityService userLoginSecurityService;
    private final SecurityPolicyService securityPolicyService;
    private final RefreshTokenService refreshTokenService;
    private final LoginLogRecorder loginLogRecorder;

    public AuthenticationService(
            UserLoginQueryService userLoginQueryService,
            UserAccessCacheService userAccessCacheService,
            JwtTokenService jwtTokenService,
            LoginSessionService sessionService,
            PasswordPolicyService passwordPolicyService,
            UserLoginSecurityService userLoginSecurityService,
            SecurityPolicyService securityPolicyService,
            RefreshTokenService refreshTokenService,
            LoginLogRecorder loginLogRecorder
    ) {
        this.userLoginQueryService = userLoginQueryService;
        this.userAccessCacheService = userAccessCacheService;
        this.jwtTokenService = jwtTokenService;
        this.sessionService = sessionService;
        this.passwordPolicyService = passwordPolicyService;
        this.userLoginSecurityService = userLoginSecurityService;
        this.securityPolicyService = securityPolicyService;
        this.refreshTokenService = refreshTokenService;
        this.loginLogRecorder = loginLogRecorder;
    }

    public LoginAuthenticationResult login(
            LoginRequest request,
            Set<ClientType> allowedClientTypes,
            ClientRequestInfo requestInfo
    ) {
        String clientTypeValue = request.clientType() == null ? null : request.clientType().name();
        String failureReason = null;
        try {
            ClientType clientType = requireAllowedClientType(request.clientType(), allowedClientTypes);
            passwordPolicyService.requireLoginInput(request.password());
            SystemUser user = userLoginQueryService.findByUsername(request.username());
            if (user == null) {
                failureReason = "用户不存在";
                throw PlatformException.unauthorized(LOGIN_FAILURE_MESSAGE);
            }
            if (!Boolean.TRUE.equals(user.getEnabled())) {
                failureReason = "账号已停用";
                throw PlatformException.unauthorized(LOGIN_FAILURE_MESSAGE);
            }
            if (userLoginSecurityService.isLocked(user, Instant.now())) {
                failureReason = "账号处于临时锁定状态";
                throw PlatformException.unauthorized(LOGIN_FAILURE_MESSAGE);
            }
            String verifiedPasswordHash = user.getPasswordHash();
            if (!passwordPolicyService.matches(request.password(), verifiedPasswordHash)) {
                boolean loginLocked = userLoginSecurityService.recordPasswordFailure(
                        user.getId(),
                        verifiedPasswordHash,
                        securityPolicyService.current().loginFailure()
                );
                failureReason = loginLocked ? "密码错误，账号已临时锁定" : "密码错误";
                throw PlatformException.unauthorized(LOGIN_FAILURE_MESSAGE);
            }
            user = userLoginSecurityService.completePasswordSuccess(
                    user.getId(),
                    verifiedPasswordHash
            );
            if (Boolean.TRUE.equals(user.getPasswordChangeRequired())) {
                return LoginAuthenticationResult.passwordChangeRequired(
                        jwtTokenService.issuePasswordChangeToken(
                                user.getId(),
                                clientType.name(),
                                user.getPasswordChangedAt()
                        )
                );
            }
            clearUserAccessCache(user.getId());
            String sessionId = UUID.randomUUID().toString();
            IssuedTokens issuedTokens = jwtTokenService.issueForLogin(
                    user.getId(),
                    sessionId,
                    clientType.name()
            );
            LoginSession session = new LoginSession(
                    sessionId,
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    clientType.name(),
                    requestInfo == null ? null : requestInfo.ip(),
                    requestInfo == null ? null : requestInfo.userAgent(),
                    Instant.now(),
                    issuedTokens.sessionExpiresAt(),
                    issuedTokens.refreshJti()
            );
            sessionService.create(session);
            loginLogRecorder.loginSuccess(
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    clientType.name(),
                    sessionId,
                    requestInfo
            );
            return LoginAuthenticationResult.authenticated(toAuthenticatedTokens(issuedTokens));
        } catch (RuntimeException exception) {
            loginLogRecorder.loginFailure(
                    request.username(),
                    clientTypeValue,
                    requestInfo,
                    failureReason == null ? exception.getMessage() : failureReason
            );
            throw exception;
        }
    }

    public AuthenticatedTokens refresh(String refreshToken, Set<ClientType> allowedClientTypes) {
        return refreshTokenService.refresh(refreshToken, allowedClientTypes);
    }

    public void logout(
            String refreshToken,
            Set<ClientType> allowedClientTypes,
            ClientRequestInfo requestInfo
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        try {
            Jwt jwt = jwtTokenService.decodeRefreshToken(refreshToken);
            String clientType = jwt.getClaimAsString(JwtTokenService.CLIENT_TYPE_CLAIM);
            if (!isAllowedClientType(clientType, allowedClientTypes)) {
                return;
            }
            String sessionId = jwt.getClaimAsString(JwtTokenService.SESSION_ID_CLAIM);
            LoginSession session = sessionService.findSession(sessionId);
            sessionService.deleteSession(sessionId, jwt.getId());
            if (session != null) {
                loginLogRecorder.logout(
                        session.userId(),
                        session.username(),
                        session.realName(),
                        session.clientType(),
                        session.sessionId(),
                        requestInfo
                );
            }
        } catch (JwtException ignored) {
            // 退出接口保持幂等，失效 Token 不影响客户端清理本地状态。
        }
    }

    private void clearUserAccessCache(Long userId) {
        try {
            userAccessCacheService.clearAfterLogin(userId);
        } catch (RuntimeException exception) {
            log.error("登录时清理用户访问缓存失败，userId={}", userId, exception);
            throw PlatformException.serviceUnavailable("认证服务暂时不可用");
        }
    }

    private AuthenticatedTokens toAuthenticatedTokens(IssuedTokens issuedTokens) {
        return new AuthenticatedTokens(
                issuedTokens.accessToken(),
                issuedTokens.refreshToken(),
                issuedTokens.sessionExpiresAt()
        );
    }

    private ClientType requireAllowedClientType(
            ClientType clientType,
            Set<ClientType> allowedClientTypes
    ) {
        if (clientType == null || !allowedClientTypes.contains(clientType)) {
            throw unsupportedClientType(allowedClientTypes);
        }
        return clientType;
    }

    private boolean isAllowedClientType(String clientType, Set<ClientType> allowedClientTypes) {
        return allowedClientTypes.stream().anyMatch(type -> type.name().equals(clientType));
    }

    private PlatformException unsupportedClientType(Set<ClientType> allowedClientTypes) {
        String supportedClientTypes = allowedClientTypes.stream()
                .sorted()
                .map(ClientType::name)
                .collect(Collectors.joining("、"));
        return PlatformException.badRequest("当前接口仅支持 " + supportedClientTypes + " 客户端");
    }
}
