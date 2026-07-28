package com.yuncheng.system.login.email.constant;

import java.time.Duration;

/** 邮箱验证码运行规则。 */
public final class EmailVerificationConstants {

    public static final int CODE_LENGTH = 6;
    public static final Duration CODE_TTL = Duration.ofMinutes(5);
    public static final Duration RESEND_INTERVAL = Duration.ofSeconds(60);
    public static final int MAX_FAILURES = 5;

    private EmailVerificationConstants() {
    }
}
