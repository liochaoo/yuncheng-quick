package com.yuncheng.system.log.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/** 手动清理系统日志。 */
public record LogCleanRequest(
        @NotNull LogType type,
        @NotNull Instant before
) {
}
