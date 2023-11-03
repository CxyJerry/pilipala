package com.jerry.pilipala.infrastructure.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FileType {
    String[] types();
}
