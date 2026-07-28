package com.yuncheng.system.user.service;

import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.security.dto.SecurityPolicyData;
import com.yuncheng.system.security.service.SecurityPolicyService;
import com.yuncheng.system.user.constant.PasswordPolicyConstants;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** 统一校验并编码用户密码。 */
@Service
public class PasswordPolicyService {

    private final PasswordEncoder passwordEncoder;
    private final SecurityPolicyService securityPolicyService;

    public PasswordPolicyService(
            PasswordEncoder passwordEncoder,
            SecurityPolicyService securityPolicyService
    ) {
        this.passwordEncoder = passwordEncoder;
        this.securityPolicyService = securityPolicyService;
    }

    public String encodeNewPassword(String password) {
        requireNewPassword(password);
        return passwordEncoder.encode(password);
    }

    public void requireLoginInput(String password) {
        requirePresent(password);
        requireUtf8Length(password);
    }

    public boolean matches(String password, String passwordHash) {
        return passwordEncoder.matches(password, passwordHash);
    }

    public int currentHistoryCount() {
        return securityPolicyService.current().password().historyCount();
    }

    public void requireNewPassword(String password) {
        requirePresent(password);
        SecurityPolicyData.Password policy = securityPolicyService.current().password();
        int length = codePointLength(password);
        if (length < policy.minLength() || length > policy.maxLength()) {
            throw PlatformException.badRequest(securityPolicyService.passwordRuleText(policy));
        }
        if ((policy.requireLowercase()
                && password.codePoints().noneMatch(Character::isLowerCase))
                || (policy.requireUppercase()
                && password.codePoints().noneMatch(Character::isUpperCase))
                || (policy.requireDigit()
                && password.codePoints().noneMatch(Character::isDigit))
                || (policy.requireSpecial()
                && password.codePoints().noneMatch(this::isSpecial))) {
            throw PlatformException.badRequest(securityPolicyService.passwordRuleText(policy));
        }
        requireUtf8Length(password);
    }

    private void requirePresent(String password) {
        if (password == null || password.isBlank()) {
            throw PlatformException.badRequest("密码不能为空");
        }
    }

    private void requireUtf8Length(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length
                > PasswordPolicyConstants.BCRYPT_MAX_UTF8_BYTES) {
            throw PlatformException.badRequest("密码内容过长，请适当缩短");
        }
    }

    private int codePointLength(String password) {
        return password.codePointCount(0, password.length());
    }

    private boolean isSpecial(int codePoint) {
        return "~!@#$%^&*()_+-={}[]|:;\"'<>,.?/`\\".indexOf(codePoint) >= 0;
    }
}
