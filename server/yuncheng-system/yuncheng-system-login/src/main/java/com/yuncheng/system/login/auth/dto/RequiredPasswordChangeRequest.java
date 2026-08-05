package com.yuncheng.system.login.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** 使用短期凭据完成强制密码修改。 */
public record RequiredPasswordChangeRequest(
        @NotBlank(message = "修改密码凭据不能为空") String passwordChangeToken,
        @NotBlank(message = "新密码不能为空") String newPassword
) {
}
