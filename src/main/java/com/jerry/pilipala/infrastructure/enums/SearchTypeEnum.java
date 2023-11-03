package com.jerry.pilipala.infrastructure.enums;


import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum SearchTypeEnum {
    TITLE("视频", "video"),
    USER("用户", "user");

    private final String type;
    private final String field;

    SearchTypeEnum(String type, String field) {
        this.type = type;
        this.field = field;
    }

    public static String parse(String type) {
        Optional<SearchTypeEnum> first = Arrays.stream(SearchTypeEnum.values()).filter(searchType -> searchType.type.equals(type)).findFirst();
        if (first.isEmpty()) {
            throw new BusinessException("非常搜索类型", StandardResponse.ERROR);
        }
        return first.get().field;
    }
}
