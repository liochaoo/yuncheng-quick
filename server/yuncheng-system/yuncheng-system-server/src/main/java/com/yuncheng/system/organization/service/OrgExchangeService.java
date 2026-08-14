package com.yuncheng.system.organization.service;

import com.yuncheng.common.constant.BuiltInOrgIds;
import com.yuncheng.framework.excel.ExcelCellSupport;
import com.yuncheng.framework.excel.ExcelFileSupport;
import com.yuncheng.framework.excel.ExcelWorkbookSupport;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.api.organization.SystemOrgType;
import com.yuncheng.system.exchange.dto.ExcelImportError;
import com.yuncheng.system.exchange.dto.ExcelImportResult;
import com.yuncheng.system.organization.constant.OrgConstants;
import com.yuncheng.system.organization.dto.OrgCreateRequest;
import com.yuncheng.system.organization.dto.OrgNameConflict;
import com.yuncheng.system.organization.entity.SystemOrg;
import com.yuncheng.system.organization.mapper.SystemOrgMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/** 组织交换工作簿的模板、导入和导出。 */
@Service
public class OrgExchangeService {

    public static final String FILE_TYPE = "YUNCHENG_ORG_EXCHANGE";
    public static final int FORMAT_VERSION = 1;
    public static final int MAX_EXCHANGE_ROWS = 10_000;
    public static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    private static final int ERROR_LIMIT = 100;
    private static final String DATA_SHEET = "组织数据";
    private static final String INSTRUCTION_SHEET = "填写说明";
    private static final String PARENT_REFERENCE_SHEET = "上级组织参考";
    private static final String DEFAULT_ORG_ANCHOR = "default";
    private static final String ROOT_PARENT_KEY = "<ROOT>";
    private static final List<String> SHEET_NAMES = List.of(
            DATA_SHEET, INSTRUCTION_SHEET, PARENT_REFERENCE_SHEET
    );
    private static final List<String> HEADERS = List.of(
            "组织编码", "组织名称", "组织类型", "上级组织编码", "排序号", "说明"
    );
    private static final Pattern ORG_CODE_PATTERN =
            Pattern.compile("[a-z][a-z0-9_-]{0,63}");

    private final OrgQueryService queryService;
    private final OrgCommandService commandService;
    private final SystemOrgMapper orgMapper;
    private final JsonMapper jsonMapper;

    public OrgExchangeService(
            OrgQueryService queryService,
            OrgCommandService commandService,
            SystemOrgMapper orgMapper,
            JsonMapper jsonMapper
    ) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.orgMapper = orgMapper;
        this.jsonMapper = jsonMapper;
    }

    public byte[] template() {
        return requireExchangeFileSize(writeWorkbook(List.of(), queryService.allOrgs()));
    }

    public byte[] exportOrgs() {
        List<SystemOrg> allOrgs = queryService.allOrgs();
        List<SystemOrg> exportedOrgs = allOrgs.stream()
                .filter(org -> org.getId() != BuiltInOrgIds.DEFAULT_ORG)
                .sorted(Comparator.comparing(SystemOrg::getPathIds))
                .toList();
        if (exportedOrgs.size() > MAX_EXCHANGE_ROWS) {
            throw PlatformException.badRequest("组织数据超过 10000 条，不能导出");
        }
        Map<Long, SystemOrg> orgsById = allOrgs.stream().collect(
                java.util.stream.Collectors.toMap(SystemOrg::getId, org -> org)
        );
        List<List<?>> rows = exportedOrgs.stream().<List<?>>map(org -> List.of(
                org.getOrgCode(),
                org.getOrgName(),
                typeName(org.getOrgType()),
                parentCode(org, orgsById),
                org.getSortOrder(),
                valueOrEmpty(org.getDescription())
        )).toList();
        List<SystemOrg> references = allOrgs.stream()
                .filter(org -> org.getId() == BuiltInOrgIds.DEFAULT_ORG)
                .toList();
        return requireExchangeFileSize(writeWorkbook(rows, references));
    }

    @Transactional
    public ExcelImportResult importOrgs(MultipartFile file) {
        byte[] content = requireImportFile(file);
        try (XSSFWorkbook workbook = ExcelFileSupport.openXlsx(content)) {
            requireWorkbookStructure(workbook);
            List<RawRow> rawRows = readRows(workbook.getSheet(DATA_SHEET));
            if (rawRows.isEmpty()) {
                throw PlatformException.badRequest("组织数据工作表中没有可导入的数据");
            }
            Validation validation = validate(rawRows);
            if (validation.errorCount() > 0) {
                return new ExcelImportResult(
                        false, rawRows.size(), 0,
                        validation.errorCount(), validation.errors()
                );
            }
            Map<String, Long> orgIdsByCode = queryService.allOrgs().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            SystemOrg::getOrgCode,
                            SystemOrg::getId,
                            (left, right) -> left,
                            LinkedHashMap::new
                    ));
            orgIdsByCode.put(DEFAULT_ORG_ANCHOR, BuiltInOrgIds.DEFAULT_ORG);
            for (NormalizedRow row : validation.orderedRows()) {
                Long parentId = row.parentCode() == null
                        ? null : orgIdsByCode.get(row.parentCode());
                Long orgId = commandService.create(new OrgCreateRequest(
                        parentId,
                        row.orgType(),
                        row.orgCode(),
                        row.orgName(),
                        row.sortOrder(),
                        row.description()
                ));
                orgIdsByCode.put(row.orgCode(), orgId);
            }
            return new ExcelImportResult(
                    true, rawRows.size(), validation.orderedRows().size(), 0, List.of()
            );
        } catch (PlatformException exception) {
            throw exception;
        } catch (IOException | IllegalArgumentException exception) {
            throw PlatformException.badRequest("Excel 文件无效：" + exception.getMessage());
        }
    }

    private Validation validate(List<RawRow> rawRows) {
        ErrorCollector errors = new ErrorCollector();
        List<NormalizedRow> rows = rawRows.stream()
                .map(row -> normalize(row, errors))
                .toList();
        List<SystemOrg> existingOrgs = queryService.allOrgs();
        Map<String, SystemOrg> existingByCode = existingOrgs.stream()
                .collect(java.util.stream.Collectors.toMap(
                        SystemOrg::getOrgCode,
                        org -> org,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        SystemOrg defaultOrg = existingOrgs.stream()
                .filter(org -> org.getId() == BuiltInOrgIds.DEFAULT_ORG)
                .findFirst()
                .orElseThrow(() -> PlatformException.serviceUnavailable("内置默认组织不存在"));
        existingByCode.put(DEFAULT_ORG_ANCHOR, defaultOrg);
        Map<Long, String> existingCodesById = existingByCode.values().stream()
                .collect(java.util.stream.Collectors.toMap(
                        SystemOrg::getId,
                        org -> org.getId() == BuiltInOrgIds.DEFAULT_ORG
                                ? DEFAULT_ORG_ANCHOR : org.getOrgCode(),
                        (left, right) -> left
                ));
        Map<String, NormalizedRow> rowsByCode = new LinkedHashMap<>();
        for (NormalizedRow row : rows) {
            if (row.orgCode() == null) {
                continue;
            }
            NormalizedRow first = rowsByCode.putIfAbsent(row.orgCode(), row);
            if (first != null) {
                errors.add(
                        row.rowNumber(), "组织编码",
                        "组织编码与第 " + first.rowNumber() + " 行重复"
                );
            }
            if (existingByCode.containsKey(row.orgCode())) {
                errors.add(row.rowNumber(), "组织编码", "组织编码已存在于目标系统");
            }
        }
        validateSiblingNames(rows, existingOrgs, existingCodesById, errors);
        rows.forEach(row -> {
            if (row.parentCode() != null
                    && !existingByCode.containsKey(row.parentCode())
                    && !rowsByCode.containsKey(row.parentCode())) {
                errors.add(row.rowNumber(), "上级组织编码", "上级组织不存在");
            }
            if (row.orgCode() != null && row.orgCode().equals(row.parentCode())) {
                errors.add(row.rowNumber(), "上级组织编码", "上级组织不能是当前组织自身");
            }
        });

        List<NormalizedRow> orderedRows = resolveRows(
                rowsByCode, existingByCode, errors
        );
        return new Validation(orderedRows, errors.total(), errors.items());
    }

    private List<NormalizedRow> resolveRows(
            Map<String, NormalizedRow> rowsByCode,
            Map<String, SystemOrg> existingByCode,
            ErrorCollector errors
    ) {
        List<NormalizedRow> orderedRows = new ArrayList<>();
        Set<String> completed = new HashSet<>();
        Map<String, Integer> depths = new HashMap<>();
        for (NormalizedRow start : rowsByCode.values()) {
            if (completed.contains(start.orgCode())) {
                continue;
            }
            List<NormalizedRow> chain = new ArrayList<>();
            Map<String, Integer> positions = new HashMap<>();
            NormalizedRow current = start;
            int parentDepth = 0;
            SystemOrgType parentType = null;
            while (current != null && !completed.contains(current.orgCode())) {
                Integer cycleStart = positions.putIfAbsent(
                        current.orgCode(), chain.size()
                );
                if (cycleStart != null) {
                    List<String> cycle = chain.subList(cycleStart, chain.size())
                            .stream()
                            .map(NormalizedRow::orgCode)
                            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
                    cycle.add(current.orgCode());
                    errors.add(
                            current.rowNumber(), "上级组织编码",
                            "组织父子关系形成循环：" + String.join(" -> ", cycle)
                    );
                    break;
                }
                chain.add(current);
                if (current.parentCode() == null) {
                    break;
                }
                SystemOrg existingParent = existingByCode.get(current.parentCode());
                if (existingParent != null) {
                    parentDepth = existingParent.getDepth();
                    parentType = existingParent.getOrgType();
                    break;
                }
                NormalizedRow importedParent = rowsByCode.get(current.parentCode());
                if (importedParent == null) {
                    break;
                }
                if (completed.contains(importedParent.orgCode())) {
                    parentDepth = depths.getOrDefault(importedParent.orgCode(), 0);
                    parentType = importedParent.orgType();
                    break;
                }
                current = importedParent;
            }
            for (int index = chain.size() - 1; index >= 0; index--) {
                NormalizedRow row = chain.get(index);
                int depth = parentDepth + 1;
                validateHierarchy(row, parentType, depth, errors);
                completed.add(row.orgCode());
                depths.put(row.orgCode(), depth);
                orderedRows.add(row);
                parentDepth = depth;
                parentType = row.orgType();
            }
        }
        return orderedRows;
    }

    private void validateHierarchy(
            NormalizedRow row,
            SystemOrgType parentType,
            int depth,
            ErrorCollector errors
    ) {
        if (row.parentCode() == null && row.orgType() != null
                && row.orgType() != SystemOrgType.ORGANIZATION) {
            errors.add(row.rowNumber(), "组织类型", "顶级节点只能是组织");
        } else if (row.parentCode() != null && row.orgType() != null && parentType != null
                && !allowedChild(parentType, row.orgType())) {
            errors.add(row.rowNumber(), "组织类型", "组织类型不符合上级节点的层级规则");
        }
        if (depth > OrgConstants.MAX_DEPTH) {
            errors.add(
                    row.rowNumber(), "上级组织编码",
                    "组织层级不能超过 " + OrgConstants.MAX_DEPTH + " 层"
            );
        }
    }

    private void validateSiblingNames(
            List<NormalizedRow> rows,
            List<SystemOrg> existingOrgs,
            Map<Long, String> existingCodesById,
            ErrorCollector errors
    ) {
        List<OrgNameCandidate> candidates = new ArrayList<>();
        for (SystemOrg org : existingOrgs) {
            String parentCode = org.getParentId() == null
                    ? ROOT_PARENT_KEY : existingCodesById.get(org.getParentId());
            candidates.add(new OrgNameCandidate(0, parentCode, org.getOrgName()));
        }
        for (NormalizedRow row : rows) {
            if (row.orgName() == null) {
                continue;
            }
            String parentCode = row.parentCode() == null
                    ? ROOT_PARENT_KEY : row.parentCode();
            candidates.add(new OrgNameCandidate(
                    row.rowNumber(), parentCode, row.orgName()
            ));
        }
        if (candidates.isEmpty()) {
            return;
        }
        String candidatesJson;
        try {
            candidatesJson = jsonMapper.writeValueAsString(candidates);
        } catch (JacksonException exception) {
            throw PlatformException.serviceUnavailable("组织名称唯一性校验失败");
        }
        for (OrgNameConflict conflict
                : orgMapper.selectSiblingNameConflicts(candidatesJson)) {
            String reason = conflict.firstRowNumber() == 0
                    ? "同一上级下已存在同名组织"
                    : "组织名称与第 " + conflict.firstRowNumber()
                    + " 行在同一上级下重复";
            errors.add(conflict.rowNumber(), "组织名称", reason);
        }
    }

    private NormalizedRow normalize(RawRow row, ErrorCollector errors) {
        String orgCode = normalizeCode(row.value(0));
        if (orgCode == null) {
            errors.add(row.rowNumber(), "组织编码", "组织编码不能为空");
        } else if (!ORG_CODE_PATTERN.matcher(orgCode).matches()) {
            errors.add(
                    row.rowNumber(), "组织编码",
                    "组织编码必须以字母开头，只能包含字母、数字、下划线和连字符，且不能超过 64 个字符"
            );
        }
        String orgName = normalizedText(row.value(1));
        if (orgName == null) {
            errors.add(row.rowNumber(), "组织名称", "组织名称不能为空");
        } else {
            if (orgName.length() > OrgConstants.MAX_NAME_LENGTH) {
                errors.add(row.rowNumber(), "组织名称", "组织名称不能超过 100 个字符");
            }
            if (orgName.contains("/")) {
                errors.add(row.rowNumber(), "组织名称", "组织名称不能包含路径分隔符 /");
            }
        }
        SystemOrgType orgType = parseType(row, errors);
        String parentCode = normalizeCode(row.value(3));
        if (parentCode != null && !ORG_CODE_PATTERN.matcher(parentCode).matches()) {
            errors.add(
                    row.rowNumber(), "上级组织编码",
                    "上级组织编码必须以字母开头，只能包含字母、数字、下划线和连字符"
            );
        }
        Integer sortOrder = parseSortOrder(row, errors);
        String description = normalizedText(row.value(5));
        if (description != null && description.length() > OrgConstants.MAX_DESCRIPTION_LENGTH) {
            errors.add(row.rowNumber(), "说明", "组织说明不能超过 500 个字符");
        }
        return new NormalizedRow(
                row.rowNumber(), orgCode, orgName, orgType, parentCode,
                sortOrder == null ? 0 : sortOrder, description
        );
    }

    private SystemOrgType parseType(RawRow row, ErrorCollector errors) {
        String value = normalizedText(row.value(2));
        if (value == null) {
            errors.add(row.rowNumber(), "组织类型", "组织类型不能为空");
            return null;
        }
        return switch (value) {
            case "组织" -> SystemOrgType.ORGANIZATION;
            case "部门" -> SystemOrgType.DEPARTMENT;
            case "小组" -> SystemOrgType.GROUP;
            default -> {
                errors.add(row.rowNumber(), "组织类型", "只能填写“组织”“部门”或“小组”");
                yield null;
            }
        };
    }

    private Integer parseSortOrder(RawRow row, ErrorCollector errors) {
        String value = normalizedText(row.value(4));
        if (value == null) {
            return 0;
        }
        try {
            int sortOrder = Integer.parseInt(value);
            if (sortOrder < 0) {
                throw new NumberFormatException();
            }
            return sortOrder;
        } catch (NumberFormatException exception) {
            errors.add(row.rowNumber(), "排序号", "排序号必须为非负整数");
            return null;
        }
    }

    private byte[] writeWorkbook(List<List<?>> rows, List<SystemOrg> references) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(200);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            workbook.setCompressTempFiles(true);
            CellStyle headerStyle = ExcelWorkbookSupport.headerStyle(workbook);
            CellStyle bodyStyle = ExcelWorkbookSupport.borderedStyle(workbook);
            writeDataSheet(workbook, rows, headerStyle, bodyStyle);
            writeInstructionSheet(workbook, headerStyle, bodyStyle);
            writeReferenceSheet(workbook, references, headerStyle, bodyStyle);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw PlatformException.serviceUnavailable("生成组织 Excel 文件失败");
        }
    }

    private void writeDataSheet(
            Workbook workbook,
            List<List<?>> rows,
            CellStyle headerStyle,
            CellStyle bodyStyle
    ) {
        Sheet sheet = workbook.createSheet(DATA_SHEET);
        ExcelWorkbookSupport.writeRow(sheet, 0, HEADERS, headerStyle);
        for (int index = 0; index < rows.size(); index++) {
            ExcelWorkbookSupport.writeRow(sheet, index + 1, rows.get(index), bodyStyle);
        }
        setWidths(sheet, 22, 26, 14, 24, 12, 50);
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(
                0, Math.max(0, rows.size()), 0, HEADERS.size() - 1
        ));
        addTypeValidation(sheet);
    }

    private void writeInstructionSheet(
            Workbook workbook,
            CellStyle headerStyle,
            CellStyle bodyStyle
    ) {
        Sheet sheet = workbook.createSheet(INSTRUCTION_SHEET);
        ExcelWorkbookSupport.writeRow(sheet, 0, List.of("文件类型", FILE_TYPE), bodyStyle);
        ExcelWorkbookSupport.writeRow(sheet, 1, List.of("格式版本", FORMAT_VERSION), bodyStyle);
        ExcelWorkbookSupport.writeRow(sheet, 3, List.of("字段", "填写要求"), headerStyle);
        List<List<?>> instructions = List.of(
                List.of("组织编码", "必填；全局唯一，以字母开头，只能使用字母、数字、下划线和连字符"),
                List.of("组织名称", "必填；同一上级下唯一，最多 100 个字符，不能包含 /"),
                List.of("组织类型", "必填；填写“组织”“部门”或“小组”"),
                List.of("组织类型层级", "顶级只能是组织；组织下可建组织或部门；部门下可建部门或小组；小组下只能建小组"),
                List.of("上级组织编码", "顶级组织留空；可以引用目标系统已有组织或本文件中的组织，数据行顺序不限"),
                List.of("排序号", "选填；非负整数，留空默认为 0"),
                List.of("说明", "选填；最多 500 个字符"),
                List.of("文件限制", "组织数据最多 10000 行，Excel 文件不得超过 20 MB"),
                List.of("导入规则", "只新增组织；全部校验通过后一次写入，不会修改、移动或删除已有组织"),
                List.of("默认组织", "系统导出不创建内置默认组织；上级编码 default 会映射到目标系统的内置默认组织")
        );
        for (int index = 0; index < instructions.size(); index++) {
            ExcelWorkbookSupport.writeRow(sheet, index + 4, instructions.get(index), bodyStyle);
        }
        setWidths(sheet, 22, 96);
    }

    private void writeReferenceSheet(
            Workbook workbook,
            List<SystemOrg> references,
            CellStyle headerStyle,
            CellStyle bodyStyle
    ) {
        Sheet sheet = workbook.createSheet(PARENT_REFERENCE_SHEET);
        ExcelWorkbookSupport.writeRow(
                sheet, 0, List.of("组织编码", "组织名称", "组织类型", "完整路径"), headerStyle
        );
        for (int index = 0; index < references.size(); index++) {
            SystemOrg org = references.get(index);
            ExcelWorkbookSupport.writeRow(sheet, index + 1, List.of(
                    org.getId() == BuiltInOrgIds.DEFAULT_ORG
                            ? DEFAULT_ORG_ANCHOR : org.getOrgCode(),
                    org.getOrgName(),
                    typeName(org.getOrgType()), org.getFullPath()
            ), bodyStyle);
        }
        setWidths(sheet, 22, 26, 14, 60);
        sheet.createFreezePane(0, 1);
    }

    private void requireWorkbookStructure(XSSFWorkbook workbook) {
        List<String> actualNames = new ArrayList<>();
        for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
            actualNames.add(workbook.getSheetName(index));
        }
        if (!actualNames.equals(SHEET_NAMES)) {
            throw PlatformException.badRequest("工作表结构不正确，请使用系统模板或导出文件");
        }
        rejectFormulaCells(workbook);
        requireMetadata(workbook.getSheet(INSTRUCTION_SHEET));
        requireHeaders(workbook.getSheet(DATA_SHEET));
    }

    private void rejectFormulaCells(Workbook workbook) {
        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                for (org.apache.poi.ss.usermodel.Cell cell : row) {
                    if (cell.getCellType() == CellType.FORMULA) {
                        throw PlatformException.badRequest(
                                "Excel 文件不能包含公式单元格：" + sheet.getSheetName()
                                        + "!" + cell.getAddress().formatAsString()
                        );
                    }
                }
            }
        }
    }

    private void requireMetadata(Sheet sheet) {
        Row fileTypeRow = sheet.getRow(0);
        Row versionRow = sheet.getRow(1);
        String fileType = fileTypeRow == null
                ? null : ExcelCellSupport.text(fileTypeRow.getCell(1));
        String version = versionRow == null
                ? null : ExcelCellSupport.text(versionRow.getCell(1));
        if (!FILE_TYPE.equals(fileType) || !Integer.toString(FORMAT_VERSION).equals(version)) {
            throw PlatformException.badRequest("文件类型或格式版本不受支持");
        }
    }

    private void requireHeaders(Sheet sheet) {
        Row header = sheet.getRow(0);
        if (header == null) {
            throw PlatformException.badRequest("组织数据表头不能为空");
        }
        for (int column = 0; column < HEADERS.size(); column++) {
            if (!HEADERS.get(column).equals(ExcelCellSupport.text(header.getCell(column)))) {
                throw PlatformException.badRequest("组织数据表头不正确，请勿修改模板列名或顺序");
            }
        }
        if (header.getLastCellNum() > HEADERS.size()) {
            throw PlatformException.badRequest("组织数据包含模板之外的列");
        }
    }

    private List<RawRow> readRows(Sheet sheet) {
        if (sheet.getLastRowNum() > MAX_EXCHANGE_ROWS) {
            throw PlatformException.badRequest("组织数据不能超过 10000 行");
        }
        List<RawRow> rows = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (hasUnexpectedCells(row)) {
                throw PlatformException.badRequest(
                        "组织数据第 " + (rowIndex + 1) + " 行包含模板之外的列"
                );
            }
            List<String> values = new ArrayList<>(HEADERS.size());
            for (int column = 0; column < HEADERS.size(); column++) {
                values.add(row == null ? null : ExcelCellSupport.text(row.getCell(column)));
            }
            if (values.stream().allMatch(value -> value == null)) {
                continue;
            }
            rows.add(new RawRow(rowIndex + 1, values));
        }
        if (rows.size() > MAX_EXCHANGE_ROWS) {
            throw PlatformException.badRequest("组织数据不能超过 10000 行");
        }
        return rows;
    }

    private boolean hasUnexpectedCells(Row row) {
        if (row == null || row.getLastCellNum() <= HEADERS.size()) {
            return false;
        }
        for (int column = HEADERS.size(); column < row.getLastCellNum(); column++) {
            if (ExcelCellSupport.text(row.getCell(column)) != null) {
                return true;
            }
        }
        return false;
    }

    private byte[] requireImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw PlatformException.badRequest("请选择需要导入的 Excel 文件");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename)
                || !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw PlatformException.badRequest("只支持 .xlsx 格式的 Excel 文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw PlatformException.badRequest("Excel 文件不能超过 20 MB");
        }
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw PlatformException.badRequest("读取 Excel 文件失败");
        }
    }

    private byte[] requireExchangeFileSize(byte[] content) {
        if (content.length > MAX_FILE_SIZE) {
            throw PlatformException.badRequest("Excel 文件超过 20 MB，不能导出");
        }
        return content;
    }

    private void addTypeValidation(Sheet sheet) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(
                new String[]{"组织", "部门", "小组"}
        );
        org.apache.poi.ss.util.CellRangeAddressList range =
                new org.apache.poi.ss.util.CellRangeAddressList(1, MAX_EXCHANGE_ROWS, 2, 2);
        DataValidation validation = helper.createValidation(constraint, range);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("组织类型无效", "请选择“组织”“部门”或“小组”");
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    private boolean allowedChild(SystemOrgType parent, SystemOrgType child) {
        return switch (parent) {
            case ORGANIZATION -> child == SystemOrgType.ORGANIZATION
                    || child == SystemOrgType.DEPARTMENT;
            case DEPARTMENT -> child == SystemOrgType.DEPARTMENT
                    || child == SystemOrgType.GROUP;
            case GROUP -> child == SystemOrgType.GROUP;
        };
    }

    private String parentCode(SystemOrg org, Map<Long, SystemOrg> orgsById) {
        if (org.getParentId() == null) {
            return "";
        }
        SystemOrg parent = orgsById.get(org.getParentId());
        if (parent == null) {
            throw PlatformException.serviceUnavailable("组织上级数据不完整");
        }
        if (parent.getId() == BuiltInOrgIds.DEFAULT_ORG) {
            return DEFAULT_ORG_ANCHOR;
        }
        return parent.getOrgCode();
    }

    private String normalizeCode(String value) {
        String normalized = normalizedText(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizedText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String typeName(SystemOrgType type) {
        return switch (type) {
            case ORGANIZATION -> "组织";
            case DEPARTMENT -> "部门";
            case GROUP -> "小组";
        };
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private void setWidths(Sheet sheet, int... widths) {
        for (int column = 0; column < widths.length; column++) {
            sheet.setColumnWidth(column, widths[column] * 256);
        }
    }

    private record OrgNameCandidate(int rowNumber, String parentCode, String orgName) {
    }

    private record RawRow(int rowNumber, List<String> values) {
        String value(int column) {
            return values.get(column);
        }
    }

    private record NormalizedRow(
            int rowNumber,
            String orgCode,
            String orgName,
            SystemOrgType orgType,
            String parentCode,
            int sortOrder,
            String description
    ) {
    }

    private record Validation(
            List<NormalizedRow> orderedRows,
            int errorCount,
            List<ExcelImportError> errors
    ) {
    }

    private static final class ErrorCollector {
        private final List<ExcelImportError> items = new ArrayList<>();
        private final Set<String> uniqueErrors = new HashSet<>();
        private int total;

        void add(int rowNumber, String field, String message) {
            String key = rowNumber + "\u0000" + field + "\u0000" + message;
            if (!uniqueErrors.add(key)) {
                return;
            }
            total++;
            if (items.size() < ERROR_LIMIT) {
                items.add(new ExcelImportError(rowNumber, field, message));
            }
        }

        int total() {
            return total;
        }

        List<ExcelImportError> items() {
            return List.copyOf(items);
        }
    }
}
