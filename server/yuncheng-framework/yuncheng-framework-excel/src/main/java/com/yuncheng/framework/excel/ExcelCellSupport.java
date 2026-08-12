package com.yuncheng.framework.excel;

import java.util.Locale;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;

/** Excel 单元格的稳定文本读取。 */
public final class ExcelCellSupport {

    private static final ThreadLocal<DataFormatter> FORMATTER =
            ThreadLocal.withInitial(() -> new DataFormatter(Locale.CHINA));

    private ExcelCellSupport() {
    }

    public static String text(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        if (cell.getCellType() == CellType.FORMULA) {
            throw new IllegalArgumentException("不允许使用公式单元格");
        }
        String value = FORMATTER.get().formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }
}
