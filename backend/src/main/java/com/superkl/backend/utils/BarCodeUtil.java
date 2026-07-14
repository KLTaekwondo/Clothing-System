package com.superkl.backend.utils;

public class BarCodeUtil {
    private BarCodeUtil () {} // 私有构造函数，防止外部实例化

    // 1. 生成条码
    public static String generateEAN13(){
        long prefix = 697_000_000_000L + (long) (Math.random() * 100_000_000_000L);
        String raw = String.format("%012d", prefix);// 生成12位随机数
        int checkDigit = calculateCheckDigit(raw);
        return raw + checkDigit;
    }

    // 2.生成校验位
    private static int calculateCheckDigit(String raw){
        int sum = 0;
        for(int i = 0; i < raw.length(); i++){
            int digit = Character.getNumericValue(raw.charAt(i));
            sum += (i + 1) % 2 == 0 ? digit * 3 : digit;
        }
        return (10 - (sum % 10)) % 10;
    }
}
