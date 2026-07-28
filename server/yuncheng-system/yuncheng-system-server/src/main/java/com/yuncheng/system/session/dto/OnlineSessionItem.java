package com.yuncheng.system.session.dto;

import java.time.Instant;

/** 在线会话列表与详情数据。 */
public record OnlineSessionItem(
        String sessionId,
        String userId,
        String username,
        String realName,
        String clientType,
        String loginIp,
        String userAgent,
        Instant loginTime,
        Instant expiresAt,
        boolean current
) {
}
