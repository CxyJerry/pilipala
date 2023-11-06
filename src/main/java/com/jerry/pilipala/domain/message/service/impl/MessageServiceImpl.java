package com.jerry.pilipala.domain.message.service.impl;

import com.jerry.pilipala.domain.message.entity.Message;
import com.jerry.pilipala.domain.message.service.MessageService;
import com.jerry.pilipala.infrastructure.enums.MessageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageServiceImpl implements MessageService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;

    public MessageServiceImpl(RedisTemplate<String, Object> redisTemplate,
                              MongoTemplate mongoTemplate) {
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void send(String senderId, String receiverId, String message) {
        Message msg = new Message();
        msg.setSenderId(senderId)
                .setReceiverId(receiverId)
                .setContent(message);
        mongoTemplate.save(msg);
    }

    @Override
    public long unreadCount(String uid) {
        return mongoTemplate.count(new Query(
                        Criteria.where("status").is(MessageStatusEnum.UNREAD.getStatus())
                                .and("receiverId").is(uid)),
                Message.class);
    }
}
