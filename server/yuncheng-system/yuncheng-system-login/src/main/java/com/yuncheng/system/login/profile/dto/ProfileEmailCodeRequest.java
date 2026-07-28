package com.yuncheng.system.login.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 发送个人邮箱修改验证码参数。 */
public record ProfileEmailCodeRequest(
        @NotBlank(message = "新邮箱不能为空")
        @Email(message = "新邮箱格式不正确")
        @Size(max = 254, message = "新邮箱不能超过 254 个字符")
        String email,
        @NotBlank(message = "当前密码不能为空")
        String currentPassword,
        @NotBlank(message = "请先完成图形验证")
        String captchaVerification
) {
}
