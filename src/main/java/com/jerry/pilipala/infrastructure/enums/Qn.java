package com.jerry.pilipala.infrastructure.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

@Getter
public enum Qn {
    _PREVIEW(400, "预览"),
    _360P(16, "360p"),
    _480P(32, "480p"),
    _720P(48, "720p"),
    _1080P(80, "1080p"),
    _4K(120, "4k"),
    _8K(127, "8k"),
    ;
    private final Integer qn;
    private final String description;
    private static HashMap<String, Qn> stringQnHashMap = null;
    private static HashMap<Integer, Qn> integerQnHashMap = null;

    Qn(Integer qn, String description) {
        this.qn = qn;
        this.description = description;
    }

    public static Qn valueOfFormat(String format) {
        if (Objects.isNull(stringQnHashMap)) {
            synchronized (Qn.class) {
                if (Objects.isNull(stringQnHashMap)) {
                    stringQnHashMap = new HashMap<>();
                    Arrays.stream(Qn.values()).forEach(qn -> {
                        stringQnHashMap.put(qn.getDescription(), qn);
                    });
                }
            }
        }
        return stringQnHashMap.get(format);
    }

    public static Qn valueOfQn(Integer qnVal) {
        if (Objects.isNull(integerQnHashMap)) {
            synchronized (Qn.class) {
                if (Objects.isNull(integerQnHashMap)) {
                    integerQnHashMap = new HashMap<>();
                    Arrays.stream(Qn.values()).forEach(qn -> {
                        integerQnHashMap.put(qn.qn, qn);
                    });
                }
            }
        }
        return integerQnHashMap.get(qnVal);
    }

}
