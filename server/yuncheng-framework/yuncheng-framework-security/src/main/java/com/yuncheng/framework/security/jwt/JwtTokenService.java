package com.yuncheng.framework.security.jwt;

import com.yuncheng.framework.security.config.JwtProperties;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

/** 签发和解析用户 JWT。 */
@Component
public class JwtTokenService {

    public static final String SESSION_ID_CLAIM = "sid";
    public static final String CLIENT_TYPE_CLAIM = "client_type";
    public static final String PASSWORD_CHANGED_AT_CLAIM = "password_changed_at";
    public static final String PASSWORD_CHANGE_REQUIRED_CLAIM = "password_change_required";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder refreshJwtDecoder;
    private final JwtDecoder passwordChangeJwtDecoder;
    private final JwtProperties properties;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            @Qualifier("refreshJwtDecoder") JwtDecoder refreshJwtDecoder,
            @Qualifier("passwordChangeJwtDecoder") JwtDecoder passwordChangeJwtDecoder,
            JwtProperties properties
    ) {
        this.jwtEncoder = jwtEncoder;
        this.refreshJwtDecoder = refreshJwtDecoder;
        this.passwordChangeJwtDecoder = passwordChangeJwtDecoder;
        this.properties = properties;
    }

    public IssuedTokens issueForLogin(Long userId, String sessionId, String clientType) {
        Instant sessionExpiresAt = Instant.now().plus(properties.getRefreshTokenTtl());
        return issue(userId, sessionId, clientType, sessionExpiresAt);
    }

    public IssuedTokens issueForRefresh(
            Long userId,
            String sessionId,
            String clientType,
            Instant sessionExpiresAt
    ) {
        return issue(userId, sessionId, clientType, sessionExpiresAt);
    }

    public Jwt decodeRefreshToken(String token) {
        return refreshJwtDecoder.decode(token);
    }

    public String issuePasswordChangeToken(
            Long userId,
            String clientType,
            Instant passwordChangedAt
    ) {
        Instant now = Instant.now();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .type(JwtTokenTypes.PASSWORD_CHANGE)
                .build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .audience(List.of(properties.getAudience()))
                .subject(userId.toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .notBefore(now)
                .expiresAt(now.plus(properties.getPasswordChangeTokenTtl()))
                .claim(CLIENT_TYPE_CLAIM, clientType)
                .claim(PASSWORD_CHANGED_AT_CLAIM, passwordChangedAt.toEpochMilli())
                .claim(PASSWORD_CHANGE_REQUIRED_CLAIM, true)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public Jwt decodePasswordChangeToken(String token) {
        return passwordChangeJwtDecoder.decode(token);
    }

    private IssuedTokens issue(
            Long userId,
            String sessionId,
            String clientType,
            Instant sessionExpiresAt
    ) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plus(properties.getAccessTokenTtl());
        if (accessExpiresAt.isAfter(sessionExpiresAt)) {
            accessExpiresAt = sessionExpiresAt;
        }
        String refreshJti = UUID.randomUUID().toString();
        String accessToken = encode(
                JwtTokenTypes.ACCESS,
                userId,
                sessionId,
                UUID.randomUUID().toString(),
                clientType,
                now,
                accessExpiresAt
        );
        String refreshToken = encode(
                JwtTokenTypes.REFRESH,
                userId,
                sessionId,
                refreshJti,
                clientType,
                now,
                sessionExpiresAt
        );
        return new IssuedTokens(accessToken, refreshToken, refreshJti, sessionExpiresAt);
    }

    private String encode(
            String tokenType,
            Long userId,
            String sessionId,
            String tokenId,
            String clientType,
            Instant issuedAt,
            Instant expiresAt
    ) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .type(tokenType)
                .build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .audience(List.of(properties.getAudience()))
                .subject(userId.toString())
                .id(tokenId)
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(expiresAt)
                .claim(SESSION_ID_CLAIM, sessionId)
                .claim(CLIENT_TYPE_CLAIM, clientType)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
