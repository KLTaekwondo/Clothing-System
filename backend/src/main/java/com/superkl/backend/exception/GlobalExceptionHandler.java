package com.superkl.backend.exception;

import com.superkl.backend.common.Result;
import com.superkl.backend.enums.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        HttpStatus status = HttpStatus.resolve(e.getCode());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(e.getMessage());
    }

    // 参数校验失败（@Valid 校验 DTO 字段）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败：{}", msg);
        return ResponseEntity.status(ErrorCodeEnum.PARAM_ERROR.getCode())
                .body(msg);
    }

    // 请求体 JSON 格式错误
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleJsonParseError(HttpMessageNotReadableException e) {
        log.warn("JSON解析失败：{}", e.getMessage());
        return ResponseEntity.status(ErrorCodeEnum.JSON_PARSE_ERROR.getCode())
                .body(e.getMessage());
    }

    // 路径参数类型转换错误
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误：{}", e.getMessage());
        return ResponseEntity.status(ErrorCodeEnum.PARAM_TYPE_ERROR.getCode())
                .body("参数类型错误，期望类型：" + e.getRequiredType().getSimpleName());
    }

    // 缺少请求参数
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数：{}", e.getMessage());
        return ResponseEntity.status(ErrorCodeEnum.MISSING_PARAM_ERROR.getCode())
                .body("缺少必要参数：" + e.getParameterName());
    }

    // 枚举参数不合法
    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<String> handleConversion(HttpMessageConversionException e) {
        log.warn("参数转换异常：{}", e.getMessage());
        return ResponseEntity.status(ErrorCodeEnum.PARAM_FORMAT_ERROR.getCode())
                .body("参数格式不正确");
    }

    // 键重复异常
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("键重复异常：{}", e.getMessage());
        return ResponseEntity.status(ErrorCodeEnum.PARAM_VALUE_DUPLICATE_ERROR.getCode())
                .body("数据已存在或违反约束，请检查后重试");
    }

    // 其他未预期异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(ErrorCodeEnum.SYSTEM_ERROR.getCode())
                .body("服务器内部错误，请联系管理员");
    }
}
