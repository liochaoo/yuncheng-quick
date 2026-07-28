package com.yuncheng.framework.captcha.dto;

/** 图形验证码通过后签发的一次性校验凭据。 */
public record CaptchaVerificationResponse(String captchaVerification) {
}
