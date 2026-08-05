package com.yuncheng.framework.security.jwt;

import com.yuncheng.framework.security.config.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTypeValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/** 创建 JWT 编码器、解码器并校验签名密钥。 */
@Configuration(proxyBeanMethods = false)
public class JwtSecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JwtSecurityConfiguration.class);

    @Bean
    SecretKey jwtSecretKey(JwtProperties properties) {
        validateProperties(properties);
        byte[] secret = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(secret, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey secretKey) {
        return NimbusJwtEncoder.withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey secretKey, JwtProperties properties) {
        NimbusJwtDecoder decoder = createDecoder(secretKey, properties, JwtTokenTypes.ACCESS);
        return decoder;
    }

    @Bean("refreshJwtDecoder")
    JwtDecoder refreshJwtDecoder(SecretKey secretKey, JwtProperties properties) {
        return createDecoder(secretKey, properties, JwtTokenTypes.REFRESH);
    }

    @Bean("passwordChangeJwtDecoder")
    JwtDecoder passwordChangeJwtDecoder(SecretKey secretKey, JwtProperties properties) {
        return createDecoder(secretKey, properties, JwtTokenTypes.PASSWORD_CHANGE);
    }

    private NimbusJwtDecoder createDecoder(
            SecretKey secretKey,
            JwtProperties properties,
            String tokenType
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .validateType(false)
                .build();
        OAuth2TokenValidator<Jwt> issuerValidator = new JwtIssuerValidator(properties.getIssuer());
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(properties.getAudience())
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "JWT 接收方不正确", null));
        OAuth2TokenValidator<Jwt> typeValidator = new JwtTypeValidator(tokenType);
        decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(
                List.of(issuerValidator, audienceValidator, typeValidator)
        ));
        return decoder;
    }

    private void validateProperties(JwtProperties properties) {
        requireText(properties.getSecret(), "JWT 签名密钥不能为空");
        if (properties.getSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            fail("JWT 签名密钥长度不能少于 32 字节");
        }
        requireText(properties.getIssuer(), "JWT 签发者不能为空");
        requireText(properties.getAudience(), "JWT 接收方不能为空");
        requirePositive(properties.getAccessTokenTtl(), "Access Token 有效期必须大于 0");
        requirePositive(properties.getRefreshTokenTtl(), "Refresh Token 有效期必须大于 0");
        requirePositive(properties.getRefreshTokenOverlap(), "Refresh Token 重叠时间必须大于 0");
        requirePositive(properties.getPasswordChangeTokenTtl(), "强制修改密码 Token 有效期必须大于 0");
        if (properties.getAccessTokenTtl().compareTo(properties.getRefreshTokenTtl()) >= 0) {
            fail("Access Token 有效期必须小于 Refresh Token 有效期");
        }
        if (properties.getRefreshTokenOverlap().compareTo(properties.getRefreshTokenTtl()) >= 0) {
            fail("Refresh Token 重叠时间必须小于 Refresh Token 有效期");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            fail(message);
        }
    }

    private void requirePositive(Duration value, String message) {
        if (value == null || value.isZero() || value.isNegative()) {
            fail(message);
        }
    }

    private void fail(String message) {
        log.error("JWT 配置错误：{}，平台无法启动", message);
        throw new IllegalStateException(message);
    }
}
