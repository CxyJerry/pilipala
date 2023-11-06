package com.jerry.pilipala.domain.vod.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerry.pilipala.application.vo.DanmakuValueVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DanmakuSseManager {
    private final Map<Long, Map<String, SseEmitter>> danmakuEmmiterMap = new ConcurrentHashMap<>();
    private final ObjectMapper mapper;

    public DanmakuSseManager(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public SseEmitter create(Long cid, String uid) {
        SseEmitter sseEmitter = new SseEmitter(30 * 60 * 1000L);
        Map<String, SseEmitter> vodEmmiterMap = danmakuEmmiterMap.getOrDefault(cid, null);
        if (Objects.isNull(vodEmmiterMap)) {
            vodEmmiterMap = new ConcurrentHashMap<>();
        }
        vodEmmiterMap.put(uid, sseEmitter);
        danmakuEmmiterMap.put(cid, vodEmmiterMap);

        Map<String, SseEmitter> finalVodEmmiterMap = vodEmmiterMap;
        sseEmitter.onCompletion(() -> finalVodEmmiterMap.remove(uid));
        sseEmitter.onTimeout(() -> finalVodEmmiterMap.remove(uid));
        return sseEmitter;
    }

    public void send(Long cid, DanmakuValueVO danmakuVO) {
        CompletableFuture.runAsync(() -> {
            Map<String, SseEmitter> vodEmmiterMap = danmakuEmmiterMap.getOrDefault(cid, null);
            if (Objects.isNull(vodEmmiterMap)) {
                return;
            }
            for (SseEmitter emitter : vodEmmiterMap.values()) {
                try {
                    String json = mapper.writeValueAsString(danmakuVO);
                    emitter.send(SseEmitter.event().data(json));
                } catch (IOException e) {
                    log.error("弹幕推送失败，", e);
                }
            }
        });
    }
}
