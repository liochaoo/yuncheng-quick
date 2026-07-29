package com.yuncheng.system.login.experience.controller;

import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.system.login.experience.config.ExperienceProperties;
import com.yuncheng.system.login.experience.dto.ExperienceConfigResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 公开前端需要的体验环境标识。 */
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/auth/experience-config")
public class ExperienceConfigController {

    private final ExperienceProperties properties;

    public ExperienceConfigController(ExperienceProperties properties) {
        this.properties = properties;
    }

    @GetMapping
    public ApiResponse<ExperienceConfigResponse> get() {
        return ApiResponse.success(new ExperienceConfigResponse(properties.isEnabled()));
    }
}
