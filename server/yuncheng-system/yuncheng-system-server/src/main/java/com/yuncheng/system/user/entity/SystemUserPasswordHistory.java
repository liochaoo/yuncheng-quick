package com.yuncheng.system.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yuncheng.framework.mybatis.entity.BaseEntity;
import com.yuncheng.system.user.enums.PasswordChangeSource;

/** 用户历次成功设置的密码摘要。 */
@TableName("system_user_password_history")
public class SystemUserPasswordHistory extends BaseEntity {

    private Long userId;
    private String passwordHash;
    private PasswordChangeSource changeSource;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public PasswordChangeSource getChangeSource() {
        return changeSource;
    }

    public void setChangeSource(PasswordChangeSource changeSource) {
        this.changeSource = changeSource;
    }
}
