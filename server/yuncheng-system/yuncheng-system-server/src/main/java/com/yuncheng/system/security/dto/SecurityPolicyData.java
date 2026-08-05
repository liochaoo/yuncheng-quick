package com.yuncheng.system.security.dto;

/** 当前实际生效的安全策略。 */
public record SecurityPolicyData(
        Feature feature,
        Captcha captcha,
        LoginFailure loginFailure,
        Password password,
        DefaultPassword defaultPassword
) {

    public record Feature(
            boolean registrationEnabled,
            boolean passwordRecoveryEnabled,
            boolean profileEmailEnabled
    ) {
    }

    public record Captcha(
            boolean loginEnabled
    ) {
    }

    public record LoginFailure(
            int maxFailedAttempts,
            int windowMinutes,
            int lockMinutes
    ) {
    }

    public record Password(
            int minLength,
            int maxLength,
            boolean requireLowercase,
            boolean requireUppercase,
            boolean requireDigit,
            boolean requireSpecial,
            int historyCount
    ) {
    }

    public record DefaultPassword(boolean configured) {
    }
}
