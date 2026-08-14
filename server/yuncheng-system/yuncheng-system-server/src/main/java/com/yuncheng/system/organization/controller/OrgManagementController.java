package com.yuncheng.system.organization.controller;

import com.yuncheng.framework.excel.ExcelFileSupport;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.framework.web.response.AvailabilityResponse;
import com.yuncheng.system.exchange.dto.ExcelImportResult;
import com.yuncheng.system.organization.constant.OrgPermissionCodes;
import com.yuncheng.system.organization.dto.OrgCreateRequest;
import com.yuncheng.system.organization.dto.OrgDetail;
import com.yuncheng.system.organization.dto.OrgItem;
import com.yuncheng.system.organization.dto.OrgListQuery;
import com.yuncheng.system.organization.dto.OrgMoveImpact;
import com.yuncheng.system.organization.dto.OrgMoveRequest;
import com.yuncheng.system.organization.dto.OrgUpdateRequest;
import com.yuncheng.system.organization.dto.OrgUniquenessCheckRequest;
import com.yuncheng.system.organization.service.OrgCommandService;
import com.yuncheng.system.organization.service.OrgExchangeService;
import com.yuncheng.system.organization.service.OrgQueryService;
import com.yuncheng.system.organization.service.OrgUniquenessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 组织管理接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/system/orgs")
@Tag(name = "组织管理")
public class OrgManagementController {

    private final OrgQueryService queryService;
    private final OrgCommandService commandService;
    private final OrgUniquenessService uniquenessService;
    private final OrgExchangeService exchangeService;

    public OrgManagementController(
            OrgQueryService queryService,
            OrgCommandService commandService,
            OrgUniquenessService uniquenessService,
            OrgExchangeService exchangeService
    ) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.uniquenessService = uniquenessService;
        this.exchangeService = exchangeService;
    }

    @GetMapping
    @Operation(summary = "查询组织列表")
    @RequirePermission(OrgPermissionCodes.QUERY)
    public ApiResponse<List<OrgItem>> list(
            @Valid OrgListQuery query
    ) {
        return ApiResponse.success(queryService.list(query));
    }

    @GetMapping("/children")
    @Operation(summary = "查询下级组织")
    @RequirePermission(OrgPermissionCodes.QUERY)
    public ApiResponse<List<OrgItem>> children(
            @RequestParam(required = false)
            @Positive(message = "上级组织主键必须大于 0")
            Long parentId
    ) {
        return ApiResponse.success(queryService.children(parentId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询组织详情")
    @RequirePermission(OrgPermissionCodes.QUERY)
    public ApiResponse<OrgDetail> detail(@PathVariable @Positive Long id) {
        return ApiResponse.success(queryService.detail(id));
    }

    @PostMapping
    @RequirePermission(OrgPermissionCodes.ADD)
    @OperationLog("新增组织")
    public ApiResponse<String> create(@Valid @RequestBody OrgCreateRequest request) {
        return ApiResponse.success(commandService.create(request).toString());
    }

    @GetMapping("/import-template")
    @Operation(summary = "下载组织导入模板")
    @RequirePermission(OrgPermissionCodes.ADD)
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelFileSupport.writeXlsx(
                exchangeService.template(),
                "组织导入模板.xlsx",
                response
        );
    }

    @PostMapping("/import")
    @RequirePermission(OrgPermissionCodes.ADD)
    @OperationLog("导入组织")
    public ApiResponse<ExcelImportResult> importOrgs(
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success(exchangeService.importOrgs(file));
    }

    @GetMapping("/export")
    @RequirePermission(OrgPermissionCodes.EXPORT)
    @OperationLog("导出组织")
    public void exportOrgs(HttpServletResponse response) throws IOException {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        ExcelFileSupport.writeXlsx(
                exchangeService.exportOrgs(),
                "组织数据_" + date + ".xlsx",
                response
        );
    }

    @PostMapping("/uniqueness-check")
    @Operation(summary = "校验组织唯一性")
    @RequirePermission({OrgPermissionCodes.ADD, OrgPermissionCodes.EDIT})
    public ApiResponse<AvailabilityResponse> checkUniqueness(
            @Valid @RequestBody OrgUniquenessCheckRequest request
    ) {
        boolean available = uniquenessService.isAvailable(
                request.field(), request.value(), request.parentId(), request.id());
        return ApiResponse.success(new AvailabilityResponse(available));
    }

    @PutMapping("/{id}")
    @RequirePermission(OrgPermissionCodes.EDIT)
    @OperationLog("编辑组织")
    public ApiResponse<Void> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody OrgUpdateRequest request
    ) {
        commandService.update(id, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/move-impact")
    @Operation(summary = "查询组织移动影响")
    @RequirePermission(OrgPermissionCodes.MOVE)
    public ApiResponse<OrgMoveImpact> moveImpact(
            @PathVariable @Positive Long id,
            @RequestParam(required = false)
            @Positive(message = "上级组织主键必须大于 0")
            Long parentId
    ) {
        return ApiResponse.success(commandService.moveImpact(id, parentId));
    }

    @PutMapping("/{id}/parent")
    @RequirePermission(OrgPermissionCodes.MOVE)
    @OperationLog("移动组织")
    public ApiResponse<Void> move(
            @PathVariable @Positive Long id,
            @Valid @RequestBody OrgMoveRequest request
    ) {
        commandService.move(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission(OrgPermissionCodes.DELETE)
    @OperationLog("删除组织")
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        commandService.delete(id);
        return ApiResponse.success(null);
    }
}
