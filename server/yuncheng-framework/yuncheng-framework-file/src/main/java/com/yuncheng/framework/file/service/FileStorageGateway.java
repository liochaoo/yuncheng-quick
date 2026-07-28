package com.yuncheng.framework.file.service;

import com.yuncheng.framework.file.entity.PlatformFile;
import com.yuncheng.framework.web.exception.PlatformException;
import java.io.OutputStream;
import java.util.Objects;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** 将平台文件服务适配到具体存储组件。 */
@Component
public class FileStorageGateway {

    private static final Logger log = LoggerFactory.getLogger(FileStorageGateway.class);

    private final FileStorageService fileStorageService;

    public FileStorageGateway(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public StoredFile upload(MultipartFile file, String path, String filename) {
        try {
            FileInfo info = fileStorageService.of(file)
                    .setPath(path)
                    .setSaveFilename(filename)
                    .upload();
            if (info == null) {
                throw PlatformException.serviceUnavailable("文件存储服务暂时不可用");
            }
            String objectKey = value(info.getBasePath()) + value(info.getPath()) + value(info.getFilename());
            return new StoredFile(
                    info.getPlatform(),
                    objectKey,
                    info.getContentType(),
                    Objects.requireNonNullElse(info.getSize(), file.getSize())
            );
        } catch (PlatformException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("上传文件到存储平台失败", exception);
            throw PlatformException.serviceUnavailable("文件存储服务暂时不可用");
        }
    }

    public void write(PlatformFile file, OutputStream outputStream) {
        try {
            fileStorageService.download(toFileInfo(file)).outputStream(outputStream);
        } catch (RuntimeException exception) {
            log.error("从存储平台读取文件失败：文件ID={}", file.getId(), exception);
            throw PlatformException.serviceUnavailable("文件读取失败");
        }
    }

    public void delete(PlatformFile file) {
        try {
            if (!fileStorageService.delete(toFileInfo(file))) {
                throw PlatformException.serviceUnavailable("文件删除失败");
            }
        } catch (PlatformException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("从存储平台删除文件失败：文件ID={}", file.getId(), exception);
            throw PlatformException.serviceUnavailable("文件删除失败");
        }
    }

    private FileInfo toFileInfo(PlatformFile file) {
        return new FileInfo()
                .setPlatform(file.getStoragePlatform())
                .setPath(file.getObjectKey())
                .setFilename("")
                .setOriginalFilename(file.getOriginalName())
                .setContentType(file.getContentType())
                .setSize(file.getFileSize());
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    /** 存储组件返回的稳定文件定位信息。 */
    public record StoredFile(
            String storagePlatform,
            String objectKey,
            String contentType,
            long fileSize
    ) {
    }
}
