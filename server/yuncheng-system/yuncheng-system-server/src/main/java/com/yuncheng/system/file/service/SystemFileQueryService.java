package com.yuncheng.system.file.service;

import com.yuncheng.framework.file.dto.FileMetadata;
import com.yuncheng.framework.file.dto.FileMetadataPageQuery;
import com.yuncheng.framework.file.service.FileMetadataService;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.system.file.dto.FileDetail;
import com.yuncheng.system.file.dto.FileListItem;
import com.yuncheng.system.file.dto.FilePageQuery;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 查询文件管理数据。 */
@Service
public class SystemFileQueryService {

    private static final String MANAGEMENT_PATH = WebConstants.API_PREFIX + "/system/files/";

    private final FileMetadataService metadataService;

    public SystemFileQueryService(FileMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    public PageResult<FileListItem> page(FilePageQuery query) {
        normalize(query);
        PageResult<FileMetadata> page = metadataService.page(new FileMetadataPageQuery(
                query.getPage(),
                query.getPageSize(),
                query.getOriginalName(),
                query.getStoragePlatform(),
                query.getPolicyCode(),
                query.getAccessType(),
                query.getBusinessType()
        ));
        List<FileListItem> items = page.items().stream().map(this::toListItem).toList();
        return new PageResult<>(items, page.total(), page.page(), page.pageSize());
    }

    public FileDetail detail(Long fileId) {
        return toDetail(metadataService.get(fileId));
    }

    private FileListItem toListItem(FileMetadata file) {
        return new FileListItem(
                id(file.id()), file.originalName(), file.fileExtension(),
                file.contentType(), file.fileSize(), file.storagePlatform(),
                file.policyCode(), file.accessType(),
                file.businessType(), id(file.businessId()), file.businessPosition(),
                file.sortOrder(), previewUrl(file.id()), downloadUrl(file.id()),
                file.createdAt()
        );
    }

    private FileDetail toDetail(FileMetadata file) {
        return new FileDetail(
                id(file.id()), file.originalName(), file.fileExtension(),
                file.contentType(), file.fileSize(), file.sha256(),
                file.storagePlatform(), file.objectKey(), file.policyCode(),
                file.accessType(), file.businessType(),
                id(file.businessId()), file.businessPosition(), file.sortOrder(),
                previewUrl(file.id()), downloadUrl(file.id()),
                file.createdAt(), id(file.createdBy()),
                file.updatedAt(), id(file.updatedBy())
        );
    }

    private void normalize(FilePageQuery query) {
        query.setOriginalName(trimToNull(query.getOriginalName()));
        query.setStoragePlatform(trimToNull(query.getStoragePlatform()));
        query.setPolicyCode(trimToNull(query.getPolicyCode()));
        query.setBusinessType(trimToNull(query.getBusinessType()));
    }

    private String previewUrl(Long fileId) {
        return MANAGEMENT_PATH + fileId + "/preview";
    }

    private String downloadUrl(Long fileId) {
        return MANAGEMENT_PATH + fileId + "/download";
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String id(Long value) {
        return value == null ? null : value.toString();
    }
}
