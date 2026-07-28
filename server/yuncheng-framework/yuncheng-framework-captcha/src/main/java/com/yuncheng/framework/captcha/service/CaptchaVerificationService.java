package com.yuncheng.framework.captcha.service;

import com.yuncheng.framework.captcha.CaptchaScene;
import com.yuncheng.framework.captcha.model.CaptchaVerificationState;
import com.yuncheng.framework.redis.config.AuthRedisProperties;
import com.yuncheng.framework.redis.store.RedisJsonStore;
import com.yuncheng.framework.web.exception.PlatformException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** 签发并消费与业务场景绑定的一次性图形验证码凭据。 */
@Service
public class CaptchaVerificationService {

    private static final Duration VERIFICATION_TTL = Duration.ofMinutes(2);

    private final RedisJsonStore redisJsonStore;
    private final AuthRedisProperties redisProperties;

    public CaptchaVerificationService(
            RedisJsonStore redisJsonStore,
            AuthRedisProperties redisProperties
    ) {
        this.redisJsonStore = redisJsonStore;
        this.redisProperties = redisProperties;
    }

    public String issue(CaptchaScene scene) {
        String verification = UUID.randomUUID().toString().replace("-", "");
        redisJsonStore.set(key(verification), new CaptchaVerificationState(scene), VERIFICATION_TTL);
        return verification;
    }

    public void consume(CaptchaScene scene, String verification) {
        if (verification == null || verification.isBlank()) {
            throw PlatformException.badRequest("请先完成图形验证码校验");
        }
        CaptchaVerificationState state = redisJsonStore.getAndDelete(
                key(verification.trim()),
                CaptchaVerificationState.class
        );
        if (state == null || state.scene() != scene) {
            throw PlatformException.badRequest("图形验证码已失效，请重新验证");
        }
    }

    private String key(String verification) {
        return redisProperties.runtimeKey("captcha:verification:" + sha256(verification));
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
