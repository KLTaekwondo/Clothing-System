package com.superkl.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LimitRate {
    int limitCount() default 5; // 限制次数
    int limitTime() default 60; // 限制时间（秒）
}
