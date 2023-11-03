package com.jerry.pilipala.infrastructure.annotations;


import com.jerry.pilipala.infrastructure.enums.LimitType;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {
    /**
     * 限流 key
     */
    String key() default "limit:";

    /**
     * 限流时间
     */
    int seconds() default 60;

    /**
     * 限流次数
     */
    int count() default 100;

    String message() default "访问过于频繁，请稍候再试";

    LimitType limitType() default LimitType.DEFAULT;
}
