package com.yuncheng.system.log.dto;

import java.time.Instant;

/** 操作日志列表和详情数据。 */
public record OperationLogItem(
        String id,
        String action,
        String className,
        String methodName,
        String httpMethod,
        String requestPath,
        String requestParams,
        boolean success,
        String errorMessage,
        long durationMillis,
        String userId,
        String username,
        String realName,
        String ip,
        String userAgent,
        String traceId,
        Instant occurredAt
) {
}
