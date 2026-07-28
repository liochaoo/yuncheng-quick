package com.yuncheng.framework.file.service;

import com.yuncheng.common.context.CurrentUserContext;
import com.yuncheng.framework.file.dto.FileAssociationRequest;
import com.yuncheng.framework.file.dto.FileRecord;
import com.yuncheng.framework.file.dto.FileMetadata;
import com.yuncheng.framework.file.constant.FilePolicyCodes;
import com.yuncheng.framework.web.exception.PlatformException;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用 HTTP 文件接口的当前用户及业务数据操作边界。
 *
 * <p>系统文件管理、头像等专用业务入口直接调用 {@link FileService}，不经过本服务。</p>
 */
@Service
public class CurrentUserFileService {

    private final CurrentUserContext currentUserContext;
    private final FileMetadataService metadataService;
    private final FileBusinessAccessService businessAccessService;
    private final FileService fileService;

    public CurrentUserFileService(
            CurrentUserContext currentUserContext,
            FileMetadataService metadataService,
            FileBusinessAccessService businessAccessService,
            FileService fileService
    ) {
        this.currentUserContext = currentUserContext;
        this.metadataService = metadataService;
        this.businessAccessService = businessAccessService;
        this.fileService = fileService;
    }

    @Transactional
    public FileRecord upload(
            MultipartFile file,
            String policyCode,
            String businessType,
            Long businessId,
            String businessPosition,
            Integer sortOrder
    ) {
        String allowedPolicyCode = requireGeneralUploadPolicy(policyCode);
        requireBusinessWriteIfPresent(businessType, businessId, businessPosition);
        return fileService.upload(
                file, allowedPolicyCode, businessType, businessId, businessPosition, sortOrder
        );
    }

    public FileRecord get(Long fileId) {
        FileMetadata file = metadataService.get(fileId);
        requireRead(file);
        return fileService.get(fileId);
    }

    public List<FileRecord> list(
            String businessType,
            Long businessId,
            String businessPosition
    ) {
        requireCompleteAssociation(businessType, businessId, businessPosition);
        businessAccessService.requireRead(businessType, businessId);
        return fileService.list(businessType, businessId, businessPosition);
    }

    @Transactional
    public void associate(Long fileId, FileAssociationRequest request) {
        requireWrite(metadataService.get(fileId));
        businessAccessService.requireWrite(request.businessType(), request.businessId());
        fileService.associate(fileId, request);
    }

    @Transactional
    public void removeAssociation(Long fileId) {
        requireWrite(metadataService.get(fileId));
        fileService.removeAssociation(fileId);
    }

    @Transactional
    public void delete(Long fileId) {
        requireWrite(metadataService.get(fileId));
        fileService.delete(fileId);
    }

    public FileContent openContent(Long fileId) {
        FileMetadata file = metadataService.get(fileId);
        requireRead(file);
        return fileService.openContent(fileId, false);
    }

    private String requireGeneralUploadPolicy(String policyCode) {
        String normalizedPolicyCode = StringUtils.hasText(policyCode)
                ? policyCode.trim().toLowerCase(Locale.ROOT)
                : FilePolicyCodes.ATTACHMENT;
        if (!FilePolicyCodes.ATTACHMENT.equals(normalizedPolicyCode)
                && !FilePolicyCodes.IMAGE.equals(normalizedPolicyCode)) {
            throw PlatformException.badRequest("通用文件接口只允许上传私有附件或图片");
        }
        return normalizedPolicyCode;
    }

    private void requireRead(FileMetadata file) {
        if (isAssociated(file)) {
            businessAccessService.requireRead(file.businessType(), file.businessId());
            return;
        }
        requireUploader(file);
    }

    private void requireWrite(FileMetadata file) {
        if (isAssociated(file)) {
            businessAccessService.requireWrite(file.businessType(), file.businessId());
            return;
        }
        requireUploader(file);
    }

    private void requireBusinessWriteIfPresent(
            String businessType,
            Long businessId,
            String businessPosition
    ) {
        boolean hasAny = StringUtils.hasText(businessType)
                || businessId != null
                || StringUtils.hasText(businessPosition);
        if (!hasAny) {
            return;
        }
        requireCompleteAssociation(businessType, businessId, businessPosition);
        businessAccessService.requireWrite(businessType, businessId);
    }

    private void requireCompleteAssociation(
            String businessType,
            Long businessId,
            String businessPosition
    ) {
        if (!StringUtils.hasText(businessType)
                || businessId == null
                || businessId <= 0
                || !StringUtils.hasText(businessPosition)) {
            throw PlatformException.badRequest("文件业务关联信息不完整");
        }
    }

    private boolean isAssociated(FileMetadata file) {
        return StringUtils.hasText(file.businessType())
                && file.businessId() != null
                && StringUtils.hasText(file.businessPosition());
    }

    private void requireUploader(FileMetadata file) {
        if (!currentUserContext.getUserId().equals(file.createdBy())) {
            throw PlatformException.notFound("文件不存在");
        }
    }
}
