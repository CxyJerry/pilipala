package com.jerry.pilipala.infrastructure.utils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jerry.pilipala.application.vo.vod.DanmakuValueVO;

import java.io.IOException;

public class DanmakuValueVOSerializer extends JsonSerializer<DanmakuValueVO> {
    @Override
    public void serialize(DanmakuValueVO danmakuValueVO,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();
        jsonGenerator.writeNumber(danmakuValueVO.getTime());
        jsonGenerator.writeNumber(danmakuValueVO.getVisible());
        jsonGenerator.writeNumber(danmakuValueVO.getColor());
        jsonGenerator.writeString(danmakuValueVO.getUid());
        jsonGenerator.writeString(danmakuValueVO.getText());
        jsonGenerator.writeEndArray();
    }
}
