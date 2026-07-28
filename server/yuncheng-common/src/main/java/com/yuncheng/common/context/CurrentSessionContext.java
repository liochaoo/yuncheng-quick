package com.yuncheng.common.context;

import java.util.Optional;

/** 统一读取当前请求所使用的登录会话标识。 */
public interface CurrentSessionContext {

    Optional<String> findSessionId();

    default String getSessionId() {
        return findSessionId().orElseThrow(() -> new IllegalStateException("当前登录会话上下文不存在"));
    }
}
