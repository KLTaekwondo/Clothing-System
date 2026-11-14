package com.superkl.backend.utils;

import com.superkl.backend.common.ExcelPullList;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// createWorkbook / writeToResponse 的生成行为
class ExcelUtilCreateTest {

    private static final String[] HEADERS = {"商品编码", "商品名称", "季节"};
    private static final int[] WIDTHS = {16, 26, 12};

    // 表头内容、列宽、冻结窗格
    @Test
    void createWorkbook_headerWidthAndFreeze() throws Exception {
        XSSFWorkbook wb = ExcelUtil.createWorkbook("商品导入", HEADERS, WIDTHS, null);
        Sheet sheet = wb.getSheetAt(0);

        assertEquals("商品编码", sheet.getRow(0).getCell(0).getStringCellValue());
        assertEquals("季节", sheet.getRow(0).getCell(2).getStringCellValue());

        // 列宽单位是 1/256 字符，16 字符 = 16 * 256
        assertEquals(16 * 256, sheet.getColumnWidth(0));
        assertEquals(26 * 256, sheet.getColumnWidth(1));

        // 冻结首行：POI 里 HorizontalSplitPosition 对应"冻结了几行"
        assertNotNull(sheet.getPaneInformation());
        assertTrue(sheet.getPaneInformation().isFreezePane());
        assertEquals(1, sheet.getPaneInformation().getHorizontalSplitPosition());
        assertEquals(0, sheet.getPaneInformation().getVerticalSplitPosition());

        wb.close();
    }

    // 下拉框：公式指向隐藏表、范围落位正确
    @Test
    void createWorkbook_dropdown() throws Exception {
        ExcelPullList season = ExcelPullList.builder()
                .startRow(1).endRow(500).startColumn(2).endColumn(2)
                .list(new String[]{"春款", "夏款", "秋款"})
                .build();

        XSSFWorkbook wb = ExcelUtil.createWorkbook("t", HEADERS, WIDTHS,
                new ExcelPullList[]{season});
        Sheet sheet = wb.getSheetAt(0);

        assertEquals(1, sheet.getDataValidations().size());

        DataValidation dv = sheet.getDataValidations().get(0);
        assertEquals("C2:C501", dv.getRegions().getCellRangeAddresses()[0].formatAsString());
        assertEquals("invSheet!$C$1:$C$3", dv.getValidationConstraint().getFormula1());

        // 隐藏表存在且被隐藏，值写在 C 列
        Sheet dict = wb.getSheet("invSheet");
        assertNotNull(dict);
        assertTrue(wb.isSheetHidden(wb.getSheetIndex(dict)));
        assertEquals("春款", dict.getRow(0).getCell(2).getStringCellValue());
        assertEquals("秋款", dict.getRow(2).getCell(2).getStringCellValue());

        wb.close();
    }

    // 多个下拉各占一列，互不覆盖
    @Test
    void createWorkbook_multipleDropdowns() throws Exception {
        ExcelPullList season = ExcelPullList.builder()
                .startRow(1).endRow(500).startColumn(2).endColumn(2)
                .list(new String[]{"春款", "夏款"}).build();
        ExcelPullList special = ExcelPullList.builder()
                .startRow(1).endRow(500).startColumn(10).endColumn(10)
                .list(new String[]{"是", "否"}).build();

        XSSFWorkbook wb = ExcelUtil.createWorkbook("t", HEADERS, WIDTHS,
                new ExcelPullList[]{season, special});

        assertEquals(2, wb.getSheetAt(0).getDataValidations().size());

        Sheet dict = wb.getSheet("invSheet");
        assertEquals("春款", dict.getRow(0).getCell(2).getStringCellValue());
        assertEquals("是", dict.getRow(0).getCell(10).getStringCellValue());

        wb.close();
    }

    // 空列表：不生成下拉，也不能抛异常
    @Test
    void createWorkbook_emptyListIgnored() throws Exception {
        ExcelPullList empty = ExcelPullList.builder()
                .startRow(1).endRow(10).startColumn(2).endColumn(2)
                .list(new String[]{}).build();

        XSSFWorkbook wb = ExcelUtil.createWorkbook("t", HEADERS, WIDTHS,
                new ExcelPullList[]{empty});

        assertEquals(0, wb.getSheetAt(0).getDataValidations().size());
        wb.close();
    }

    // 不给下拉时，不生成多余的隐藏表
    @Test
    void createWorkbook_noPullListNoDictSheet() throws Exception {
        XSSFWorkbook wb = ExcelUtil.createWorkbook("t", HEADERS, WIDTHS, null);

        assertEquals(1, wb.getNumberOfSheets());
        wb.close();
    }

    // 导出到响应：Content-Type、文件名编码
    @Test
    void writeToResponse_headersAndFileName() throws Exception {
        XSSFWorkbook wb = ExcelUtil.createWorkbook("t", HEADERS, WIDTHS, null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        ExcelUtil.writeToResponse(wb, response, "商品导入模板");

        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                response.getContentType());

        String disposition = response.getHeader("Content-Disposition");
        assertNotNull(disposition);
        assertTrue(disposition.contains("attachment"));
        // 中文必须编码，不能出现乱码原始字符
        assertFalse(disposition.contains("商品导入模板"));
        assertTrue(disposition.contains("%E5%95%86%E5%93%81%E5%AF%BC%E5%85%A5%E6%A8%A1%E6%9D%BF"));

        // 响应体确实是 xlsx（PK 开头）
        byte[] body = response.getContentAsByteArray();
        assertTrue(body.length > 0);
        assertEquals(0x50, body[0] & 0xFF);
        assertEquals(0x4B, body[1] & 0xFF);

        wb.close();
    }

    // 生成的字节流能被重新打开，结构完整
    @Test
    void createWorkbook_outputCanBeReopened() throws Exception {
        ExcelPullList season = ExcelPullList.builder()
                .startRow(1).endRow(500).startColumn(2).endColumn(2)
                .list(new String[]{"春款", "夏款"}).build();

        XSSFWorkbook wb = ExcelUtil.createWorkbook("商品导入", HEADERS, WIDTHS,
                new ExcelPullList[]{season});

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        wb.write(buffer);
        wb.close();

        try (XSSFWorkbook read = new XSSFWorkbook(new ByteArrayInputStream(buffer.toByteArray()))) {
            assertEquals("商品导入", read.getSheetName(0));
            assertEquals(3, read.getSheetAt(0).getRow(0).getLastCellNum());
            assertEquals(1, read.getSheetAt(0).getDataValidations().size());
        }
    }
}
