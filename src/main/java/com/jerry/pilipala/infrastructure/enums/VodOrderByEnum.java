package com.jerry.pilipala.infrastructure.enums;

import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import com.jerry.pilipala.infrastructure.common.response.StandardResponse;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum VodOrderByEnum {
    POST_TIME("投稿时间", "ctime"),
    VOD_NAME("稿件名称", "title"),
    LIKE_COUNT("点赞数量", "likeCount"),
    VIEW_COUNT("播放量", "viewCount"),
    COLLECT_COUNT("收藏数量", "collectCount");
    private final String orderBy;
    private final String fieldName;

    VodOrderByEnum(String orderBy, String fieldName) {
        this.orderBy = orderBy;
        this.fieldName = fieldName;
    }

    public static VodOrderByEnum parse(String orderBy) {
        if (StringUtils.isBlank(orderBy)) {
            return null;
        }
        Optional<VodOrderByEnum> first = Arrays.stream(VodOrderByEnum.values())
                .filter(val -> val.orderBy.equals(orderBy)).findFirst();
        if (first.isEmpty()) {
            throw new BusinessException("排序方式错误", StandardResponse.ERROR);
        }
        return first.get();
    }
}
