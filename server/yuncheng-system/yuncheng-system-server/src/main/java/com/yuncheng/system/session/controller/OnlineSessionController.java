package com.yuncheng.system.session.controller;

import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.system.session.constant.OnlineSessionPermissionCodes;
import com.yuncheng.system.session.dto.OnlineSessionIdsRequest;
import com.yuncheng.system.session.dto.OnlineSessionItem;
import com.yuncheng.system.session.dto.OnlineSessionPageQuery;
import com.yuncheng.system.session.service.OnlineSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 在线会话查询和强制下线接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/system/sessions")
@Tag(name = "在线会话")
public class OnlineSessionController {

    private final OnlineSessionService onlineSessionService;

    public OnlineSessionController(OnlineSessionService onlineSessionService) {
        this.onlineSessionService = onlineSessionService;
    }

    @GetMapping
    @Operation(summary = "分页查询在线会话")
    @RequirePermission(OnlineSessionPermissionCodes.QUERY)
    public ApiResponse<PageResult<OnlineSessionItem>> page(@Valid OnlineSessionPageQuery query) {
        return ApiResponse.success(onlineSessionService.page(query));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "查询在线会话详情")
    @RequirePermission(OnlineSessionPermissionCodes.QUERY)
    public ApiResponse<OnlineSessionItem> detail(
            @PathVariable
            @NotBlank(message = "会话标识不能为空")
            @Size(max = 64, message = "会话标识不能超过 64 个字符")
            String sessionId
    ) {
        return ApiResponse.success(onlineSessionService.detail(sessionId));
    }

    @DeleteMapping("/{sessionId}")
    @RequirePermission(OnlineSessionPermissionCodes.KICKOUT)
    @OperationLog("强制下线会话")
    public ApiResponse<Void> kickout(
            @PathVariable
            @NotBlank(message = "会话标识不能为空")
            @Size(max = 64, message = "会话标识不能超过 64 个字符")
            String sessionId
    ) {
        onlineSessionService.kickout(sessionId);
        return ApiResponse.success(null);
    }

    @PostMapping("/batch-kickout")
    @RequirePermission(OnlineSessionPermissionCodes.KICKOUT)
    @OperationLog("批量强制下线会话")
    public ApiResponse<Void> batchKickout(@Valid @RequestBody OnlineSessionIdsRequest request) {
        onlineSessionService.batchKickout(request.sessionIds());
        return ApiResponse.success(null);
    }
}
