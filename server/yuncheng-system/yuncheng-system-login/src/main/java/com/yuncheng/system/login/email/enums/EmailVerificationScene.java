package com.yuncheng.system.login.email.enums;

/** 邮箱验证码使用场景。 */
public enum EmailVerificationScene {
    REGISTER("register", "注册账号"),
    RESET_PASSWORD("reset-password", "找回密码"),
    CHANGE_EMAIL("change-email", "修改邮箱");

    private final String key;
    private final String action;

    EmailVerificationScene(String key, String action) {
        this.key = key;
        this.action = action;
    }

    public String getKey() {
        return key;
    }

    public String getAction() {
        return action;
    }
}
