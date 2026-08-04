package com.yuncheng.system.dictionary.controller;

import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.framework.web.response.AvailabilityResponse;
import com.yuncheng.system.dictionary.constant.DictionaryPermissionCodes;
import com.yuncheng.system.dictionary.dto.DictionaryCreateRequest;
import com.yuncheng.system.dictionary.dto.DictionaryDetail;
import com.yuncheng.system.dictionary.dto.DictionaryListItem;
import com.yuncheng.system.dictionary.dto.DictionaryOptionCreateRequest;
import com.yuncheng.system.dictionary.dto.DictionaryOptionDetail;
import com.yuncheng.system.dictionary.dto.DictionaryOptionListItem;
import com.yuncheng.system.dictionary.dto.DictionaryOptionPageQuery;
import com.yuncheng.system.dictionary.dto.DictionaryOptionStatusRequest;
import com.yuncheng.system.dictionary.dto.DictionaryOptionUniquenessCheckRequest;
import com.yuncheng.system.dictionary.dto.DictionaryOptionUpdateRequest;
import com.yuncheng.system.dictionary.dto.DictionaryPageQuery;
import com.yuncheng.system.dictionary.dto.DictionaryUniquenessCheckRequest;
import com.yuncheng.system.dictionary.dto.DictionaryUpdateRequest;
import com.yuncheng.system.dictionary.service.DictionaryCommandService;
import com.yuncheng.system.dictionary.service.DictionaryQueryService;
import com.yuncheng.system.dictionary.service.DictionaryUniquenessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 数据字典管理接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/system/dictionaries")
@Tag(name = "字典管理")
public class DictionaryManagementController {

    private final DictionaryQueryService queryService;
    private final DictionaryCommandService commandService;
    private final DictionaryUniquenessService uniquenessService;

    public DictionaryManagementController(
            DictionaryQueryService queryService,
            DictionaryCommandService commandService,
            DictionaryUniquenessService uniquenessService
    ) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.uniquenessService = uniquenessService;
    }

    @GetMapping
    @Operation(summary = "分页查询数据字典")
    @RequirePermission(DictionaryPermissionCodes.QUERY)
    public ApiResponse<PageResult<DictionaryListItem>> page(
            @Valid DictionaryPageQuery query
    ) {
        return ApiResponse.success(queryService.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询数据字典详情")
    @RequirePermission(DictionaryPermissionCodes.QUERY)
    public ApiResponse<DictionaryDetail> detail(@PathVariable @Positive Long id) {
        return ApiResponse.success(queryService.detail(id));
    }

    @PostMapping
    @RequirePermission(DictionaryPermissionCodes.ADD)
    @OperationLog("新增数据字典")
    public ApiResponse<String> create(@Valid @RequestBody DictionaryCreateRequest request) {
        return ApiResponse.success(commandService.create(request).toString());
    }

    @PostMapping("/uniqueness-check")
    @Operation(summary = "校验数据字典唯一性")
    @RequirePermission(DictionaryPermissionCodes.ADD)
    public ApiResponse<AvailabilityResponse> checkUniqueness(
            @Valid @RequestBody DictionaryUniquenessCheckRequest request
    ) {
        return ApiResponse.success(new AvailabilityResponse(
                uniquenessService.isDictionaryCodeAvailable(request.value())
        ));
    }

    @PutMapping("/{id}")
    @RequirePermission(DictionaryPermissionCodes.EDIT)
    @OperationLog("编辑数据字典")
    public ApiResponse<Void> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DictionaryUpdateRequest request
    ) {
        commandService.update(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission(DictionaryPermissionCodes.DELETE)
    @OperationLog("删除数据字典")
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        commandService.delete(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{dictionaryId}/options")
    @Operation(summary = "分页查询字典选项")
    @RequirePermission(DictionaryPermissionCodes.QUERY)
    public ApiResponse<PageResult<DictionaryOptionListItem>> pageOptions(
            @PathVariable @Positive Long dictionaryId,
            @Valid DictionaryOptionPageQuery query
    ) {
        return ApiResponse.success(queryService.pageOptions(dictionaryId, query));
    }

    @GetMapping("/{dictionaryId}/options/{optionId}")
    @Operation(summary = "查询字典选项详情")
    @RequirePermission(DictionaryPermissionCodes.QUERY)
    public ApiResponse<DictionaryOptionDetail> optionDetail(
            @PathVariable @Positive Long dictionaryId,
            @PathVariable @Positive Long optionId
    ) {
        return ApiResponse.success(queryService.optionDetail(dictionaryId, optionId));
    }

    @PostMapping("/{dictionaryId}/options")
    @RequirePermission(DictionaryPermissionCodes.ADD)
    @OperationLog("新增字典选项")
    public ApiResponse<String> createOption(
            @PathVariable @Positive Long dictionaryId,
            @Valid @RequestBody DictionaryOptionCreateRequest request
    ) {
        return ApiResponse.success(commandService.createOption(dictionaryId, request).toString());
    }

    @PostMapping("/{dictionaryId}/options/uniqueness-check")
    @Operation(summary = "校验字典选项唯一性")
    @RequirePermission(DictionaryPermissionCodes.ADD)
    public ApiResponse<AvailabilityResponse> checkOptionUniqueness(
            @PathVariable @Positive Long dictionaryId,
            @Valid @RequestBody DictionaryOptionUniquenessCheckRequest request
    ) {
        queryService.requireDictionary(dictionaryId);
        return ApiResponse.success(new AvailabilityResponse(
                uniquenessService.isOptionValueAvailable(dictionaryId, request.value())
        ));
    }

    @PutMapping("/{dictionaryId}/options/{optionId}")
    @RequirePermission(DictionaryPermissionCodes.EDIT)
    @OperationLog("编辑字典选项")
    public ApiResponse<Void> updateOption(
            @PathVariable @Positive Long dictionaryId,
            @PathVariable @Positive Long optionId,
            @Valid @RequestBody DictionaryOptionUpdateRequest request
    ) {
        commandService.updateOption(dictionaryId, optionId, request);
        return ApiResponse.success(null);
    }

    @PutMapping("/{dictionaryId}/options/{optionId}/enabled")
    @RequirePermission(DictionaryPermissionCodes.CHANGE_STATUS)
    @OperationLog("启停字典选项")
    public ApiResponse<Void> changeOptionStatus(
            @PathVariable @Positive Long dictionaryId,
            @PathVariable @Positive Long optionId,
            @Valid @RequestBody DictionaryOptionStatusRequest request
    ) {
        commandService.changeOptionStatus(dictionaryId, optionId, request.enabled());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{dictionaryId}/options/{optionId}")
    @RequirePermission(DictionaryPermissionCodes.DELETE)
    @OperationLog("删除字典选项")
    public ApiResponse<Void> deleteOption(
            @PathVariable @Positive Long dictionaryId,
            @PathVariable @Positive Long optionId
    ) {
        commandService.deleteOption(dictionaryId, optionId);
        return ApiResponse.success(null);
    }
}
