package com.yuncheng.framework.job.context;

import java.time.Instant;

/** 单次定时任务执行上下文。 */
public record JobContext(
        String jobName,
        String traceId,
        Long operatorId,
        Instant startedAt
) {
}
