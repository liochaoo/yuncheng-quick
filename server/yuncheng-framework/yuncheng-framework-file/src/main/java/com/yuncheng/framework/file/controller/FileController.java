package com.yuncheng.framework.file.controller;

import com.yuncheng.framework.file.dto.FileAssociationRequest;
import com.yuncheng.framework.file.dto.FileRecord;
import com.yuncheng.framework.file.service.CurrentUserFileService;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
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

/** 已登录用户通用文件接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/files")
@Tag(name = "通用文件")
public class FileController extends AbstractFileContentController {

    private final CurrentUserFileService currentUserFileService;

    public FileController(CurrentUserFileService currentUserFileService) {
        this.currentUserFileService = currentUserFileService;
    }

    @OperationLog("上传文件")
    @PostMapping
    public ApiResponse<FileRecord> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String policyCode,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Long businessId,
            @RequestParam(required = false) String businessPosition,
            @RequestParam(required = false) Integer sortOrder
    ) {
        return ApiResponse.success(currentUserFileService.upload(
                file,
                policyCode,
                businessType,
                businessId,
                businessPosition,
                sortOrder
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询文件详情")
    public ApiResponse<FileRecord> detail(@PathVariable @Positive Long id) {
        return ApiResponse.success(currentUserFileService.get(id));
    }

    @GetMapping
    @Operation(summary = "查询文件列表")
    public ApiResponse<List<FileRecord>> list(
            @RequestParam String businessType,
            @RequestParam @Positive Long businessId,
            @RequestParam String businessPosition
    ) {
        return ApiResponse.success(currentUserFileService.list(
                businessType,
                businessId,
                businessPosition
        ));
    }

    @OperationLog("关联文件")
    @PutMapping("/{id}/association")
    public ApiResponse<Void> associate(
            @PathVariable @Positive Long id,
            @Valid @RequestBody FileAssociationRequest request
    ) {
        currentUserFileService.associate(id, request);
        return ApiResponse.success(null);
    }

    @OperationLog("解除文件关联")
    @DeleteMapping("/{id}/association")
    public ApiResponse<Void> removeAssociation(@PathVariable @Positive Long id) {
        currentUserFileService.removeAssociation(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "预览文件")
    public void preview(
            @PathVariable @Positive Long id,
            HttpServletResponse response
    ) throws IOException {
        writeContent(currentUserFileService.openContent(id), false, false, response);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "下载文件")
    public void download(
            @PathVariable @Positive Long id,
            HttpServletResponse response
    ) throws IOException {
        writeContent(currentUserFileService.openContent(id), true, false, response);
    }

    @OperationLog("删除文件")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        currentUserFileService.delete(id);
        return ApiResponse.success(null);
    }
}
