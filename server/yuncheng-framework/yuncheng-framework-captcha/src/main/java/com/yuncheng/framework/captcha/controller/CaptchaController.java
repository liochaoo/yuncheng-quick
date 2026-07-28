package com.yuncheng.framework.captcha.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.yuncheng.framework.captcha.CaptchaScene;
import com.yuncheng.framework.captcha.dto.CaptchaCheckRequest;
import com.yuncheng.framework.captcha.dto.CaptchaVerificationResponse;
import com.yuncheng.framework.captcha.service.CaptchaGenerationRateLimiter;
import com.yuncheng.framework.captcha.service.CaptchaVerificationService;
import com.yuncheng.framework.web.client.ClientRequestInfoResolver;
import com.yuncheng.framework.web.constant.WebConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 获取并校验图形验证码。 */
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/auth/captcha")
public class CaptchaController {

    private final ImageCaptchaApplication captchaApplication;
    private final CaptchaGenerationRateLimiter generationRateLimiter;
    private final CaptchaVerificationService verificationService;
    private final ClientRequestInfoResolver requestInfoResolver;

    public CaptchaController(
            ImageCaptchaApplication captchaApplication,
            CaptchaGenerationRateLimiter generationRateLimiter,
            CaptchaVerificationService verificationService,
            ClientRequestInfoResolver requestInfoResolver
    ) {
        this.captchaApplication = captchaApplication;
        this.generationRateLimiter = generationRateLimiter;
        this.verificationService = verificationService;
        this.requestInfoResolver = requestInfoResolver;
    }

    @PostMapping("/get")
    public ApiResponse<ImageCaptchaVO> get(
            @RequestParam CaptchaScene scene,
            HttpServletRequest request
    ) {
        generationRateLimiter.requireAllowed(requestInfoResolver.resolve(request).ip());
        return captchaApplication.generateCaptcha(CaptchaTypeConstant.SLIDER);
    }

    @PostMapping("/check")
    public ApiResponse<CaptchaVerificationResponse> check(
            @RequestParam CaptchaScene scene,
            @RequestBody CaptchaCheckRequest request
    ) {
        if (request == null || request.getId() == null || request.getId().isBlank()) {
            return ApiResponse.ofCheckError("验证码参数不能为空");
        }
        ImageCaptchaTrack track = request.resolveTrack();
        if (track == null) {
            return ApiResponse.ofCheckError("验证码轨迹不能为空");
        }
        ApiResponse<?> result = captchaApplication.matching(request.getId(), track);
        if (!result.isSuccess()) {
            return ApiResponse.of(result.getCode(), result.getMsg(), null);
        }
        String verification = verificationService.issue(scene);
        return ApiResponse.ofSuccess(new CaptchaVerificationResponse(verification));
    }
}
