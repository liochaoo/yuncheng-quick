package com.yuncheng.system.organization.controller;

import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.system.organization.dto.OrganizationNodeItem;
import com.yuncheng.system.organization.dto.OrganizationNodePageQuery;
import com.yuncheng.system.organization.service.OrganizationQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 已登录业务模块消费组织节点的公共接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/organization-nodes")
public class OrganizationNodeOptionController {

    private final OrganizationQueryService queryService;

    public OrganizationNodeOptionController(OrganizationQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public ApiResponse<List<OrganizationNodeItem>> children(
            @RequestParam(required = false)
            @Positive(message = "上级组织主键必须大于 0")
            Long parentId
    ) {
        return ApiResponse.success(queryService.children(parentId));
    }

    @GetMapping("/search")
    public ApiResponse<PageResult<OrganizationNodeItem>> search(
            @Valid OrganizationNodePageQuery query
    ) {
        return ApiResponse.success(queryService.page(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrganizationNodeItem> item(@PathVariable @Positive Long id) {
        return ApiResponse.success(queryService.item(id));
    }
}
