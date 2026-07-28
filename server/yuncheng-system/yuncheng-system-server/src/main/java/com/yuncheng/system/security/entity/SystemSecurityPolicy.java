package com.yuncheng.system.security.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yuncheng.framework.mybatis.entity.BaseEntity;

/** 数据库中保存的平台安全策略。 */
@TableName("system_security_policy")
public class SystemSecurityPolicy extends BaseEntity {

    private String policyKey;
    private boolean registrationEnabled;
    private boolean passwordRecoveryEnabled;
    private boolean profileEmailEnabled;
    private boolean loginCaptchaEnabled;
    private Integer passwordMinLength;
    private Integer passwordMaxLength;
    private boolean passwordRequireLowercase;
    private boolean passwordRequireUppercase;
    private boolean passwordRequireDigit;
    private boolean passwordRequireSpecial;
    private Integer passwordHistoryCount;
    private Integer loginMaxFailedAttempts;
    private Integer loginFailureWindowMinutes;
    private Integer loginLockMinutes;

    public String getPolicyKey() {
        return policyKey;
    }

    public void setPolicyKey(String policyKey) {
        this.policyKey = policyKey;
    }

    public boolean isRegistrationEnabled() {
        return registrationEnabled;
    }

    public void setRegistrationEnabled(boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }

    public boolean isPasswordRecoveryEnabled() {
        return passwordRecoveryEnabled;
    }

    public void setPasswordRecoveryEnabled(boolean passwordRecoveryEnabled) {
        this.passwordRecoveryEnabled = passwordRecoveryEnabled;
    }

    public boolean isProfileEmailEnabled() {
        return profileEmailEnabled;
    }

    public void setProfileEmailEnabled(boolean profileEmailEnabled) {
        this.profileEmailEnabled = profileEmailEnabled;
    }

    public boolean isLoginCaptchaEnabled() {
        return loginCaptchaEnabled;
    }

    public void setLoginCaptchaEnabled(boolean loginCaptchaEnabled) {
        this.loginCaptchaEnabled = loginCaptchaEnabled;
    }

    public Integer getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(Integer passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public Integer getPasswordMaxLength() {
        return passwordMaxLength;
    }

    public void setPasswordMaxLength(Integer passwordMaxLength) {
        this.passwordMaxLength = passwordMaxLength;
    }

    public boolean isPasswordRequireLowercase() {
        return passwordRequireLowercase;
    }

    public void setPasswordRequireLowercase(boolean passwordRequireLowercase) {
        this.passwordRequireLowercase = passwordRequireLowercase;
    }

    public boolean isPasswordRequireUppercase() {
        return passwordRequireUppercase;
    }

    public void setPasswordRequireUppercase(boolean passwordRequireUppercase) {
        this.passwordRequireUppercase = passwordRequireUppercase;
    }

    public boolean isPasswordRequireDigit() {
        return passwordRequireDigit;
    }

    public void setPasswordRequireDigit(boolean passwordRequireDigit) {
        this.passwordRequireDigit = passwordRequireDigit;
    }

    public boolean isPasswordRequireSpecial() {
        return passwordRequireSpecial;
    }

    public void setPasswordRequireSpecial(boolean passwordRequireSpecial) {
        this.passwordRequireSpecial = passwordRequireSpecial;
    }

    public Integer getPasswordHistoryCount() {
        return passwordHistoryCount;
    }

    public void setPasswordHistoryCount(Integer passwordHistoryCount) {
        this.passwordHistoryCount = passwordHistoryCount;
    }

    public Integer getLoginMaxFailedAttempts() {
        return loginMaxFailedAttempts;
    }

    public void setLoginMaxFailedAttempts(Integer loginMaxFailedAttempts) {
        this.loginMaxFailedAttempts = loginMaxFailedAttempts;
    }

    public Integer getLoginFailureWindowMinutes() {
        return loginFailureWindowMinutes;
    }

    public void setLoginFailureWindowMinutes(Integer loginFailureWindowMinutes) {
        this.loginFailureWindowMinutes = loginFailureWindowMinutes;
    }

    public Integer getLoginLockMinutes() {
        return loginLockMinutes;
    }

    public void setLoginLockMinutes(Integer loginLockMinutes) {
        this.loginLockMinutes = loginLockMinutes;
    }
}
