package com.yuncheng.framework.openapi;

import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 接口文档状态查询。 */
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/openapi")
public class OpenApiController {

    private static final String DOCUMENT_URL = "/v3/api-docs";

    private final OpenApiProperties properties;

    public OpenApiController(OpenApiProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/config")
    @RequirePermission(OpenApiPermissionCodes.QUERY)
    public ApiResponse<OpenApiStatusResponse> config() {
        boolean enabled = properties.isEnabled();
        return ApiResponse.success(new OpenApiStatusResponse(
                enabled,
                enabled ? DOCUMENT_URL : null
        ));
    }
}
