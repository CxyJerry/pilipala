package com.jerry.pilipala.infrastructure.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Component
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisStreamListener {
    String topic() default "";

    int batchSize() default 1;

    int pollTimeout() default 10;
}
