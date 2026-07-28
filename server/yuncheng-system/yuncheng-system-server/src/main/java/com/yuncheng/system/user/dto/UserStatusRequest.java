package com.yuncheng.system.user.dto;

import jakarta.validation.constraints.NotNull;

/** 用户启用状态请求。 */
public record UserStatusRequest(@NotNull(message = "用户启用状态不能为空") Boolean enabled) {
}
