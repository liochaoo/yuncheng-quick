package com.yuncheng.system.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 管理用户密码和登录锁定。 */
@Service
public class UserPasswordService {

    private final UserAccessService userAccessService;
    private final UserAccountService userAccountService;
    private final UserLoginSecurityService loginSecurityService;

    public UserPasswordService(
            UserAccessService userAccessService,
            UserAccountService userAccountService,
            UserLoginSecurityService loginSecurityService
    ) {
        this.userAccessService = userAccessService;
        this.userAccountService = userAccountService;
        this.loginSecurityService = loginSecurityService;
    }

    @Transactional
    public void reset(Long userId, String password) {
        userAccessService.requireCanManage(userId);
        userAccountService.resetPassword(userId, password);
    }

    public void unlock(Long userId) {
        userAccessService.requireCanManage(userId);
        loginSecurityService.unlock(userId);
    }
}
