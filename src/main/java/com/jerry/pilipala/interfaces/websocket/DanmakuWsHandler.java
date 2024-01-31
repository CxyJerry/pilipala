package com.jerry.pilipala.interfaces.websocket;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.jerry.pilipala.infrastructure.annotations.WsMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@EnableScheduling
@WsMapping("/danmaku/{cid}")
public class DanmakuWsHandler implements WebSocketHandler {
    private final Map<String, Set<WebSocketSession>> sessionPool = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String cid = (String) session.getAttributes().get("cid");
        log.info("建立关于稿件：{} 的 ws 连接", cid);
        Set<WebSocketSession> sessionSet = sessionPool.get(cid);
        if (Objects.isNull(sessionSet)) {
            sessionSet = new ConcurrentHashSet<>();
        }
        sessionSet.add(session);
        sessionPool.put(cid, sessionSet);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String cid = (String) session.getAttributes().get("cid");
        if (Objects.isNull(cid)) {
            return;
        }
        Set<WebSocketSession> sessionSet = sessionPool.get(cid);
        for (WebSocketSession webSocketSession : sessionSet) {
            webSocketSession.sendMessage(message);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("transport error");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String cid = (String) session.getAttributes().get("cid");
        Set<WebSocketSession> sessionSet = sessionPool.get(cid);
        if (Objects.isNull(sessionSet)) {
            return;
        }
        sessionSet.remove(session);
        sessionPool.put(cid, sessionSet);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Scheduled(fixedRate = 10 * 1000)
    public void heartbeat() {
        sessionPool.forEach((cid, sessionSet) -> {
            List<WebSocketSession> failSessions = new ArrayList<>();
            sessionSet.forEach(session -> {
                try {
                    session.sendMessage(new PingMessage());
                } catch (IOException e) {
                    log.error("心跳包发送失败，准备关闭连接");
                    failSessions.add(session);
                }
            });
            failSessions.forEach(sessionSet::remove);
        });
    }
}
