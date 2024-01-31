package com.jerry.pilipala.domain.vod.service;

import com.jerry.pilipala.application.vo.vod.DanmakuValueVO;
import com.jerry.pilipala.infrastructure.utils.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@EnableScheduling
public class DanmakuSseManager {
    private final Map<Long, Map<String, SseEmitter>> danmakuEmmiterMap = new ConcurrentHashMap<>();
    private final JsonHelper jsonHelper;

    public DanmakuSseManager(JsonHelper jsonHelper) {
        this.jsonHelper = jsonHelper;
    }

    public SseEmitter create(Long cid, String uid) {
        log.info("uid: {} -> cid: {} SSE 连接开始建立.", uid, cid);
        // 设置超时时间为30分钟
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        danmakuEmmiterMap.computeIfAbsent(cid, k -> new ConcurrentHashMap<>()).put(uid, emitter);

        // 添加回调来处理完成和超时
        emitter.onCompletion(() -> removeEmitter(cid, uid));
        emitter.onTimeout(() -> {
            emitter.complete();
            removeEmitter(cid, uid);
        });

        return emitter;
    }

    private void removeEmitter(Long cid, String uid) {
        Map<String, SseEmitter> userEmitterMap = danmakuEmmiterMap.get(cid);
        if (userEmitterMap != null) {
            userEmitterMap.remove(uid);
            if (userEmitterMap.isEmpty()) {
                danmakuEmmiterMap.remove(cid);
            }
        }
    }

    public void send(Long cid, DanmakuValueVO danmakuValueVO) {
        Map<String, SseEmitter> userEmitterMap = danmakuEmmiterMap.get(cid);
        if (Objects.isNull(userEmitterMap)) {
            return;
        }
        log.info("弹幕推送 -> {}", danmakuValueVO);
        userEmitterMap.forEach((uid, emitter) -> {
            try {
                String message = jsonHelper.as(danmakuValueVO);
                emitter.send(SseEmitter.event().data(message));
            } catch (IOException e) {
                removeEmitter(cid, uid);
            }
        });
    }

    @Scheduled(fixedRate = 10 * 1000)
    public void heartbeat() {
        danmakuEmmiterMap.forEach((cid, map) -> {
            map.forEach((uid, emitter) -> {
                try {
                    emitter.send(SseEmitter.event().comment("hiphop"));
                } catch (IOException e) {
                    removeEmitter(cid, uid);
                }
            });
        });
    }
}

