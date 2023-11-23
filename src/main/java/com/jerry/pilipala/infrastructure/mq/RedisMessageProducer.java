package com.jerry.pilipala.infrastructure.mq;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RedisMessageProducer {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisMessageProducer(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void send(String topic, Map<String, Object> message) {
        MapRecord<String, String, Object> record = MapRecord.create(topic, message);

        redisTemplate.opsForStream().add(record);
    }
}
