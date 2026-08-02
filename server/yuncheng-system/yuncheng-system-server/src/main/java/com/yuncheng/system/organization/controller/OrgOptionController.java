package com.yuncheng.system.organization.controller;

import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.system.organization.dto.OrgContextItem;
import com.yuncheng.system.organization.dto.OrgIdsRequest;
import com.yuncheng.system.organization.dto.OrgItem;
import com.yuncheng.system.organization.dto.OrgPageQuery;
import com.yuncheng.system.organization.service.OrgQueryService;
import com.yuncheng.system.user.constant.UserPermissionCodes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 已登录业务模块消费组织的公共接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/orgs")
public class OrgOptionController {

    private final OrgQueryService queryService;

    public OrgOptionController(OrgQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @RequirePermission(UserPermissionCodes.QUERY)
    public ApiResponse<List<OrgItem>> children(
            @RequestParam(required = false)
            @Positive(message = "上级组织主键必须大于 0")
            Long parentId
    ) {
        return ApiResponse.success(queryService.children(parentId));
    }

    @GetMapping("/search")
    @RequirePermission(UserPermissionCodes.QUERY)
    public ApiResponse<PageResult<OrgItem>> search(
            @Valid OrgPageQuery query
    ) {
        return ApiResponse.success(queryService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission(UserPermissionCodes.QUERY)
    public ApiResponse<OrgContextItem> item(@PathVariable @Positive Long id) {
        return ApiResponse.success(queryService.contextItems(List.of(id)).getFirst());
    }

    @PostMapping("/by-ids")
    @RequirePermission(UserPermissionCodes.QUERY)
    public ApiResponse<List<OrgContextItem>> items(
            @Valid @RequestBody OrgIdsRequest request
    ) {
        return ApiResponse.success(queryService.contextItems(request.ids()));
    }
}
