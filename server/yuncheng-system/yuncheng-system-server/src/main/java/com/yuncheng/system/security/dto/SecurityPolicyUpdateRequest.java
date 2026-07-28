package com.yuncheng.system.security.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** 完整保存安全策略的请求。 */
public record SecurityPolicyUpdateRequest(
        @NotNull(message = "功能开关不能为空") @Valid Feature feature,
        @NotNull(message = "验证码设置不能为空") @Valid Captcha captcha,
        @NotNull(message = "登录失败控制不能为空") @Valid LoginFailure loginFailure,
        @NotNull(message = "密码规则不能为空") @Valid Password password
) {

    public SecurityPolicyData toData() {
        return new SecurityPolicyData(
                new SecurityPolicyData.Feature(
                        feature.registrationEnabled(),
                        feature.passwordRecoveryEnabled(),
                        feature.profileEmailEnabled()
                ),
                new SecurityPolicyData.Captcha(captcha.loginEnabled()),
                new SecurityPolicyData.LoginFailure(
                        loginFailure.maxFailedAttempts(),
                        loginFailure.windowMinutes(),
                        loginFailure.lockMinutes()
                ),
                new SecurityPolicyData.Password(
                        password.minLength(),
                        password.maxLength(),
                        password.requireLowercase(),
                        password.requireUppercase(),
                        password.requireDigit(),
                        password.requireSpecial(),
                        password.historyCount()
                )
        );
    }

    public record Feature(
            @NotNull(message = "用户注册开关不能为空") Boolean registrationEnabled,
            @NotNull(message = "找回密码开关不能为空") Boolean passwordRecoveryEnabled,
            @NotNull(message = "个人中心邮箱开关不能为空") Boolean profileEmailEnabled
    ) {
    }

    public record Captcha(
            @NotNull(message = "登录图形验证码开关不能为空") Boolean loginEnabled
    ) {
    }

    public record LoginFailure(
            @NotNull(message = "最大登录失败次数不能为空")
            @Min(value = 3, message = "最大登录失败次数不能小于 3")
            @Max(value = 20, message = "最大登录失败次数不能超过 20")
            Integer maxFailedAttempts,
            @NotNull(message = "登录失败观察窗口不能为空")
            @Min(value = 1, message = "登录失败观察窗口不能小于 1 分钟")
            @Max(value = 1440, message = "登录失败观察窗口不能超过 1440 分钟")
            Integer windowMinutes,
            @NotNull(message = "登录锁定时长不能为空")
            @Min(value = 1, message = "登录锁定时长不能小于 1 分钟")
            @Max(value = 1440, message = "登录锁定时长不能超过 1440 分钟")
            Integer lockMinutes
    ) {
    }

    public record Password(
            @NotNull(message = "密码最小长度不能为空")
            @Min(value = 1, message = "密码最小长度必须大于 0")
            @Max(value = 64, message = "密码最小长度不能超过 64")
            Integer minLength,
            @NotNull(message = "密码最大长度不能为空")
            @Min(value = 1, message = "密码最大长度必须大于 0")
            @Max(value = 64, message = "密码最大长度不能超过 64")
            Integer maxLength,
            @NotNull(message = "小写字母规则不能为空") Boolean requireLowercase,
            @NotNull(message = "大写字母规则不能为空") Boolean requireUppercase,
            @NotNull(message = "数字规则不能为空") Boolean requireDigit,
            @NotNull(message = "特殊字符规则不能为空") Boolean requireSpecial,
            @NotNull(message = "历史密码次数不能为空")
            @Min(value = 1, message = "历史密码次数不能小于 1")
            @Max(value = 10, message = "历史密码次数不能超过 10")
            Integer historyCount
    ) {
    }
}
