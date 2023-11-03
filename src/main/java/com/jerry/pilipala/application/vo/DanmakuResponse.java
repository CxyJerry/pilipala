package com.jerry.pilipala.application.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DanmakuResponse<T> {
    private Integer code;
    private T data;
}
