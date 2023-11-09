package com.jerry.pilipala.application.vo.vod;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jerry.pilipala.infrastructure.utils.serializer.DanmakuValueVOSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonSerialize(using = DanmakuValueVOSerializer.class)
public class DanmakuValueVO {
    private Double time;
    private Integer visible;
    private Integer color;
    private String uid;
    private String text;
}
