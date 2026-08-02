package com.yuncheng.system.login.registration.service;

import com.yuncheng.common.constant.SystemRoleCodes;
import com.yuncheng.common.constant.BuiltInOrgIds;
import com.yuncheng.framework.captcha.CaptchaScene;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.api.role.SystemRoleInfo;
import com.yuncheng.system.api.user.SystemUserCreateCommand;
import com.yuncheng.system.login.email.enums.EmailVerificationScene;
import com.yuncheng.system.login.email.service.EmailVerificationService;
import com.yuncheng.system.login.registration.dto.RegisterEmailCodeRequest;
import com.yuncheng.system.login.registration.dto.RegisterRequest;
import com.yuncheng.system.login.security.service.LoginSecurityService;
import com.yuncheng.system.role.service.RoleQueryService;
import com.yuncheng.system.role.service.RoleUserService;
import com.yuncheng.system.security.service.SecurityPolicyService;
import com.yuncheng.system.user.service.UserAccountService;
import com.yuncheng.system.user.service.UserCommandService;
import com.yuncheng.system.user.service.UserInputService;
import com.yuncheng.system.user.service.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 编排用户注册流程。 */
@Service
public class RegistrationService {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final UserAccountService userAccountService;
    private final RoleQueryService roleQueryService;
    private final RoleUserService roleUserService;
    private final UserInputService inputService;
    private final EmailVerificationService verificationService;
    private final LoginSecurityService loginSecurityService;
    private final SecurityPolicyService securityPolicyService;

    public RegistrationService(
            UserQueryService userQueryService,
            UserCommandService userCommandService,
            UserAccountService userAccountService,
            RoleQueryService roleQueryService,
            RoleUserService roleUserService,
            UserInputService inputService,
            EmailVerificationService verificationService,
            LoginSecurityService loginSecurityService,
            SecurityPolicyService securityPolicyService
    ) {
        this.userQueryService = userQueryService;
        this.userCommandService = userCommandService;
        this.userAccountService = userAccountService;
        this.roleQueryService = roleQueryService;
        this.roleUserService = roleUserService;
        this.inputService = inputService;
        this.verificationService = verificationService;
        this.loginSecurityService = loginSecurityService;
        this.securityPolicyService = securityPolicyService;
    }

    public void sendEmailCode(RegisterEmailCodeRequest request) {
        securityPolicyService.requireRegistrationEnabled();
        String username = inputService.normalizeNewUsername(request.username());
        String email = inputService.requireEmail(request.email());
        loginSecurityService.verifyEmailCode(
                CaptchaScene.REGISTER_EMAIL,
                request.captchaVerification()
        );
        if (!userQueryService.isUsernameAvailable(username)) {
            throw PlatformException.conflict("登录名已经存在");
        }
        if (!userQueryService.isEmailAvailable(email, null)) {
            throw PlatformException.conflict("电子邮箱已经存在");
        }
        verificationService.send(EmailVerificationScene.REGISTER, username, email);
    }

    @Transactional
    public void register(RegisterRequest request) {
        securityPolicyService.requireRegistrationEnabled();
        String username = inputService.normalizeNewUsername(request.username());
        String realName = inputService.normalizeRealName(request.realName());
        String email = inputService.requireEmail(request.email());
        userAccountService.requireNewPassword(request.password());
        if (!userQueryService.isUsernameAvailable(username)) {
            throw PlatformException.conflict("登录名已经存在");
        }
        if (!userQueryService.isEmailAvailable(email, null)) {
            throw PlatformException.conflict("电子邮箱已经存在");
        }
        SystemRoleInfo defaultRole = roleQueryService.findByCode(SystemRoleCodes.DEFAULT_USER)
                .orElseThrow(() -> PlatformException.serviceUnavailable("一般用户角色尚未初始化"));
        verificationService.verify(
                EmailVerificationScene.REGISTER,
                username,
                email,
                request.code()
        );
        Long userId = userCommandService.create(new SystemUserCreateCommand(
                null,
                username,
                request.password(),
                realName,
                null,
                email,
                0,
                true,
                BuiltInOrgIds.DEFAULT_ORG
        ));
        roleUserService.bind(userId, defaultRole.roleId());
    }
}
