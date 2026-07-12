package com.superkl.backend.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    // 有数据时，返回成功结果
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    // 无数据时，返回成功结果
    public static <T> Result<T> successMessage(String msg) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        return result;
    }

    // 有错误码时，返回错误消息
    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    // 默认错误码为400，返回错误消息，很可能用不到
    public static  <T> Result<T> errorMessage(String msg) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMsg(msg);
        return result;
    }
}
