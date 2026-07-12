package com.superkl.backend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.superkl.backend.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public class JsonUtil {
    private JsonUtil() {}// 私有构造方法，防止外部实例化
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new BusinessException("JSON转换失败");
        }
    }

    public static Map<String, String> toMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new BusinessException("JSON解析失败");
        }
    }
}
