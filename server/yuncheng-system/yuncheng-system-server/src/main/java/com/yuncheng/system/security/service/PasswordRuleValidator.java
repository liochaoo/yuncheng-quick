package com.yuncheng.system.security.service;

import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.security.dto.SecurityPolicyData;
import com.yuncheng.system.user.constant.PasswordPolicyConstants;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** 按指定安全策略校验新密码并生成规则说明。 */
@Component
public class PasswordRuleValidator {

    public void requireNewPassword(String password, SecurityPolicyData.Password policy) {
        requirePresent(password);
        int length = password.codePointCount(0, password.length());
        if (length < policy.minLength() || length > policy.maxLength()) {
            throw PlatformException.badRequest(ruleText(policy));
        }
        if ((policy.requireLowercase() && password.codePoints().noneMatch(Character::isLowerCase))
                || (policy.requireUppercase() && password.codePoints().noneMatch(Character::isUpperCase))
                || (policy.requireDigit() && password.codePoints().noneMatch(Character::isDigit))
                || (policy.requireSpecial() && password.codePoints().noneMatch(this::isSpecial))) {
            throw PlatformException.badRequest(ruleText(policy));
        }
        requireUtf8Length(password);
    }

    public void requireLoginInput(String password) {
        requirePresent(password);
        requireUtf8Length(password);
    }

    public String ruleText(SecurityPolicyData.Password password) {
        List<String> requirements = new ArrayList<>();
        if (password.requireLowercase()) {
            requirements.add("小写字母");
        }
        if (password.requireUppercase()) {
            requirements.add("大写字母");
        }
        if (password.requireDigit()) {
            requirements.add("数字");
        }
        if (password.requireSpecial()) {
            requirements.add("特殊字符");
        }
        StringBuilder text = new StringBuilder("密码应为 ")
                .append(password.minLength())
                .append("～")
                .append(password.maxLength())
                .append(" 个字符");
        if (!requirements.isEmpty()) {
            text.append("，须包含").append(String.join("、", requirements));
        }
        return text.toString();
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

    private boolean isSpecial(int codePoint) {
        return "~!@#$%^&*()_+-={}[]|:;\"'<>,.?/`\\".indexOf(codePoint) >= 0;
    }
}
