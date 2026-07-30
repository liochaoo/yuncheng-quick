package com.yuncheng.system.organization.controller;

import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.system.organization.constant.OrganizationPermissionCodes;
import com.yuncheng.system.organization.dto.OrganizationNodeCreateRequest;
import com.yuncheng.system.organization.dto.OrganizationNodeDetail;
import com.yuncheng.system.organization.dto.OrganizationNodeItem;
import com.yuncheng.system.organization.dto.OrganizationNodeMoveImpact;
import com.yuncheng.system.organization.dto.OrganizationNodeMoveRequest;
import com.yuncheng.system.organization.dto.OrganizationNodePageQuery;
import com.yuncheng.system.organization.dto.OrganizationNodeUpdateRequest;
import com.yuncheng.system.organization.service.OrganizationCommandService;
import com.yuncheng.system.organization.service.OrganizationQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 组织节点管理接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/system/organization-nodes")
public class OrganizationManagementController {

    private final OrganizationQueryService queryService;
    private final OrganizationCommandService commandService;

    public OrganizationManagementController(
            OrganizationQueryService queryService,
            OrganizationCommandService commandService
    ) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @GetMapping
    @RequirePermission(OrganizationPermissionCodes.QUERY)
    public ApiResponse<PageResult<OrganizationNodeItem>> page(
            @Valid OrganizationNodePageQuery query
    ) {
        return ApiResponse.success(queryService.page(query));
    }

    @GetMapping("/children")
    @RequirePermission(OrganizationPermissionCodes.QUERY)
    public ApiResponse<List<OrganizationNodeItem>> children(
            @RequestParam(required = false)
            @Positive(message = "上级组织主键必须大于 0")
            Long parentId
    ) {
        return ApiResponse.success(queryService.children(parentId));
    }

    @GetMapping("/{id}")
    @RequirePermission(OrganizationPermissionCodes.QUERY)
    public ApiResponse<OrganizationNodeDetail> detail(@PathVariable @Positive Long id) {
        return ApiResponse.success(queryService.detail(id));
    }

    @PostMapping
    @RequirePermission(OrganizationPermissionCodes.ADD)
    @OperationLog("新增组织节点")
    public ApiResponse<String> create(@Valid @RequestBody OrganizationNodeCreateRequest request) {
        return ApiResponse.success(commandService.create(request).toString());
    }

    @PutMapping("/{id}")
    @RequirePermission(OrganizationPermissionCodes.EDIT)
    @OperationLog("编辑组织节点")
    public ApiResponse<Void> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody OrganizationNodeUpdateRequest request
    ) {
        commandService.update(id, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/move-impact")
    @RequirePermission(OrganizationPermissionCodes.MOVE)
    public ApiResponse<OrganizationNodeMoveImpact> moveImpact(
            @PathVariable @Positive Long id,
            @RequestParam(required = false)
            @Positive(message = "上级组织主键必须大于 0")
            Long parentId
    ) {
        return ApiResponse.success(commandService.moveImpact(id, parentId));
    }

    @PutMapping("/{id}/parent")
    @RequirePermission(OrganizationPermissionCodes.MOVE)
    @OperationLog("移动组织节点")
    public ApiResponse<Void> move(
            @PathVariable @Positive Long id,
            @Valid @RequestBody OrganizationNodeMoveRequest request
    ) {
        commandService.move(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission(OrganizationPermissionCodes.DELETE)
    @OperationLog("删除组织节点")
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        commandService.delete(id);
        return ApiResponse.success(null);
    }
}
