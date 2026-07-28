package com.yuncheng.system.file.service;

import com.yuncheng.framework.file.service.FileService;
import com.yuncheng.system.file.dto.FileDetail;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 维护文件管理数据。 */
@Service
public class SystemFileCommandService {

    private final FileService fileService;
    private final SystemFileQueryService queryService;

    public SystemFileCommandService(
            FileService fileService,
            SystemFileQueryService queryService
    ) {
        this.fileService = fileService;
        this.queryService = queryService;
    }

    public FileDetail upload(MultipartFile file, String policyCode) {
        String fileId = fileService.upload(file, policyCode, null, null, null, null).id();
        return queryService.detail(Long.valueOf(fileId));
    }

    public void delete(Long fileId) {
        fileService.delete(fileId);
    }

    public void batchDelete(List<Long> fileIds) {
        fileIds.stream().distinct().forEach(this::delete);
    }
}
