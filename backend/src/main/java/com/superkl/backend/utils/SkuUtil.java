package com.superkl.backend.utils;

import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.exception.BusinessException;

import java.util.*;

public class SkuUtil {
    private SkuUtil() {} // 私有构造方法，防止外部实例化

    // 1. 组合选中的选项，生成SKU名称
    public static List<Map<String , String>> generateSkuList(Map<String , List<String>> selectedOptions) {
        List<Map<String , String>> result = new ArrayList<>();
        result.add(new LinkedHashMap<>());

        // 1.解开外层Map(Map.Entry是指Map中的一个键值对，key是选项类型，value是选项值列表)
        for (Map.Entry<String, List<String>> entry : selectedOptions.entrySet()) {
            // 1.获取选项值和列表
            String optionName = entry.getKey();
            List<String> optionValues = entry.getValue();

            // 开始组合
            List<Map<String, String>> newResult = new ArrayList<>();
            // 遍历现有的所有组合
            for (Map<String, String> existing : result) {
                // 对每个组合，和当前类型的每个值组合出新组合
                for (String value : optionValues) {
                    Map<String, String> newCombo = new LinkedHashMap<>(existing);
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

    // 3. 校验规格参数
    public static void validateSelectedOptions(Map<String, List<String>> selectedOptions) {

        if (selectedOptions == null || selectedOptions.isEmpty())
        {
            throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "商品规格不能为空");
        }

        // 为确保笛卡尔积完整生成，至少需要两种规格（如颜色+尺码）
        if (selectedOptions.size() < 2) {
            throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "商品规格不完整，至少需要两种规格（如颜色+尺码）");
        }

        for (Map.Entry<String, List<String>> entry : selectedOptions.entrySet()) {
            String optionName = entry.getKey();
            List<String> optionValues = entry.getValue();

            if (optionName == null || optionName.isBlank()) {
                throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "商品规格名称不能为空");
            }

            if (optionValues == null || optionValues.isEmpty()) {
                throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "商品规格值不能为空");
            }

            Set<String> uniqueValues = new HashSet<>();

            for (String value : optionValues) {
                if (value == null || value.isBlank()) {
                    throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "商品规格值不能为空");
                }

                if (!uniqueValues.add(value.trim())) {
                    throw new BusinessException(ErrorCodeEnum.RULE_CONFLICT, "同一规格下不能有重复值");
                }

                if(!value.equals(value.trim())) {
                    throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "商品规格值不能包含前后空格");
                }
            }
        }
    }
}
