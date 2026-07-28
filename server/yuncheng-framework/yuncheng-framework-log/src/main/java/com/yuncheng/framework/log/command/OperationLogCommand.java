package com.yuncheng.framework.log.command;

import java.time.Instant;

/** 写入操作日志的稳定命令。 */
public record OperationLogCommand(
        String action,
        String className,
        String methodName,
        String httpMethod,
        String requestPath,
        String requestParams,
        boolean success,
        String errorMessage,
        long durationMillis,
        Long userId,
        String username,
        String realName,
        String ip,
        String userAgent,
        String traceId,
        Instant occurredAt
) {
}
