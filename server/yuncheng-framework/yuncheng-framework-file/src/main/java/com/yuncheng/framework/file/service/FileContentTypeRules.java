package com.yuncheng.framework.file.service;

import com.yuncheng.framework.web.exception.PlatformException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** 平台支持文件的真实 MIME 类型规则。 */
@Service
public class FileContentTypeRules {

    private static final Tika TIKA = new Tika();
    private static final OfficeFileContentTypeDetector OFFICE_DETECTOR =
            new OfficeFileContentTypeDetector();
    private static final Map<String, Set<String>> ALLOWED_CONTENT_TYPES_BY_EXTENSION = Map.ofEntries(
            Map.entry("pdf", Set.of("application/pdf")),
            Map.entry("txt", Set.of("text/plain")),
            Map.entry("csv", Set.of("text/csv", "text/plain")),
            Map.entry("xls", Set.of("application/vnd.ms-excel")),
            Map.entry("xlsx", Set.of(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )),
            Map.entry("doc", Set.of("application/msword")),
            Map.entry("docx", Set.of(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            )),
            Map.entry("ppt", Set.of("application/vnd.ms-powerpoint")),
            Map.entry("pptx", Set.of(
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            )),
            Map.entry("zip", Set.of("application/zip", "application/x-zip-compressed")),
            Map.entry("rar", Set.of("application/vnd.rar", "application/x-rar-compressed")),
            Map.entry("7z", Set.of("application/x-7z-compressed")),
            Map.entry("jpg", Set.of("image/jpeg")),
            Map.entry("jpeg", Set.of("image/jpeg")),
            Map.entry("png", Set.of("image/png")),
            Map.entry("gif", Set.of("image/gif")),
            Map.entry("webp", Set.of("image/webp")),
            Map.entry("bmp", Set.of("image/bmp")),
            Map.entry("ico", Set.of("image/vnd.microsoft.icon", "image/x-icon"))
    );

    /** 按实际文件字节识别 MIME 类型。 */
    public String detect(MultipartFile file, String extension) {
        try {
            String officeContentType = OFFICE_DETECTOR.detect(file, extension);
            if (officeContentType != null) {
                return officeContentType;
            }
            if (OFFICE_DETECTOR.supports(extension)) {
                throw PlatformException.badRequest("上传文件的实际类型与扩展名不匹配");
            }
            try (InputStream inputStream = file.getInputStream()) {
                String detectedContentType = TIKA.detect(inputStream);
                if (!StringUtils.hasText(detectedContentType)) {
                    throw PlatformException.badRequest("无法识别上传文件的实际类型");
                }
                return detectedContentType.toLowerCase(Locale.ROOT);
            }
        } catch (IllegalArgumentException exception) {
            throw PlatformException.badRequest("上传文件的实际类型与扩展名不匹配");
        } catch (IOException exception) {
            throw PlatformException.badRequest("文件读取失败，请重新选择文件");
        }
    }

    /** 校验扩展名与真实 MIME 类型的对应关系。 */
    public void validate(String extension, String contentType) {
        Set<String> allowedContentTypes = ALLOWED_CONTENT_TYPES_BY_EXTENSION.get(extension);
        if (allowedContentTypes == null || !allowedContentTypes.contains(contentType)) {
            throw PlatformException.badRequest("上传文件的实际类型与扩展名不匹配");
        }
    }
}
