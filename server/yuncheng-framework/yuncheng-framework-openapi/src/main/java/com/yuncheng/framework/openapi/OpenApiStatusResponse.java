package com.yuncheng.framework.openapi;

/** 当前环境的接口文档状态。 */
public record OpenApiStatusResponse(boolean enabled, String documentUrl) {
}
