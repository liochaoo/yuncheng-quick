package com.yuncheng.framework.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.file.config.FileProperties;
import com.yuncheng.framework.file.dto.FileAssociationRequest;
import com.yuncheng.framework.file.dto.FileRecord;
import com.yuncheng.framework.file.entity.PlatformFile;
import com.yuncheng.framework.file.enums.FileAccessType;
import com.yuncheng.framework.file.mapper.PlatformFileMapper;
import com.yuncheng.framework.file.service.FileStorageGateway.StoredFile;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.exception.PlatformException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** 默认通用文件服务。 */
@Service
public class DefaultFileService implements FileService {

    private static final Logger log = LoggerFactory.getLogger(DefaultFileService.class);
    private static final int MAX_ORIGINAL_NAME_LENGTH = 255;
    private static final int MAX_OBJECT_KEY_LENGTH = 1000;

    private final PlatformFileMapper fileMapper;
    private final FilePolicyService policyService;
    private final FileContentTypeRules contentTypeRules;
    private final FileStorageGateway storageGateway;

    public DefaultFileService(
            PlatformFileMapper fileMapper,
            FilePolicyService policyService,
            FileContentTypeRules contentTypeRules,
            FileStorageGateway storageGateway
    ) {
        this.fileMapper = fileMapper;
        this.policyService = policyService;
        this.contentTypeRules = contentTypeRules;
        this.storageGateway = storageGateway;
    }

    @Override
    @Transactional
    public FileRecord upload(
            MultipartFile file,
            String policyCode,
            String businessType,
            Long businessId,
            String businessPosition,
            Integer sortOrder
    ) {
        String normalizedPolicyCode = policyService.normalizePolicyCode(policyCode);
        FileProperties.Policy policy = policyService.requirePolicy(normalizedPolicyCode);
        String originalName = normalizeOriginalName(file == null ? null : file.getOriginalFilename());
        String extension = normalizeExtension(originalName);
        policyService.validateBeforeUpload(file, extension, policy);
        String detectedContentType = contentTypeRules.detect(file, extension);
        contentTypeRules.validate(extension, detectedContentType);
        validateAssociation(businessType, businessId, businessPosition);
        String fileSha256 = sha256(file);

        StoredFile storedFile = storageGateway.upload(
                file,
                buildStoragePath(normalizedPolicyCode),
                buildStorageFilename(extension)
        );
        try {
            validateStoredFile(storedFile);
            PlatformFile entity = new PlatformFile();
            entity.setStoragePlatform(storedFile.storagePlatform());
            entity.setObjectKey(storedFile.objectKey());
            entity.setOriginalName(originalName);
            entity.setFileExtension(extension);
            entity.setContentType(detectedContentType);
            entity.setFileSize(storedFile.fileSize());
            entity.setSha256(fileSha256);
            entity.setPolicyCode(normalizedPolicyCode);
            entity.setAccessType(policyService.resolveAccessType(normalizedPolicyCode).name());
            entity.setBusinessType(trimToNull(businessType));
            entity.setBusinessId(businessId);
            entity.setBusinessPosition(trimToNull(businessPosition));
            entity.setSortOrder(sortOrder == null ? 0 : sortOrder);
            fileMapper.insert(entity);
            return toRecord(entity);
        } catch (RuntimeException exception) {
            cleanupUploadedFile(storedFile, originalName);
            throw exception;
        }
    }

    @Override
    public FileRecord get(Long id) {
        return toRecord(requireFile(id));
    }

    @Override
    public List<FileRecord> list(
            String businessType,
            Long businessId,
            String businessPosition
    ) {
        validateAssociation(businessType, businessId, businessPosition);
        return fileMapper.selectList(new LambdaQueryWrapper<PlatformFile>()
                        .eq(PlatformFile::getBusinessType, businessType.trim())
                        .eq(PlatformFile::getBusinessId, businessId)
                        .eq(PlatformFile::getBusinessPosition, businessPosition.trim())
                        .orderByAsc(PlatformFile::getSortOrder)
                        .orderByAsc(PlatformFile::getId))
                .stream()
                .map(this::toRecord)
                .toList();
    }

    @Override
    @Transactional
    public void associate(Long id, FileAssociationRequest request) {
        PlatformFile file = requireFile(id);
        file.setBusinessType(request.businessType().trim());
        file.setBusinessId(request.businessId());
        file.setBusinessPosition(request.businessPosition().trim());
        file.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        fileMapper.updateById(file);
    }

    @Override
    @Transactional
    public void removeAssociation(Long id) {
        PlatformFile file = requireFile(id);
        file.setBusinessType(null);
        file.setBusinessId(null);
        file.setBusinessPosition(null);
        file.setSortOrder(0);
        fileMapper.updateById(file);
    }

    @Override
    public FileContent openContent(Long id, boolean publicOnly) {
        PlatformFile file = requireFile(id);
        if (publicOnly && !FileAccessType.PUBLIC.name().equals(file.getAccessType())) {
            throw PlatformException.notFound("文件不存在");
        }
        return new FileContent(toRecord(file), outputStream -> storageGateway.write(file, outputStream));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PlatformFile file = requireFile(id);
        storageGateway.delete(file);
        fileMapper.deleteById(id);
    }

    private PlatformFile requireFile(Long id) {
        PlatformFile file = fileMapper.selectById(id);
        if (file == null) {
            throw PlatformException.notFound("文件不存在");
        }
        return file;
    }

    private void validateAssociation(
            String businessType,
            Long businessId,
            String businessPosition
    ) {
        boolean allEmpty = !StringUtils.hasText(businessType)
                && businessId == null
                && !StringUtils.hasText(businessPosition);
        boolean allPresent = StringUtils.hasText(businessType)
                && businessId != null
                && businessId > 0
                && StringUtils.hasText(businessPosition);
        if (!allEmpty && !allPresent) {
            throw PlatformException.badRequest("文件业务关联信息不完整");
        }
        if (StringUtils.hasText(businessType) && businessType.trim().length() > 64) {
            throw PlatformException.badRequest("业务类型不能超过64个字符");
        }
        if (StringUtils.hasText(businessPosition) && businessPosition.trim().length() > 64) {
            throw PlatformException.badRequest("业务位置不能超过64个字符");
        }
    }

    private String normalizeOriginalName(String originalName) {
        if (!StringUtils.hasText(originalName)) {
            throw PlatformException.badRequest("文件名不能为空");
        }
        String normalized = StringUtils.getFilename(StringUtils.cleanPath(originalName.trim()));
        if (!StringUtils.hasText(normalized) || normalized.length() > MAX_ORIGINAL_NAME_LENGTH) {
            throw PlatformException.badRequest("文件名不能超过255个字符");
        }
        return normalized;
    }

    private String normalizeExtension(String filename) {
        String extension = StringUtils.getFilenameExtension(filename);
        return extension == null ? "" : extension.toLowerCase(Locale.ROOT);
    }

    private String buildStoragePath(String policyCode) {
        String datePath = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return policyCode + "/" + datePath + "/";
    }

    private String buildStorageFilename(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    private String normalizeContentType(String contentType) {
        return StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
    }

    private void validateStoredFile(StoredFile storedFile) {
        if (!StringUtils.hasText(storedFile.storagePlatform())) {
            log.error("文件上传完成，但存储组件未返回存储平台标识");
            throw PlatformException.serviceUnavailable("文件保存失败，请稍后重试");
        }
        if (!StringUtils.hasText(storedFile.objectKey())
                || storedFile.objectKey().length() > MAX_OBJECT_KEY_LENGTH) {
            log.error("文件上传完成，但存储组件返回的文件位置无效");
            throw PlatformException.serviceUnavailable("文件保存失败，请稍后重试");
        }
    }

    private String sha256(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) >= 0) {
                digest.update(buffer, 0, length);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException exception) {
            log.warn("读取上传文件失败", exception);
            throw PlatformException.badRequest("文件读取失败，请重新选择文件");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境不支持SHA-256", exception);
        }
    }

    private void cleanupUploadedFile(StoredFile storedFile, String originalName) {
        PlatformFile temporary = new PlatformFile();
        temporary.setStoragePlatform(storedFile.storagePlatform());
        temporary.setObjectKey(storedFile.objectKey());
        temporary.setOriginalName(originalName);
        temporary.setContentType(normalizeContentType(storedFile.contentType()));
        temporary.setFileSize(storedFile.fileSize());
        try {
            storageGateway.delete(temporary);
        } catch (RuntimeException ignored) {
            // 原始异常更能说明上传失败原因，清理失败由存储网关记录日志。
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private FileRecord toRecord(PlatformFile file) {
        FileAccessType accessType = FileAccessType.valueOf(file.getAccessType());
        String basePath = accessType == FileAccessType.PUBLIC
                ? WebConstants.API_PREFIX + "/public/files/"
                : WebConstants.API_PREFIX + "/files/";
        String id = file.getId().toString();
        return new FileRecord(
                id,
                file.getOriginalName(),
                file.getFileExtension(),
                file.getContentType(),
                file.getFileSize(),
                file.getPolicyCode(),
                accessType,
                file.getBusinessType(),
                file.getBusinessId() == null ? null : file.getBusinessId().toString(),
                file.getBusinessPosition(),
                file.getSortOrder(),
                basePath + id + "/preview",
                basePath + id + "/download",
                file.getCreatedAt()
        );
    }
}
