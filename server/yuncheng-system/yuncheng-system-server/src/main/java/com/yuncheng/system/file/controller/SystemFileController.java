package com.yuncheng.system.file.controller;

import com.yuncheng.framework.file.constant.FilePolicyCodes;
import com.yuncheng.framework.file.controller.AbstractFileContentController;
import com.yuncheng.framework.file.service.FileService;
import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.system.file.constant.FilePermissionCodes;
import com.yuncheng.system.file.dto.FileDetail;
import com.yuncheng.system.file.dto.FileIdListRequest;
import com.yuncheng.system.file.dto.FileListItem;
import com.yuncheng.system.file.dto.FilePageQuery;
import com.yuncheng.system.file.service.SystemFileCommandService;
import com.yuncheng.system.file.service.SystemFileQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 文件管理接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/system/files")
@Tag(name = "系统文件管理")
public class SystemFileController extends AbstractFileContentController {

    private final FileService fileService;
    private final SystemFileQueryService queryService;
    private final SystemFileCommandService commandService;

    public SystemFileController(
            FileService fileService,
            SystemFileQueryService queryService,
            SystemFileCommandService commandService
    ) {
        this.fileService = fileService;
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @GetMapping
    @Operation(summary = "分页查询系统文件")
    @RequirePermission(FilePermissionCodes.QUERY)
    public ApiResponse<PageResult<FileListItem>> page(@Valid FilePageQuery query) {
        return ApiResponse.success(queryService.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询系统文件详情")
    @RequirePermission(FilePermissionCodes.QUERY)
    public ApiResponse<FileDetail> detail(@PathVariable @Positive Long id) {
        return ApiResponse.success(queryService.detail(id));
    }

    @PostMapping
    @RequirePermission(FilePermissionCodes.UPLOAD)
    @OperationLog("上传文件")
    public ApiResponse<FileDetail> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = FilePolicyCodes.ATTACHMENT) String policyCode
    ) {
        return ApiResponse.success(commandService.upload(file, policyCode));
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "预览系统文件")
    @RequirePermission(FilePermissionCodes.PREVIEW)
    public void preview(
            @PathVariable @Positive Long id,
            HttpServletResponse response
    ) throws IOException {
        writeContent(fileService.openContent(id, false), false, false, response);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "下载系统文件")
    @RequirePermission(FilePermissionCodes.DOWNLOAD)
    public void download(
            @PathVariable @Positive Long id,
            HttpServletResponse response
    ) throws IOException {
        writeContent(fileService.openContent(id, false), true, false, response);
    }

    @DeleteMapping("/{id}")
    @RequirePermission(FilePermissionCodes.DELETE)
    @OperationLog("删除文件")
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        commandService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/batch-delete")
    @RequirePermission(FilePermissionCodes.DELETE)
    @OperationLog("批量删除文件")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody FileIdListRequest request) {
        commandService.batchDelete(request.ids());
        return ApiResponse.success(null);
    }
}
