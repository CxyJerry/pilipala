package com.jerry.pilipala.domain.common.template;

import com.jerry.pilipala.domain.common.entity.Template;
import com.jerry.pilipala.domain.message.entity.Message;
import com.jerry.pilipala.infrastructure.enums.message.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class MessageTrigger {
    private final MongoTemplate mongoTemplate;
    private final TemplateResolver templateResolver;

    public MessageTrigger(MongoTemplate mongoTemplate,
                          TemplateResolver templateResolver) {
        this.mongoTemplate = mongoTemplate;
        this.templateResolver = templateResolver;
    }

    public void trigger(String templateName,
                        MessageType type,
                        String senderId,
                        String receiveId,
                        Map<String, String> variables) {
        CompletableFuture.runAsync(() -> {
            try {
                handle(templateName,
                        type,
                        senderId,
                        receiveId,
                        variables);
            } catch (Exception e) {
                log.error("站内信触发失败，", e);
            }
        });
    }

    public void triggerSystemMessage(String templateName,
                                     String receiveId,
                                     Map<String, String> variables) {
        trigger(templateName,
                MessageType.SYSTEM,
                "",
                receiveId,
                variables);
    }

    public void handle(String templateName,
                       MessageType type,
                       String senderId,
                       String receiveId,
                       Map<String, String> variables) {
        Template template = mongoTemplate.findOne(
                new Query(Criteria.where("_id").is(templateName)),
                Template.class);
        if (Objects.isNull(template)) {
            log.error("消息模板:{} -> 不存在", templateName);
            return;
        }
        String content = template.getContent();
        String messageContent = templateResolver.fillVariable(content, variables);
        Message message = new Message();
        message.setSenderId(senderId)
                .setReceiverId(receiveId)
                .setContent(messageContent)
                .setType(type.getType());
        mongoTemplate.save(message);
    }
}
