package com.yuncheng.system.login.security.service;

import com.yuncheng.framework.captcha.CaptchaScene;
import com.yuncheng.framework.captcha.service.CaptchaVerificationService;
import com.yuncheng.system.login.security.dto.SecurityPolicyResponse;
import com.yuncheng.system.security.dto.SecurityPolicyData;
import com.yuncheng.system.security.service.SecurityPolicyService;
import com.yuncheng.system.user.constant.PasswordPolicyConstants;
import org.springframework.stereotype.Service;

/** 认证模块安全开关和图形验证码的统一入口。 */
@Service
public class LoginSecurityService {

    private final SecurityPolicyService policyService;
    private final CaptchaVerificationService captchaVerificationService;

    public LoginSecurityService(
            SecurityPolicyService policyService,
            CaptchaVerificationService captchaVerificationService
    ) {
        this.policyService = policyService;
        this.captchaVerificationService = captchaVerificationService;
    }

    public void verifyLogin(String verification) {
        if (policyService.current().captcha().loginEnabled()) {
            captchaVerificationService.consume(CaptchaScene.LOGIN, verification);
        }
    }

    public void verifyEmailCode(CaptchaScene scene, String verification) {
        captchaVerificationService.consume(scene, verification);
    }

    public SecurityPolicyResponse policy() {
        SecurityPolicyData policy = policyService.current();
        SecurityPolicyData.Password password = policy.password();
        return new SecurityPolicyResponse(
                new SecurityPolicyResponse.Feature(
                        policy.feature().registrationEnabled(),
                        policy.feature().passwordRecoveryEnabled(),
                        policy.feature().profileEmailEnabled()
                ),
                new SecurityPolicyResponse.Captcha(
                        policy.captcha().loginEnabled()
                ),
                new SecurityPolicyResponse.Password(
                        password.minLength(),
                        password.maxLength(),
                        PasswordPolicyConstants.BCRYPT_MAX_UTF8_BYTES,
                        password.requireLowercase(),
                        password.requireUppercase(),
                        password.requireDigit(),
                        password.requireSpecial(),
                        password.historyCount(),
                        policyService.passwordRuleText(password)
                )
        );
    }
}
