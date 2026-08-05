package com.yuncheng.system.user.dto;

import com.yuncheng.system.user.enums.PasswordSetupMode;
import jakarta.validation.constraints.NotNull;

/** 重置用户密码请求。 */
public record PasswordResetRequest(
        @NotNull(message = "密码设置方式不能为空") PasswordSetupMode passwordMode,
        String password
) {
}
