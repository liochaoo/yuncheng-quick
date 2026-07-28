package com.yuncheng.framework.web.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/** 在容器完成代理头处理后统一读取客户端信息。 */
@Component
public class ClientRequestInfoResolver {

    private static final int MAX_USER_AGENT_LENGTH = 1000;

    public ClientRequestInfo resolve(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > MAX_USER_AGENT_LENGTH) {
            userAgent = userAgent.substring(0, MAX_USER_AGENT_LENGTH);
        }
        return new ClientRequestInfo(request.getRemoteAddr(), userAgent);
    }
}
