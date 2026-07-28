package com.yuncheng.system.login.profile.dto;

import jakarta.validation.constraints.NotBlank;

/** 当前用户修改密码参数。 */
public record ProfilePasswordChangeRequest(
        @NotBlank(message = "当前密码不能为空")
        String currentPassword,
        @NotBlank(message = "新密码不能为空")
        String newPassword
) {
}
