package com.superkl.backend.utils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class CSVUtil {
    private CSVUtil() {}

    // 设置响应头
    public static void setResponseHeaders(HttpServletResponse response, String fileName) {
        response.setContentType("text/csv;charset=UTF-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition",
                "attachment;filename=" + encodedFileName + ".csv;filename*=UTF-8''" + encodedFileName + ".csv");
    }

    public static void writeBom(PrintWriter writer) {
        writer.write('\uFEFF');
    }

    public static void writeLine(PrintWriter writer, String... cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) sb.append(",");
            String value = sanitize(cells[i]);
            if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                sb.append("\"").append(value.replace("\"", "\"\"")).append("\"");
            } else {
                sb.append(value);
            }
        }
        writer.println(sb);
    }

    // 合法数字（含负数和小数），这类值不能加引号前缀，否则金额列会变成文本
    private static final Pattern NUMBER = Pattern.compile("^-?\\d+(\\.\\d+)?$");

    // 防止 CSV 公式注入：以 = + - @ 制表符 回车 开头的单元格
    // Excel 打开时会当公式执行，加一个单引号强制转成文本
    private static String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        char first = value.charAt(0);
        boolean risky = first == '=' || first == '+' || first == '-'
                || first == '@' || first == '\t' || first == '\r';
        if (!risky) {
            return value;
        }
        if (NUMBER.matcher(value).matches()) {
            return value;
        }
        return "'" + value;
    }
}
