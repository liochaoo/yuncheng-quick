package com.yuncheng.framework.web.constant;

/** Web模块统一常量。 */
public final class WebConstants {

    /** 业务接口统一前缀。 */
    public static final String API_PREFIX = "/api";

    /** HTTP Trace ID请求头和响应头名称。 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** 日志上下文中的Trace ID名称。 */
    public static final String TRACE_ID_MDC_KEY = "traceId";

    private WebConstants() {
    }
}
