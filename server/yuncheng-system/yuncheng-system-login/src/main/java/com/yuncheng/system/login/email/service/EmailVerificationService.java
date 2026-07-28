package com.yuncheng.system.login.email.service;

import com.yuncheng.common.util.DataMaskingUtils;
import com.yuncheng.framework.mail.config.PlatformMailProperties;
import com.yuncheng.framework.mail.exception.MailDeliveryException;
import com.yuncheng.framework.mail.service.MailSenderService;
import com.yuncheng.framework.redis.config.AuthRedisProperties;
import com.yuncheng.framework.redis.store.RedisJsonStore;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.login.email.constant.EmailVerificationConstants;
import com.yuncheng.system.login.email.enums.EmailVerificationScene;
import com.yuncheng.system.login.email.record.EmailVerificationState;
import com.yuncheng.system.user.service.UserInputService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 发送并校验带场景隔离的邮箱验证码。 */
@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RedisJsonStore redisJsonStore;
    private final AuthRedisProperties redisProperties;
    private final MailSenderService mailSenderService;
    private final PlatformMailProperties mailProperties;
    private final UserInputService inputService;

    public EmailVerificationService(
            RedisJsonStore redisJsonStore,
            AuthRedisProperties redisProperties,
            MailSenderService mailSenderService,
            PlatformMailProperties mailProperties,
            UserInputService inputService
    ) {
        this.redisJsonStore = redisJsonStore;
        this.redisProperties = redisProperties;
        this.mailSenderService = mailSenderService;
        this.mailProperties = mailProperties;
        this.inputService = inputService;
    }

    public void send(EmailVerificationScene scene, String owner, String email) {
        String normalizedEmail = inputService.requireEmail(email);
        String key = key(scene, owner, normalizedEmail);
        Instant now = Instant.now();
        EmailVerificationState existing = redisJsonStore.get(key, EmailVerificationState.class);
        if (existing != null && existing.sentAt()
                .plus(EmailVerificationConstants.RESEND_INTERVAL)
                .isAfter(now)) {
            throw PlatformException.badRequest("验证码发送过于频繁，请稍后再试");
        }
        String code = generateCode();
        Instant expiresAt = now.plus(EmailVerificationConstants.CODE_TTL);
        try {
            mailSenderService.sendHtml(
                    normalizedEmail,
                    mailProperties.getFromName() + "邮箱验证码",
                    content(scene, code)
            );
        } catch (MailDeliveryException exception) {
            log.error("邮箱验证码发送失败：场景={}，收件人={}",
                    scene, DataMaskingUtils.maskEmail(normalizedEmail), exception);
            throw PlatformException.serviceUnavailable("邮件发送失败，请稍后再试");
        }
        redisJsonStore.set(
                key,
                new EmailVerificationState(hash(code), 0, now, expiresAt),
                EmailVerificationConstants.CODE_TTL
        );
        log.info("邮箱验证码已发送：场景={}，收件人={}",
                scene, DataMaskingUtils.maskEmail(normalizedEmail));
    }

    public void verify(
            EmailVerificationScene scene,
            String owner,
            String email,
            String code
    ) {
        String normalizedEmail = inputService.requireEmail(email);
        String key = key(scene, owner, normalizedEmail);
        EmailVerificationState state = redisJsonStore.get(key, EmailVerificationState.class);
        Instant now = Instant.now();
        if (state == null || !state.expiresAt().isAfter(now)) {
            redisJsonStore.delete(key);
            throw PlatformException.badRequest("验证码已失效，请重新获取");
        }
        if (code != null && MessageDigest.isEqual(
                state.codeHash().getBytes(StandardCharsets.UTF_8),
                hash(code.trim()).getBytes(StandardCharsets.UTF_8)
        )) {
            redisJsonStore.delete(key);
            return;
        }
        int failureCount = state.failureCount() + 1;
        if (failureCount >= EmailVerificationConstants.MAX_FAILURES) {
            redisJsonStore.delete(key);
            throw PlatformException.badRequest("验证码错误次数过多，请重新获取");
        }
        Duration remaining = Duration.between(now, state.expiresAt());
        redisJsonStore.set(
                key,
                new EmailVerificationState(
                        state.codeHash(), failureCount, state.sentAt(), state.expiresAt()
                ),
                remaining
        );
        throw PlatformException.badRequest("验证码不正确");
    }

    private String key(EmailVerificationScene scene, String owner, String email) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("邮箱验证码所有者不能为空");
        }
        return redisProperties.runtimeKey(
                "email-verification:" + scene.getKey() + ":" + owner + ":" + email
        );
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, EmailVerificationConstants.CODE_LENGTH);
        return String.format("%0" + EmailVerificationConstants.CODE_LENGTH + "d", RANDOM.nextInt(bound));
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", exception);
        }
    }

    private String content(EmailVerificationScene scene, String code) {
        return "<p>您正在进行" + scene.getAction() + "操作。</p>"
                + "<p>验证码：<strong>" + code + "</strong></p>"
                + "<p>验证码 5 分钟内有效，请勿转发给他人。</p>";
    }
}
