package com.yuncheng.system.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yuncheng.framework.mybatis.entity.BaseEntity;
import java.time.Instant;

/** 系统用户。 */
@TableName("system_user")
public class SystemUser extends BaseEntity {

    private String username;
    private String passwordHash;
    private Instant passwordChangedAt;
    private Boolean passwordChangeRequired;
    private Integer loginFailedCount;
    private Instant loginFailureWindowStartedAt;
    private Instant loginLockedUntil;
    private String realName;
    private String avatar;
    private String phone;
    private String email;
    private Integer sortOrder;
    private Boolean enabled;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(Instant passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public Boolean getPasswordChangeRequired() {
        return passwordChangeRequired;
    }

    public void setPasswordChangeRequired(Boolean passwordChangeRequired) {
        this.passwordChangeRequired = passwordChangeRequired;
    }

    public Integer getLoginFailedCount() {
        return loginFailedCount;
    }

    public void setLoginFailedCount(Integer loginFailedCount) {
        this.loginFailedCount = loginFailedCount;
    }

    public Instant getLoginFailureWindowStartedAt() {
        return loginFailureWindowStartedAt;
    }

    public void setLoginFailureWindowStartedAt(Instant loginFailureWindowStartedAt) {
        this.loginFailureWindowStartedAt = loginFailureWindowStartedAt;
    }

    public Instant getLoginLockedUntil() {
        return loginLockedUntil;
    }

    public void setLoginLockedUntil(Instant loginLockedUntil) {
        this.loginLockedUntil = loginLockedUntil;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
