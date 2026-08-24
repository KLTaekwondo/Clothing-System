package com.superkl.backend.utils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
            String value = cells[i] == null ? "" : cells[i];
            if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                sb.append("\"").append(value.replace("\"", "\"\"")).append("\"");
            } else {
                sb.append(value);
            }
        }
        writer.println(sb);
    }
}
