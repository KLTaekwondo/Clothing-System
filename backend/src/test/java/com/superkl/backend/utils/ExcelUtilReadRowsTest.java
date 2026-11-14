package com.superkl.backend.utils;

import com.superkl.backend.common.ExcelRow;
import com.superkl.backend.exception.BusinessException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// readRows 的读取行为
// 覆盖：表头跳过、行号保真、空行跳过、数字格式化、trim、损坏文件
class ExcelUtilReadRowsTest {

    private static final String XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // 正常读取：表头跳过，行号从 2 开始
    @Test
    void readRows_skipHeaderAndKeepRowNumber() throws Exception {
        MockMultipartFile file = build(wb -> {
            Sheet sheet = wb.createSheet("商品导入");
            sheet.createRow(0).createCell(0).setCellValue("商品编码");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("P001");
            r1.createCell(1).setCellValue("连衣裙");

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("P002");
            r2.createCell(1).setCellValue("卫衣");
        });

        List<ExcelRow> rows = ExcelUtil.readRows(file);

        assertEquals(2, rows.size());
        // 第一条数据在 Excel 第 2 行
        assertEquals(2, rows.get(0).getRowIndex());
        assertEquals("P001", rows.get(0).getData()[0]);
        // 第二条在 Excel 第 3 行
        assertEquals(3, rows.get(1).getRowIndex());
        assertEquals("P002", rows.get(1).getData()[0]);
    }

    // 数字单元格不能读成 110.0
    @Test
    void readRows_numericCellShouldNotBecomeDecimal() throws Exception {
        MockMultipartFile file = build(wb -> {
            Sheet sheet = wb.createSheet("t");
            sheet.createRow(0).createCell(0).setCellValue("尺码");

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue(110.0);
            row.createCell(1).setCellValue(30.5);
        });

        ExcelRow row = ExcelUtil.readRows(file).get(0);

        assertEquals("110", row.getData()[0]);
        assertEquals("30.5", row.getData()[1]);
    }

    // 中间空行要跳过，但后面的行号不能被压缩
    @Test
    void readRows_blankRowSkippedButRowNumberKept() throws Exception {
        MockMultipartFile file = build(wb -> {
            Sheet sheet = wb.createSheet("t");
            sheet.createRow(0).createCell(0).setCellValue("编码");

            sheet.createRow(1).createCell(0).setCellValue("P001");

            // 第 3 行只建了 Row，没有任何格子（getLastCellNum 返回 -1）
            sheet.createRow(2);

            sheet.createRow(3).createCell(0).setCellValue("P002");
        });

        List<ExcelRow> rows = ExcelUtil.readRows(file);

        assertEquals(2, rows.size());
        assertEquals(2, rows.get(0).getRowIndex());
        // 空行被跳过，但这条仍然是 Excel 第 4 行
        assertEquals(4, rows.get(1).getRowIndex());
    }

    // 整行都是空字符串也要跳过
    @Test
    void readRows_allBlankCellsSkipped() throws Exception {
        MockMultipartFile file = build(wb -> {
            Sheet sheet = wb.createSheet("t");
            sheet.createRow(0).createCell(0).setCellValue("编码");

            sheet.createRow(1).createCell(0).setCellValue("P001");

            // 有格子但内容全是空字符串
            Row blank = sheet.createRow(2);
            blank.createCell(0).setCellValue("");
            blank.createCell(1).setCellValue("");
        });

        List<ExcelRow> rows = ExcelUtil.readRows(file);

        assertEquals(1, rows.size());
        assertEquals("P001", rows.get(0).getData()[0]);
    }

    // 只填了前面几列时，data 长度跟着实际列数走
    @Test
    void readRows_partialColumns() throws Exception {
        MockMultipartFile file = build(wb -> {
            Sheet sheet = wb.createSheet("t");
            sheet.createRow(0).createCell(0).setCellValue("编码");

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("P001");
            row.createCell(1).setCellValue("连衣裙");
            // 后面 11 列都没填
        });

        ExcelRow row = ExcelUtil.readRows(file).get(0);

        assertEquals(2, row.getData().length);
    }

    // 首尾空格要去掉
    @Test
    void readRows_trimWhitespace() throws Exception {
        MockMultipartFile file = build(wb -> {
            Sheet sheet = wb.createSheet("t");
            sheet.createRow(0).createCell(0).setCellValue("编码");

            sheet.createRow(1).createCell(0).setCellValue("  P001  ");
        });

        assertEquals("P001", ExcelUtil.readRows(file).get(0).getData()[0]);
    }

    // 不是 Excel 的文件要报业务异常，不能把 IOException 漏给上层
    @Test
    void readRows_invalidFileShouldThrowBusinessException() {
        MockMultipartFile file = new MockMultipartFile("file", "fake.xlsx", XLSX,
                "这不是一个 excel 文件".getBytes());

        BusinessException e = assertThrows(BusinessException.class,
                () -> ExcelUtil.readRows(file));

        assertTrue(e.getMessage().contains("Excel"));
    }

    // 只有表头、没有数据行时返回空列表
    @Test
    void readRows_headerOnly() throws Exception {
        MockMultipartFile file = build(wb -> {
            Sheet sheet = wb.createSheet("t");
            sheet.createRow(0).createCell(0).setCellValue("编码");
            sheet.createRow(0).createCell(1).setCellValue("名称");
        });

        assertTrue(ExcelUtil.readRows(file).isEmpty());
    }

    // ===== 工具方法 =====

    private interface SheetBuilder {
        void build(XSSFWorkbook wb);
    }

    private MockMultipartFile build(SheetBuilder builder) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            builder.build(wb);
            wb.write(buffer);
        }
        return new MockMultipartFile("file", "test.xlsx", XLSX, buffer.toByteArray());
    }
}
