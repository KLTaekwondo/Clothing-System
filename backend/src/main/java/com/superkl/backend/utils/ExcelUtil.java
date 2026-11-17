package com.superkl.backend.utils;

import com.superkl.backend.common.excel.ExcelPullList;
import com.superkl.backend.common.excel.ExcelRow;
import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {
    private ExcelUtil() {
    } // 私有化构造方法，防止外部实例化

    // 创建工作簿
    public static XSSFWorkbook createWorkbook(String sheetName, List<String> headers, List<Integer> columnWidths, ExcelPullList[] pullLists) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName); // sheet表名称
        // 建立隐藏表
        if (pullLists != null && pullLists.length > 0) {
            Sheet invSheet = wb.createSheet("invSheet");
            wb.setSheetHidden(wb.getSheetIndex(invSheet), true);
            // 设置下拉框
            for (ExcelPullList pullList : pullLists) {
                setPullDownList(sheet, invSheet, pullList);
            }
        }

        // 设置冻结窗格
        sheet.createFreezePane(0, 1);
        // 设置列宽
        setColumnWidths(sheet, columnWidths);
        // 设置表头样式和内容
        headerSetter(wb, headers, sheet);

        return wb;
    }

    // 写入响应流
    public static void writeToResponse(XSSFWorkbook wb, HttpServletResponse response, String fileName) throws IOException {
        // 1. 告诉浏览器：这是什么类型的文件
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // 2. 告诉浏览器：这是附件，要下载；并给出文件名（中文要编码）
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + encoded + ".xlsx;filename*=UTF-8''" + encoded + ".xlsx");

        // 3. 把工作簿写进响应流
        wb.write(response.getOutputStream());
    }

    // 读取Excel文档
    public static List<ExcelRow> readRows(MultipartFile multipartFile) {
        List<ExcelRow> rows = new ArrayList<>();
        // 1. 读取文件
        try(InputStream inputStream = multipartFile.getInputStream();
            Workbook wb = WorkbookFactory.create(inputStream);
        ) {
            // 2. 读取工作表
            Sheet sheet = wb.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            int headerLength = sheet.getRow(0).getLastCellNum();

            // 3. 读取数据
            for(int i = 1; i <= sheet.getLastRowNum(); i++ ) {
                // 获取sheet的第i+1行数据，默认表头跳过第一行
                Row row = sheet.getRow(i);
                // 为空则跳过
                if(row == null) {
                    continue;
                }

                // 看看整条数据的长度为多少
                int lastCellNum = row.getLastCellNum();
                // 为空则跳过
                if(lastCellNum <= 0) {
                    continue;
                }

                // 读取数据
                if(lastCellNum < headerLength) {
                    throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "第 " + (i+1) + " 行数据列数不足");
                }

                String[] data = new String[lastCellNum];
                boolean allBlank = true;

                for(int j = 0; j < lastCellNum; j++) {
                    // 直接提取本行的第j列数据，并去除空格
                    String value = formatter.formatCellValue(row.getCell(j)).trim();
                    data[j] = value;

                    // 如果本行数据不为空，就设置allBlank为false
                    if(!value.isEmpty()) {
                        allBlank = false;
                    }
                }

                // 如果本行数据全为空，就跳过
                if(allBlank) {
                    continue;
                }

                // 保存本行数据
                rows.add(new ExcelRow(i+1, data));
            }
        }catch (IOException | RuntimeException e) {
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "读取Excel文档失败，请确认为未加密文件");
        }

        return rows;
    }


    // === 辅助方法 ===
    // 创建表头样式
    private static void headerSetter(XSSFWorkbook wb, List<String> headers, Sheet sheet) {
        // new一个新的样式对象
        CellStyle headerStyle = wb.createCellStyle();
        // 1.字体
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 15);
        headerStyle.setFont(headerFont);

        // 2.背景颜色
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 3.对齐方式
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setWrapText(true);

        // 4.边框
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);

        // 5.表头内容
        Row row = sheet.createRow(0); // 创建第一行
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

    }

    // 设置列宽
    private static void setColumnWidths(Sheet sheet, List<Integer> columnWidths) {
        for (int i = 0; i < columnWidths.size(); i++) {
            sheet.setColumnWidth(i, columnWidths.get(i) * 256);
        }
    }

    // 设置下拉框
    private static void setPullDownList(Sheet targetSheet, Sheet sourceSheet, ExcelPullList pullList) {
        // 设置下拉框
        DataValidation validation = getValidation(pullList, sourceSheet);
        if(validation == null) return;
        targetSheet.addValidationData(validation);
    }

    // 获取下拉框验证
    private static DataValidation getValidation(ExcelPullList pullList, Sheet sourceSheet) {
        // 提前校验，避免后续消耗和报错
        List<String> list = pullList.getList();
        if (list == null || list.isEmpty()) return null;

        // 取出必要数据
        int startRow = pullList.getStartRow();
        int endRow = pullList.getEndRow();
        int startColumn = pullList.getStartColumn();
        int endColumn = pullList.getEndColumn();


        // 列索引转换为Excel列字母
        String column = CellReference.convertNumToColString(startColumn);;
        // 获取隐藏表名称
        String sheetName = sourceSheet.getSheetName();
        // 计算下拉框范围的Excel公式
        String format = sheetName + "!$" + column + "$1:$" + column + "$" + list.size();

        // 设置下拉框约束
        DataValidationHelper helper = sourceSheet.getDataValidationHelper();
        // 设置下拉框范围
        DataValidationConstraint constraint = helper.createFormulaListConstraint(format);
        // 设置下拉框验证范围（从startRow到endRow，从startColumn到endColumn）
        CellRangeAddressList range = new CellRangeAddressList(startRow, endRow, startColumn, endColumn);
        // 创建下拉框验证
        DataValidation validation = helper.createValidation(constraint, range);

        // 初始化下拉框
        for (int index = 0; index < list.size(); index++) {
            Row row = sourceSheet.getRow(index);
            if (row == null) {
                row = sourceSheet.createRow(index);
            }
            row.createCell(startColumn).setCellValue(list.get(index));
        }

        validation.setShowErrorBox(true);
        if (pullList.isLocked()) {
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox("取值不存在", "请选择正确的值");
        } else {
            validation.setErrorStyle(DataValidation.ErrorStyle.WARNING);
            validation.createErrorBox("不在候选列表中", "可以继续输入，导入时会自动创建");
        }
        return validation;
    }


}