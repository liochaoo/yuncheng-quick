package com.yuncheng.system.security.constant;

import com.yuncheng.system.security.dto.SecurityPolicyData;

/** 安全策略默认值，是数据库尚未保存策略时的唯一默认值来源。 */
public final class SecurityPolicyDefaults {

    public static final String POLICY_KEY = "default";

    public static final SecurityPolicyData POLICY = new SecurityPolicyData(
            new SecurityPolicyData.Feature(true, true, true),
            new SecurityPolicyData.Captcha(false),
            new SecurityPolicyData.LoginFailure(5, 15, 15),
            new SecurityPolicyData.Password(8, 64, true, false, true, true, 5)
    );

    private SecurityPolicyDefaults() {
    }
}
