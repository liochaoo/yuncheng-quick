package com.yuncheng.framework.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuncheng.framework.file.dto.FileMetadata;
import com.yuncheng.framework.file.dto.FileMetadataPageQuery;
import com.yuncheng.framework.file.entity.PlatformFile;
import com.yuncheng.framework.file.enums.FileAccessType;
import com.yuncheng.framework.file.mapper.PlatformFileMapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.framework.web.page.PageResult;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 统一封装文件表的内部查询能力。 */
@Service
public class FileMetadataService {

    private final PlatformFileMapper fileMapper;

    public FileMetadataService(PlatformFileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    public PageResult<FileMetadata> page(FileMetadataPageQuery query) {
        LambdaQueryWrapper<PlatformFile> wrapper = new LambdaQueryWrapper<PlatformFile>()
                .like(StringUtils.hasText(query.originalName()),
                        PlatformFile::getOriginalName, query.originalName())
                .like(StringUtils.hasText(query.storagePlatform()),
                        PlatformFile::getStoragePlatform, query.storagePlatform())
                .eq(StringUtils.hasText(query.policyCode()),
                        PlatformFile::getPolicyCode, query.policyCode())
                .eq(query.accessType() != null,
                        PlatformFile::getAccessType,
                        query.accessType() == null ? null : query.accessType().name())
                .eq(StringUtils.hasText(query.businessType()),
                        PlatformFile::getBusinessType, query.businessType())
                .orderByDesc(PlatformFile::getCreatedAt)
                .orderByDesc(PlatformFile::getId);
        IPage<PlatformFile> page = fileMapper.selectPage(
                new Page<>(query.page(), query.pageSize()),
                wrapper
        );
        List<FileMetadata> items = page.getRecords().stream()
                .map(this::toMetadata)
                .toList();
        return new PageResult<>(items, page.getTotal(), query.page(), query.pageSize());
    }

    public FileMetadata get(Long fileId) {
        PlatformFile file = fileId == null ? null : fileMapper.selectById(fileId);
        if (file == null) {
            throw PlatformException.notFound("文件不存在");
        }
        return toMetadata(file);
    }

    private FileMetadata toMetadata(PlatformFile file) {
        return new FileMetadata(
                file.getId(),
                file.getStoragePlatform(),
                file.getObjectKey(),
                file.getOriginalName(),
                file.getFileExtension(),
                file.getContentType(),
                file.getFileSize(),
                file.getSha256(),
                file.getPolicyCode(),
                FileAccessType.valueOf(file.getAccessType()),
                file.getBusinessType(),
                file.getBusinessId(),
                file.getBusinessPosition(),
                file.getSortOrder(),
                file.getCreatedAt(),
                file.getCreatedBy(),
                file.getUpdatedAt(),
                file.getUpdatedBy()
        );
    }
}
