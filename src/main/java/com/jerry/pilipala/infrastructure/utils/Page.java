package com.jerry.pilipala.infrastructure.utils;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Page<T> {
    private int pageNo;
    private int pageSize;
    private Long total;
    private List<T> page;
}
