package com.yuncheng.system.session.model;

import java.util.List;

/** Redis 登录会话索引的分页结果。 */
public record LoginSessionPage(
        List<LoginSession> sessions,
        long total
) {

    public LoginSessionPage {
        sessions = sessions == null ? List.of() : List.copyOf(sessions);
    }
}
