package com.engseg.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelBuilder {

    private final Workbook workbook;
    private final Sheet sheet;
    private final CellStyle headerStyle;
    private final CellStyle evenRowStyle;
    private int currentRow = 0;

    public ExcelBuilder(String sheetName) {
        this.workbook = new XSSFWorkbook();
        this.sheet = workbook.createSheet(sheetName);
        this.headerStyle = buildHeaderStyle();
        this.evenRowStyle = buildEvenRowStyle();
    }

    public void writeHeader(List<String> columns) {
        Row row = sheet.createRow(currentRow++);
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columns.get(i));
            cell.setCellStyle(headerStyle);
        }
        sheet.createFreezePane(0, 1);
    }

    public void writeRow(List<Object> values) {
        Row row = sheet.createRow(currentRow);
        CellStyle style = (currentRow % 2 == 0) ? evenRowStyle : null;
        for (int i = 0; i < values.size(); i++) {
            Cell cell = row.createCell(i);
            Object val = values.get(i);
            if (val == null) {
                cell.setCellValue("");
            } else if (val instanceof Number) {
                cell.setCellValue(((Number) val).doubleValue());
            } else if (val instanceof Boolean) {
                cell.setCellValue((Boolean) val);
            } else {
                cell.setCellValue(val.toString());
            }
            if (style != null) cell.setCellStyle(style);
        }
        currentRow++;
    }

    public byte[] build() throws IOException {
        if (sheet.getRow(0) != null) {
            for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }

    private CellStyle buildHeaderStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle buildEvenRowStyle() {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
