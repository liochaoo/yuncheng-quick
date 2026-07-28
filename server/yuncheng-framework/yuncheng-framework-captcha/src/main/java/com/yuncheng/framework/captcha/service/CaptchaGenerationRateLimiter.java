package com.yuncheng.framework.captcha.service;

import com.yuncheng.framework.redis.config.AuthRedisProperties;
import com.yuncheng.framework.web.exception.PlatformException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Collections;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

/** 限制单个客户端 IP 创建图形验证码的频率。 */
@Service
public class CaptchaGenerationRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(CaptchaGenerationRateLimiter.class);
    private static final int LIMIT = 30;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local count = redis.call('incr', KEYS[1]); "
                    + "if count == 1 then redis.call('pexpire', KEYS[1], ARGV[1]); end; "
                    + "if count <= tonumber(ARGV[2]) then return 0; end; "
                    + "local ttl = redis.call('pttl', KEYS[1]); "
                    + "if ttl < 1 then "
                    + "redis.call('pexpire', KEYS[1], ARGV[1]); ttl = tonumber(ARGV[1]); end; "
                    + "if count == tonumber(ARGV[2]) + 1 then return ttl; end; "
                    + "return -ttl;",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final AuthRedisProperties redisProperties;

    public CaptchaGenerationRateLimiter(
            StringRedisTemplate redisTemplate,
            AuthRedisProperties redisProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.redisProperties = redisProperties;
    }

    public void requireAllowed(String ip) {
        String ipHash = sha256(normalizeIp(ip));
        Long result;
        try {
            result = redisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    Collections.singletonList(key(ipHash)),
                    Long.toString(WINDOW.toMillis()),
                    Integer.toString(LIMIT)
            );
        } catch (RuntimeException exception) {
            log.error("图形验证码生成频率校验失败，ipHash={}", ipHash, exception);
            throw PlatformException.serviceUnavailable("验证码服务暂时不可用");
        }
        if (result == null) {
            log.error("图形验证码生成频率校验未返回结果，ipHash={}", ipHash);
            throw PlatformException.serviceUnavailable("验证码服务暂时不可用");
        }
        if (result == 0) {
            return;
        }

        long retryAfterSeconds = Math.max(1, (Math.abs(result) + 999) / 1000);
        if (result > 0) {
            log.warn(
                    "图形验证码生成超过频率限制：ipHash={}，limit={}，windowSeconds={}，retryAfterSeconds={}",
                    ipHash,
                    LIMIT,
                    WINDOW.toSeconds(),
                    retryAfterSeconds
            );
        }
        throw PlatformException.tooManyRequests(
                "图形验证码获取过于频繁，请稍后再试",
                retryAfterSeconds
        );
    }

    private String key(String ipHash) {
        return redisProperties.runtimeKey("captcha-generation-rate:" + ipHash);
    }

    private String normalizeIp(String ip) {
        return ip == null || ip.isBlank() ? "unknown" : ip.trim();
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", exception);
        }
    }
}
