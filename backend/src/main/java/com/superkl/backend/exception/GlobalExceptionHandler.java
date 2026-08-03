package com.superkl.backend.exception;

import com.superkl.backend.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // 参数校验失败（@Valid 校验 DTO 字段）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败：{}", msg);
        return Result.error(422, msg);
    }

    // 请求体 JSON 格式错误
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleJsonParseError(HttpMessageNotReadableException e) {
        log.warn("JSON解析失败：{}", e.getMessage());
        return Result.error(410, "请求数据格式错误，请检查请求体");
    }

    // 路径参数类型转换错误
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误：{}", e.getMessage());
        return Result.error(411, "参数" + e.getName() + "类型错误，期望类型：" + e.getRequiredType().getSimpleName());
    }

    // 缺少请求参数
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数：{}", e.getMessage());
        return Result.error(412, "缺少必要参数：" + e.getParameterName());
    }

    // 枚举参数不合法
    @ExceptionHandler(HttpMessageConversionException.class)
    public Result<?> handleConversion(HttpMessageConversionException e) {
        log.warn("参数转换异常：{}", e.getMessage());
        return Result.error(413, "参数格式不正确");
    }

    // 键重复异常
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("键重复异常：{}", e.getMessage());
        return Result.error(414, "键重复异常：" + e.getMessage());
    }

    // 其他未预期异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "服务器内部错误，请稍后重试");
    }
}
