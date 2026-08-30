package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {
    // 参数校验错误
    PARAM_ERROR(400, "参数校验错误"),
    JSON_PARSE_ERROR(400, "请求数据格式错误"),
    PARAM_TYPE_ERROR(400, "参数类型错误"),
    MISSING_PARAM_ERROR(400, "缺少必要参数"),
    PARAM_FORMAT_ERROR(400, "参数格式不正确"),
    PARAM_VALUE_DUPLICATE_ERROR(400, "参数值重复"),

    // 认证错误
    UNAUTHORIZED(401, "未登录或登录已失效"),
    FORBIDDEN(403, "没有权限"),
    BLACKLISTED(401, "账号已被踢下线"),
    INVALID_SESSION(401, "会话已失效，请重新登录"),

    // 资源错误
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),

    // 业务规则错误
    RULE_ERROR(422, "业务规则错误"),
    RULE_VALID_ERROR(422, "业务资源校验错误"),
    RULE_NOT_FOUND(422, "业务资源不存在"),
    RULE_CONFLICT(422, "业务资源冲突"),
    RULE_FORBIDDEN(422, "业务资源被禁用"),


    // 系统错误
    SYSTEM_ERROR(500, "系统错误");

    private final int code;
    private final String desc;
}
