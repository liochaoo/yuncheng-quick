package com.yuncheng.system.login.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 修改个人邮箱参数。 */
public record ProfileEmailChangeRequest(
        @NotBlank(message = "新邮箱不能为空")
        @Email(message = "新邮箱格式不正确")
        @Size(max = 254, message = "新邮箱不能超过 254 个字符")
        String email,
        @NotBlank(message = "验证码不能为空")
        @Size(min = 6, max = 6, message = "验证码必须为 6 位")
        String code,
        @NotBlank(message = "当前密码不能为空")
        String currentPassword
) {
}
