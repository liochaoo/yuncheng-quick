package com.yuncheng.system.session.service;

import com.yuncheng.common.context.CurrentSessionContext;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.system.session.dto.OnlineSessionItem;
import com.yuncheng.system.session.dto.OnlineSessionPageQuery;
import com.yuncheng.system.session.model.LoginSession;
import com.yuncheng.system.session.model.LoginSessionPage;
import com.yuncheng.system.user.entity.SystemUser;
import com.yuncheng.system.user.service.UserLoginQueryService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 查询在线会话并执行强制下线。 */
@Service
public class OnlineSessionService {

    private final LoginSessionService loginSessionService;
    private final UserLoginQueryService userLoginQueryService;
    private final CurrentSessionContext currentSessionContext;

    public OnlineSessionService(
            LoginSessionService loginSessionService,
            UserLoginQueryService userLoginQueryService,
            CurrentSessionContext currentSessionContext
    ) {
        this.loginSessionService = loginSessionService;
        this.userLoginQueryService = userLoginQueryService;
        this.currentSessionContext = currentSessionContext;
    }

    public PageResult<OnlineSessionItem> page(OnlineSessionPageQuery query) {
        Long userId = resolveUserId(query.getUsername());
        if (hasText(query.getUsername()) && userId == null) {
            return PageResult.of(List.of(), 0, query);
        }
        LoginSessionPage result = loginSessionService.page(
                userId,
                query.getPage(),
                query.getPageSize()
        );
        String currentSessionId = currentSessionContext.findSessionId().orElse(null);
        List<OnlineSessionItem> items = result.sessions().stream()
                .map(session -> toItem(session, currentSessionId))
                .toList();
        return PageResult.of(items, result.total(), query);
    }

    public OnlineSessionItem detail(String sessionId) {
        LoginSession session = requireSession(sessionId);
        return toItem(session, currentSessionContext.findSessionId().orElse(null));
    }

    public void kickout(String sessionId) {
        requireNotCurrent(sessionId);
        LoginSession session = requireSession(sessionId);
        loginSessionService.deleteSession(session.sessionId(), session.refreshJti());
    }

    public void batchKickout(List<String> sessionIds) {
        String currentSessionId = currentSessionContext.getSessionId();
        if (sessionIds.contains(currentSessionId)) {
            throw PlatformException.badRequest("不能下线当前正在使用的会话");
        }
        for (String sessionId : sessionIds.stream().distinct().toList()) {
            LoginSession session = loginSessionService.findSession(sessionId);
            if (session != null) {
                loginSessionService.deleteSession(session.sessionId(), session.refreshJti());
            }
        }
    }

    private Long resolveUserId(String username) {
        if (!hasText(username)) {
            return null;
        }
        SystemUser user = userLoginQueryService.findByUsername(username.trim());
        return user == null ? null : user.getId();
    }

    private LoginSession requireSession(String sessionId) {
        LoginSession session = loginSessionService.findSession(sessionId);
        if (session == null) {
            throw PlatformException.notFound("在线会话不存在或已失效");
        }
        return session;
    }

    private void requireNotCurrent(String sessionId) {
        if (currentSessionContext.getSessionId().equals(sessionId)) {
            throw PlatformException.badRequest("不能下线当前正在使用的会话");
        }
    }

    private OnlineSessionItem toItem(LoginSession session, String currentSessionId) {
        return new OnlineSessionItem(
                session.sessionId(),
                session.userId().toString(),
                session.username(),
                session.realName(),
                session.clientType(),
                session.loginIp(),
                session.userAgent(),
                session.createdAt(),
                session.expiresAt(),
                session.sessionId().equals(currentSessionId)
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
