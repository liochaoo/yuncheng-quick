package com.yuncheng.system.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.security.constant.SecurityPolicyDefaults;
import com.yuncheng.system.security.dto.SecurityPolicyData;
import com.yuncheng.system.security.dto.SecurityPolicyUpdateRequest;
import com.yuncheng.system.security.entity.SystemSecurityPolicy;
import com.yuncheng.system.security.mapper.SystemSecurityPolicyMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 读取、校验并保存平台安全策略。 */
@Service
public class SecurityPolicyService {

    private final SystemSecurityPolicyMapper policyMapper;

    public SecurityPolicyService(SystemSecurityPolicyMapper policyMapper) {
        this.policyMapper = policyMapper;
    }

    public SecurityPolicyData current() {
        SystemSecurityPolicy entity = findStoredPolicy();
        return entity == null ? SecurityPolicyDefaults.POLICY : toData(entity);
    }

    @Transactional
    public SecurityPolicyData update(SecurityPolicyUpdateRequest request) {
        SecurityPolicyData policy = request.toData();
        validate(policy);
        SystemSecurityPolicy entity = findStoredPolicy();
        if (entity == null) {
            entity = new SystemSecurityPolicy();
            entity.setPolicyKey(SecurityPolicyDefaults.POLICY_KEY);
            apply(entity, policy);
            policyMapper.insert(entity);
        } else {
            apply(entity, policy);
            policyMapper.updateById(entity);
        }
        return policy;
    }

    public void requireRegistrationEnabled() {
        if (!current().feature().registrationEnabled()) {
            throw PlatformException.forbidden("用户注册功能未开启");
        }
    }

    public void requirePasswordRecoveryEnabled() {
        if (!current().feature().passwordRecoveryEnabled()) {
            throw PlatformException.forbidden("找回密码功能未开启");
        }
    }

    public void requireProfileEmailEnabled() {
        if (!current().feature().profileEmailEnabled()) {
            throw PlatformException.forbidden("个人中心邮箱绑定与修改功能未开启");
        }
    }

    public String passwordRuleText() {
        return passwordRuleText(current().password());
    }

    public String passwordRuleText(SecurityPolicyData.Password password) {
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

    private SystemSecurityPolicy findStoredPolicy() {
        return policyMapper.selectOne(new LambdaQueryWrapper<SystemSecurityPolicy>()
                .eq(SystemSecurityPolicy::getPolicyKey, SecurityPolicyDefaults.POLICY_KEY));
    }

    private void validate(SecurityPolicyData policy) {
        if (policy.password().maxLength() < policy.password().minLength()) {
            throw PlatformException.badRequest("密码最大长度不能小于最小长度");
        }
    }

    private SecurityPolicyData toData(SystemSecurityPolicy entity) {
        return new SecurityPolicyData(
                new SecurityPolicyData.Feature(
                        entity.isRegistrationEnabled(),
                        entity.isPasswordRecoveryEnabled(),
                        entity.isProfileEmailEnabled()
                ),
                new SecurityPolicyData.Captcha(entity.isLoginCaptchaEnabled()),
                new SecurityPolicyData.LoginFailure(
                        entity.getLoginMaxFailedAttempts(),
                        entity.getLoginFailureWindowMinutes(),
                        entity.getLoginLockMinutes()
                ),
                new SecurityPolicyData.Password(
                        entity.getPasswordMinLength(),
                        entity.getPasswordMaxLength(),
                        entity.isPasswordRequireLowercase(),
                        entity.isPasswordRequireUppercase(),
                        entity.isPasswordRequireDigit(),
                        entity.isPasswordRequireSpecial(),
                        entity.getPasswordHistoryCount()
                )
        );
    }

    private void apply(SystemSecurityPolicy entity, SecurityPolicyData policy) {
        entity.setRegistrationEnabled(policy.feature().registrationEnabled());
        entity.setPasswordRecoveryEnabled(policy.feature().passwordRecoveryEnabled());
        entity.setProfileEmailEnabled(policy.feature().profileEmailEnabled());
        entity.setLoginCaptchaEnabled(policy.captcha().loginEnabled());
        entity.setLoginMaxFailedAttempts(policy.loginFailure().maxFailedAttempts());
        entity.setLoginFailureWindowMinutes(policy.loginFailure().windowMinutes());
        entity.setLoginLockMinutes(policy.loginFailure().lockMinutes());
        entity.setPasswordMinLength(policy.password().minLength());
        entity.setPasswordMaxLength(policy.password().maxLength());
        entity.setPasswordRequireLowercase(policy.password().requireLowercase());
        entity.setPasswordRequireUppercase(policy.password().requireUppercase());
        entity.setPasswordRequireDigit(policy.password().requireDigit());
        entity.setPasswordRequireSpecial(policy.password().requireSpecial());
        entity.setPasswordHistoryCount(policy.password().historyCount());
    }
}
