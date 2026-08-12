package com.yuncheng.framework.excel;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;

/** 通用 OOXML 工作簿安全读取和下载响应。 */
public final class ExcelFileSupport {

    private static final int MAX_ARCHIVE_ENTRIES = 10_000;
    private static final long MAX_EXPANDED_BYTES = 50L * 1024 * 1024;

    public static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private ExcelFileSupport() {
    }

    public static XSSFWorkbook openXlsx(byte[] content) throws IOException {
        requireBoundedArchive(content);
        OPCPackage container = null;
        try {
            container = OPCPackage.open(new ByteArrayInputStream(content), false);
            requirePassiveWorkbook(container);
            return new XSSFWorkbook(container);
        } catch (IOException exception) {
            closeQuietly(container);
            throw exception;
        } catch (Exception exception) {
            closeQuietly(container);
            throw new IOException("Excel 文件结构无效", exception);
        }
    }

    private static void requireBoundedArchive(byte[] content) throws IOException {
        int entries = 0;
        long expandedBytes = 0;
        byte[] buffer = new byte[8192];
        try (ZipArchiveInputStream input = new ZipArchiveInputStream(
                new ByteArrayInputStream(content)
        )) {
            ZipArchiveEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                entries++;
                if (entries > MAX_ARCHIVE_ENTRIES) {
                    throw new IOException("Excel 文件包含过多压缩条目");
                }
                int read;
                while ((read = input.read(buffer)) != -1) {
                    expandedBytes += read;
                    if (expandedBytes > MAX_EXPANDED_BYTES) {
                        throw new IOException("Excel 文件解压后内容过大");
                    }
                }
            }
        }
        if (entries == 0) {
            throw new IOException("Excel 文件不是有效的 OOXML 压缩容器");
        }
    }

    public static void writeXlsx(
            byte[] content,
            String filename,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType(XLSX_CONTENT_TYPE);
        response.setContentLengthLong(content.length);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build()
                        .toString()
        );
        response.getOutputStream().write(content);
    }

    private static void requirePassiveWorkbook(OPCPackage container) throws IOException {
        try {
            for (PackageRelationship relationship : container.getRelationships()) {
                requireInternalRelationship(relationship);
            }
            for (PackagePart part : container.getParts()) {
                String partName = part.getPartName().getName().toLowerCase(Locale.ROOT);
                if (partName.endsWith("vbaproject.bin")) {
                    throw new IOException("Excel 文件不能包含宏");
                }
                if (part.isRelationshipPart()) {
                    continue;
                }
                for (PackageRelationship relationship : part.getRelationships()) {
                    requireInternalRelationship(relationship);
                }
            }
        } catch (IOException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IOException("Excel 文件结构无效", exception);
        }
    }

    private static void requireInternalRelationship(
            PackageRelationship relationship
    ) throws IOException {
        if (relationship.getTargetMode() == TargetMode.EXTERNAL) {
            throw new IOException("Excel 文件不能包含外部链接");
        }
    }

    private static void closeQuietly(OPCPackage container) {
        if (container == null) {
            return;
        }
        try {
            container.close();
        } catch (Exception ignored) {
            // 保留原始校验异常。
        }
    }
}
