package com.yuncheng.system.login.password.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 找回密码重置参数。 */
public record PasswordResetRequest(
        @NotBlank(message = "登录名不能为空")
        @Size(max = 50, message = "登录名不能超过 50 个字符")
        String username,
        @NotBlank(message = "电子邮箱不能为空")
        @Email(message = "电子邮箱格式不正确")
        @Size(max = 254, message = "电子邮箱不能超过 254 个字符")
        String email,
        @NotBlank(message = "验证码不能为空")
        @Size(min = 6, max = 6, message = "验证码必须为 6 位")
        String code,
        @NotBlank(message = "新密码不能为空")
        String newPassword
) {
}
