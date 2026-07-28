package com.yuncheng.framework.file.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.web.multipart.MultipartFile;

/** 通过 Office 容器结构识别真实文件类型。 */
final class OfficeFileContentTypeDetector {

    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_ZIP_ENTRIES = 10_000;
    private static final long MAX_ZIP_UNCOMPRESSED_SIZE = 100L * 1024 * 1024;
    private static final long MIN_ZIP_UNCOMPRESSED_ALLOWANCE = 10L * 1024 * 1024;
    private static final long MAX_ZIP_EXPANSION_RATIO = 100;
    private static final int MAX_XML_METADATA_SIZE = 1024 * 1024;

    private static final String CONTENT_TYPES_ENTRY = "[Content_Types].xml";
    private static final String ROOT_RELATIONSHIPS_ENTRY = "_rels/.rels";
    private static final Set<String> OFFICE_DOCUMENT_RELATIONSHIP_TYPES = Set.of(
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument",
            "http://purl.oclc.org/ooxml/officeDocument/relationships/officeDocument"
    );

    private static final Map<String, OoxmlRule> OOXML_RULES = Map.of(
            "docx", new OoxmlRule(
                    "word/document.xml",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ),
            "xlsx", new OoxmlRule(
                    "xl/workbook.xml",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ),
            "pptx", new OoxmlRule(
                    "ppt/presentation.xml",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            )
    );

    private static final Map<String, OleRule> OLE_RULES = Map.of(
            "doc", new OleRule(Set.of("WordDocument"), "application/msword"),
            "xls", new OleRule(Set.of("Workbook", "Book"), "application/vnd.ms-excel"),
            "ppt", new OleRule(Set.of("PowerPoint Document"), "application/vnd.ms-powerpoint")
    );

    String detect(MultipartFile file, String extension) throws IOException {
        OoxmlRule ooxmlRule = OOXML_RULES.get(extension);
        if (ooxmlRule != null) {
            return detectOoxml(file, ooxmlRule);
        }
        OleRule oleRule = OLE_RULES.get(extension);
        if (oleRule != null) {
            return detectOle(file, oleRule);
        }
        return null;
    }

    boolean supports(String extension) {
        return OOXML_RULES.containsKey(extension) || OLE_RULES.containsKey(extension);
    }

    private String detectOoxml(MultipartFile file, OoxmlRule rule) throws IOException {
        Set<String> entryNames = new HashSet<>();
        ByteArrayOutputStream contentTypes = new ByteArrayOutputStream();
        ByteArrayOutputStream rootRelationships = new ByteArrayOutputStream();
        long maxUncompressedSize = Math.min(
                MAX_ZIP_UNCOMPRESSED_SIZE,
                Math.max(MIN_ZIP_UNCOMPRESSED_ALLOWANCE, file.getSize() * MAX_ZIP_EXPANSION_RATIO)
        );
        long totalUncompressedSize = 0;
        int entryCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];

        try (InputStream inputStream = file.getInputStream();
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > MAX_ZIP_ENTRIES
                        || !isSafeEntryName(entry.getName())
                        || !entryNames.add(entry.getName())) {
                    return null;
                }
                int read;
                while ((read = zipInputStream.read(buffer)) != -1) {
                    totalUncompressedSize += read;
                    if (totalUncompressedSize > maxUncompressedSize) {
                        return null;
                    }
                    ByteArrayOutputStream metadata = switch (entry.getName()) {
                        case CONTENT_TYPES_ENTRY -> contentTypes;
                        case ROOT_RELATIONSHIPS_ENTRY -> rootRelationships;
                        default -> null;
                    };
                    if (metadata != null) {
                        if (metadata.size() + read > MAX_XML_METADATA_SIZE) {
                            return null;
                        }
                        metadata.write(buffer, 0, read);
                    }
                }
                zipInputStream.closeEntry();
            }
        } catch (ZipException exception) {
            return null;
        }

        if (!entryNames.contains(CONTENT_TYPES_ENTRY)
                || !entryNames.contains(ROOT_RELATIONSHIPS_ENTRY)
                || !entryNames.contains(rule.mainEntry())) {
            return null;
        }
        return hasMainContentType(contentTypes.toByteArray(), rule)
                && hasMainRelationship(rootRelationships.toByteArray(), rule)
                ? rule.contentType()
                : null;
    }

    private String detectOle(MultipartFile file, OleRule rule) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             POIFSFileSystem fileSystem = new POIFSFileSystem(inputStream)) {
            DirectoryEntry root = fileSystem.getRoot();
            return rule.requiredEntries().stream().anyMatch(root::hasEntry)
                    ? rule.contentType()
                    : null;
        } catch (IllegalArgumentException | NotOLE2FileException exception) {
            return null;
        }
    }

    private boolean isSafeEntryName(String entryName) {
        return !entryName.isBlank()
                && !entryName.startsWith("/")
                && !entryName.startsWith("\\")
                && !entryName.contains("../")
                && !entryName.contains("..\\");
    }

    private boolean hasMainContentType(byte[] xml, OoxmlRule rule) {
        return hasMatchingElement(
                xml,
                "Override",
                "PartName",
                "/" + rule.mainEntry(),
                "ContentType",
                Set.of(rule.mainContentType())
        );
    }

    private boolean hasMainRelationship(byte[] xml, OoxmlRule rule) {
        return hasMatchingElement(
                xml,
                "Relationship",
                "Target",
                rule.mainEntry(),
                "Type",
                OFFICE_DOCUMENT_RELATIONSHIP_TYPES
        );
    }

    private boolean hasMatchingElement(
            byte[] xml,
            String elementName,
            String firstAttributeName,
            String firstAttributeValue,
            String secondAttributeName,
            Set<String> secondAttributeValues
    ) {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
        try {
            XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xml));
            try {
                while (reader.hasNext()) {
                    if (reader.next() == XMLStreamConstants.START_ELEMENT
                            && elementName.equals(reader.getLocalName())
                            && firstAttributeValue.equals(
                                    reader.getAttributeValue(null, firstAttributeName))
                            && secondAttributeValues.contains(
                                    reader.getAttributeValue(null, secondAttributeName))) {
                        return true;
                    }
                }
                return false;
            } finally {
                reader.close();
            }
        } catch (XMLStreamException exception) {
            return false;
        }
    }

    private record OleRule(Set<String> requiredEntries, String contentType) {
    }

    private record OoxmlRule(String mainEntry, String mainContentType, String contentType) {
    }
}
