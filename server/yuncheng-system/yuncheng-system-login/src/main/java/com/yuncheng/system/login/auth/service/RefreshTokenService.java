package com.yuncheng.system.login.auth.service;

import com.yuncheng.framework.security.jwt.IssuedTokens;
import com.yuncheng.framework.security.jwt.JwtTokenService;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.login.auth.dto.AuthenticatedTokens;
import com.yuncheng.system.login.auth.enums.ClientType;
import com.yuncheng.system.session.model.LoginSession;
import com.yuncheng.system.session.model.RefreshTokenReplay;
import com.yuncheng.system.session.service.LoginSessionService;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

/** 处理 Refresh Token 校验和幂等轮换。 */
@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final JwtTokenService jwtTokenService;
    private final LoginSessionService sessionService;

    public RefreshTokenService(
            JwtTokenService jwtTokenService,
            LoginSessionService sessionService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.sessionService = sessionService;
    }

    public AuthenticatedTokens refresh(
            String refreshToken,
            Set<ClientType> allowedClientTypes
    ) {
        try {
            return doRefresh(refreshToken, allowedClientTypes);
        } catch (PlatformException exception) {
            throw exception;
        } catch (DataAccessException | IllegalStateException exception) {
            log.error("Refresh Token 轮换失败", exception);
            throw PlatformException.serviceUnavailable("认证服务暂时不可用");
        }
    }

    private AuthenticatedTokens doRefresh(
            String refreshToken,
            Set<ClientType> allowedClientTypes
    ) {
        Jwt jwt = decodeRefreshToken(refreshToken);
        Long userId = parseUserId(jwt);
        String oldJti = jwt.getId();
        String sessionId = jwt.getClaimAsString(JwtTokenService.SESSION_ID_CLAIM);
        String clientType = jwt.getClaimAsString(JwtTokenService.CLIENT_TYPE_CLAIM);
        if (oldJti == null
                || oldJti.isBlank()
                || !isAllowedClientType(clientType, allowedClientTypes)) {
            throw invalidRefreshToken();
        }

        RefreshTokenReplay replay = sessionService.findValidRefreshReplay(oldJti);
        if (replay != null) {
            return requireMatchingReplay(replay, sessionId, userId, clientType);
        }

        LoginSession session = sessionService.findByRefreshJti(oldJti);
        if (!matchesCurrentSession(session, oldJti, sessionId, userId, clientType)) {
            throw invalidRefreshToken();
        }
        IssuedTokens issuedTokens = jwtTokenService.issueForRefresh(
                session.userId(),
                session.sessionId(),
                session.clientType(),
                session.expiresAt()
        );
        RefreshTokenReplay firstResult = new RefreshTokenReplay(
                session.sessionId(),
                session.userId(),
                session.clientType(),
                issuedTokens.refreshJti(),
                issuedTokens.accessToken(),
                issuedTokens.refreshToken(),
                issuedTokens.sessionExpiresAt()
        );
        RefreshTokenReplay result = sessionService.rotateRefresh(
                oldJti,
                issuedTokens.refreshJti(),
                session,
                firstResult
        );
        if (result == null) {
            throw invalidRefreshToken();
        }
        return requireMatchingReplay(result, sessionId, userId, clientType);
    }

    private boolean matchesCurrentSession(
            LoginSession session,
            String oldJti,
            String sessionId,
            Long userId,
            String clientType
    ) {
        return session != null
                && Objects.equals(session.refreshJti(), oldJti)
                && Objects.equals(session.sessionId(), sessionId)
                && Objects.equals(session.userId(), userId)
                && Objects.equals(session.clientType(), clientType)
                && session.expiresAt().isAfter(Instant.now());
    }

    private AuthenticatedTokens requireMatchingReplay(
            RefreshTokenReplay replay,
            String sessionId,
            Long userId,
            String clientType
    ) {
        if (!Objects.equals(replay.sessionId(), sessionId)
                || !Objects.equals(replay.userId(), userId)
                || !Objects.equals(replay.clientType(), clientType)
                || replay.sessionExpiresAt() == null
                || !replay.sessionExpiresAt().isAfter(Instant.now())) {
            throw invalidRefreshToken();
        }
        return toAuthenticatedTokens(replay);
    }

    private Jwt decodeRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw invalidRefreshToken();
        }
        try {
            return jwtTokenService.decodeRefreshToken(refreshToken);
        } catch (JwtException exception) {
            throw invalidRefreshToken();
        }
    }

    private Long parseUserId(Jwt jwt) {
        try {
            Long userId = Long.valueOf(jwt.getSubject());
            if (userId <= 0) {
                throw new NumberFormatException("用户标识必须为正数");
            }
            return userId;
        } catch (NumberFormatException exception) {
            throw invalidRefreshToken();
        }
    }

    private boolean isAllowedClientType(String clientType, Set<ClientType> allowedClientTypes) {
        return allowedClientTypes.stream().anyMatch(type -> type.name().equals(clientType));
    }

    private AuthenticatedTokens toAuthenticatedTokens(RefreshTokenReplay replay) {
        return new AuthenticatedTokens(
                replay.accessToken(),
                replay.refreshToken(),
                replay.sessionExpiresAt()
        );
    }

    private PlatformException invalidRefreshToken() {
        return PlatformException.unauthorized("刷新凭据无效或已过期");
    }
}
