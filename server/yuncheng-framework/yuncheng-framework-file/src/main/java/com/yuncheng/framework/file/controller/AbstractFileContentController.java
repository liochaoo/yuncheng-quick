package com.yuncheng.framework.file.controller;

import com.yuncheng.framework.file.dto.FileRecord;
import com.yuncheng.framework.file.service.FileContent;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/** 文件内容响应的公共处理。 */
public abstract class AbstractFileContentController {

    private static final String CONTENT_SECURITY_POLICY =
            "sandbox; default-src 'none'; base-uri 'none'; form-action 'none'";

    protected void writeContent(
            FileContent content,
            boolean download,
            boolean anonymousAccess,
            HttpServletResponse response
    ) throws IOException {
        FileRecord file = content.record();
        response.setContentType(normalizeContentType(file.contentType()));
        response.setContentLengthLong(file.fileSize());
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "no-referrer");
        if (!download) {
            response.setHeader("Content-Security-Policy", CONTENT_SECURITY_POLICY);
        }
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                buildContentDisposition(
                        download || !isInlinePreviewAllowed(file.contentType()),
                        file.originalName()
                )
        );
        response.setHeader(
                HttpHeaders.CACHE_CONTROL,
                anonymousAccess ? "public, max-age=3600" : "no-store"
        );
        content.writeTo(response.getOutputStream());
    }

    private String normalizeContentType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType).toString();
        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    private String buildContentDisposition(boolean download, String filename) {
        ContentDisposition.Builder builder = download
                ? ContentDisposition.attachment()
                : ContentDisposition.inline();
        return builder.filename(filename, StandardCharsets.UTF_8)
                .build()
                .toString();
    }

    private boolean isInlinePreviewAllowed(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.equals(MediaType.APPLICATION_PDF_VALUE)
                || contentType.equals("image/bmp")
                || contentType.equals("image/gif")
                || contentType.equals("image/jpeg")
                || contentType.equals("image/png")
                || contentType.equals("image/vnd.microsoft.icon")
                || contentType.equals("image/webp")
                || contentType.equals("image/x-icon");
    }
}
