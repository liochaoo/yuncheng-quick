package com.yuncheng.framework.openapi;

import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 接口文档状态查询。 */
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/openapi")
public class OpenApiController {

    private static final String DOCUMENT_URL = "/v3/api-docs";

    private final boolean enabled;

    public OpenApiController(@Value("${springdoc.api-docs.enabled:false}") boolean enabled) {
        this.enabled = enabled;
    }

    @GetMapping("/config")
    @RequirePermission(OpenApiPermissionCodes.QUERY)
    public ApiResponse<OpenApiStatusResponse> config() {
        return ApiResponse.success(new OpenApiStatusResponse(
                enabled,
                enabled ? DOCUMENT_URL : null
        ));
    }
}
