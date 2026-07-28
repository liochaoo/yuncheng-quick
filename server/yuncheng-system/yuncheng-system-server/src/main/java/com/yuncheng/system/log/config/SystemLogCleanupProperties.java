package com.yuncheng.system.log.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** 系统日志自动清理和保留期限配置。 */
@Validated
@ConfigurationProperties(prefix = "platform.log.cleanup")
public class SystemLogCleanupProperties {

    private boolean autoEnabled = true;

    @Min(value = 1, message = "系统日志保留天数必须大于 0")
    private int retentionDays = 180;

    public boolean isAutoEnabled() {
        return autoEnabled;
    }

    public void setAutoEnabled(boolean autoEnabled) {
        this.autoEnabled = autoEnabled;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }
}
