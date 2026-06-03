package com.engseg.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelBuilderTest {

    @Test
    void writeHeader_colocaColunasCorretas() throws IOException {
        ExcelBuilder builder = new ExcelBuilder("Teste");
        builder.writeHeader(List.of("COL_A", "COL_B"));
        builder.writeRow(List.of("v1", "v2"));
        byte[] bytes = builder.build();

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("COL_A");
            assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("COL_B");
        }
    }

    @Test
    void writeRow_colocaValoresNaLinhaCorreta() throws IOException {
        ExcelBuilder builder = new ExcelBuilder("Teste");
        builder.writeHeader(List.of("COL_A"));
        builder.writeRow(List.of("valor_esperado"));
        byte[] bytes = builder.build();

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("valor_esperado");
        }
    }

    @Test
    void writeRow_comNumero_armazenaComoNumerico() throws IOException {
        ExcelBuilder builder = new ExcelBuilder("Teste");
        builder.writeHeader(List.of("NUM"));
        builder.writeRow(List.of(42L));
        byte[] bytes = builder.build();

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getRow(1).getCell(0).getNumericCellValue()).isEqualTo(42.0);
        }
    }

    @Test
    void build_retornaByteArrayNaoVazio() throws IOException {
        ExcelBuilder builder = new ExcelBuilder("Teste");
        builder.writeHeader(List.of("A"));
        byte[] bytes = builder.build();
        assertThat(bytes).isNotEmpty();
    }
}
