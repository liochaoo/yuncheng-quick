package com.yuncheng.system.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.excel.ExcelCellSupport;
import com.yuncheng.framework.excel.ExcelFileSupport;
import com.yuncheng.framework.excel.ExcelWorkbookSupport;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.organization.entity.SystemOrg;
import com.yuncheng.system.organization.service.OrgQueryService;
import com.yuncheng.system.role.dto.RoleSummary;
import com.yuncheng.system.role.entity.SystemRole;
import com.yuncheng.system.role.service.RoleQueryService;
import com.yuncheng.system.role.service.UserRoleService;
import com.yuncheng.system.user.dto.UserImportError;
import com.yuncheng.system.user.dto.UserImportItem;
import com.yuncheng.system.user.dto.UserImportResult;
import com.yuncheng.system.user.dto.UserOrgCodeAssignment;
import com.yuncheng.system.user.dto.UserPageQuery;
import com.yuncheng.system.user.entity.SystemUser;
import com.yuncheng.system.user.mapper.SystemUserMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** 用户交换工作簿的模板、导入和导出。 */
@Service
public class UserExchangeService {

    public static final String FILE_TYPE = "YUNCHENG_USER_EXCHANGE";
    public static final int FORMAT_VERSION = 1;
    public static final int MAX_EXCHANGE_ROWS = 10_000;
    public static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    private static final int ERROR_LIMIT = 100;
    private static final String DATA_SHEET = "用户数据";
    private static final String INSTRUCTION_SHEET = "填写说明";
    private static final String ORG_SHEET = "组织参考";
    private static final String ROLE_SHEET = "角色参考";
    private static final List<String> SHEET_NAMES = List.of(
            DATA_SHEET, INSTRUCTION_SHEET, ORG_SHEET, ROLE_SHEET
    );
    private static final List<String> HEADERS = List.of(
            "登录名", "姓名", "手机号码", "电子邮箱", "主组织编码",
            "其他组织编码", "角色编码", "启用状态", "排序号"
    );

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final UserInputService inputService;
    private final UserOrgService userOrgService;
    private final UserRoleService userRoleService;
    private final OrgQueryService orgQueryService;
    private final RoleQueryService roleQueryService;
    private final SystemUserMapper userMapper;

    public UserExchangeService(
            UserQueryService userQueryService,
            UserCommandService userCommandService,
            UserInputService inputService,
            UserOrgService userOrgService,
            UserRoleService userRoleService,
            OrgQueryService orgQueryService,
            RoleQueryService roleQueryService,
            SystemUserMapper userMapper
    ) {
        this.userQueryService = userQueryService;
        this.userCommandService = userCommandService;
        this.inputService = inputService;
        this.userOrgService = userOrgService;
        this.userRoleService = userRoleService;
        this.orgQueryService = orgQueryService;
        this.roleQueryService = roleQueryService;
        this.userMapper = userMapper;
    }

    public byte[] template() {
        return requireExchangeFileSize(writeWorkbook(
                List.of(), orgQueryService.allOrgs(), roleQueryService.assignableRoles()
        ));
    }

    public byte[] exportUsers(UserPageQuery query) {
        List<SystemUser> users = userQueryService.usersForExport(query, MAX_EXCHANGE_ROWS + 1);
        if (users.size() > MAX_EXCHANGE_ROWS) {
            throw PlatformException.badRequest(
                    "符合条件的用户超过 10000 条，请缩小查询范围后再导出"
            );
        }
        if (users.isEmpty()) {
            return requireExchangeFileSize(writeWorkbook(List.of(), List.of(), List.of()));
        }
        List<Long> userIds = users.stream().map(SystemUser::getId).toList();
        Map<Long, UserOrgCodeAssignment> orgAssignments =
                userOrgService.codeAssignmentsByUserIds(userIds);
        Map<Long, List<RoleSummary>> roleAssignments =
                userRoleService.summariesByUserIds(userIds);
        List<List<?>> rows = users.stream().<List<?>>map(user -> {
            UserOrgCodeAssignment orgs = orgAssignments.get(user.getId());
            if (orgs == null) {
                throw PlatformException.serviceUnavailable("用户归属组织数据不完整");
            }
            List<RoleSummary> roles = roleAssignments.getOrDefault(user.getId(), List.of());
            if (roles.isEmpty()) {
                throw PlatformException.serviceUnavailable("用户角色数据不完整");
            }
            return List.of(
                    user.getUsername(),
                    user.getRealName(),
                    valueOrEmpty(user.getPhone()),
                    valueOrEmpty(user.getEmail()),
                    orgs.primaryOrgCode(),
                    String.join(",", orgs.otherOrgCodes()),
                    roles.stream().map(RoleSummary::roleCode).collect(Collectors.joining(",")),
                    Boolean.TRUE.equals(user.getEnabled()) ? "启用" : "停用",
                    user.getSortOrder()
            );
        }).toList();
        List<SystemOrg> referencedOrgs = referencedOrgs(orgAssignments.values());
        List<SystemRole> referencedRoles = referencedRoles(roleAssignments.values());
        return requireExchangeFileSize(writeWorkbook(rows, referencedOrgs, referencedRoles));
    }

    private byte[] requireExchangeFileSize(byte[] content) {
        if (content.length > MAX_FILE_SIZE) {
            throw PlatformException.badRequest(
                    "Excel 文件超过 20 MB，请缩小数据范围后重试"
            );
        }
        return content;
    }

    public UserImportResult importUsers(MultipartFile file) {
        byte[] content = requireImportFile(file);
        try (XSSFWorkbook workbook = ExcelFileSupport.openXlsx(content)) {
            requireWorkbookStructure(workbook);
            List<RawRow> rawRows = readRows(workbook.getSheet(DATA_SHEET));
            if (rawRows.isEmpty()) {
                throw PlatformException.badRequest("用户数据工作表中没有可导入的数据");
            }
            Validation validation = validate(rawRows);
            if (validation.errorCount() > 0) {
                return new UserImportResult(
                        false, rawRows.size(), 0, validation.errorCount(), validation.errors()
                );
            }
            int imported = userCommandService.importUsers(validation.items());
            return new UserImportResult(true, rawRows.size(), imported, 0, List.of());
        } catch (PlatformException exception) {
            throw exception;
        } catch (IOException | IllegalArgumentException exception) {
            throw PlatformException.badRequest("Excel 文件无效：" + exception.getMessage());
        }
    }

    private byte[] writeWorkbook(
            List<List<?>> dataRows,
            List<SystemOrg> orgs,
            List<SystemRole> roles
    ) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(200);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            workbook.setCompressTempFiles(true);
            CellStyle headerStyle = ExcelWorkbookSupport.headerStyle(workbook);
            CellStyle bodyStyle = ExcelWorkbookSupport.borderedStyle(workbook);
            writeDataSheet(workbook, dataRows, headerStyle, bodyStyle);
            writeInstructionSheet(workbook, headerStyle, bodyStyle);
            writeOrgSheet(workbook, orgs, headerStyle, bodyStyle);
            writeRoleSheet(workbook, roles, headerStyle, bodyStyle);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw PlatformException.serviceUnavailable("生成用户 Excel 文件失败");
        }
    }

    private List<SystemOrg> referencedOrgs(
            Collection<UserOrgCodeAssignment> assignments
    ) {
        Set<String> orgCodes = assignments.stream()
                .flatMap(assignment -> Stream.concat(
                        Stream.of(assignment.primaryOrgCode()),
                        assignment.otherOrgCodes().stream()
                ))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, SystemOrg> orgs = orgQueryService.orgsByCodes(orgCodes);
        if (orgs.size() != orgCodes.size()) {
            throw PlatformException.serviceUnavailable("用户归属组织数据不完整");
        }
        return orgs.values().stream()
                .sorted(Comparator.comparing(SystemOrg::getPathIds))
                .toList();
    }

    private List<SystemRole> referencedRoles(
            Collection<List<RoleSummary>> assignments
    ) {
        Set<Long> roleIds = assignments.stream()
                .flatMap(Collection::stream)
                .map(role -> Long.valueOf(role.id()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return roleQueryService.requireRoles(roleIds).values().stream()
                .sorted(Comparator.comparingInt(SystemRole::getSortOrder)
                        .thenComparing(SystemRole::getId))
                .toList();
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
        int[] widths = {18, 18, 16, 28, 20, 32, 28, 12, 12};
        for (int column = 0; column < widths.length; column++) {
            sheet.setColumnWidth(column, widths[column] * 256);
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(
                0, Math.max(0, rows.size()), 0, HEADERS.size() - 1
        ));
        addEnabledValidation(sheet);
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
                List.of("登录名", "必填；3～50 位，以字母开头，可使用小写字母、数字、点、下划线和连字符"),
                List.of("姓名", "必填；最多 64 个字符"),
                List.of("手机号码", "选填；中国大陆手机号码"),
                List.of("电子邮箱", "选填；有效电子邮箱地址"),
                List.of("主组织编码", "必填；必须存在于目标系统，每个用户只能有一个主组织"),
                List.of("其他组织编码", "选填；多个编码使用英文逗号分隔，不能与主组织重复"),
                List.of("角色编码", "必填；多个编码使用英文逗号分隔，必须存在且当前管理员可分配"),
                List.of("启用状态", "选填；填写“启用”或“停用”，留空默认为启用"),
                List.of("排序号", "选填；非负整数，留空默认为 0"),
                List.of("文件限制", "用户数据最多 10000 行，Excel 文件不得超过 20 MB"),
                List.of("导入规则", "只新增用户；全部校验通过后一次写入，任何错误都不会写入数据"),
                List.of("初始密码", "导入用户统一使用目标系统当前公共默认密码，首次登录必须修改")
        );
        for (int index = 0; index < instructions.size(); index++) {
            ExcelWorkbookSupport.writeRow(sheet, index + 4, instructions.get(index), bodyStyle);
        }
        sheet.setColumnWidth(0, 22 * 256);
        sheet.setColumnWidth(1, 90 * 256);
    }

    private void writeOrgSheet(
            Workbook workbook,
            List<SystemOrg> orgs,
            CellStyle headerStyle,
            CellStyle bodyStyle
    ) {
        Sheet sheet = workbook.createSheet(ORG_SHEET);
        ExcelWorkbookSupport.writeRow(
                sheet, 0, List.of("组织编码", "组织名称", "组织类型", "完整路径"), headerStyle
        );
        for (int index = 0; index < orgs.size(); index++) {
            SystemOrg org = orgs.get(index);
            ExcelWorkbookSupport.writeRow(sheet, index + 1, List.of(
                    org.getOrgCode(), org.getOrgName(), orgTypeName(org), org.getFullPath()
            ), bodyStyle);
        }
        setWidths(sheet, 22, 24, 14, 60);
        sheet.createFreezePane(0, 1);
    }

    private void writeRoleSheet(
            Workbook workbook,
            List<SystemRole> roles,
            CellStyle headerStyle,
            CellStyle bodyStyle
    ) {
        Sheet sheet = workbook.createSheet(ROLE_SHEET);
        ExcelWorkbookSupport.writeRow(
                sheet, 0, List.of("角色编码", "角色名称", "角色类型"), headerStyle
        );
        for (int index = 0; index < roles.size(); index++) {
            SystemRole role = roles.get(index);
            ExcelWorkbookSupport.writeRow(sheet, index + 1, List.of(
                    role.getRoleCode(), role.getRoleName(),
                    role.getRoleType().name().equals("SYSTEM") ? "系统角色" : "自定义角色"
            ), bodyStyle);
        }
        setWidths(sheet, 24, 28, 16);
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
            throw PlatformException.badRequest("用户数据表头不能为空");
        }
        for (int column = 0; column < HEADERS.size(); column++) {
            if (!HEADERS.get(column).equals(ExcelCellSupport.text(header.getCell(column)))) {
                throw PlatformException.badRequest("用户数据表头不正确，请勿修改模板列名或顺序");
            }
        }
        if (header.getLastCellNum() > HEADERS.size()) {
            throw PlatformException.badRequest("用户数据包含模板之外的列");
        }
    }

    private List<RawRow> readRows(Sheet sheet) {
        if (sheet.getLastRowNum() > MAX_EXCHANGE_ROWS) {
            throw PlatformException.badRequest("用户数据不能超过 10000 行");
        }
        List<RawRow> rows = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (hasUnexpectedCells(row)) {
                throw PlatformException.badRequest(
                        "用户数据第 " + (rowIndex + 1) + " 行包含模板之外的列"
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
            throw PlatformException.badRequest("用户数据不能超过 10000 行");
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

    private Validation validate(List<RawRow> rows) {
        ErrorCollector errors = new ErrorCollector();
        List<NormalizedRow> normalizedRows = new ArrayList<>();
        Set<String> allOrgCodes = new LinkedHashSet<>();
        Set<String> allRoleCodes = new LinkedHashSet<>();
        for (RawRow row : rows) {
            NormalizedRow normalized = normalize(row, errors);
            normalizedRows.add(normalized);
            allOrgCodes.addAll(normalized.orgCodes());
            allRoleCodes.addAll(normalized.roleCodes());
        }
        checkDuplicateAndExistingValues(normalizedRows, errors);
        Map<String, SystemOrg> orgs = orgQueryService.orgsByCodes(allOrgCodes);
        Map<String, SystemRole> roles = roleQueryService.rolesByCodes(allRoleCodes);
        List<UserImportItem> items = new ArrayList<>();
        for (NormalizedRow row : normalizedRows) {
            checkMissingCodes(row, orgs, roles, errors);
            if (!row.valid() || errors.hasRowErrors(row.rowNumber())) {
                continue;
            }
            List<Long> orgIds = row.orgCodes().stream().map(code -> orgs.get(code).getId()).toList();
            List<Long> roleIds = row.roleCodes().stream().map(code -> roles.get(code).getId()).toList();
            items.add(new UserImportItem(
                    row.rowNumber(), row.username(), row.realName(), row.phone(), row.email(),
                    orgs.get(row.primaryOrgCode()).getId(), orgIds, roleIds,
                    row.enabled(), row.sortOrder()
            ));
        }
        return new Validation(items, errors.total(), errors.items());
    }

    private NormalizedRow normalize(RawRow row, ErrorCollector errors) {
        String username = normalizeField(row, 0, "登录名", inputService::normalizeNewUsername, errors);
        String realName = normalizeField(row, 1, "姓名", inputService::normalizeRealName, errors);
        String phone = normalizeField(row, 2, "手机号码", inputService::normalizePhone, errors);
        String email = normalizeField(row, 3, "电子邮箱", inputService::normalizeEmail, errors);
        String primaryOrgCode = normalizeCode(row.value(4));
        if (primaryOrgCode == null) {
            errors.add(row.rowNumber(), "主组织编码", "主组织编码不能为空");
        }
        LinkedHashSet<String> orgCodes = splitCodes(
                row, 5, "其他组织编码", errors
        );
        if (primaryOrgCode != null && !orgCodes.add(primaryOrgCode)) {
            errors.add(row.rowNumber(), "其他组织编码", "其他组织不能与主组织重复");
        }
        if (orgCodes.size() > 100) {
            errors.add(row.rowNumber(), "其他组织编码", "单个用户最多归属 100 个组织");
        }
        LinkedHashSet<String> roleCodes = splitCodes(row, 6, "角色编码", errors);
        if (roleCodes.isEmpty()) {
            errors.add(row.rowNumber(), "角色编码", "角色编码不能为空");
        }
        if (roleCodes.size() > 100) {
            errors.add(row.rowNumber(), "角色编码", "单个用户最多分配 100 个角色");
        }
        Boolean enabled = parseEnabled(row, errors);
        Integer sortOrder = parseSortOrder(row, errors);
        boolean valid = username != null && realName != null && primaryOrgCode != null
                && !roleCodes.isEmpty() && enabled != null && sortOrder != null;
        List<String> orderedOrgCodes = new ArrayList<>();
        if (primaryOrgCode != null) {
            orderedOrgCodes.add(primaryOrgCode);
        }
        orgCodes.stream().filter(code -> !code.equals(primaryOrgCode)).forEach(orderedOrgCodes::add);
        return new NormalizedRow(
                row.rowNumber(), username, realName, phone, email, primaryOrgCode,
                orderedOrgCodes, new ArrayList<>(roleCodes),
                enabled != null && enabled, sortOrder == null ? 0 : sortOrder, valid
        );
    }

    private <T> T normalizeField(
            RawRow row,
            int column,
            String field,
            Function<String, T> normalizer,
            ErrorCollector errors
    ) {
        try {
            return normalizer.apply(row.value(column));
        } catch (PlatformException exception) {
            errors.add(row.rowNumber(), field, exception.getMessage());
            return null;
        }
    }

    private Boolean parseEnabled(RawRow row, ErrorCollector errors) {
        String value = row.value(7);
        if (!StringUtils.hasText(value) || "启用".equals(value.trim())) {
            return true;
        }
        if ("停用".equals(value.trim())) {
            return false;
        }
        errors.add(row.rowNumber(), "启用状态", "只能填写“启用”或“停用”");
        return null;
    }

    private Integer parseSortOrder(RawRow row, ErrorCollector errors) {
        String value = row.value(8);
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        try {
            int sortOrder = Integer.parseInt(value.trim());
            if (sortOrder < 0) {
                throw new NumberFormatException();
            }
            return sortOrder;
        } catch (NumberFormatException exception) {
            errors.add(row.rowNumber(), "排序号", "排序号必须为非负整数");
            return null;
        }
    }

    private void checkDuplicateAndExistingValues(
            List<NormalizedRow> rows,
            ErrorCollector errors
    ) {
        Map<String, Integer> usernames = new HashMap<>();
        Map<String, Integer> phones = new HashMap<>();
        Map<String, Integer> emails = new HashMap<>();
        rows.forEach(row -> {
            checkDuplicate(row.rowNumber(), "登录名", row.username(), usernames, errors);
            checkDuplicate(row.rowNumber(), "手机号码", row.phone(), phones, errors);
            checkDuplicate(row.rowNumber(), "电子邮箱", row.email(), emails, errors);
        });
        List<String> usernameValues = rows.stream().map(NormalizedRow::username).filter(java.util.Objects::nonNull).toList();
        List<String> phoneValues = rows.stream().map(NormalizedRow::phone).filter(java.util.Objects::nonNull).toList();
        List<String> emailValues = rows.stream().map(NormalizedRow::email).filter(java.util.Objects::nonNull).toList();
        LambdaQueryWrapper<SystemUser> wrapper = new LambdaQueryWrapper<>();
        boolean hasCondition = false;
        if (!usernameValues.isEmpty()) {
            wrapper.in(SystemUser::getUsername, usernameValues);
            hasCondition = true;
        }
        if (!phoneValues.isEmpty()) {
            if (hasCondition) wrapper.or();
            wrapper.in(SystemUser::getPhone, phoneValues);
            hasCondition = true;
        }
        if (!emailValues.isEmpty()) {
            if (hasCondition) wrapper.or();
            wrapper.in(SystemUser::getEmail, emailValues);
            hasCondition = true;
        }
        if (!hasCondition) {
            return;
        }
        Map<String, SystemUser> existingUsernames = new HashMap<>();
        Map<String, SystemUser> existingPhones = new HashMap<>();
        Map<String, SystemUser> existingEmails = new HashMap<>();
        userMapper.selectList(wrapper).forEach(user -> {
            existingUsernames.put(user.getUsername(), user);
            if (user.getPhone() != null) existingPhones.put(user.getPhone(), user);
            if (user.getEmail() != null) existingEmails.put(user.getEmail(), user);
        });
        rows.forEach(row -> {
            checkExisting(row.rowNumber(), "登录名", row.username(), existingUsernames, errors);
            checkExisting(row.rowNumber(), "手机号码", row.phone(), existingPhones, errors);
            checkExisting(row.rowNumber(), "电子邮箱", row.email(), existingEmails, errors);
        });
    }

    private void checkMissingCodes(
            NormalizedRow row,
            Map<String, SystemOrg> orgs,
            Map<String, SystemRole> roles,
            ErrorCollector errors
    ) {
        List<String> missingOrgs = row.orgCodes().stream().filter(code -> !orgs.containsKey(code)).toList();
        if (!missingOrgs.isEmpty()) {
            errors.add(row.rowNumber(), "组织编码", "组织不存在：" + String.join(",", missingOrgs));
        }
        List<String> missingRoles = row.roleCodes().stream().filter(code -> !roles.containsKey(code)).toList();
        if (!missingRoles.isEmpty()) {
            errors.add(row.rowNumber(), "角色编码", "角色不存在或当前管理员不可分配：" + String.join(",", missingRoles));
        }
    }

    private void checkDuplicate(
            int rowNumber,
            String field,
            String value,
            Map<String, Integer> firstRows,
            ErrorCollector errors
    ) {
        if (value == null) return;
        Integer firstRow = firstRows.putIfAbsent(value, rowNumber);
        if (firstRow != null) {
            errors.add(rowNumber, field, field + "与第 " + firstRow + " 行重复");
        }
    }

    private void checkExisting(
            int rowNumber,
            String field,
            String value,
            Map<String, SystemUser> existing,
            ErrorCollector errors
    ) {
        if (value != null && existing.containsKey(value)) {
            errors.add(rowNumber, field, field + "已被使用");
        }
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

    private LinkedHashSet<String> splitCodes(
            RawRow row,
            int column,
            String field,
            ErrorCollector errors
    ) {
        String value = row.value(column);
        if (!StringUtils.hasText(value)) {
            return new LinkedHashSet<>();
        }
        LinkedHashSet<String> codes = new LinkedHashSet<>();
        for (String part : value.split(",", -1)) {
            String code = normalizeCode(part);
            if (code == null) {
                errors.add(row.rowNumber(), field, field + "中不能包含空编码");
            } else if (!codes.add(code)) {
                errors.add(row.rowNumber(), field, field + "中存在重复编码：" + code);
            }
        }
        return codes;
    }

    private String normalizeCode(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : null;
    }

    private String orgTypeName(SystemOrg org) {
        return switch (org.getOrgType()) {
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

    private void addEnabledValidation(Sheet sheet) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(
                new String[]{"启用", "停用"}
        );
        org.apache.poi.ss.util.CellRangeAddressList range =
                new org.apache.poi.ss.util.CellRangeAddressList(1, MAX_EXCHANGE_ROWS, 7, 7);
        DataValidation validation = helper.createValidation(constraint, range);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("启用状态无效", "请选择“启用”或“停用”");
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    private record RawRow(int rowNumber, List<String> values) {
        String value(int column) {
            return values.get(column);
        }
    }

    private record NormalizedRow(
            int rowNumber,
            String username,
            String realName,
            String phone,
            String email,
            String primaryOrgCode,
            List<String> orgCodes,
            List<String> roleCodes,
            boolean enabled,
            int sortOrder,
            boolean valid
    ) {
    }

    private record Validation(
            List<UserImportItem> items,
            int errorCount,
            List<UserImportError> errors
    ) {
    }

    private static final class ErrorCollector {
        private final List<UserImportError> items = new ArrayList<>();
        private final Set<Integer> rows = new HashSet<>();
        private int total;

        void add(int rowNumber, String field, String message) {
            total++;
            rows.add(rowNumber);
            if (items.size() < ERROR_LIMIT) {
                items.add(new UserImportError(rowNumber, field, message));
            }
        }

        boolean hasRowErrors(int rowNumber) {
            return rows.contains(rowNumber);
        }

        int total() {
            return total;
        }

        List<UserImportError> items() {
            return List.copyOf(items);
        }
    }
}
