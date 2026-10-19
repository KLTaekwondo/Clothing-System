package com.superkl.backend.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CSVUtilTest {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    // 测试普通值写入CSV
    @Test
    public void test_NormalValue(){
        CSVUtil.writeLine(pw, "123");
        pw.flush();
        assertEquals("123", sw.toString().trim());
    }

    // 需要转译的值
    @Test
    public void test_SpecialValue(){
        CSVUtil.writeLine(pw, "123,456");
        pw.flush();
        assertEquals("\"123,456\"",  sw.toString().trim());
    }

    // 含引号的值：引号必须翻倍转义，且整体用双引号包裹
    @Test
    public void test_ValueWithQuote(){
        CSVUtil.writeLine(pw, "他说\"好\"");
        pw.flush();
        assertEquals("\"他说\"\"好\"\"\"", sw.toString().trim());
    }

    // 含换行的值：必须用双引号包起来，防止 CSV 行错乱
    @Test
    public void test_ValueWithNewline(){
        CSVUtil.writeLine(pw, "a\nb");
        pw.flush();
        assertEquals("\"a\nb\"", sw.toString().trim());
    }

    // null 值：输出为空
    @Test
    public void test_NullValue(){
        CSVUtil.writeLine(pw, (String) null);
        pw.flush();
        assertEquals("", sw.toString().trim());
    }

    // 多列：逗号分隔
    @Test
    public void test_MultipleCells(){
        CSVUtil.writeLine(pw, "a", "b", "c");
        pw.flush();
        assertEquals("a,b,c", sw.toString().trim());
    }

    // 混合：普通列 + 需要转义的列，只给特殊列加引号
    @Test
    public void test_MixedCells(){
        CSVUtil.writeLine(pw, "a", "b,c");
        pw.flush();
        assertEquals("a,\"b,c\"", sw.toString().trim());
    }

    // 空单元格混排：空的保持空，不加引号
    @Test
    public void test_EmptyCellBetweenValues(){
        CSVUtil.writeLine(pw, "a", "", "c");
        pw.flush();
        assertEquals("a,,c", sw.toString().trim());
    }

    @Test
    public void test_SetResponseHeaders(){
        HttpServletResponse response = mock(HttpServletResponse.class);
        CSVUtil.setResponseHeaders(response, "库存报表");

        // ① Content-Type 必须对
        verify(response).setContentType("text/csv;charset=UTF-8");
        // ② 中文文件名必须被 URL 编码(否则下载乱码)
        verify(response).setHeader(eq("Content-Disposition"),
                contains("attachment;filename=%E5%BA%93%E5%AD%98%E6%8A%A5%E8%A1%A8.csv"));
    }
}
