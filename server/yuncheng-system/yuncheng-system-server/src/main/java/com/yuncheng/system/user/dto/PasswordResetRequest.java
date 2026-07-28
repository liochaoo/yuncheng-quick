package com.yuncheng.system.user.dto;

import jakarta.validation.constraints.NotBlank;

/** 重置用户密码请求。 */
public record PasswordResetRequest(
        @NotBlank(message = "新密码不能为空")
        String password
) {
}
