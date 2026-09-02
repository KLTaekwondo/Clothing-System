package com.superkl.backend.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)  // 启用 Mockito
public class BarCodeUtilTest {
    // 测试一: 测试生成EAN13条码, 长度为13, 且只包含数字
    @Test
    public void testBarCodeUtil() {
        String barCode = BarCodeUtil.generateEAN13();
        assertEquals(13, barCode.length());
        assertTrue(barCode.matches("\\d{13}"));
    }

    // 测试二：校验位必须符合 EAN-13 算法(核心,防算法被改坏)
    @Test
    public void generateEAN13_shouldHaveValidCheckDigit() {
        String code = BarCodeUtil.generateEAN13();
        int expectedCheck = calculateCheckDigit(code);
        assertEquals(expectedCheck, Character.getNumericValue(code.charAt(12)));
    }
    // 测试三：条形码同时生成时，不能重复
    @Test
    public void test_barCodeNotDuplicate() {
        String barCode1 = BarCodeUtil.generateEAN13();
        String barCode2 = BarCodeUtil.generateEAN13();
        assertNotEquals(barCode1, barCode2);
    }
    // 测试四：条形码校验反例是否通过
    @Test
    public void test_generateEAN13_shouldHaveValidCheckDigit() {
        String code = "69712345678901";
        boolean flag = calculateCheckDigit(code) == (Character.getNumericValue(code.charAt(12)));
        assertFalse(flag);
    }

    private int calculateCheckDigit(String rawCode) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(rawCode.charAt(i));
            sum += (i + 1) % 2 == 0 ? digit * 3 : digit;
        }
        return (10 - sum % 10) % 10;
    }
}
