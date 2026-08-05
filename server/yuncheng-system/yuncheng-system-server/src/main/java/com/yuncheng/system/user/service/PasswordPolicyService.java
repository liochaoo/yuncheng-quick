package com.yuncheng.system.user.service;

import com.yuncheng.system.security.dto.SecurityPolicyData;
import com.yuncheng.system.security.service.PasswordRuleValidator;
import com.yuncheng.system.security.service.SecurityPolicyService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** 统一校验并编码用户密码。 */
@Service
public class PasswordPolicyService {

    private final PasswordEncoder passwordEncoder;
    private final SecurityPolicyService securityPolicyService;
    private final PasswordRuleValidator passwordRuleValidator;

    public PasswordPolicyService(
            PasswordEncoder passwordEncoder,
            SecurityPolicyService securityPolicyService,
            PasswordRuleValidator passwordRuleValidator
    ) {
        this.passwordEncoder = passwordEncoder;
        this.securityPolicyService = securityPolicyService;
        this.passwordRuleValidator = passwordRuleValidator;
    }

    public String encodeNewPassword(String password) {
        requireNewPassword(password);
        return passwordEncoder.encode(password);
    }

    public void requireLoginInput(String password) {
        passwordRuleValidator.requireLoginInput(password);
    }

    public boolean matches(String password, String passwordHash) {
        return passwordEncoder.matches(password, passwordHash);
    }

    public int currentHistoryCount() {
        return securityPolicyService.current().password().historyCount();
    }

    public void requireNewPassword(String password) {
        SecurityPolicyData.Password policy = securityPolicyService.current().password();
        passwordRuleValidator.requireNewPassword(password, policy);
    }
}
