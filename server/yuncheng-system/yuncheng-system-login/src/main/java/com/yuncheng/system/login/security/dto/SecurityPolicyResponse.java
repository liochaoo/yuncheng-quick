package com.yuncheng.system.login.security.dto;

/** 前端可使用的安全策略。 */
public record SecurityPolicyResponse(
        Feature feature,
        Captcha captcha,
        Password password
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

    public record Password(
            int minLength,
            int maxLength,
            int maxUtf8Bytes,
            boolean requireLowercase,
            boolean requireUppercase,
            boolean requireDigit,
            boolean requireSpecial,
            int historyCount,
            String ruleText
    ) {
    }
}
