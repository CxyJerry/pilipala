package com.jerry.pilipala.infrastructure.utils;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class Page<T> {
    private int pageNo = 1;
    private int pageSize = 10;
    private Long total = 0L;
    private List<T> page = new ArrayList<>();
}
