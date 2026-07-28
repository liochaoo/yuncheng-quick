package com.yuncheng.framework.web.trace;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/** 创建并校验平台统一的 Trace ID。 */
public final class TraceIdGenerator {

    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("[0-9a-fA-F]{32}");

    private TraceIdGenerator() {
    }

    public static String create() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String resolve(String candidate) {
        if (candidate == null || !TRACE_ID_PATTERN.matcher(candidate).matches()) {
            return create();
        }
        return candidate.toLowerCase(Locale.ROOT);
    }
}
