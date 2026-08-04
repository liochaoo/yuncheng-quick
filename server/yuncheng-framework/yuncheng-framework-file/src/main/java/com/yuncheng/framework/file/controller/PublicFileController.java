package com.yuncheng.framework.file.controller;

import com.yuncheng.framework.file.service.FileService;
import com.yuncheng.framework.web.constant.WebConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 无需登录的公开文件读取接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/public/files")
@Tag(name = "公开文件")
public class PublicFileController extends AbstractFileContentController {

    private final FileService fileService;

    public PublicFileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "预览公开文件")
    public void preview(
            @PathVariable @Positive Long id,
            HttpServletResponse response
    ) throws IOException {
        writeContent(fileService.openContent(id, true), false, true, response);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "下载公开文件")
    public void download(
            @PathVariable @Positive Long id,
            HttpServletResponse response
    ) throws IOException {
        writeContent(fileService.openContent(id, true), true, true, response);
    }
}
