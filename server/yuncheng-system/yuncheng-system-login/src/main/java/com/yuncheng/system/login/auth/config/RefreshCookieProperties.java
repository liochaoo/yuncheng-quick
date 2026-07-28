package com.yuncheng.system.login.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** WEB Refresh Token Cookie 配置。 */
@ConfigurationProperties(prefix = "platform.auth.refresh-cookie")
public class RefreshCookieProperties {

    private String name = "yuncheng-quick-refresh-token";
    private String path = "/api/auth";
    private boolean secure = false;
    private String sameSite = "Lax";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getSameSite() {
        return sameSite;
    }

    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }
}
