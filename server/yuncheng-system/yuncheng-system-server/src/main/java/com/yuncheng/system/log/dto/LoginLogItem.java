package com.yuncheng.system.log.dto;

import java.time.Instant;

/** 登录日志列表和详情数据。 */
public record LoginLogItem(
        String id,
        String eventType,
        boolean success,
        String userId,
        String loginName,
        String realName,
        String clientType,
        String sessionId,
        String ip,
        String userAgent,
        String failureReason,
        String traceId,
        Instant occurredAt
) {
}
