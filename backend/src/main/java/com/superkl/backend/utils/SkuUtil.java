package com.superkl.backend.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkuUtil {
    private SkuUtil() {} // 私有构造方法，防止外部实例化

    // 1. 组合选中的选项，生成SKU名称
    public static List<Map<String , String>> generateSkuList(Map<String , List<String>> selectedOptions) {
        List<Map<String , String>> result = new ArrayList<>();
        result.add(new HashMap<>());

        // 1.解开外层Map(Map.Entry是指Map中的一个键值对，key是选项类型，value是选项值列表)
        for (Map.Entry<String, List<String>> entry : selectedOptions.entrySet()) {
            String optionName = entry.getKey();
            List<String> optionValues = entry.getValue();

            List<Map<String, String>> newResult = new ArrayList<>();
            // 遍历现有的所有组合
            for (Map<String, String> existing : result) {
                // 对每个组合，和当前类型的每个值组合出新组合
                for (String value : optionValues) {
                    Map<String, String> newCombo = new HashMap<>(existing);
                    newCombo.put(optionName, value);
                    newResult.add(newCombo);
                }
            }
            // 替换结果
            result = newResult;
        }
        return result;
    }

    // 2. 组合skuName
    public static String generateSkuName(Map<String, String> combo) {
        return String.join("-", combo.values());
    }
}
