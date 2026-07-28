package com.yuncheng.framework.log.command;

import com.yuncheng.framework.log.LoginEventType;
import java.time.Instant;

/** 写入登录日志的稳定命令。 */
public record LoginLogCommand(
        LoginEventType eventType,
        boolean success,
        Long userId,
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
