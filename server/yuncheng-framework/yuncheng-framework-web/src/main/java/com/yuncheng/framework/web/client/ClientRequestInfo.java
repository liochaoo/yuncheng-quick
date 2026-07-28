package com.yuncheng.framework.web.client;

/** 当前 HTTP 请求的客户端信息。 */
public record ClientRequestInfo(
        String ip,
        String userAgent
) {
}
