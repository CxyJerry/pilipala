package com.jerry.pilipala.application.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SearchResultVO {
    private String type;
    private List<Object> result;
}
