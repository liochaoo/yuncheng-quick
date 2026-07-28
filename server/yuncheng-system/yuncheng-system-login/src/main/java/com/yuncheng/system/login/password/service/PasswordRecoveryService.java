package com.yuncheng.system.login.password.service;

import com.yuncheng.framework.captcha.CaptchaScene;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.api.user.SystemUserInfo;
import com.yuncheng.system.login.email.enums.EmailVerificationScene;
import com.yuncheng.system.login.email.service.EmailVerificationService;
import com.yuncheng.system.login.password.dto.PasswordEmailCodeRequest;
import com.yuncheng.system.login.password.dto.PasswordResetRequest;
import com.yuncheng.system.login.security.service.LoginSecurityService;
import com.yuncheng.system.security.service.SecurityPolicyService;
import com.yuncheng.system.user.service.UserAccountService;
import com.yuncheng.system.user.service.UserInputService;
import com.yuncheng.system.user.service.UserQueryService;
import org.springframework.stereotype.Service;

/** 编排邮箱找回密码流程。 */
@Service
public class PasswordRecoveryService {

    private final UserQueryService userQueryService;
    private final UserAccountService userAccountService;
    private final UserInputService inputService;
    private final EmailVerificationService verificationService;
    private final LoginSecurityService loginSecurityService;
    private final SecurityPolicyService securityPolicyService;

    public PasswordRecoveryService(
            UserQueryService userQueryService,
            UserAccountService userAccountService,
            UserInputService inputService,
            EmailVerificationService verificationService,
            LoginSecurityService loginSecurityService,
            SecurityPolicyService securityPolicyService
    ) {
        this.userQueryService = userQueryService;
        this.userAccountService = userAccountService;
        this.inputService = inputService;
        this.verificationService = verificationService;
        this.loginSecurityService = loginSecurityService;
        this.securityPolicyService = securityPolicyService;
    }

    public void sendEmailCode(PasswordEmailCodeRequest request) {
        securityPolicyService.requirePasswordRecoveryEnabled();
        String username = inputService.normalizeUsername(request.username());
        String email = inputService.requireEmail(request.email());
        loginSecurityService.verifyEmailCode(
                CaptchaScene.RESET_PASSWORD_EMAIL,
                request.captchaVerification()
        );
        userQueryService.findEnabledByUsernameAndEmail(username, email).ifPresent(user ->
                verificationService.send(
                        EmailVerificationScene.RESET_PASSWORD,
                        user.userId().toString(),
                        email
                )
        );
    }

    public void resetPassword(PasswordResetRequest request) {
        securityPolicyService.requirePasswordRecoveryEnabled();
        String username = inputService.normalizeUsername(request.username());
        String email = inputService.requireEmail(request.email());
        SystemUserInfo user = userQueryService.findEnabledByUsernameAndEmail(username, email)
                .orElseThrow(() -> PlatformException.badRequest("登录名或电子邮箱不正确"));
        userAccountService.requireNewPassword(request.newPassword());
        verificationService.verify(
                EmailVerificationScene.RESET_PASSWORD,
                user.userId().toString(),
                email,
                request.code()
        );
        userAccountService.recoverPassword(user.userId(), request.newPassword());
    }
}
