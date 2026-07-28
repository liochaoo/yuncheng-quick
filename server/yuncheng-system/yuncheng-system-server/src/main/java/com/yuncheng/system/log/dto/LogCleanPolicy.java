package com.yuncheng.system.log.dto;

import java.time.Instant;

/** 当前系统日志清理策略。 */
public record LogCleanPolicy(
        int retentionDays,
        Instant latestCleanableBefore
) {
}
