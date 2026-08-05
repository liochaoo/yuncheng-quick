package com.yuncheng.system.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.security.constant.SecurityPolicyDefaults;
import com.yuncheng.system.security.dto.SecurityPolicyData;
import com.yuncheng.system.security.dto.SecurityPolicyUpdateRequest;
import com.yuncheng.system.security.entity.SystemSecurityPolicy;
import com.yuncheng.system.security.mapper.SystemSecurityPolicyMapper;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 读取、校验并保存平台安全策略。 */
@Service
public class SecurityPolicyService {

    private final SystemSecurityPolicyMapper policyMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordRuleValidator passwordRuleValidator;

    public SecurityPolicyService(
            SystemSecurityPolicyMapper policyMapper,
            PasswordEncoder passwordEncoder,
            PasswordRuleValidator passwordRuleValidator
    ) {
        this.policyMapper = policyMapper;
        this.passwordEncoder = passwordEncoder;
        this.passwordRuleValidator = passwordRuleValidator;
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
        String requestedDefaultPassword = normalizePassword(request.defaultPassword().password());
        SecurityPolicyData.Password previousPasswordPolicy = entity == null
                ? SecurityPolicyDefaults.POLICY.password()
                : toData(entity).password();
        if (requestedDefaultPassword == null
                && !Objects.equals(previousPasswordPolicy, policy.password())) {
            throw PlatformException.badRequest("修改密码规则时必须同时重新设置系统默认密码");
        }
        if (requestedDefaultPassword != null) {
            passwordRuleValidator.requireNewPassword(requestedDefaultPassword, policy.password());
        }
        if (entity == null) {
            entity = new SystemSecurityPolicy();
            entity.setPolicyKey(SecurityPolicyDefaults.POLICY_KEY);
            apply(entity, policy);
        } else {
            apply(entity, policy);
        }
        if (requestedDefaultPassword != null) {
            entity.setDefaultPasswordHash(passwordEncoder.encode(requestedDefaultPassword));
        }
        if (entity.getId() == null) {
            policyMapper.insert(entity);
        } else {
            policyMapper.updateById(entity);
        }
        return toData(entity);
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
        return passwordRuleValidator.ruleText(password);
    }

    public String defaultPasswordHash() {
        SystemSecurityPolicy entity = findStoredPolicy();
        if (entity != null && entity.getDefaultPasswordHash() != null
                && !entity.getDefaultPasswordHash().isBlank()) {
            return entity.getDefaultPasswordHash();
        }
        return passwordEncoder.encode(SecurityPolicyDefaults.INITIAL_DEFAULT_PASSWORD);
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
                ),
                new SecurityPolicyData.DefaultPassword(
                        entity.getDefaultPasswordHash() != null
                                && !entity.getDefaultPasswordHash().isBlank()
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

    private String normalizePassword(String password) {
        return password == null || password.isBlank() ? null : password;
    }
}
