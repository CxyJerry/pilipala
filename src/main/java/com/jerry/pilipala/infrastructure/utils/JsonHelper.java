package com.jerry.pilipala.infrastructure.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerry.pilipala.infrastructure.common.errors.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JsonHelper {
    private final ObjectMapper mapper;

    public JsonHelper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> T parse(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("json 解析失败，", e);
            throw BusinessException.businessError("json 解析失败");
        }
    }

    public <T> T parse(String json, TypeReference<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("json 解析失败，", e);
            throw BusinessException.businessError("json 解析失败");
        }
    }

    public <T> T convert(Object obj, Class<T> clazz) {
        try {
            return mapper.convertValue(obj, clazz);
        } catch (Exception e) {
            throw BusinessException.businessError("类型转换失败");
        }
    }

    public String as(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("json 转换失败",e);
            throw BusinessException.businessError("json 转换失败");
        }
    }
}
