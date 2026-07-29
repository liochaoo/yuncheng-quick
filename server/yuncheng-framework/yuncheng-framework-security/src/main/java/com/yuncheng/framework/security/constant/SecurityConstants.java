package com.yuncheng.framework.security.constant;

import com.yuncheng.framework.web.constant.WebConstants;
import java.util.List;

/** 安全模块统一常量。 */
public final class SecurityConstants {

    /** 无需登录即可访问的接口。 */
    public static final List<String> ANONYMOUS_URLS = List.of(
            WebConstants.API_PREFIX + "/auth/login",
            WebConstants.API_PREFIX + "/auth/refresh",
            WebConstants.API_PREFIX + "/auth/logout",
            WebConstants.API_PREFIX + "/auth/register/email-code",
            WebConstants.API_PREFIX + "/auth/register",
            WebConstants.API_PREFIX + "/auth/password/email-code",
            WebConstants.API_PREFIX + "/auth/password/reset",
            WebConstants.API_PREFIX + "/auth/security-policy",
            WebConstants.API_PREFIX + "/auth/experience-config",
            WebConstants.API_PREFIX + "/auth/captcha/get",
            WebConstants.API_PREFIX + "/auth/captcha/check",
            WebConstants.API_PREFIX + "/public/files/*/preview",
            WebConstants.API_PREFIX + "/public/files/*/download",
            WebConstants.API_PREFIX + "/mobile/auth/login",
            WebConstants.API_PREFIX + "/mobile/auth/refresh",
            WebConstants.API_PREFIX + "/mobile/auth/logout",
            "/actuator/health"
    );

    private SecurityConstants() {
    }
}
