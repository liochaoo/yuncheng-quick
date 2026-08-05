package com.yuncheng.system.user.service;

import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.security.service.SecurityPolicyService;
import com.yuncheng.system.user.enums.PasswordSetupMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 管理用户密码和登录锁定。 */
@Service
public class UserPasswordService {

    private final UserAccessService userAccessService;
    private final UserAccountService userAccountService;
    private final UserLoginSecurityService loginSecurityService;
    private final SecurityPolicyService securityPolicyService;

    public UserPasswordService(
            UserAccessService userAccessService,
            UserAccountService userAccountService,
            UserLoginSecurityService loginSecurityService,
            SecurityPolicyService securityPolicyService
    ) {
        this.userAccessService = userAccessService;
        this.userAccountService = userAccountService;
        this.loginSecurityService = loginSecurityService;
        this.securityPolicyService = securityPolicyService;
    }

    @Transactional
    public void reset(Long userId, PasswordSetupMode passwordMode, String password) {
        userAccessService.requireCanManage(userId);
        if (passwordMode == PasswordSetupMode.DEFAULT) {
            userAccountService.resetToDefaultPassword(userId, securityPolicyService.defaultPasswordHash());
        } else if (passwordMode == PasswordSetupMode.MANUAL) {
            userAccountService.resetPassword(userId, password);
        } else {
            throw PlatformException.badRequest("密码设置方式不正确");
        }
    }

    public void unlock(Long userId) {
        userAccessService.requireCanManage(userId);
        loginSecurityService.unlock(userId);
    }
}
