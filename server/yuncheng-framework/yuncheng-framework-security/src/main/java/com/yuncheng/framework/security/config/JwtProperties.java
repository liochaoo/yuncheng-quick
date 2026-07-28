package com.yuncheng.framework.security.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** JWT 配置。 */
@ConfigurationProperties(prefix = "platform.auth.jwt")
public class JwtProperties {

    private String secret = "";
    private String issuer = "yuncheng-quick";
    private String audience = "yuncheng-quick-server";
    private Duration accessTokenTtl = Duration.ofMinutes(30);
    private Duration refreshTokenTtl = Duration.ofDays(7);
    private Duration refreshTokenOverlap = Duration.ofSeconds(30);

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Duration refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public Duration getRefreshTokenOverlap() {
        return refreshTokenOverlap;
    }

    public void setRefreshTokenOverlap(Duration refreshTokenOverlap) {
        this.refreshTokenOverlap = refreshTokenOverlap;
    }
}
