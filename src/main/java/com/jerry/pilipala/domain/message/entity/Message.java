package com.jerry.pilipala.domain.message.entity;

import com.jerry.pilipala.infrastructure.enums.MessageStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Accessors(chain = true)
@Document("message")
public class Message {
    @Id
    private ObjectId id;
    private String senderId;
    private String receiverId;
    private String content;
    private String status = MessageStatusEnum.UNREAD.getStatus();
    private Long ctime = System.currentTimeMillis();
}
