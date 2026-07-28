package com.yuncheng.framework.captcha.model;

import com.yuncheng.framework.captcha.CaptchaScene;

/** Redis 中保存的图形验证码二次校验状态。 */
public record CaptchaVerificationState(CaptchaScene scene) {
}
