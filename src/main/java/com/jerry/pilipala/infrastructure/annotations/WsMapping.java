package com.jerry.pilipala.infrastructure.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Component
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WsMapping {
    String value() default "";
}
