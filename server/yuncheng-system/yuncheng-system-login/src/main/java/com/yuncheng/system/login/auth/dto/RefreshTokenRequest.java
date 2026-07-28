package com.yuncheng.system.login.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** 通过请求体提交 Refresh Token。 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh Token 不能为空") String refreshToken
) {
}
