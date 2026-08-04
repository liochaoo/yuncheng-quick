package com.yuncheng.framework.openapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** OpenAPI 接口文档配置。 */
@ConfigurationProperties("platform.openapi")
public class OpenApiProperties {

    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
