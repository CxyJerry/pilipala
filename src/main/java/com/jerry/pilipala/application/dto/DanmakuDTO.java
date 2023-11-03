package com.jerry.pilipala.application.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DanmakuDTO {
    // 用户ID
    private String author;
    private Integer color;
    // 稿件ID
    private Long id;
    private String text;
    private Double time;
    private Integer type;
}
